import api from './api';
import type { ApiResponse } from '../types';

export interface DictionaryMeaning {
  partOfSpeech: string;
  definitions: {
    definition: string;
    example?: string;
    synonyms?: string[];
    antonyms?: string[];
  }[];
  synonyms?: string[];
  antonyms?: string[];
}

export interface DictionaryData {
  word: string;
  phonetic?: string;
  audioUrl?: string;
  meanings: DictionaryMeaning[];
}

export interface TranslationResult {
  originalText: string;
  translatedText: string;
  phonetic?: string;
  detectedLanguage?: string;
  sourceLanguage?: string;
  targetLanguage?: string;
  dictionary?: DictionaryData;
  source: 'backend' | 'google' | 'mymemory' | 'fallback' | string;
  fromCache?: boolean;
}

export interface AIExplanationResult {
  summary: string;
  partOfSpeech?: string;
  grammarPoint?: string;
  contextUsage?: string;
  collocations?: string[];
  synonyms?: string[];
  examples?: { en: string; vi: string }[];
  rawExplanation?: string;
}

// Hàm kiểm tra tiếng Việt có dấu
export const containsVietnamese = (text: string): boolean => {
  if (!text) return false;
  return /[àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ]/.test(text);
};

// In-memory Client Cache cho dịch nghĩa tức thì (< 0.1ms)
const memoryCache = new Map<string, TranslationResult>();

export const aiTranslationService = {
  /**
   * Lấy nhanh từ cache nếu đã có (0ms)
   */
  getCached(text: string, targetLang = 'auto', sourceLang = 'auto'): TranslationResult | null {
    if (!text) return null;
    const isVi = containsVietnamese(text);
    const src = sourceLang !== 'auto' ? sourceLang : (isVi ? 'vi' : 'en');
    const tgt = targetLang !== 'auto' ? targetLang : (src === 'vi' ? 'en' : 'vi');
    const key = `${src}:${tgt}:${text.trim().toLowerCase()}`;
    return memoryCache.get(key) || null;
  },

  /**
   * Tải trước (Pre-fetch) bản dịch trong nền khi vừa bôi đen
   */
  prefetch(text: string, targetLang = 'auto', sourceLang = 'auto'): void {
    if (!text || text.length > 500) return;
    const isVi = containsVietnamese(text);
    const src = sourceLang !== 'auto' ? sourceLang : (isVi ? 'vi' : 'en');
    const tgt = targetLang !== 'auto' ? targetLang : (src === 'vi' ? 'en' : 'vi');
    const key = `${src}:${tgt}:${text.trim().toLowerCase()}`;
    if (memoryCache.has(key)) return;

    // Chạy ngầm không chặn luồng chính
    this.translate(text, tgt, src).catch(() => {});
  },

  /**
   * Tra cứu & dịch 2 chiều Anh <-> Việt siêu tốc (Cache RAM -> Backend Fast Proxy -> Client Fallback)
   */
  async translate(text: string, targetLang = 'auto', sourceLang = 'auto'): Promise<TranslationResult> {
    const trimmed = text.trim();
    if (!trimmed) {
      return {
        originalText: text,
        translatedText: '',
        sourceLanguage: 'en',
        targetLanguage: 'vi',
        source: 'empty',
      };
    }

    const isVi = containsVietnamese(trimmed);
    const src = sourceLang !== 'auto' ? sourceLang : (isVi ? 'vi' : 'en');
    let tgt = targetLang !== 'auto' ? targetLang : (src === 'vi' ? 'en' : 'vi');
    if (src === tgt) {
      tgt = src === 'vi' ? 'en' : 'vi';
    }

    const cacheKey = `${src}:${tgt}:${trimmed.toLowerCase()}`;

    // 1. Kiểm tra RAM Cache phía Client (Phản hồi tức thì 0ms)
    if (memoryCache.has(cacheKey)) {
      return {
        ...memoryCache.get(cacheKey)!,
        source: 'client-cache (0ms)',
        fromCache: true,
      };
    }

    // 2. Ưu tiên gọi Backend Proxy với Timeout ngắn (1.5s)
    try {
      const res = await api.get<ApiResponse<TranslationResult>>('/ai/lookup', {
        params: { q: trimmed, source: src, target: tgt },
        timeout: 1500,
      });
      if (res.data && res.data.data) {
        const data = res.data.data;
        if (data.translatedText && data.translatedText.trim().toLowerCase() !== trimmed.toLowerCase()) {
          const result: TranslationResult = {
            ...data,
            sourceLanguage: src,
            targetLanguage: tgt,
            source: data.fromCache ? 'cache (0ms)' : 'backend (fast)',
          };
          memoryCache.set(cacheKey, result);
          return result;
        }
      }
    } catch (err) {
      console.warn('Backend translation slow/unavailable, using instant client fallback...');
    }

    // 3. Fallback trực tiếp phía Client (Google Clients5 - Cực nhanh < 80ms)
    try {
      const url = `https://clients5.google.com/translate_a/t?client=dict-chrome-ex&sl=${src}&tl=${tgt}&q=${encodeURIComponent(trimmed)}`;
      const res = await fetch(url, { signal: AbortSignal.timeout(1500) });
      if (res.ok) {
        const data = await res.json();
        let translatedText = '';
        if (Array.isArray(data)) {
          if (Array.isArray(data[0]) && typeof data[0][0] === 'string') {
            translatedText = data[0][0];
          } else if (typeof data[0] === 'string') {
            translatedText = data[0];
          }
        }

        if (translatedText && translatedText.trim().toLowerCase() !== trimmed.toLowerCase()) {
          const result: TranslationResult = {
            originalText: trimmed,
            translatedText: translatedText.trim(),
            phonetic: '',
            detectedLanguage: src,
            sourceLanguage: src,
            targetLanguage: tgt,
            source: 'google-instant',
          };
          memoryCache.set(cacheKey, result);
          return result;
        }
      }
    } catch (err) {
      console.warn('Google client instant fallback failed:', err);
    }

    // 4. Fallback tiếp sang MyMemory
    try {
      const url = `https://api.mymemory.translated.net/get?q=${encodeURIComponent(trimmed)}&langpair=${src}|${tgt}`;
      const res = await fetch(url, { signal: AbortSignal.timeout(2000) });
      if (res.ok) {
        const data = await res.json();
        const translatedText = data?.responseData?.translatedText;
        if (translatedText && translatedText.trim().toLowerCase() !== trimmed.toLowerCase()) {
          const result: TranslationResult = {
            originalText: trimmed,
            translatedText: translatedText.trim(),
            phonetic: '',
            detectedLanguage: src,
            sourceLanguage: src,
            targetLanguage: tgt,
            source: 'mymemory-client',
          };
          memoryCache.set(cacheKey, result);
          return result;
        }
      }
    } catch (err) {
      console.warn('MyMemory client fallback failed:', err);
    }

    return {
      originalText: trimmed,
      translatedText: trimmed,
      sourceLanguage: src,
      targetLanguage: tgt,
      source: 'fallback',
    };
  },

  /**
   * Phân tích chuyên sâu AI (gọi Backend hoặc Gemini)
   */
  async explainWithAI(text: string, contextSentence = ''): Promise<AIExplanationResult> {
    const apiKey = localStorage.getItem('gemini_api_key')?.trim() || '';

    // 1. Gọi Backend AI service
    try {
      const res = await api.post<ApiResponse<AIExplanationResult>>('/ai/explain', {
        text,
        contextSentence,
        apiKey,
      }, { timeout: 7000 });

      if (res.data && res.data.data) {
        return res.data.data;
      }
    } catch (err) {
      console.warn('Backend AI explainer failed, fallback to client...', err);
    }

    // 2. Client fallback
    const isSingleWord = !text.trim().includes(' ');
    return {
      summary: isSingleWord
        ? `"${text}" là một từ tiếng Anh thông dụng trong bài thi TOEIC.`
        : `"${text}" là một cấu trúc / cụm từ diễn đạt tự nhiên.`,
      partOfSpeech: isSingleWord ? 'Từ vựng (Vocabulary)' : 'Cụm từ / Thành ngữ (Phrase)',
      grammarPoint: contextSentence
        ? `Được sử dụng trong câu: "${contextSentence}"`
        : 'Chú ý kết hợp từ loại và giới từ đi kèm.',
      contextUsage: '💡 Mẹo: Bạn có thể nhập mã Google Gemini API Key trong Cài đặt để AI phân tích cấu trúc câu sâu hơn.',
      examples: contextSentence
        ? [{ en: contextSentence, vi: 'Ví dụ từ văn bản bạn đang đọc' }]
        : [],
    };
  },

  /**
   * Phát âm tức thì bằng Web Speech API (Hỗ trợ cả giọng Anh & giọng Việt)
   */
  speak(text: string, lang = 'auto'): void {
    if (!('speechSynthesis' in window) || !text) return;
    window.speechSynthesis.cancel();

    const isVi = lang === 'vi' || lang === 'vi-VN' || (lang === 'auto' && containsVietnamese(text));
    const targetLang = isVi ? 'vi-VN' : 'en-US';

    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = targetLang;
    utterance.rate = isVi ? 0.95 : 0.9;
    utterance.pitch = 1.0;

    const voices = window.speechSynthesis.getVoices();
    if (isVi) {
      const viVoice = voices.find((v) => v.lang.startsWith('vi') || v.lang.includes('VI'));
      if (viVoice) utterance.voice = viVoice;
    } else {
      const enVoice = voices.find(
        (v) => (v.lang.startsWith('en') || v.lang === 'en-US') && (v.name.includes('Google') || v.name.includes('Natural') || v.name.includes('Samantha') || v.name.includes('Daniel'))
      ) || voices.find((v) => v.lang.startsWith('en'));
      if (enVoice) utterance.voice = enVoice;
    }

    window.speechSynthesis.speak(utterance);
  },
};
