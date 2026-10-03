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

export const aiTranslationService = {
  /**
   * Tra cứu từ & dịch nghĩa: Ưu tiên Backend Cache siêu tốc (<= 5ms), fallback sang Google Translate
   */
  async translate(text: string): Promise<TranslationResult> {
    const trimmed = text.trim();
    if (!trimmed) {
      return {
        originalText: text,
        translatedText: '',
        source: 'empty',
      };
    }

    // 1. Ưu tiên gọi Backend Proxy & In-Memory Cache (Cực nhanh, đa luồng)
    try {
      const res = await api.get<ApiResponse<TranslationResult>>('/ai/lookup', {
        params: { q: trimmed },
        timeout: 3500,
      });
      if (res.data && res.data.data) {
        return {
          ...res.data.data,
          source: res.data.data.fromCache ? 'cache (0ms)' : 'backend (fast)',
        };
      }
    } catch (err) {
      console.warn('Backend translation service unavailable, using client fallback...', err);
    }

    // 2. Fallback trực tiếp phía Client (Google Translate)
    try {
      const url = `https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=vi&dt=t&dt=bd&dt=rm&q=${encodeURIComponent(trimmed)}`;
      const res = await fetch(url);
      if (res.ok) {
        const data = await res.json();
        let translatedText = '';
        if (Array.isArray(data[0])) {
          translatedText = data[0].map((item: any) => item[0]).filter(Boolean).join(' ');
        }

        let phonetic = '';
        if (Array.isArray(data[0])) {
          const phoneticItem = data[0].find((item: any) => item[3]);
          if (phoneticItem && phoneticItem[3]) {
            phonetic = phoneticItem[3];
          }
        }

        return {
          originalText: trimmed,
          translatedText: translatedText.trim(),
          phonetic,
          detectedLanguage: data[2] || 'en',
          source: 'google-client',
        };
      }
    } catch (err) {
      console.warn('Google client fallback failed:', err);
    }

    return {
      originalText: trimmed,
      translatedText: trimmed,
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
   * Phát âm tức thì bằng Web Speech API
   */
  speak(text: string, lang = 'en-US'): void {
    if (!('speechSynthesis' in window)) return;
    window.speechSynthesis.cancel();

    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = lang;
    utterance.rate = 0.9;
    utterance.pitch = 1.0;

    const voices = window.speechSynthesis.getVoices();
    const englishVoice = voices.find(
      (v) => (v.lang === lang || v.lang.startsWith('en')) && (v.name.includes('Google') || v.name.includes('Natural') || v.name.includes('Samantha') || v.name.includes('Daniel'))
    ) || voices.find((v) => v.lang.startsWith('en'));

    if (englishVoice) {
      utterance.voice = englishVoice;
    }

    window.speechSynthesis.speak(utterance);
  },
};
