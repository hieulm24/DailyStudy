package com.englishlearning.service;

import com.englishlearning.dto.ai.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
public class AiTranslationService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;
    private final ExecutorService executorService = Executors.newFixedThreadPool(8);

    // In-memory LRU Cache cho từ vựng & bản dịch (tốc độ < 1ms)
    private final Map<String, TranslationResponse> translationCache = new ConcurrentHashMap<>(1000);
    private final Map<String, DictionaryDataDTO> dictionaryCache = new ConcurrentHashMap<>(1000);

    public AiTranslationService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofMillis(1500))
                .executor(executorService)
                .build();
    }

    /**
     * Tra cứu & dịch từ vựng siêu tốc với In-Memory Cache và thực thi song song
     */
    public TranslationResponse translateAndLookup(String text) {
        if (text == null || text.trim().isEmpty()) {
            return TranslationResponse.builder()
                    .originalText("")
                    .translatedText("")
                    .source("empty")
                    .build();
        }

        String trimmed = text.trim();
        String cacheKey = trimmed.toLowerCase();

        // 1. Kiểm tra Cache trên RAM
        if (translationCache.containsKey(cacheKey)) {
            TranslationResponse cached = translationCache.get(cacheKey);
            return TranslationResponse.builder()
                    .originalText(cached.getOriginalText())
                    .translatedText(cached.getTranslatedText())
                    .phonetic(cached.getPhonetic())
                    .detectedLanguage(cached.getDetectedLanguage())
                    .dictionary(cached.getDictionary())
                    .source(cached.getSource())
                    .fromCache(true)
                    .build();
        }

        // 2. Chạy song song: Dịch nghĩa Google Translate + Tra từ điển Oxford/FreeDict
        boolean isSingleWord = !trimmed.contains(" ") && trimmed.length() < 35 && trimmed.matches("^[a-zA-Z-]+$");

        CompletableFuture<String[]> translateFuture = CompletableFuture.supplyAsync(() -> fetchGoogleTranslate(trimmed), executorService);
        CompletableFuture<DictionaryDataDTO> dictFuture = isSingleWord
                ? CompletableFuture.supplyAsync(() -> fetchDictionary(trimmed), executorService)
                : CompletableFuture.completedFuture(null);

        try {
            // Chờ cả 2 xong (tối đa 2.5s)
            CompletableFuture.allOf(translateFuture, dictFuture).get(2500, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("Translation/Dictionary lookup timed out or failed: {}", e.getMessage());
        }

        String[] translateRes = translateFuture.getNow(new String[]{trimmed, "", "en"});
        String translatedText = translateRes[0];
        String phoneticFromGoogle = translateRes[1];
        String detectedLang = translateRes[2];

        DictionaryDataDTO dictData = dictFuture.getNow(null);
        String finalPhonetic = (dictData != null && dictData.getPhonetic() != null && !dictData.getPhonetic().isEmpty())
                ? dictData.getPhonetic()
                : phoneticFromGoogle;

        TranslationResponse response = TranslationResponse.builder()
                .originalText(trimmed)
                .translatedText(translatedText)
                .phonetic(finalPhonetic)
                .detectedLanguage(detectedLang)
                .dictionary(dictData)
                .source("google")
                .fromCache(false)
                .build();

        // Lưu Cache
        if (translationCache.size() > 5000) {
            translationCache.clear(); // Xóa bớt nếu quá lớn
        }
        translationCache.put(cacheKey, response);

        return response;
    }

    /**
     * Gọi Google Translate API
     */
    private String[] fetchGoogleTranslate(String text) {
        try {
            String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=vi&dt=t&dt=bd&dt=rm&q=" + encoded;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(2000))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                StringBuilder sb = new StringBuilder();
                String phonetic = "";

                if (root.isArray() && root.size() > 0 && root.get(0).isArray()) {
                    for (JsonNode item : root.get(0)) {
                        if (item.isArray() && item.size() > 0 && !item.get(0).isNull()) {
                            sb.append(item.get(0).asText()).append(" ");
                        }
                        if (item.isArray() && item.size() > 3 && !item.get(3).isNull()) {
                            phonetic = item.get(3).asText();
                        }
                    }
                }

                String detectedLang = (root.size() > 2 && !root.get(2).isNull()) ? root.get(2).asText() : "en";
                return new String[]{sb.toString().trim(), phonetic, detectedLang};
            }
        } catch (Exception e) {
            log.warn("Google Translate call failed: {}", e.getMessage());
        }

        // Fallback sang MyMemory
        return fetchMyMemoryTranslate(text);
    }

    /**
     * Fallback sang MyMemory Translation API
     */
    private String[] fetchMyMemoryTranslate(String text) {
        try {
            String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = "https://api.mymemory.translated.net/get?q=" + encoded + "&langpair=en|vi";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(2000))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                String translated = root.path("responseData").path("translatedText").asText(text);
                return new String[]{translated, "", "en"};
            }
        } catch (Exception e) {
            log.warn("MyMemory fallback failed: {}", e.getMessage());
        }
        return new String[]{text, "", "en"};
    }

    /**
     * Tra cứu từ điển tiếng Anh (Free Dictionary API)
     */
    public DictionaryDataDTO fetchDictionary(String word) {
        String cleanWord = word.trim().toLowerCase().replaceAll("[^a-z-]", "");
        if (cleanWord.isEmpty()) return null;

        if (dictionaryCache.containsKey(cleanWord)) {
            return dictionaryCache.get(cleanWord);
        }

        try {
            String url = "https://api.dictionaryapi.dev/api/v2/entries/en/" + URLEncoder.encode(cleanWord, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(2000))
                    .header("User-Agent", "DailyStudyHub/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                if (root.isArray() && root.size() > 0) {
                    JsonNode first = root.get(0);
                    String phonetic = first.path("phonetic").asText("");
                    String audioUrl = "";

                    JsonNode phoneticsNode = first.path("phonetics");
                    if (phoneticsNode.isArray()) {
                        for (JsonNode p : phoneticsNode) {
                            if (phonetic.isEmpty() && p.has("text")) {
                                phonetic = p.get("text").asText();
                            }
                            if (audioUrl.isEmpty() && p.has("audio") && !p.get("audio").asText().isEmpty()) {
                                audioUrl = p.get("audio").asText();
                            }
                        }
                    }

                    List<DictionaryMeaningDTO> meanings = new ArrayList<>();
                    JsonNode meaningsNode = first.path("meanings");
                    if (meaningsNode.isArray()) {
                        for (JsonNode m : meaningsNode) {
                            String partOfSpeech = m.path("partOfSpeech").asText("other");
                            List<DictionaryDefinitionDTO> defs = new ArrayList<>();
                            JsonNode defsNode = m.path("definitions");
                            if (defsNode.isArray()) {
                                for (JsonNode d : defsNode) {
                                    if (defs.size() >= 3) break;
                                    defs.add(DictionaryDefinitionDTO.builder()
                                            .definition(d.path("definition").asText(""))
                                            .example(d.path("example").asText(null))
                                            .build());
                                }
                            }

                            meanings.add(DictionaryMeaningDTO.builder()
                                    .partOfSpeech(partOfSpeech)
                                    .definitions(defs)
                                    .build());
                        }
                    }

                    DictionaryDataDTO dictData = DictionaryDataDTO.builder()
                            .word(first.path("word").asText(cleanWord))
                            .phonetic(phonetic)
                            .audioUrl(audioUrl)
                            .meanings(meanings)
                            .build();

                    dictionaryCache.put(cleanWord, dictData);
                    return dictData;
                }
            }
        } catch (Exception e) {
            log.warn("Dictionary API failed for '{}': {}", cleanWord, e.getMessage());
        }
        return null;
    }

    /**
     * Phân tích chuyên sâu bằng AI (Google Gemini)
     */
    public AIExplanationResponse explainWithAI(AIExplanationRequest request) {
        String text = request.getText();
        String contextSentence = request.getContextSentence() != null ? request.getContextSentence().trim() : "";
        String apiKey = request.getApiKey();

        if (apiKey != null && !apiKey.trim().isEmpty()) {
            try {
                String prompt = String.format(
                        "Bạn là chuyên gia TOEIC. Hãy phân tích ngắn gọn từ/cụm từ: \"%s\".%s Trả về JSON thuần: {\"summary\": \"nghĩa và sắc thái ngắn gọn\", \"partOfSpeech\": \"từ loại\", \"grammarPoint\": \"ngữ pháp/cấu trúc đi kèm\", \"contextUsage\": \"lưu ý sử dụng khi thi TOEIC/giao tiếp\", \"collocations\": [\"cụm hay gặp 1\", \"cụm 2\"], \"examples\": [{\"en\": \"ví dụ en\", \"vi\": \"dịch vi\"}]}",
                        text,
                        contextSentence.isEmpty() ? "" : " Trong ngữ cảnh câu: \"" + contextSentence + "\"."
                );

                String requestBody = objectMapper.writeValueAsString(Map.of(
                        "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                        "generationConfig", Map.of("temperature", 0.2, "responseMimeType", "application/json")
                ));

                String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey.trim();

                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(6))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                if (httpResponse.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(httpResponse.body());
                    String rawJson = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
                    if (rawJson != null && !rawJson.isEmpty()) {
                        return objectMapper.readValue(rawJson, AIExplanationResponse.class);
                    }
                }
            } catch (Exception e) {
                log.warn("Gemini AI call failed: {}", e.getMessage());
            }
        }

        // Phân tích thông minh mặc định (Offline/No-Key Fast Explainer)
        boolean isSingle = !text.trim().contains(" ");
        return AIExplanationResponse.builder()
                .summary(isSingle ? String.format("\"%s\" là một từ vựng tiếng Anh phổ biến trong bài thi TOEIC.", text) : String.format("\"%s\" là cụm từ diễn đạt tự nhiên.", text))
                .partOfSpeech(isSingle ? "Từ vựng (Vocabulary)" : "Cụm từ / Ngữ pháp (Phrase)")
                .grammarPoint(contextSentence.isEmpty() ? "Chú ý ngữ cảnh và cấu trúc câu khi áp dụng." : "Được sử dụng trong ngữ cảnh câu: \"" + contextSentence + "\"")
                .contextUsage("💡 Mẹo: Bạn có thể nhập mã Google Gemini API Key trong Cài đặt để AI phân tích cấu trúc ngữ pháp sâu hơn.")
                .examples(contextSentence.isEmpty() ? List.of() : List.of(AIExplanationResponse.ExamplePair.builder().en(contextSentence).vi("Câu ví dụ từ văn bản bạn đang đọc").build()))
                .build();
    }
}
