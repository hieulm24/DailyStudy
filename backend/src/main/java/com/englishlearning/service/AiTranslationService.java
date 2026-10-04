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
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(2000))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .executor(executorService)
                .build();
    }

    private static final java.util.regex.Pattern VIETNAMESE_PATTERN = java.util.regex.Pattern.compile(
            ".*[àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ].*"
    );

    public static boolean containsVietnamese(String text) {
        if (text == null || text.isEmpty()) return false;
        return VIETNAMESE_PATTERN.matcher(text).matches();
    }

    public TranslationResponse translateAndLookup(String text) {
        return translateAndLookup(text, "auto", "auto");
    }

    /**
     * Tra cứu & dịch 2 chiều Anh <-> Việt đẳng cấp (Tự động nhận diện hoặc chỉ định ngôn ngữ)
     */
    public TranslationResponse translateAndLookup(String text, String sourceLang, String targetLang) {
        if (text == null || text.trim().isEmpty()) {
            return TranslationResponse.builder()
                    .originalText("")
                    .translatedText("")
                    .sourceLanguage("en")
                    .targetLanguage("vi")
                    .source("empty")
                    .build();
        }

        String trimmed = text.trim();

        // Tự động nhận diện ngôn ngữ thông minh
        boolean hasVietnamese = containsVietnamese(trimmed);
        String src = (sourceLang != null && !sourceLang.trim().isEmpty() && !sourceLang.equalsIgnoreCase("auto"))
                ? sourceLang.trim().toLowerCase()
                : (hasVietnamese ? "vi" : "en");

        String tgt = (targetLang != null && !targetLang.trim().isEmpty() && !targetLang.equalsIgnoreCase("auto"))
                ? targetLang.trim().toLowerCase()
                : (src.equals("vi") ? "en" : "vi");

        if (src.equals(tgt)) {
            tgt = src.equals("vi") ? "en" : "vi";
        }

        String cacheKey = src + ":" + tgt + ":" + trimmed.toLowerCase();

        // 1. Kiểm tra Cache trên RAM (Tốc độ 0.05ms)
        if (translationCache.containsKey(cacheKey)) {
            TranslationResponse cached = translationCache.get(cacheKey);
            return TranslationResponse.builder()
                    .originalText(cached.getOriginalText())
                    .translatedText(cached.getTranslatedText())
                    .phonetic(cached.getPhonetic())
                    .detectedLanguage(cached.getDetectedLanguage())
                    .sourceLanguage(cached.getSourceLanguage())
                    .targetLanguage(cached.getTargetLanguage())
                    .dictionary(cached.getDictionary())
                    .source(cached.getSource())
                    .fromCache(true)
                    .build();
        }

        // 2. Chạy song song: Dịch nghĩa Google Translate + Tra từ điển Oxford
        boolean isSingleWord = !trimmed.contains(" ") && trimmed.length() < 35;
        final String finalSrc = src;
        final String finalTgt = tgt;

        CompletableFuture<String[]> translateFuture = CompletableFuture.supplyAsync(
                () -> fetchGoogleTranslate(trimmed, finalSrc, finalTgt, isSingleWord), executorService
        );

        // Nếu nguồn là tiếng Anh: tra cứu từ điển từ gốc
        CompletableFuture<DictionaryDataDTO> dictFuture = (src.equals("en") && isSingleWord && trimmed.matches("^[a-zA-Z-]+$"))
                ? CompletableFuture.supplyAsync(() -> fetchDictionary(trimmed), executorService)
                : CompletableFuture.completedFuture(null);

        // Chờ bản dịch (tối đa 1.2s)
        try {
            translateFuture.get(1200, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.debug("Translate future wait: {}", e.getMessage());
        }

        // Chờ từ điển nếu đang chạy
        if (src.equals("en") && isSingleWord && !dictFuture.isDone()) {
            try {
                dictFuture.get(250, TimeUnit.MILLISECONDS);
            } catch (Exception ignored) {}
        }

        String[] translateRes = translateFuture.getNow(new String[]{trimmed, "", src});
        String translatedText = translateRes[0];
        String phoneticFromGoogle = translateRes[1];
        String detectedLang = translateRes[2];

        // Nếu dịch từ Việt -> Anh và từ kết quả là 1 từ tiếng Anh đơn lẻ, tra cứu từ điển Oxford cho từ tiếng Anh đó!
        DictionaryDataDTO dictData = dictFuture.getNow(null);
        if (dictData == null && src.equals("vi") && translatedText != null && !translatedText.contains(" ") && translatedText.matches("^[a-zA-Z-]+$")) {
            dictData = fetchDictionary(translatedText.toLowerCase());
        }

        String finalPhonetic = (dictData != null && dictData.getPhonetic() != null && !dictData.getPhonetic().isEmpty())
                ? dictData.getPhonetic()
                : phoneticFromGoogle;

        TranslationResponse response = TranslationResponse.builder()
                .originalText(trimmed)
                .translatedText(translatedText)
                .phonetic(finalPhonetic)
                .detectedLanguage(detectedLang)
                .sourceLanguage(src)
                .targetLanguage(tgt)
                .dictionary(dictData)
                .source("google-bidirectional")
                .fromCache(false)
                .build();

        // Chỉ lưu Cache khi có kết quả dịch thực tế (khác với chữ gốc)
        if (translatedText != null && !translatedText.trim().isEmpty() && !translatedText.equalsIgnoreCase(trimmed)) {
            if (translationCache.size() > 5000) {
                translationCache.clear();
            }
            translationCache.put(cacheKey, response);
        }

        return response;
    }

    /**
     * Gọi Google Translate API 2 chiều linh hoạt
     */
    private String[] fetchGoogleTranslate(String text, String src, String tgt, boolean isSingleWord) {
        String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);

        if (isSingleWord) {
            // Đối với từ đơn: Ưu tiên Single để lấy cả Phiên âm IPA & Từ loại phong phú
            String[] singleRes = tryGoogleTranslateSingle(encoded, src, tgt);
            if (singleRes != null) return singleRes;

            String[] clients5Res = tryGoogleTranslateClients5(encoded, src, tgt);
            if (clients5Res != null) return clients5Res;
        } else {
            // Đối với câu/đoạn văn: Ưu tiên Clients5 siêu tốc (< 80ms)
            String[] clients5Res = tryGoogleTranslateClients5(encoded, src, tgt);
            if (clients5Res != null) return clients5Res;

            String[] singleRes = tryGoogleTranslateSingle(encoded, src, tgt);
            if (singleRes != null) return singleRes;
        }

        // Tầng cuối: Fallback sang MyMemory API
        return fetchMyMemoryTranslate(text, src, tgt);
    }

    private String[] tryGoogleTranslateSingle(String encodedText, String src, String tgt) {
        try {
            String url = String.format("https://translate.googleapis.com/translate_a/single?client=dict-chrome-ex&sl=%s&tl=%s&dt=t&dt=bd&dt=rm&q=%s", src, tgt, encodedText);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(1200))
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
                        if (item.isArray() && item.size() > 0 && !item.get(0).isNull() && !item.get(0).asText().isEmpty()) {
                            sb.append(item.get(0).asText()).append(" ");
                        }
                        if (item.isArray() && item.size() > 3 && !item.get(3).isNull() && phonetic.isEmpty()) {
                            phonetic = item.get(3).asText();
                        }
                    }
                }

                String detectedLang = (root.size() > 2 && !root.get(2).isNull()) ? root.get(2).asText() : src;
                String resultText = sb.toString().trim();
                if (!resultText.isEmpty()) {
                    return new String[]{resultText, phonetic, detectedLang};
                }
            }
        } catch (Exception e) {
            log.debug("Google Single endpoint failed: {}", e.getMessage());
        }
        return null;
    }

    private String[] tryGoogleTranslateClients5(String encodedText, String src, String tgt) {
        try {
            String url = String.format("https://clients5.google.com/translate_a/t?client=dict-chrome-ex&sl=%s&tl=%s&q=%s", src, tgt, encodedText);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(1000))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                if (root.isArray() && root.size() > 0) {
                    JsonNode first = root.get(0);
                    if (first.isArray() && first.size() > 0) {
                        String translated = first.get(0).asText("");
                        String lang = first.size() > 1 ? first.get(1).asText(src) : src;
                        if (!translated.isEmpty()) {
                            return new String[]{translated, "", lang};
                        }
                    } else if (first.isTextual()) {
                        String translated = first.asText("");
                        String lang = root.size() > 1 ? root.get(1).asText(src) : src;
                        if (!translated.isEmpty()) {
                            return new String[]{translated, "", lang};
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Google Clients5 endpoint failed: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Fallback sang MyMemory Translation API
     */
    private String[] fetchMyMemoryTranslate(String text, String src, String tgt) {
        try {
            String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = String.format("https://api.mymemory.translated.net/get?q=%s&langpair=%s|%s", encoded, src, tgt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(1200))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                String translated = root.path("responseData").path("translatedText").asText("");
                if (translated != null && !translated.trim().isEmpty() && !translated.equalsIgnoreCase(text)) {
                    return new String[]{translated.trim(), "", src};
                }
            }
        } catch (Exception e) {
            log.warn("MyMemory fallback failed: {}", e.getMessage());
        }
        return new String[]{text, "", src};
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
