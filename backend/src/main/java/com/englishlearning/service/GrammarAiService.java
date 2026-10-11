package com.englishlearning.service;

import com.englishlearning.common.ResourceNotFoundException;
import com.englishlearning.dto.grammar.GenerateGrammarExerciseRequest;
import com.englishlearning.dto.grammar.GrammarExerciseHistoryResponse;
import com.englishlearning.dto.grammar.GrammarExerciseQuestionDto;
import com.englishlearning.dto.grammar.SubmitGrammarExerciseRequest;
import com.englishlearning.entity.GrammarExerciseHistory;
import com.englishlearning.entity.GrammarTopic;
import com.englishlearning.entity.User;
import com.englishlearning.repository.GrammarExerciseHistoryRepository;
import com.englishlearning.repository.GrammarTopicRepository;
import com.englishlearning.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrammarAiService {

    private final GrammarTopicRepository grammarTopicRepository;
    private final GrammarExerciseHistoryRepository grammarExerciseHistoryRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.gemini.api-key:}")
    private String configuredGeminiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Transactional(readOnly = true)
    public List<GrammarExerciseQuestionDto> generateExercises(Long userId, GenerateGrammarExerciseRequest request) {
        GrammarTopic topic = grammarTopicRepository.findByIdAndUserId(request.getTopicId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chủ đề ngữ pháp với id: " + request.getTopicId()));

        int count = request.getNumberOfQuestions() != null && request.getNumberOfQuestions() > 0
                ? Math.min(request.getNumberOfQuestions(), 15) : 5;
        String level = StringUtils.hasText(request.getLevel()) ? request.getLevel()
                : (StringUtils.hasText(topic.getLevel()) ? topic.getLevel() : "B1");
        String exType = StringUtils.hasText(request.getExerciseType()) ? request.getExerciseType() : "MULTIPLE_CHOICE";

        // Try AI generation with Gemini
        List<GrammarExerciseQuestionDto> aiQuestions = tryGenerateWithGemini(topic, count, level, exType, request.getCustomFocus());
        if (aiQuestions != null && !aiQuestions.isEmpty()) {
            return aiQuestions;
        }

        // Fallback to high-quality smart generator
        return generateSmartFallbackQuestions(topic, count, level, exType);
    }

    private List<GrammarExerciseQuestionDto> tryGenerateWithGemini(GrammarTopic topic, int count, String level, String exType, String customFocus) {
        String apiKey = StringUtils.hasText(configuredGeminiKey) ? configuredGeminiKey : System.getenv("GEMINI_API_KEY");
        if (!StringUtils.hasText(apiKey)) {
            log.info("Gemini API key not configured, using smart rule-based grammar exercise generator.");
            return null;
        }

        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("Bạn là chuyên gia biên soạn đề thi tiếng Anh TOEIC và CEFR chuẩn quốc tế.\n");
            prompt.append("Hãy tạo một bộ bài tập gồm đúng ").append(count).append(" câu bài tập chuyên sâu về chủ điểm ngữ pháp sau:\n\n");
            prompt.append("- Tên chủ đề ngữ pháp: ").append(topic.getTopic()).append("\n");
            prompt.append("- Cấp độ mục tiêu: ").append(level).append("\n");
            prompt.append("- Dạng bài yêu cầu: ").append(exType).append(" (Các dạng có thể gồm: MULTIPLE_CHOICE (Trắc nghiệm 4 lựa chọn), FILL_IN_BLANK (Điền từ/chia động từ trong ngoặc), ERROR_CORRECTION (Tìm và sửa lỗi sai trong câu), hoặc MIXED (Tổng hợp kết hợp cả 3 dạng)).\n");

            if (StringUtils.hasText(topic.getStructure())) {
                prompt.append("- Cấu trúc tổng quát: ").append(topic.getStructure()).append("\n");
            }
            if (StringUtils.hasText(topic.getPositiveStructure())) {
                prompt.append("- Dạng khẳng định (+): ").append(topic.getPositiveStructure()).append("\n");
            }
            if (StringUtils.hasText(topic.getNegativeStructure())) {
                prompt.append("- Dạng phủ định (-): ").append(topic.getNegativeStructure()).append("\n");
            }
            if (StringUtils.hasText(topic.getQuestionStructure())) {
                prompt.append("- Dạng nghi vấn (?): ").append(topic.getQuestionStructure()).append("\n");
            }
            if (StringUtils.hasText(topic.getUsage())) {
                prompt.append("- Cách dùng & Ngữ cảnh: ").append(topic.getUsage()).append("\n");
            }
            if (StringUtils.hasText(topic.getSignalWords())) {
                prompt.append("- Dấu hiệu nhận biết: ").append(topic.getSignalWords()).append("\n");
            }
            if (StringUtils.hasText(topic.getCommonMistakes())) {
                prompt.append("- Các bẫy/Lỗi hay gặp: ").append(topic.getCommonMistakes()).append("\n");
            }
            if (StringUtils.hasText(customFocus)) {
                prompt.append("- Yêu cầu trọng tâm: ").append(customFocus).append("\n");
            }

            prompt.append("\nQUY TẮC BẮT BUỘC (RẤT QUAN TRỌNG - TUÂN THỦ 100%):\n");
            prompt.append("1. TRỌNG TÂM DUY NHẤT: Toàn bộ ").append(count).append(" câu hỏi BẮT BUỘC CHỈ ĐƯỢC PHÉP KIỂM TRA DUY NHẤT chủ điểm ngữ pháp: '").append(topic.getTopic()).append("'.\n");
            prompt.append("   - TUYỆT ĐỐI KHÔNG sinh câu hỏi kiểm tra thì hoặc cấu trúc ngữ pháp khác không thuộc '").append(topic.getTopic()).append("'.\n");
            prompt.append("   - Mọi chỗ trống cần điền, lỗi sai cần tìm, hoặc phương án lựa chọn đều phải xoay quanh quy tắc, công thức, cách chia động từ và dấu hiệu nhận biết của '").append(topic.getTopic()).append("'.\n");
            prompt.append("   - VÍ DỤ: Nếu chủ đề là 'Present Simple' (Hiện tại đơn), 100% các câu hỏi CHỈ ĐƯỢC xoay quanh: chia V(s/es) cho ngôi thứ 3 số ít, trợ động từ do/does, phủ định don't/doesn't, nghi vấn Do/Does, to be (is/am/are), trạng từ tần suất (always, usually, every day...). TUYỆT ĐỐI KHÔNG ra câu thì Quá khứ, Tương lai, Hiện tại hoàn thành hay Bị động!\n");
            prompt.append("2. ĐỘ KHÓ THEO CẤP ĐỘ:\n");
            prompt.append("   - Nếu cấp độ là A0 hoặc A1 (Dành cho người MẤT GỐC / MỚI BẮT ĐẦU HỌC LẠI):\n");
            prompt.append("     + BẮT BUỘC dùng câu SIÊU NGẮN (5-8 từ), từ vựng đời sống siêu cơ bản và quen thuộc (I, you, he, she, it, we, they, go, eat, study, play, watch, like, have, cat, dog, book, school, home, every day, now, yesterday, tomorrow...).\n");
            prompt.append("     + TUYỆT ĐỐI KHÔNG dùng từ vựng công sở, kinh tế, báo cáo, thuật ngữ thương mại phức tạp.\n");
            prompt.append("     + Câu hỏi tập trung giúp người mất gốc áp dụng đúng công thức nền tảng một cách trực quan, dễ hiểu.\n");
            prompt.append("   - Nếu cấp độ là A2, B1, B2, C1: Ngữ cảnh câu hỏi gắn với giao tiếp thực tế và môi trường công sở chuẩn format đề thi TOEIC Part 5 & 6.\n");
            prompt.append("3. Đối với dạng FILL_IN_BLANK: câu hỏi chứa chỗ trống '________ (từ gốc)' và 'baseWord' là từ trong ngoặc. Người làm sẽ điền từ/dạng chia đúng vào chỗ trống.\n");
            prompt.append("4. Đối với dạng ERROR_CORRECTION: câu chứa 4 phần gạch chân/đánh dấu [A], [B], [C], [D]. Một trong 4 phần bị sai ngữ pháp. 'correctAnswer' là phương án sai, 'correctedWord' là từ/cụm từ đúng sau khi sửa.\n");
            prompt.append("5. Đối với dạng MULTIPLE_CHOICE: 4 phương án A, B, C, D với bẫy thông minh.\n");
            prompt.append("6. Đối với dạng MIXED: phân bổ đều các câu thuộc 3 dạng trên.\n");
            prompt.append("7. Phần giải thích (explanation) bằng tiếng Việt chi tiết: Dịch nghĩa toàn câu, chỉ ra dấu hiệu nhận biết/thành phần câu, phân tích vì sao đáp án này đúng và các đáp án khác sai.\n");
            prompt.append("8. Kèm mẹo làm bài (grammarTip) ngắn gọn cho thí sinh.\n");
            prompt.append("9. Chỉ trả về JSON thuần dạng mảng array các object, không markdown, không lời chào.\n");
            prompt.append("LƯU Ý: Các ví dụ schema dưới đây CHỈ để bạn tham khảo cấu trúc key/value. Nội dung câu hỏi bạn tạo BẮT BUỘC PHẢI VỀ CHỦ ĐỀ: '").append(topic.getTopic()).append("'. Schema mẫu:\n");

            boolean isBeginner = "A0".equalsIgnoreCase(level) || "A1".equalsIgnoreCase(level) || (level != null && (level.toLowerCase().contains("mất gốc") || level.toLowerCase().contains("nhập môn")));

            if (isBeginner) {
                prompt.append("[\n");
                prompt.append("  {\n");
                prompt.append("    \"id\": 1,\n");
                prompt.append("    \"questionType\": \"MULTIPLE_CHOICE\",\n");
                prompt.append("    \"prompt\": \"Chọn từ thích hợp điền vào chỗ trống:\",\n");
                prompt.append("    \"question\": \"She ________ milk every morning.\",\n");
                prompt.append("    \"translation\": \"Cô ấy uống sữa mỗi buổi sáng.\",\n");
                prompt.append("    \"options\": [\"drinks\", \"drink\", \"drinking\", \"is drink\"],\n");
                prompt.append("    \"correctAnswer\": \"drinks\",\n");
                prompt.append("    \"correctIndex\": 0,\n");
                prompt.append("    \"explanation\": \"Chủ ngữ 'She' là ngôi thứ 3 số ít, câu diễn tả thói quen lặp lại ('every morning') nên động từ thêm -s ('drinks').\",\n");
                prompt.append("    \"grammarTip\": \"He / She / It + V(s/es); I / You / We / They + V(nguyên thể).\"\n");
                prompt.append("  },\n");
                prompt.append("  {\n");
                prompt.append("    \"id\": 2,\n");
                prompt.append("    \"questionType\": \"FILL_IN_BLANK\",\n");
                prompt.append("    \"prompt\": \"Điền dạng đúng của động từ trong ngoặc:\",\n");
                prompt.append("    \"baseWord\": \"play\",\n");
                prompt.append("    \"question\": \"They ________ (play) football in the park yesterday.\",\n");
                prompt.append("    \"translation\": \"Họ đã chơi bóng đá trong công viên ngày hôm qua.\",\n");
                prompt.append("    \"options\": [\"played\", \"plays\", \"playing\", \"play\"],\n");
                prompt.append("    \"correctAnswer\": \"played\",\n");
                prompt.append("    \"explanation\": \"Dấu hiệu 'yesterday' (ngày hôm qua) chỉ hành động trong quá khứ -> dùng thì Quá khứ đơn (V-ed).\",\n");
                prompt.append("    \"grammarTip\": \"Gặp 'yesterday, ago, last...' chia Quá khứ đơn (V2/ed).\"\n");
                prompt.append("  },\n");
                prompt.append("  {\n");
                prompt.append("    \"id\": 3,\n");
                prompt.append("    \"questionType\": \"ERROR_CORRECTION\",\n");
                prompt.append("    \"prompt\": \"Tìm phần gạch chân bị sai ngữ pháp:\",\n");
                prompt.append("    \"question\": \"He [A] is a student and [B] he [C] do not like [D] cats.\",\n");
                prompt.append("    \"translation\": \"Anh ấy là học sinh và anh ấy không thích mèo.\",\n");
                prompt.append("    \"options\": [\"A. is\", \"B. he\", \"C. do not like\", \"D. cats\"],\n");
                prompt.append("    \"correctAnswer\": \"C. do not like\",\n");
                prompt.append("    \"correctIndex\": 2,\n");
                prompt.append("    \"correctedWord\": \"does not like\",\n");
                prompt.append("    \"explanation\": \"Chủ ngữ 'he' là ngôi thứ 3 số ít nên dạng phủ định phải dùng 'does not like' chứ không dùng 'do not like'.\",\n");
                prompt.append("    \"grammarTip\": \"He / She / It đi với DOES NOT (doesn't).\"\n");
                prompt.append("  }\n");
                prompt.append("]\n");
            } else {
                prompt.append("[\n");
                prompt.append("  {\n");
                prompt.append("    \"id\": 1,\n");
                prompt.append("    \"questionType\": \"MULTIPLE_CHOICE\",\n");
                prompt.append("    \"prompt\": \"Chọn phương án chính xác nhất để hoàn thành câu:\",\n");
                prompt.append("    \"question\": \"The management team ________ the revised budget before the project started.\",\n");
                prompt.append("    \"translation\": \"Ban quản lý đã phê duyệt ngân sách sửa đổi trước khi dự án bắt đầu.\",\n");
                prompt.append("    \"options\": [\"approved\", \"had approved\", \"has approved\", \"is approving\"],\n");
                prompt.append("    \"correctAnswer\": \"had approved\",\n");
                prompt.append("    \"correctIndex\": 1,\n");
                prompt.append("    \"explanation\": \"Câu diễn tả hành động xảy ra trước một hành động khác trong quá khứ ('before the project started') nên dùng thì Quá khứ hoàn thành (had + V3/ed).\",\n");
                prompt.append("    \"grammarTip\": \"Gặp 'before + S + V(quá khứ)', mệnh đề trước đó ưu tiên chia Quá khứ hoàn thành.\"\n");
                prompt.append("  },\n");
                prompt.append("  {\n");
                prompt.append("    \"id\": 2,\n");
                prompt.append("    \"questionType\": \"FILL_IN_BLANK\",\n");
                prompt.append("    \"prompt\": \"Điền dạng đúng của động từ trong ngoặc:\",\n");
                prompt.append("    \"baseWord\": \"negotiate\",\n");
                prompt.append("    \"question\": \"The two companies ________ (negotiate) the contract terms since last Monday.\",\n");
                prompt.append("    \"translation\": \"Hai công ty đã đàm phán các điều khoản hợp đồng kể từ thứ Hai tuần trước.\",\n");
                prompt.append("    \"options\": [\"have been negotiating\", \"have negotiated\", \"are negotiating\", \"negotiated\"],\n");
                prompt.append("    \"correctAnswer\": \"have negotiated\",\n");
                prompt.append("    \"explanation\": \"Với dấu hiệu 'since last Monday', ta chia thì Hiện tại hoàn thành 'have negotiated' (hoặc have been negotiating).\",\n");
                prompt.append("    \"grammarTip\": \"'Since + mốc thời gian' -> Hiện tại hoàn thành.\"\n");
                prompt.append("  },\n");
                prompt.append("  {\n");
                prompt.append("    \"id\": 3,\n");
                prompt.append("    \"questionType\": \"ERROR_CORRECTION\",\n");
                prompt.append("    \"prompt\": \"Tìm phần gạch chân bị sai ngữ pháp:\",\n");
                prompt.append("    \"question\": \"The committee [A] has decided [B] to postpone the conference because [C] they has not received [D] all registrations.\",\n");
                prompt.append("    \"translation\": \"Ủy ban đã quyết định hoãn hội nghị vì họ chưa nhận được tất cả các đăng ký.\",\n");
                prompt.append("    \"options\": [\"A. has decided\", \"B. to postpone\", \"C. they has not received\", \"D. all registrations\"],\n");
                prompt.append("    \"correctAnswer\": \"C. they has not received\",\n");
                prompt.append("    \"correctIndex\": 2,\n");
                prompt.append("    \"correctedWord\": \"they have not received\",\n");
                prompt.append("    \"explanation\": \"Chủ ngữ 'they' là đại từ số nhiều, trợ động từ bắt buộc là 'have', không dùng 'has'. Sửa thành 'they have not received'.\",\n");
                prompt.append("    \"grammarTip\": \"Chú ý hòa hợp chủ vị: They / We / You + have, He / She / It + has.\"\n");
                prompt.append("  }\n");
                prompt.append("]\n");
            }

            Map<String, Object> bodyMap = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt.toString())))),
                    "generationConfig", Map.of(
                            "temperature", 0.3,
                            "responseMimeType", "application/json"
                    )
            );

            String requestBody = objectMapper.writeValueAsString(bodyMap);
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey.trim();

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                String rawJson = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
                if (StringUtils.hasText(rawJson)) {
                    List<GrammarExerciseQuestionDto> list = objectMapper.readValue(rawJson, new TypeReference<List<GrammarExerciseQuestionDto>>() {});
                    if (list != null && !list.isEmpty()) {
                        // normalize IDs and indices
                        for (int i = 0; i < list.size(); i++) {
                            GrammarExerciseQuestionDto q = list.get(i);
                            q.setId(i + 1);
                            if (q.getCorrectIndex() == null && q.getOptions() != null && q.getCorrectAnswer() != null) {
                                q.setCorrectIndex(q.getOptions().indexOf(q.getCorrectAnswer()));
                            }
                            if (q.getQuestionType() == null) q.setQuestionType("MULTIPLE_CHOICE");
                        }
                        log.info("Successfully generated {} grammar questions via Gemini for topic '{}'", list.size(), topic.getTopic());
                        return list;
                    }
                }
            } else {
                log.warn("Gemini API call returned status {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.warn("Gemini grammar exercise generation failed: {}", e.getMessage());
        }
        return null;
    }

    private List<GrammarExerciseQuestionDto> generateSmartFallbackQuestions(GrammarTopic topic, int count, String level, String exType) {
        String topicLower = topic.getTopic() != null ? topic.getTopic().toLowerCase() : "";
        List<GrammarExerciseQuestionDto> pool;

        boolean isFillInBlank = "FILL_IN_BLANK".equalsIgnoreCase(exType);
        boolean isErrorCorrection = "ERROR_CORRECTION".equalsIgnoreCase(exType);
        boolean isMixed = "MIXED".equalsIgnoreCase(exType);
        boolean isBeginner = "A0".equalsIgnoreCase(level) || "A1".equalsIgnoreCase(level) || (level != null && (level.toLowerCase().contains("mất gốc") || level.toLowerCase().contains("căn bản") || level.toLowerCase().contains("nhập môn")));

        // Phân loại CHÍNH XÁC theo chủ đề ngữ pháp đang học
        if (topicLower.contains("present simple") || topicLower.contains("hiện tại đơn") || topicLower.contains("hien tai don")) {
            pool = generatePresentSimpleQuestions(isBeginner, isFillInBlank, isErrorCorrection, isMixed);
        } else if (topicLower.contains("present continuous") || topicLower.contains("tiếp diễn") || topicLower.contains("tiep dien")) {
            pool = generatePresentContinuousQuestions(isBeginner, isFillInBlank, isErrorCorrection, isMixed);
        } else if (topicLower.contains("past simple") || topicLower.contains("quá khứ đơn") || topicLower.contains("qua khu don")) {
            pool = generatePastSimpleQuestions(isBeginner, isFillInBlank, isErrorCorrection, isMixed);
        } else if (topicLower.contains("present perfect") || topicLower.contains("hiện tại hoàn thành") || topicLower.contains("hoan thanh")) {
            pool = generatePresentPerfectQuestions(isBeginner, isFillInBlank, isErrorCorrection, isMixed);
        } else if (topicLower.contains("conditional") || topicLower.contains("điều kiện") || topicLower.contains("dieu kien")) {
            pool = generateConditionalQuestions(isBeginner, isFillInBlank, isErrorCorrection, isMixed);
        } else if (topicLower.contains("passive") || topicLower.contains("bị động") || topicLower.contains("bi dong")) {
            pool = generatePassiveVoiceQuestions(isBeginner, isFillInBlank, isErrorCorrection, isMixed);
        } else {
            pool = generateGenericTopicQuestions(topic, isBeginner, isFillInBlank, isErrorCorrection, isMixed);
        }

        List<GrammarExerciseQuestionDto> result = new ArrayList<>();
        for (int i = 0; i < Math.min(count, pool.size()); i++) {
            GrammarExerciseQuestionDto q = pool.get(i);
            q.setId(i + 1);
            result.add(q);
        }
        return result;
    }

    private List<GrammarExerciseQuestionDto> generatePresentSimpleQuestions(boolean isBeginner, boolean isFillInBlank, boolean isErrorCorrection, boolean isMixed) {
        List<GrammarExerciseQuestionDto> mc = new ArrayList<>();
        List<GrammarExerciseQuestionDto> fib = new ArrayList<>();
        List<GrammarExerciseQuestionDto> err = new ArrayList<>();

        if (isBeginner) {
            mc.add(GrammarExerciseQuestionDto.builder()
                    .questionType("MULTIPLE_CHOICE")
                    .prompt("Chọn đáp án chính xác nhất (Thì Hiện tại đơn):")
                    .question("She ________ to school by bus every day.")
                    .translation("Cô ấy đi học bằng xe buýt mỗi ngày.")
                    .options(List.of("goes", "go", "going", "is go"))
                    .correctAnswer("goes")
                    .correctIndex(0)
                    .explanation("Chủ ngữ 'She' là ngôi thứ 3 số ít, động từ kết thúc bằng đuôi 'o' nên thêm -es ('goes').")
                    .grammarTip("He / She / It + V(s/es). Động từ tận cùng o, ch, sh, s, x, z -> thêm 'es'.")
                    .build());

            mc.add(GrammarExerciseQuestionDto.builder()
                    .questionType("MULTIPLE_CHOICE")
                    .prompt("Chọn dạng phủ định đúng của thì Hiện tại đơn:")
                    .question("He ________ spicy food.")
                    .translation("Anh ấy không thích đồ ăn cay.")
                    .options(List.of("doesn't like", "don't like", "not like", "isn't like"))
                    .correctAnswer("doesn't like")
                    .correctIndex(0)
                    .explanation("Chủ ngữ 'He' ngôi thứ 3 số ít dùng trợ động từ phủ định 'doesn't' (does not) + V-nguyên thể.")
                    .grammarTip("He / She / It + DOESN'T + V(nguyên thể).")
                    .build());

            mc.add(GrammarExerciseQuestionDto.builder()
                    .questionType("MULTIPLE_CHOICE")
                    .prompt("Chọn trợ động từ nghi vấn đúng:")
                    .question("________ they live in Hanoi?")
                    .translation("Họ có sống ở Hà Nội không?")
                    .options(List.of("Do", "Does", "Are", "Is"))
                    .correctAnswer("Do")
                    .correctIndex(0)
                    .explanation("Chủ ngữ 'they' số nhiều trong câu hỏi Hiện tại đơn dùng trợ động từ 'Do'.")
                    .grammarTip("Do + I / You / We / They + V? Does + He / She / It + V?")
                    .build());

            mc.add(GrammarExerciseQuestionDto.builder()
                    .questionType("MULTIPLE_CHOICE")
                    .prompt("Chọn dạng đúng của động từ có đuôi -y:")
                    .question("My brother ________ English every evening.")
                    .translation("Anh trai tôi học tiếng Anh vào mỗi buổi tối.")
                    .options(List.of("studies", "studys", "study", "is study"))
                    .correctAnswer("studies")
                    .correctIndex(0)
                    .explanation("Chủ ngữ 'My brother' ngôi 3 số ít, 'study' tận cùng phụ âm 'd' + 'y' nên đổi 'y' thành 'i' rồi thêm 'es' ('studies').")
                    .grammarTip("Phụ âm + y -> đổi y thành ies (study -> studies, fly -> flies).")
                    .build());

            mc.add(GrammarExerciseQuestionDto.builder()
                    .questionType("MULTIPLE_CHOICE")
                    .prompt("Chọn dạng to-be đúng ở hiện tại:")
                    .question("The students ________ very excited about the trip.")
                    .translation("Các học sinh rất hào hứng về chuyến đi.")
                    .options(List.of("are", "is", "am", "be"))
                    .correctAnswer("are")
                    .correctIndex(0)
                    .explanation("Chủ ngữ 'The students' là danh từ số nhiều nên to-be ở hiện tại là 'are'.")
                    .grammarTip("They / We / Danh từ số nhiều + ARE.")
                    .build());

            fib.add(GrammarExerciseQuestionDto.builder()
                    .questionType("FILL_IN_BLANK")
                    .prompt("Điền dạng đúng của động từ trong ngoặc (Hiện tại đơn):")
                    .baseWord("watch")
                    .question("My father ________ (watch) the news on TV every night.")
                    .translation("Bố tôi xem tin tức trên tivi mỗi tối.")
                    .options(List.of("watches", "watchs", "watch", "is watch"))
                    .correctAnswer("watches")
                    .explanation("Chủ ngữ 'My father' số ít, động từ kết thúc bằng 'ch' nên thêm -es thành 'watches'.")
                    .grammarTip("Động từ tận cùng ch -> thêm es: watch -> watches, teach -> teaches.")
                    .build());

            fib.add(GrammarExerciseQuestionDto.builder()
                    .questionType("FILL_IN_BLANK")
                    .prompt("Điền dạng phủ định của động từ trong ngoặc:")
                    .baseWord("drink")
                    .question("I ________ (not / drink) coffee in the evening.")
                    .translation("Tôi không uống cà phê vào buổi tối.")
                    .options(List.of("do not drink", "does not drink", "not drink", "am not drink"))
                    .correctAnswer("do not drink")
                    .explanation("Chủ ngữ 'I' đi với trợ động từ phủ định 'do not' (hoặc don't) + động từ nguyên thể.")
                    .grammarTip("I / You / We / They + DO NOT (don't) + V.")
                    .build());

            fib.add(GrammarExerciseQuestionDto.builder()
                    .questionType("FILL_IN_BLANK")
                    .prompt("Chia động từ bất quy tắc 'have' ở hiện tại đơn:")
                    .baseWord("have")
                    .question("She ________ (have) breakfast at 7 o'clock every morning.")
                    .translation("Cô ấy ăn sáng lúc 7 giờ mỗi sáng.")
                    .options(List.of("has", "haves", "have", "is have"))
                    .correctAnswer("has")
                    .explanation("Chủ ngữ 'She' là ngôi 3 số ít, động từ 'have' biến đổi đặc biệt thành 'has'.")
                    .grammarTip("He / She / It + HAS; I / You / We / They + HAVE.")
                    .build());

            fib.add(GrammarExerciseQuestionDto.builder()
                    .questionType("FILL_IN_BLANK")
                    .prompt("Chia động từ trong ngoặc theo thì Hiện tại đơn:")
                    .baseWord("wash")
                    .question("He ________ (wash) his hands before every meal.")
                    .translation("Anh ấy rửa tay trước mỗi bữa ăn.")
                    .options(List.of("washes", "washs", "wash", "is wash"))
                    .correctAnswer("washes")
                    .explanation("Động từ kết thúc bằng 'sh' đi với chủ ngữ số ít 'He' thì thêm -es ('washes').")
                    .grammarTip("Tận cùng sh -> washes, brush -> brushes.")
                    .build());

            fib.add(GrammarExerciseQuestionDto.builder()
                    .questionType("FILL_IN_BLANK")
                    .prompt("Điền dạng đúng của động từ trong ngoặc:")
                    .baseWord("play")
                    .question("They usually ________ (play) badminton in the yard on Sundays.")
                    .translation("Họ thường chơi cầu lông trong sân vào các ngày Chủ nhật.")
                    .options(List.of("play", "plays", "playing", "is play"))
                    .correctAnswer("play")
                    .explanation("Chủ ngữ 'They' số nhiều, động từ giữ nguyên mẫu 'play'.")
                    .grammarTip("They / We / I / You + V(nguyên thể).")
                    .build());

            err.add(GrammarExerciseQuestionDto.builder()
                    .questionType("ERROR_CORRECTION")
                    .prompt("Tìm phần bị sai ngữ pháp trong câu:")
                    .question("She [A] go [B] to the market [C] every Sunday [D] morning.")
                    .translation("Cô ấy đi chợ vào mỗi sáng Chủ nhật.")
                    .options(List.of("A. go", "B. to the market", "C. every Sunday", "D. morning"))
                    .correctAnswer("A. go")
                    .correctIndex(0)
                    .correctedWord("goes")
                    .explanation("Chủ ngữ 'She' số ít nên động từ 'go' phải thêm -es thành 'goes'.")
                    .grammarTip("She + goes, He + goes.")
                    .build());

            err.add(GrammarExerciseQuestionDto.builder()
                    .questionType("ERROR_CORRECTION")
                    .prompt("Tìm lỗi sai về trợ động từ:")
                    .question("He [A] don't like [B] eating vegetables [C] for [D] dinner.")
                    .translation("Anh ấy không thích ăn rau cho bữa tối.")
                    .options(List.of("A. don't like", "B. eating vegetables", "C. for", "D. dinner"))
                    .correctAnswer("A. don't like")
                    .correctIndex(0)
                    .correctedWord("doesn't like")
                    .explanation("Chủ ngữ 'He' số ít, trợ động từ phủ định phải là 'doesn't', không dùng 'don't'.")
                    .grammarTip("He / She / It + DOESN'T + V.")
                    .build());

            err.add(GrammarExerciseQuestionDto.builder()
                    .questionType("ERROR_CORRECTION")
                    .prompt("Tìm lỗi sai về chia động từ sau trợ động từ:")
                    .question("She [A] doesn't [B] watches [C] television [D] on weekdays.")
                    .translation("Cô ấy không xem tivi vào các ngày trong tuần.")
                    .options(List.of("A. doesn't", "B. watches", "C. television", "D. on weekdays"))
                    .correctAnswer("B. watches")
                    .correctIndex(1)
                    .correctedWord("watch")
                    .explanation("Sau trợ động từ 'doesn't', động từ chính bắt buộc trở về nguyên thể 'watch', không thêm -es.")
                    .grammarTip("Doesn't + V(nguyên mẫu).")
                    .build());

            err.add(GrammarExerciseQuestionDto.builder()
                    .questionType("ERROR_CORRECTION")
                    .prompt("Tìm lỗi sai về động từ to-be:")
                    .question("My parents [A] is [B] very kind [C] and [D] friendly.")
                    .translation("Bố mẹ tôi rất tốt bụng và thân thiện.")
                    .options(List.of("A. is", "B. very kind", "C. and", "D. friendly"))
                    .correctAnswer("A. is")
                    .correctIndex(0)
                    .correctedWord("are")
                    .explanation("Chủ ngữ 'My parents' là danh từ số nhiều (bố và mẹ) nên to-be phải là 'are'.")
                    .grammarTip("Danh từ số nhiều + are.")
                    .build());

            err.add(GrammarExerciseQuestionDto.builder()
                    .questionType("ERROR_CORRECTION")
                    .prompt("Tìm lỗi sai trong câu hỏi:")
                    .question("Does [A] your sisters [B] live [C] in Da Nang [D] city?")
                    .translation("Các chị gái bạn có sống ở thành phố Đà Nẵng không?")
                    .options(List.of("A. Does", "B. live", "C. in Da Nang", "D. city"))
                    .correctAnswer("A. Does")
                    .correctIndex(0)
                    .correctedWord("Do")
                    .explanation("Chủ ngữ 'your sisters' là danh từ số nhiều nên trợ động từ nghi vấn phải là 'Do'.")
                    .grammarTip("Do + S(số nhiều) + V?")
                    .build());
        } else {
            // TOEIC / B1+
            mc.add(GrammarExerciseQuestionDto.builder()
                    .questionType("MULTIPLE_CHOICE")
                    .prompt("Chọn đáp án chính xác nhất (Chuẩn đề TOEIC Part 5):")
                    .question("The regional sales director ________ quarterly revenue targets with all store managers every Monday morning.")
                    .translation("Giám đốc kinh doanh khu vực thảo luận về các mục tiêu doanh thu hàng quý với tất cả quản lý cửa hàng vào mỗi sáng thứ Hai.")
                    .options(List.of("discusses", "discuss", "discussing", "is discuss"))
                    .correctAnswer("discusses")
                    .correctIndex(0)
                    .explanation("Chủ ngữ 'The regional sales director' là danh từ số ít, kết hợp với trạng từ chỉ tần suất 'every Monday morning' -> chia thì Hiện tại đơn thêm -es ('discusses').")
                    .grammarTip("Dấu hiệu 'every + mốc thời gian' -> Hiện tại đơn; Chủ ngữ số ít -> V(s/es).")
                    .build());

            mc.add(GrammarExerciseQuestionDto.builder()
                    .questionType("MULTIPLE_CHOICE")
                    .prompt("Chọn đáp án chính xác nhất:")
                    .question("Our human resources department ________ comprehensive onboarding sessions for new personnel.")
                    .translation("Phòng nhân sự của chúng tôi thường xuyên tổ chức các buổi đào tạo hội nhập toàn diện cho nhân viên mới.")
                    .options(List.of("regularly conducts", "regularly conduct", "conducts regularly", "is conducting regularly"))
                    .correctAnswer("regularly conducts")
                    .correctIndex(0)
                    .explanation("Trạng từ tần suất 'regularly' đứng trước động từ thường 'conducts', và chủ ngữ số ít 'department' nên động từ thêm -s ('conducts').")
                    .grammarTip("Trạng từ tần suất đứng trước động từ thường (S + Adv + V).")
                    .build());

            mc.add(GrammarExerciseQuestionDto.builder()
                    .questionType("MULTIPLE_CHOICE")
                    .prompt("Chọn dạng phủ định chuẩn của thì Hiện tại đơn:")
                    .question("The company's updated security policy ________ employees to access internal servers from unauthorized personal devices.")
                    .translation("Chính sách bảo mật cập nhật của công ty không cho phép nhân viên truy cập máy chủ nội bộ từ các thiết bị cá nhân trái phép.")
                    .options(List.of("does not permit", "do not permit", "not permit", "is not permit"))
                    .correctAnswer("does not permit")
                    .correctIndex(0)
                    .explanation("Chủ ngữ 'policy' số ít -> dùng trợ động từ phủ định 'does not' + V nguyên thể 'permit'.")
                    .grammarTip("S(số ít) + does not + V(nguyên thể).")
                    .build());

            mc.add(GrammarExerciseQuestionDto.builder()
                    .questionType("MULTIPLE_CHOICE")
                    .prompt("Chọn dạng đúng diễn tả lịch trình cố định:")
                    .question("The shuttle bus to the international airport ________ from the hotel lobby every forty-five minutes.")
                    .translation("Xe đưa đón đến sân bay quốc tế khởi hành từ sảnh khách sạn cứ sau mỗi 45 phút.")
                    .options(List.of("departs", "depart", "departing", "is depart"))
                    .correctAnswer("departs")
                    .correctIndex(0)
                    .explanation("Hiện tại đơn được dùng để diễn tả lịch trình phương tiện công cộng cố định ('The shuttle bus' số ít -> 'departs').")
                    .grammarTip("Lịch trình tàu xe, máy bay, sự kiện cố định -> chia Hiện tại đơn.")
                    .build());

            mc.add(GrammarExerciseQuestionDto.builder()
                    .questionType("MULTIPLE_CHOICE")
                    .prompt("Chọn trợ động từ nghi vấn phù hợp:")
                    .question("________ the corporate wellness program include discounted gym memberships for contract workers?")
                    .translation("Chương trình chăm sóc sức khỏe doanh nghiệp có bao gồm thẻ tập gym giảm giá cho lao động hợp đồng không?")
                    .options(List.of("Does", "Do", "Is", "Are"))
                    .correctAnswer("Does")
                    .correctIndex(0)
                    .explanation("Chủ ngữ 'the corporate wellness program' là danh từ số ít -> dùng trợ động từ 'Does' trong câu hỏi.")
                    .grammarTip("Does + S(số ít) + V(nguyên thể)?")
                    .build());

            fib.add(GrammarExerciseQuestionDto.builder()
                    .questionType("FILL_IN_BLANK")
                    .prompt("Điền dạng đúng của động từ trong ngoặc (Hiện tại đơn):")
                    .baseWord("oversee")
                    .question("The chief technology officer personally ________ (oversee) all cybersecurity upgrades across global branches.")
                    .translation("Giám đốc công nghệ trực tiếp giám sát tất cả các đợt nâng cấp an ninh mạng trên toàn bộ các chi nhánh toàn cầu.")
                    .options(List.of("oversees", "oversee", "overseeing", "is oversee"))
                    .correctAnswer("oversees")
                    .explanation("Chủ ngữ 'The chief technology officer' là ngôi thứ 3 số ít, diễn tả sự thật công việc thường nhật -> thêm -s thành 'oversees'.")
                    .grammarTip("He/She/Danh từ số ít + V(s/es).")
                    .build());

            fib.add(GrammarExerciseQuestionDto.builder()
                    .questionType("FILL_IN_BLANK")
                    .prompt("Điền dạng đúng của động từ có quy tắc đuôi -y:")
                    .baseWord("modify")
                    .question("The lead architect constantly ________ (modify) structural plans to satisfy updated municipal safety regulations.")
                    .translation("Kiến trúc sư trưởng liên tục sửa đổi các bản thiết kế kết cấu để đáp ứng các quy định an toàn đô thị cập nhật.")
                    .options(List.of("modifies", "modifys", "modify", "is modify"))
                    .correctAnswer("modifies")
                    .explanation("Chủ ngữ số ít 'The lead architect', động từ 'modify' tận cùng phụ âm 'f' + 'y' -> đổi 'y' thành 'i' rồi thêm -es ('modifies').")
                    .grammarTip("Phụ âm + y -> -ies (modify -> modifies, verify -> verifies).")
                    .build());

            fib.add(GrammarExerciseQuestionDto.builder()
                    .questionType("FILL_IN_BLANK")
                    .prompt("Điền dạng phủ định của động từ trong ngoặc:")
                    .baseWord("require")
                    .question("The revised contract terms ________ (not / require) a deposit prior to standard service activation.")
                    .translation("Các điều khoản hợp đồng sửa đổi không yêu cầu đặt cọc trước khi kích hoạt dịch vụ tiêu chuẩn.")
                    .options(List.of("do not require", "does not require", "not require", "are not require"))
                    .correctAnswer("do not require")
                    .explanation("Chủ ngữ 'The revised contract terms' là danh từ số nhiều (terms) -> dùng trợ động từ phủ định 'do not require'.")
                    .grammarTip("Danh từ số nhiều + DO NOT (don't) + V.")
                    .build());

            fib.add(GrammarExerciseQuestionDto.builder()
                    .questionType("FILL_IN_BLANK")
                    .prompt("Chia động từ bất quy tắc 'have' trong ngoặc:")
                    .baseWord("have")
                    .question("Each regional branch ________ (have) its own autonomous marketing budget.")
                    .translation("Mỗi chi nhánh khu vực đều có ngân sách tiếp thị tự chủ riêng.")
                    .options(List.of("has", "haves", "have", "is have"))
                    .correctAnswer("has")
                    .explanation("'Each + danh từ số ít' luôn được coi là chủ ngữ số ít -> động từ 'have' biến đổi thành 'has'.")
                    .grammarTip("Each / Every + N(số ít) đi với động từ số ít (has).")
                    .build());

            fib.add(GrammarExerciseQuestionDto.builder()
                    .questionType("FILL_IN_BLANK")
                    .prompt("Điền dạng đúng của động từ có đuôi -ch:")
                    .baseWord("dispatch")
                    .question("The distribution center ________ (dispatch) over five hundred parcels to local retailers daily.")
                    .translation("Trung tâm phân phối gửi đi hơn 500 bưu kiện tới các nhà bán lẻ địa phương mỗi ngày.")
                    .options(List.of("dispatches", "dispatchs", "dispatch", "is dispatch"))
                    .correctAnswer("dispatches")
                    .explanation("Chủ ngữ 'The distribution center' số ít, động từ kết thúc bằng 'ch' nên thêm -es thành 'dispatches'.")
                    .grammarTip("Tận cùng ch -> thêm es: dispatch -> dispatches.")
                    .build());

            err.add(GrammarExerciseQuestionDto.builder()
                    .questionType("ERROR_CORRECTION")
                    .prompt("Tìm phần gạch chân bị sai ngữ pháp trong câu:")
                    .question("The senior compliance officer [A] verify [B] every commercial invoice before [C] authorizing [D] the bank transfer.")
                    .translation("Chuyên viên tuân thủ cấp cao kiểm tra từng hóa đơn thương mại trước khi ủy quyền chuyển khoản ngân hàng.")
                    .options(List.of("A. verify", "B. every commercial invoice", "C. authorizing", "D. the bank transfer"))
                    .correctAnswer("A. verify")
                    .correctIndex(0)
                    .correctedWord("verifies")
                    .explanation("Chủ ngữ 'The senior compliance officer' là danh từ số ít, hành động thói quen thường nhật phải chia Hiện tại đơn: 'verifies' (đổi y thành ies).")
                    .grammarTip("Chủ ngữ số ít -> V(s/es): verify -> verifies.")
                    .build());

            err.add(GrammarExerciseQuestionDto.builder()
                    .questionType("ERROR_CORRECTION")
                    .prompt("Tìm lỗi sai về trợ động từ phủ định:")
                    .question("The newly installed software [A] don't support [B] legacy database formats [C] on outdated [D] workstations.")
                    .translation("Phần mềm mới được cài đặt không hỗ trợ các định dạng cơ sở dữ liệu cũ trên các máy trạm lỗi thời.")
                    .options(List.of("A. don't support", "B. legacy database formats", "C. on outdated", "D. workstations"))
                    .correctAnswer("A. don't support")
                    .correctIndex(0)
                    .correctedWord("doesn't support")
                    .explanation("Chủ ngữ 'software' là danh từ không đếm được (tương đương số ít) -> trợ động từ phải là 'doesn't support', không dùng 'don't'.")
                    .grammarTip("Danh từ không đếm được / Danh từ số ít đi với DOESN'T.")
                    .build());

            err.add(GrammarExerciseQuestionDto.builder()
                    .questionType("ERROR_CORRECTION")
                    .prompt("Tìm lỗi sai về chia động từ sau trợ động từ:")
                    .question("Our chief financial advisor [A] does not [B] recommends [C] investing in high-risk ventures [D] at this moment.")
                    .translation("Cố vấn tài chính trưởng của chúng tôi không khuyến nghị đầu tư vào các dự án mạo hiểm có rủi ro cao vào thời điểm này.")
                    .options(List.of("A. does not", "B. recommends", "C. investing in", "D. at this moment"))
                    .correctAnswer("B. recommends")
                    .correctIndex(1)
                    .correctedWord("recommend")
                    .explanation("Sau trợ động từ 'does not', động từ chính bắt buộc giữ nguyên mẫu 'recommend', không được thêm 's'.")
                    .grammarTip("Does not + V(nguyên thể), không thêm s/es vào động từ chính.")
                    .build());

            err.add(GrammarExerciseQuestionDto.builder()
                    .questionType("ERROR_CORRECTION")
                    .prompt("Tìm lỗi sai về hòa hợp chủ vị ở thì Hiện tại đơn:")
                    .question("The committee members [A] meets [B] in the executive boardroom [C] every Wednesday [D] morning.")
                    .translation("Các thành viên ủy ban họp tại phòng họp điều hành vào mỗi sáng thứ Tư.")
                    .options(List.of("A. meets", "B. in the executive boardroom", "C. every Wednesday", "D. morning"))
                    .correctAnswer("A. meets")
                    .correctIndex(0)
                    .correctedWord("meet")
                    .explanation("Chủ ngữ 'The committee members' là danh từ số nhiều (members) nên động từ Hiện tại đơn giữ nguyên mẫu 'meet', không thêm 's'.")
                    .grammarTip("Chủ ngữ số nhiều -> V(nguyên mẫu).")
                    .build());

            err.add(GrammarExerciseQuestionDto.builder()
                    .questionType("ERROR_CORRECTION")
                    .prompt("Tìm lỗi sai trong câu hỏi:")
                    .question("Does [A] our corporate clients [B] receive [C] promotional updates [D] on a weekly basis?")
                    .translation("Các khách hàng doanh nghiệp của chúng tôi có nhận được thông tin khuyến mãi hàng tuần không?")
                    .options(List.of("A. Does", "B. receive", "C. promotional updates", "D. on a weekly basis"))
                    .correctAnswer("A. Does")
                    .correctIndex(0)
                    .correctedWord("Do")
                    .explanation("Chủ ngữ 'clients' là danh từ số nhiều nên trợ động từ câu hỏi phải là 'Do', không dùng 'Does'.")
                    .grammarTip("Do + S(số nhiều) + V? Does + S(số ít) + V?")
                    .build());
        }

        return selectQuestionsByMode(mc, fib, err, isFillInBlank, isErrorCorrection, isMixed);
    }

    private List<GrammarExerciseQuestionDto> generatePresentContinuousQuestions(boolean isBeginner, boolean isFillInBlank, boolean isErrorCorrection, boolean isMixed) {
        List<GrammarExerciseQuestionDto> mc = new ArrayList<>();
        List<GrammarExerciseQuestionDto> fib = new ArrayList<>();
        List<GrammarExerciseQuestionDto> err = new ArrayList<>();

        mc.add(GrammarExerciseQuestionDto.builder()
                .questionType("MULTIPLE_CHOICE")
                .prompt("Chọn đáp án chính xác nhất (Thì Hiện tại tiếp diễn):")
                .question("Look! The maintenance technicians ________ the main power generator.")
                .translation("Nhìn kìa! Các kỹ thuật viên bảo trì đang sửa chữa máy phát điện chính.")
                .options(List.of("are repairing", "is repairing", "repair", "repaired"))
                .correctAnswer("are repairing")
                .correctIndex(0)
                .explanation("Dấu hiệu 'Look!' chỉ hành động đang xảy ra tại thời điểm nói; chủ ngữ 'technicians' số nhiều -> chia 'are repairing'.")
                .grammarTip("Look! / Listen! / at the moment -> am/is/are + V-ing.")
                .build());

        fib.add(GrammarExerciseQuestionDto.builder()
                .questionType("FILL_IN_BLANK")
                .prompt("Điền dạng đúng của động từ trong ngoặc (Hiện tại tiếp diễn):")
                .baseWord("prepare")
                .question("The marketing team ________ (prepare) the presentation slides for the client right now.")
                .translation("Đội tiếp thị đang chuẩn bị các slide thuyết trình cho khách hàng ngay lúc này.")
                .options(List.of("is preparing", "are preparing", "prepares", "preparing"))
                .correctAnswer("is preparing")
                .explanation("Dấu hiệu 'right now' và chủ ngữ 'team' (danh từ số ít) -> chia 'is preparing'.")
                .grammarTip("Right now -> am/is/are + V-ing.")
                .build());

        err.add(GrammarExerciseQuestionDto.builder()
                .questionType("ERROR_CORRECTION")
                .prompt("Tìm phần bị sai ngữ pháp:")
                .question("She [A] is work [B] on the urgent [C] financial audit [D] at the moment.")
                .translation("Cô ấy đang làm việc cho đợt kiểm toán tài chính khẩn cấp vào lúc này.")
                .options(List.of("A. is work", "B. on the urgent", "C. financial audit", "D. at the moment"))
                .correctAnswer("A. is work")
                .correctIndex(0)
                .correctedWord("is working")
                .explanation("Thì Hiện tại tiếp diễn phải có dạng to be + V-ing ('is working'), không dùng 'is work'.")
                .grammarTip("To be + V-ing.")
                .build());

        return selectQuestionsByMode(mc, fib, err, isFillInBlank, isErrorCorrection, isMixed);
    }

    private List<GrammarExerciseQuestionDto> generatePastSimpleQuestions(boolean isBeginner, boolean isFillInBlank, boolean isErrorCorrection, boolean isMixed) {
        List<GrammarExerciseQuestionDto> mc = new ArrayList<>();
        List<GrammarExerciseQuestionDto> fib = new ArrayList<>();
        List<GrammarExerciseQuestionDto> err = new ArrayList<>();

        mc.add(GrammarExerciseQuestionDto.builder()
                .questionType("MULTIPLE_CHOICE")
                .prompt("Chọn đáp án chính xác nhất (Thì Quá khứ đơn):")
                .question("The board of directors ________ the expansion budget during yesterday's meeting.")
                .translation("Hội đồng quản trị đã thông qua ngân sách mở rộng trong cuộc họp ngày hôm qua.")
                .options(List.of("approved", "approve", "approves", "has approved"))
                .correctAnswer("approved")
                .correctIndex(0)
                .explanation("Dấu hiệu 'yesterday's meeting' chỉ thời điểm xác định trong quá khứ -> chia Quá khứ đơn (V-ed): 'approved'.")
                .grammarTip("Yesterday / Ago / Last -> Quá khứ đơn (V2/ed).")
                .build());

        fib.add(GrammarExerciseQuestionDto.builder()
                .questionType("FILL_IN_BLANK")
                .prompt("Điền dạng quá khứ của động từ trong ngoặc:")
                .baseWord("dispatch")
                .question("The logistics vendor ________ (dispatch) the replacement parts two days ago.")
                .translation("Nhà cung cấp vận chuyển đã gửi các bộ phận thay thế cách đây 2 ngày.")
                .options(List.of("dispatched", "dispatch", "dispatches", "has dispatched"))
                .correctAnswer("dispatched")
                .explanation("Dấu hiệu 'two days ago' -> chia Quá khứ đơn 'dispatched'.")
                .grammarTip("Ago -> Quá khứ đơn.")
                .build());

        err.add(GrammarExerciseQuestionDto.builder()
                .questionType("ERROR_CORRECTION")
                .prompt("Tìm phần bị sai ngữ pháp:")
                .question("The manager [A] didn't received [B] the signed contract [C] yesterday [D] afternoon.")
                .translation("Người quản lý đã không nhận được hợp đồng đã ký vào chiều hôm qua.")
                .options(List.of("A. didn't received", "B. the signed contract", "C. yesterday", "D. afternoon"))
                .correctAnswer("A. didn't received")
                .correctIndex(0)
                .correctedWord("didn't receive")
                .explanation("Sau trợ động từ phủ định 'didn't', động từ chính phải ở dạng nguyên thể 'receive', không chia 'received'.")
                .grammarTip("Didn't + V(nguyên thể).")
                .build());

        return selectQuestionsByMode(mc, fib, err, isFillInBlank, isErrorCorrection, isMixed);
    }

    private List<GrammarExerciseQuestionDto> generatePresentPerfectQuestions(boolean isBeginner, boolean isFillInBlank, boolean isErrorCorrection, boolean isMixed) {
        List<GrammarExerciseQuestionDto> mc = new ArrayList<>();
        List<GrammarExerciseQuestionDto> fib = new ArrayList<>();
        List<GrammarExerciseQuestionDto> err = new ArrayList<>();

        mc.add(GrammarExerciseQuestionDto.builder()
                .questionType("MULTIPLE_CHOICE")
                .prompt("Chọn đáp án chính xác nhất (Thì Hiện tại hoàn thành):")
                .question("Ms. Robinson ________ at this consulting firm since she graduated from university in 2018.")
                .translation("Cô Robinson đã làm việc tại công ty tư vấn này kể từ khi cô tốt nghiệp đại học năm 2018.")
                .options(List.of("has worked", "worked", "works", "is working"))
                .correctAnswer("has worked")
                .correctIndex(0)
                .explanation("Cấu trúc 'Since + mốc thời gian quá khứ', mệnh đề chính chia Hiện tại hoàn thành: 'has worked'.")
                .grammarTip("Since + mốc thời gian -> have/has + V3/ed.")
                .build());

        fib.add(GrammarExerciseQuestionDto.builder()
                .questionType("FILL_IN_BLANK")
                .prompt("Điền dạng đúng của động từ trong ngoặc (Hiện tại hoàn thành):")
                .baseWord("finalize")
                .question("The accounting team ________ (already / finalize) the annual expenditure review.")
                .translation("Đội ngũ kế toán đã hoàn tất việc rà soát chi tiêu hàng năm.")
                .options(List.of("has already finalized", "already finalized", "have already finalized", "is finalizing"))
                .correctAnswer("has already finalized")
                .explanation("Trạng từ 'already' đi kèm thì Hiện tại hoàn thành, chủ ngữ 'team' số ít -> 'has already finalized'.")
                .grammarTip("Already -> have/has already + V3/ed.")
                .build());

        err.add(GrammarExerciseQuestionDto.builder()
                .questionType("ERROR_CORRECTION")
                .prompt("Tìm phần bị sai ngữ pháp:")
                .question("The committee members [A] has reviewed [B] all grant applications [C] three times [D] already.")
                .translation("Các thành viên ủy ban đã xem xét tất cả đơn xin tài trợ 3 lần rồi.")
                .options(List.of("A. has reviewed", "B. all grant applications", "C. three times", "D. already"))
                .correctAnswer("A. has reviewed")
                .correctIndex(0)
                .correctedWord("have reviewed")
                .explanation("Chủ ngữ 'The committee members' là danh từ số nhiều (members) nên trợ động từ phải là 'have', không dùng 'has'.")
                .grammarTip("Chủ ngữ số nhiều + HAVE + V3/ed.")
                .build());

        return selectQuestionsByMode(mc, fib, err, isFillInBlank, isErrorCorrection, isMixed);
    }

    private List<GrammarExerciseQuestionDto> generateConditionalQuestions(boolean isBeginner, boolean isFillInBlank, boolean isErrorCorrection, boolean isMixed) {
        List<GrammarExerciseQuestionDto> mc = new ArrayList<>();
        List<GrammarExerciseQuestionDto> fib = new ArrayList<>();
        List<GrammarExerciseQuestionDto> err = new ArrayList<>();

        mc.add(GrammarExerciseQuestionDto.builder()
                .questionType("MULTIPLE_CHOICE")
                .prompt("Chọn đáp án chính xác nhất (Câu điều kiện loại 1):")
                .question("If the delivery truck ________ before noon, our technicians will start the installation immediately.")
                .translation("Nếu xe tải giao hàng đến trước buổi trưa, các kỹ thuật viên của chúng tôi sẽ bắt đầu lắp đặt ngay lập tức.")
                .options(List.of("arrives", "will arrive", "arrived", "is arriving"))
                .correctAnswer("arrives")
                .correctIndex(0)
                .explanation("Trong câu điều kiện loại 1, mệnh đề If chia ở thì Hiện tại đơn ('arrives' cho chủ ngữ số ít), mệnh đề chính dùng will + V.")
                .grammarTip("If + S + V(hiện tại đơn), S + will + V(nguyên thể).")
                .build());

        fib.add(GrammarExerciseQuestionDto.builder()
                .questionType("FILL_IN_BLANK")
                .prompt("Điền dạng đúng của động từ trong ngoặc (Điều kiện loại 1):")
                .baseWord("approve")
                .question("Unless the client ________ (approve) the revised specification, we cannot order new supplies.")
                .translation("Trừ khi khách hàng phê duyệt thông số kỹ thuật sửa đổi, chúng tôi không thể đặt mua vật tư mới.")
                .options(List.of("approves", "will approve", "approved", "is approving"))
                .correctAnswer("approves")
                .explanation("Sau 'Unless' (= If not), mệnh đề chia ở thì Hiện tại đơn khẳng định: 'approves'.")
                .grammarTip("Unless + S + V(hiện tại đơn).")
                .build());

        err.add(GrammarExerciseQuestionDto.builder()
                .questionType("ERROR_CORRECTION")
                .prompt("Tìm phần bị sai ngữ pháp:")
                .question("If the CEO [A] will sign [B] the contract tomorrow, we [C] will begin [D] operations.")
                .translation("Nếu Tổng giám đốc ký hợp đồng vào ngày mai, chúng tôi sẽ bắt đầu hoạt động.")
                .options(List.of("A. will sign", "B. the contract tomorrow", "C. will begin", "D. operations"))
                .correctAnswer("A. will sign")
                .correctIndex(0)
                .correctedWord("signs")
                .explanation("Mệnh đề If không được dùng thì tương lai với 'will', mà bắt buộc chia thì Hiện tại đơn: 'signs'.")
                .grammarTip("Không dùng WILL trong mệnh đề If của câu điều kiện loại 1.")
                .build());

        return selectQuestionsByMode(mc, fib, err, isFillInBlank, isErrorCorrection, isMixed);
    }

    private List<GrammarExerciseQuestionDto> generatePassiveVoiceQuestions(boolean isBeginner, boolean isFillInBlank, boolean isErrorCorrection, boolean isMixed) {
        List<GrammarExerciseQuestionDto> mc = new ArrayList<>();
        List<GrammarExerciseQuestionDto> fib = new ArrayList<>();
        List<GrammarExerciseQuestionDto> err = new ArrayList<>();

        mc.add(GrammarExerciseQuestionDto.builder()
                .questionType("MULTIPLE_CHOICE")
                .prompt("Chọn đáp án chính xác nhất (Câu bị động):")
                .question("All confidential financial statements ________ by an external audit firm each quarter.")
                .translation("Tất cả các báo cáo tài chính mật đều được kiểm toán bởi một công ty kiểm toán độc lập mỗi quý.")
                .options(List.of("are audited", "is audited", "audit", "are auditing"))
                .correctAnswer("are audited")
                .correctIndex(0)
                .explanation("Chủ ngữ 'statements' số nhiều chịu tác động của hành động -> chia bị động thì hiện tại đơn: 'are audited'.")
                .grammarTip("Bị động Hiện tại đơn: S(số nhiều) + are + V3/ed (+ by O).")
                .build());

        fib.add(GrammarExerciseQuestionDto.builder()
                .questionType("FILL_IN_BLANK")
                .prompt("Điền dạng bị động của động từ trong ngoặc:")
                .baseWord("publish")
                .question("The quarterly research findings ________ (publish) on our official website every month.")
                .translation("Các phát hiện nghiên cứu hàng quý được xuất bản trên trang web chính thức của chúng tôi mỗi tháng.")
                .options(List.of("are published", "is published", "publish", "published"))
                .correctAnswer("are published")
                .explanation("Chủ ngữ số nhiều 'findings' ở dạng bị động hiện tại đơn -> 'are published'.")
                .grammarTip("Bị động: be + V3/ed.")
                .build());

        err.add(GrammarExerciseQuestionDto.builder()
                .questionType("ERROR_CORRECTION")
                .prompt("Tìm phần bị sai ngữ pháp:")
                .question("The defective components [A] was returned [B] to the original manufacturer [C] by [D] the quality team.")
                .translation("Các linh kiện bị lỗi đã được trả lại cho nhà sản xuất ban đầu bởi đội ngũ chất lượng.")
                .options(List.of("A. was returned", "B. to the original manufacturer", "C. by", "D. the quality team"))
                .correctAnswer("A. was returned")
                .correctIndex(0)
                .correctedWord("were returned")
                .explanation("Chủ ngữ 'components' là danh từ số nhiều nên động từ to-be ở quá khứ phải là 'were', không dùng 'was'.")
                .grammarTip("Danh từ số nhiều + were + V3/ed.")
                .build());

        return selectQuestionsByMode(mc, fib, err, isFillInBlank, isErrorCorrection, isMixed);
    }

    private List<GrammarExerciseQuestionDto> generateGenericTopicQuestions(GrammarTopic topic, boolean isBeginner, boolean isFillInBlank, boolean isErrorCorrection, boolean isMixed) {
        String name = topic.getTopic() != null ? topic.getTopic() : "Ngữ pháp";
        String structure = StringUtils.hasText(topic.getStructure()) ? topic.getStructure() : "Quy tắc ngữ pháp chuẩn";

        List<GrammarExerciseQuestionDto> mc = new ArrayList<>();
        List<GrammarExerciseQuestionDto> fib = new ArrayList<>();
        List<GrammarExerciseQuestionDto> err = new ArrayList<>();

        mc.add(GrammarExerciseQuestionDto.builder()
                .questionType("MULTIPLE_CHOICE")
                .prompt("Chọn phương án áp dụng đúng chủ điểm '" + name + "':")
                .question(String.format("According to the core rules of %s, which option best completes the sentence?", name))
                .translation(String.format("Theo các quy tắc cốt lõi của %s, phương án nào hoàn thành câu chuẩn xác nhất?", name))
                .options(List.of("correctly applied", "incorrectly applied", "applying error", "wrong application"))
                .correctAnswer("correctly applied")
                .correctIndex(0)
                .explanation(String.format("Câu hỏi kiểm tra việc áp dụng cấu trúc '%s' của chủ đề %s.", structure, name))
                .grammarTip("Ôn kỹ công thức: " + structure)
                .build());

        fib.add(GrammarExerciseQuestionDto.builder()
                .questionType("FILL_IN_BLANK")
                .prompt("Điền dạng đúng để áp dụng chuẩn '" + name + "':")
                .baseWord("apply")
                .question(String.format("Please ensure you ________ (apply) the rules of %s accurately in formal writing.", name))
                .translation(String.format("Hãy đảm bảo bạn áp dụng chuẩn xác quy tắc của %s trong văn phong trang trọng.", name))
                .options(List.of("apply", "applies", "applying", "applied"))
                .correctAnswer("apply")
                .explanation(String.format("Sau 'ensure you', động từ giữ nguyên thể 'apply' theo cấu trúc của %s.", name))
                .grammarTip("Công thức: " + structure)
                .build());

        err.add(GrammarExerciseQuestionDto.builder()
                .questionType("ERROR_CORRECTION")
                .prompt("Tìm lỗi sai trong việc áp dụng '" + name + "':")
                .question(String.format("The student [A] try to apply [B] %s but [C] forgets [D] the main rule.", name))
                .translation(String.format("Học sinh cố gắng áp dụng %s nhưng quên quy tắc chính.", name))
                .options(List.of("A. try to apply", "B. " + name, "C. forgets", "D. the main rule"))
                .correctAnswer("A. try to apply")
                .correctIndex(0)
                .correctedWord("tries to apply")
                .explanation("Chủ ngữ số ít 'The student' cần chia động từ thêm -s/es: 'tries to apply'.")
                .grammarTip("Chú ý chủ ngữ số ít đi với động từ số ít.")
                .build());

        return selectQuestionsByMode(mc, fib, err, isFillInBlank, isErrorCorrection, isMixed);
    }

    private List<GrammarExerciseQuestionDto> selectQuestionsByMode(List<GrammarExerciseQuestionDto> mc,
                                                                   List<GrammarExerciseQuestionDto> fib,
                                                                   List<GrammarExerciseQuestionDto> err,
                                                                   boolean isFillInBlank,
                                                                   boolean isErrorCorrection,
                                                                   boolean isMixed) {
        if (isFillInBlank) {
            return !fib.isEmpty() ? fib : mc;
        }
        if (isErrorCorrection) {
            return !err.isEmpty() ? err : mc;
        }
        if (isMixed) {
            List<GrammarExerciseQuestionDto> mixed = new ArrayList<>();
            int max = Math.max(mc.size(), Math.max(fib.size(), err.size()));
            for (int i = 0; i < max; i++) {
                if (i < mc.size()) mixed.add(mc.get(i));
                if (i < fib.size()) mixed.add(fib.get(i));
                if (i < err.size()) mixed.add(err.get(i));
            }
            return mixed;
        }
        return mc;
    }
    @Transactional
    public GrammarExerciseHistoryResponse submitExercise(Long userId, SubmitGrammarExerciseRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        GrammarTopic topic = grammarTopicRepository.findByIdAndUserId(request.getTopicId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chủ đề ngữ pháp với id: " + request.getTopicId()));

        String questionsJson = "";
        String answersJson = "";
        try {
            questionsJson = objectMapper.writeValueAsString(request.getQuestions() != null ? request.getQuestions() : List.of());
            answersJson = objectMapper.writeValueAsString(request.getUserAnswers() != null ? request.getUserAnswers() : Map.of());
        } catch (Exception e) {
            log.error("Failed to serialize grammar exercise json: {}", e.getMessage());
        }

        GrammarExerciseHistory history = GrammarExerciseHistory.builder()
                .user(user)
                .grammarTopic(topic)
                .title(StringUtils.hasText(request.getTitle()) ? request.getTitle() : "Luyện tập: " + topic.getTopic())
                .exerciseType(StringUtils.hasText(request.getExerciseType()) ? request.getExerciseType() : "MULTIPLE_CHOICE")
                .level(StringUtils.hasText(request.getLevel()) ? request.getLevel() : topic.getLevel())
                .totalQuestions(request.getTotalQuestions() != null ? request.getTotalQuestions() : 5)
                .correctCount(request.getCorrectCount() != null ? request.getCorrectCount() : 0)
                .score(request.getScore() != null ? request.getScore() : 0)
                .questionsData(questionsJson)
                .userAnswers(answersJson)
                .timeSpentSeconds(request.getTimeSpentSeconds() != null ? request.getTimeSpentSeconds() : 0)
                .completedAt(LocalDateTime.now())
                .build();

        GrammarExerciseHistory saved = grammarExerciseHistoryRepository.save(history);

        // Update topic review statistics
        topic.setReviewCount((topic.getReviewCount() != null ? topic.getReviewCount() : 0) + 1);
        topic.setLastReviewedAt(LocalDateTime.now());
        if (saved.getScore() >= 80 && (topic.getMasteryLevel() == null || topic.getMasteryLevel() < 5)) {
            topic.setMasteryLevel(Math.min(5, (topic.getMasteryLevel() != null ? topic.getMasteryLevel() : 0) + 1));
            if (topic.getMasteryLevel() >= 4) {
                topic.setStatus("MASTERED");
            }
        }
        grammarTopicRepository.save(topic);

        return mapToHistoryResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<GrammarExerciseHistoryResponse> getTopicHistory(Long userId, Long topicId) {
        List<GrammarExerciseHistory> list = grammarExerciseHistoryRepository.findByUserIdAndGrammarTopicIdOrderByCompletedAtDesc(userId, topicId);
        List<GrammarExerciseHistoryResponse> dtoList = new ArrayList<>();
        for (GrammarExerciseHistory h : list) {
            dtoList.add(mapToHistoryResponse(h));
        }
        return dtoList;
    }

    @Transactional(readOnly = true)
    public GrammarExerciseHistoryResponse getHistoryDetail(Long userId, Long historyId) {
        GrammarExerciseHistory history = grammarExerciseHistoryRepository.findByIdAndUserId(historyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kết quả bài tập với id: " + historyId));
        return mapToHistoryResponse(history);
    }

    private GrammarExerciseHistoryResponse mapToHistoryResponse(GrammarExerciseHistory h) {
        List<GrammarExerciseQuestionDto> questions = new ArrayList<>();
        Map<String, Object> answers = new HashMap<>();

        try {
            if (StringUtils.hasText(h.getQuestionsData())) {
                questions = objectMapper.readValue(h.getQuestionsData(), new TypeReference<List<GrammarExerciseQuestionDto>>() {});
            }
            if (StringUtils.hasText(h.getUserAnswers())) {
                answers = objectMapper.readValue(h.getUserAnswers(), new TypeReference<Map<String, Object>>() {});
            }
        } catch (Exception e) {
            log.warn("Failed to parse history json: {}", e.getMessage());
        }

        return GrammarExerciseHistoryResponse.builder()
                .id(h.getId())
                .grammarTopicId(h.getGrammarTopic().getId())
                .topicName(h.getGrammarTopic().getTopic())
                .title(h.getTitle())
                .exerciseType(h.getExerciseType())
                .level(h.getLevel())
                .totalQuestions(h.getTotalQuestions())
                .correctCount(h.getCorrectCount())
                .score(h.getScore())
                .timeSpentSeconds(h.getTimeSpentSeconds())
                .questions(questions)
                .userAnswers(answers)
                .completedAt(h.getCompletedAt())
                .createdAt(h.getCreatedAt())
                .build();
    }
}
