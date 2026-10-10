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

            prompt.append("\nQUY TẮC BẮT BUỘC VÀ ĐỘ KHÓ THEO CẤP ĐỘ:\n");
            prompt.append("1. ĐỘ KHÓ THEO CẤP ĐỘ (RẤT QUAN TRỌNG):\n");
            prompt.append("   - Nếu cấp độ là A0 hoặc A1 (Dành cho người MẤT GỐC / MỚI BẮT ĐẦU HỌC LẠI):\n");
            prompt.append("     + BẮT BUỘC dùng câu SIÊU NGẮN (5-8 từ), từ vựng đời sống siêu cơ bản và quen thuộc (I, you, he, she, it, we, they, go, eat, study, play, watch, like, have, cat, dog, book, school, home, every day, now, yesterday, tomorrow...).\n");
            prompt.append("     + TUYỆT ĐỐI KHÔNG dùng từ vựng công sở, kinh tế, báo cáo, thuật ngữ thương mại phức tạp.\n");
            prompt.append("     + Câu hỏi tập trung giúp người mất gốc áp dụng đúng công thức nền tảng một cách trực quan, dễ hiểu.\n");
            prompt.append("   - Nếu cấp độ là A2, B1, B2, C1: Ngữ cảnh câu hỏi gắn với giao tiếp thực tế và môi trường công sở chuẩn format đề thi TOEIC Part 5 & 6.\n");
            prompt.append("2. Đối với dạng FILL_IN_BLANK: câu hỏi chứa chỗ trống '________ (từ gốc)' và 'baseWord' là từ trong ngoặc. Người làm sẽ điền từ/dạng chia đúng vào chỗ trống.\n");
            prompt.append("3. Đối với dạng ERROR_CORRECTION: câu chứa 4 phần gạch chân/đánh dấu [A], [B], [C], [D]. Một trong 4 phần bị sai ngữ pháp. 'correctAnswer' là phương án sai, 'correctedWord' là từ/cụm từ đúng sau khi sửa.\n");
            prompt.append("4. Đối với dạng MULTIPLE_CHOICE: 4 phương án A, B, C, D với bẫy thông minh.\n");
            prompt.append("5. Đối với dạng MIXED: phân bổ đều các câu thuộc 3 dạng trên.\n");
            prompt.append("6. Phần giải thích (explanation) bằng tiếng Việt chi tiết: Dịch nghĩa toàn câu, chỉ ra dấu hiệu nhận biết/thành phần câu, phân tích vì sao đáp án này đúng và các đáp án khác sai.\n");
            prompt.append("7. Kèm mẹo làm bài (grammarTip) ngắn gọn cho thí sinh.\n");
            prompt.append("8. Chỉ trả về JSON thuần dạng mảng array các object, không markdown, không lời chào. Schema mỗi phần tử:\n");

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
        String topicLower = topic.getTopic().toLowerCase();
        List<GrammarExerciseQuestionDto> pool = new ArrayList<>();

        boolean isFillInBlank = "FILL_IN_BLANK".equalsIgnoreCase(exType);
        boolean isErrorCorrection = "ERROR_CORRECTION".equalsIgnoreCase(exType);
        boolean isMixed = "MIXED".equalsIgnoreCase(exType);
        boolean isBeginner = "A0".equalsIgnoreCase(level) || "A1".equalsIgnoreCase(level) || (level != null && (level.toLowerCase().contains("mất gốc") || level.toLowerCase().contains("căn bản")));

        if (isBeginner) {
            if (isFillInBlank) {
                pool.add(GrammarExerciseQuestionDto.builder()
                        .id(1)
                        .questionType("FILL_IN_BLANK")
                        .prompt("Điền dạng đúng của động từ trong ngoặc (Thì Hiện tại đơn):")
                        .baseWord("play")
                        .question("She ________ (play) badminton with her brother every Sunday.")
                        .translation("Cô ấy chơi cầu lông với anh trai vào mỗi Chủ nhật.")
                        .options(List.of("plays", "play", "playing", "is play"))
                        .correctAnswer("plays")
                        .explanation("Chủ ngữ 'She' là ngôi thứ 3 số ít, câu diễn tả thói quen lặp lại ('every Sunday') nên động từ thêm -s ('plays').")
                        .grammarTip("He / She / It + V(s/es); I / You / We / They + V(nguyên thể).")
                        .build());

                pool.add(GrammarExerciseQuestionDto.builder()
                        .id(2)
                        .questionType("FILL_IN_BLANK")
                        .prompt("Điền dạng phủ định của động từ trong ngoặc:")
                        .baseWord("like")
                        .question("I ________ (not / like) drinking cold water in winter.")
                        .translation("Tôi không thích uống nước lạnh vào mùa đông.")
                        .options(List.of("do not like", "does not like", "not like", "am not like"))
                        .correctAnswer("do not like")
                        .explanation("Chủ ngữ 'I' dùng trợ động từ phủ định 'do not' (hoặc don't) + động từ nguyên thể 'like'.")
                        .grammarTip("I / You / We / They + DO NOT + V; He / She / It + DOES NOT + V.")
                        .build());

                pool.add(GrammarExerciseQuestionDto.builder()
                        .id(3)
                        .questionType("FILL_IN_BLANK")
                        .prompt("Điền dạng đúng của động từ trong ngoặc:")
                        .baseWord("live")
                        .question("My family ________ (live) in this house since 2021.")
                        .translation("Gia đình tôi đã sống trong ngôi nhà này từ năm 2021.")
                        .options(List.of("has lived", "have lived", "lived", "is living"))
                        .correctAnswer("has lived")
                        .explanation("Dấu hiệu 'since 2021' (mốc thời gian) chỉ hành động bắt đầu trong quá khứ và vẫn tiếp diễn -> dùng Hiện tại hoàn thành 'has lived'.")
                        .grammarTip("Since + mốc thời gian -> have/has + V3/ed.")
                        .build());

                pool.add(GrammarExerciseQuestionDto.builder()
                        .id(4)
                        .questionType("FILL_IN_BLANK")
                        .prompt("Chia động từ theo thì Hiện tại tiếp diễn:")
                        .baseWord("swim")
                        .question("Look! The children ________ (swim) in the swimming pool.")
                        .translation("Nhìn kìa! Những đứa trẻ đang bơi trong hồ bơi.")
                        .options(List.of("are swimming", "is swimming", "swim", "swimming"))
                        .correctAnswer("are swimming")
                        .explanation("Dấu hiệu 'Look!' báo hiệu hành động đang diễn ra ngay lúc nói -> Hiện tại tiếp diễn: are swimming (chủ ngữ 'children' số nhiều).")
                        .grammarTip("Look! / Listen! -> Hiện tại tiếp diễn (am/is/are + V-ing).")
                        .build());

                pool.add(GrammarExerciseQuestionDto.builder()
                        .id(5)
                        .questionType("FILL_IN_BLANK")
                        .prompt("Điền dạng quá khứ của động từ trong ngoặc:")
                        .baseWord("buy")
                        .question("My father ________ (buy) a new bicycle yesterday.")
                        .translation("Bố tôi đã mua một chiếc xe đạp mới vào ngày hôm qua.")
                        .options(List.of("bought", "buys", "buyed", "has bought"))
                        .correctAnswer("bought")
                        .explanation("Dấu hiệu 'yesterday' (hôm qua) chỉ hành động đã xảy ra trong quá khứ -> chia Quá khứ đơn 'bought' (bất quy tắc của buy).")
                        .grammarTip("Yesterday / Last week / Ago -> Quá khứ đơn (V2/ed).")
                        .build());
            } else if (isErrorCorrection) {
                pool.add(GrammarExerciseQuestionDto.builder()
                        .id(1)
                        .questionType("ERROR_CORRECTION")
                        .prompt("Tìm phần bị sai ngữ pháp trong câu:")
                        .question("She [A] go [B] to school [C] by bus [D] every day.")
                        .translation("Cô ấy đi học bằng xe buýt mỗi ngày.")
                        .options(List.of("A. go", "B. to school", "C. by bus", "D. every day"))
                        .correctAnswer("A. go")
                        .correctIndex(0)
                        .correctedWord("goes")
                        .explanation("Chủ ngữ 'She' là ngôi thứ 3 số ít nên động từ 'go' phải chia thành 'goes'. Sửa thành 'goes'.")
                        .grammarTip("He / She / It + V(s/es). 'Go' kết thúc bằng 'o' -> thêm 'es' thành 'goes'.")
                        .build());

                pool.add(GrammarExerciseQuestionDto.builder()
                        .id(2)
                        .questionType("ERROR_CORRECTION")
                        .prompt("Tìm từ sai trong câu:")
                        .question("They [A] is [B] very happy [C] with the [D] new puppy.")
                        .translation("Họ rất vui vẻ với chú cún mới.")
                        .options(List.of("A. is", "B. very happy", "C. with the", "D. new puppy"))
                        .correctAnswer("A. is")
                        .correctIndex(0)
                        .correctedWord("are")
                        .explanation("Chủ ngữ 'They' số nhiều đi với to-be là 'are', không dùng 'is'. Sửa 'is' thành 'are'.")
                        .grammarTip("They / We / You + ARE; He / She / It + IS; I + AM.")
                        .build());

                pool.add(GrammarExerciseQuestionDto.builder()
                        .id(3)
                        .questionType("ERROR_CORRECTION")
                        .prompt("Tìm lỗi sai trong câu:")
                        .question("He [A] don't [B] like drinking [C] black [D] coffee.")
                        .translation("Anh ấy không thích uống cà phê đen.")
                        .options(List.of("A. don't", "B. like drinking", "C. black", "D. coffee"))
                        .correctAnswer("A. don't")
                        .correctIndex(0)
                        .correctedWord("doesn't")
                        .explanation("Chủ ngữ 'He' số ít phải dùng trợ động từ phủ định 'doesn't' (does not), không dùng 'don't'. Sửa thành 'doesn't'.")
                        .grammarTip("He / She / It + DOESN'T + V.")
                        .build());

                pool.add(GrammarExerciseQuestionDto.builder()
                        .id(4)
                        .questionType("ERROR_CORRECTION")
                        .prompt("Tìm lỗi sai về thì:")
                        .question("I [A] have visited [B] my grandparents [C] yesterday [D] afternoon.")
                        .translation("Tôi đã đến thăm ông bà vào chiều hôm qua.")
                        .options(List.of("A. have visited", "B. my grandparents", "C. yesterday", "D. afternoon"))
                        .correctAnswer("A. have visited")
                        .correctIndex(0)
                        .correctedWord("visited")
                        .explanation("Có mốc thời gian quá khứ rõ ràng 'yesterday afternoon' thì bắt buộc dùng Quá khứ đơn 'visited', không dùng Hiện tại hoàn thành.")
                        .grammarTip("Có 'yesterday/last/ago' -> Chỉ dùng Quá khứ đơn (V2/ed).")
                        .build());

                pool.add(GrammarExerciseQuestionDto.builder()
                        .id(5)
                        .questionType("ERROR_CORRECTION")
                        .prompt("Tìm lỗi sai trợ động từ:")
                        .question("My mother [A] have worked [B] at that hospital [C] for ten [D] years.")
                        .translation("Mẹ tôi đã làm việc tại bệnh viện đó được 10 năm.")
                        .options(List.of("A. have worked", "B. at that hospital", "C. for ten", "D. years"))
                        .correctAnswer("A. have worked")
                        .correctIndex(0)
                        .correctedWord("has worked")
                        .explanation("Chủ ngữ 'My mother' là danh từ số ít (tương đương She) nên trợ động từ phải là 'has', không dùng 'have'. Sửa thành 'has worked'.")
                        .grammarTip("Chủ ngữ số ít -> HAS + V3; Chủ ngữ số nhiều -> HAVE + V3.")
                        .build());
            } else if (isMixed) {
                pool.add(GrammarExerciseQuestionDto.builder()
                        .id(1)
                        .questionType("MULTIPLE_CHOICE")
                        .prompt("Chọn từ đúng để hoàn thành câu:")
                        .question("He ________ English every morning.")
                        .translation("Anh ấy học tiếng Anh vào mỗi buổi sáng.")
                        .options(List.of("studies", "study", "studying", "is study"))
                        .correctAnswer("studies")
                        .correctIndex(0)
                        .explanation("Chủ ngữ 'He' số ít, câu diễn tả thói quen 'every morning' nên động từ chia 'studies' (study đổi y thành i rồi thêm es).")
                        .grammarTip("He/She/It + V(s/es).")
                        .build());

                pool.add(GrammarExerciseQuestionDto.builder()
                        .id(2)
                        .questionType("FILL_IN_BLANK")
                        .prompt("Điền dạng đúng của từ trong ngoặc:")
                        .baseWord("play")
                        .question("She ________ (play) badminton every Sunday.")
                        .translation("Cô ấy chơi cầu lông vào mỗi Chủ nhật.")
                        .options(List.of("plays", "play", "playing", "is play"))
                        .correctAnswer("plays")
                        .explanation("Chủ ngữ 'She' số ít, thói quen lặp lại -> thêm -s thành 'plays'.")
                        .grammarTip("She + plays.")
                        .build());

                pool.add(GrammarExerciseQuestionDto.builder()
                        .id(3)
                        .questionType("ERROR_CORRECTION")
                        .prompt("Tìm lỗi sai trong câu:")
                        .question("They [A] is [B] very happy [C] today [D] morning.")
                        .translation("Họ rất vui vẻ vào sáng nay.")
                        .options(List.of("A. is", "B. very happy", "C. today", "D. morning"))
                        .correctAnswer("A. is")
                        .correctIndex(0)
                        .correctedWord("are")
                        .explanation("Chủ ngữ 'They' số nhiều đi với 'are', không dùng 'is'.")
                        .grammarTip("They + are.")
                        .build());

                pool.add(GrammarExerciseQuestionDto.builder()
                        .id(4)
                        .questionType("MULTIPLE_CHOICE")
                        .prompt("Chọn đáp án đúng (Quá khứ đơn):")
                        .question("We ________ to the cinema yesterday.")
                        .translation("Chúng tôi đã đi đến rạp chiếu phim vào ngày hôm qua.")
                        .options(List.of("went", "go", "goes", "going"))
                        .correctAnswer("went")
                        .correctIndex(0)
                        .explanation("Dấu hiệu 'yesterday' (hôm qua) -> dùng quá khứ của 'go' là 'went'.")
                        .grammarTip("Go -> Quá khứ là Went.")
                        .build());

                pool.add(GrammarExerciseQuestionDto.builder()
                        .id(5)
                        .questionType("FILL_IN_BLANK")
                        .prompt("Điền dạng phủ định của từ trong ngoặc:")
                        .baseWord("like")
                        .question("My sister ________ (not / like) drinking milk.")
                        .translation("Em gái tôi không thích uống sữa.")
                        .options(List.of("does not like", "do not like", "not like", "is not like"))
                        .correctAnswer("does not like")
                        .explanation("Chủ ngữ 'My sister' số ít dùng 'does not like' (hoặc doesn't like).")
                        .grammarTip("She / Sister -> does not like.")
                        .build());
            } else {
                pool.add(GrammarExerciseQuestionDto.builder()
                        .id(1)
                        .questionType("MULTIPLE_CHOICE")
                        .prompt("Chọn đáp án chính xác nhất:")
                        .question("He ________ English every morning.")
                        .translation("Anh ấy học tiếng Anh vào mỗi buổi sáng.")
                        .options(List.of("studies", "study", "studying", "is study"))
                        .correctAnswer("studies")
                        .correctIndex(0)
                        .explanation("Chủ ngữ 'He' số ít, câu diễn tả thói quen 'every morning' nên động từ chia 'studies'.")
                        .grammarTip("He/She/It + V(s/es).")
                        .build());

                pool.add(GrammarExerciseQuestionDto.builder()
                        .id(2)
                        .questionType("MULTIPLE_CHOICE")
                        .prompt("Chọn dạng quá khứ của động từ:")
                        .question("We ________ to the cinema yesterday.")
                        .translation("Chúng tôi đã đi đến rạp chiếu phim vào ngày hôm qua.")
                        .options(List.of("went", "go", "goes", "going"))
                        .correctAnswer("went")
                        .correctIndex(0)
                        .explanation("Dấu hiệu 'yesterday' (hôm qua) -> chia Quá khứ đơn 'went'.")
                        .grammarTip("Quá khứ của 'go' là 'went'.")
                        .build());

                pool.add(GrammarExerciseQuestionDto.builder()
                        .id(3)
                        .questionType("MULTIPLE_CHOICE")
                        .prompt("Chọn dạng phân từ đúng:")
                        .question("She has ________ three books this month.")
                        .translation("Cô ấy đã đọc ba cuốn sách trong tháng này.")
                        .options(List.of("read", "reads", "reading", "been read"))
                        .correctAnswer("read")
                        .correctIndex(0)
                        .explanation("Thì Hiện tại hoàn thành: has + V3. Động từ 'read' ở dạng V3 viết là 'read' (phát âm là /red/).")
                        .grammarTip("Has + V3.")
                        .build());

                pool.add(GrammarExerciseQuestionDto.builder()
                        .id(4)
                        .questionType("MULTIPLE_CHOICE")
                        .prompt("Chọn trợ động từ phù hợp:")
                        .question("They ________ not like spicy food.")
                        .translation("Họ không thích đồ ăn cay.")
                        .options(List.of("do", "does", "is", "are"))
                        .correctAnswer("do")
                        .correctIndex(0)
                        .explanation("Chủ ngữ 'They' số nhiều đi với trợ động từ 'do' trong câu phủ định 'do not like'.")
                        .grammarTip("They + do not.")
                        .build());

                pool.add(GrammarExerciseQuestionDto.builder()
                        .id(5)
                        .questionType("MULTIPLE_CHOICE")
                        .prompt("Chọn thì Hiện tại tiếp diễn đúng:")
                        .question("Look! The baby ________ in the bedroom.")
                        .translation("Nhìn kìa! Em bé đang ngủ trong phòng ngủ.")
                        .options(List.of("is sleeping", "sleep", "sleeps", "slept"))
                        .correctAnswer("is sleeping")
                        .correctIndex(0)
                        .explanation("Dấu hiệu 'Look!' báo hiệu hành động đang diễn ra -> Hiện tại tiếp diễn: is sleeping (chủ ngữ 'The baby' số ít).")
                        .grammarTip("Look! -> is/am/are + V-ing.")
                        .build());
            }
        } else if (isFillInBlank) {
            // Fill in the Blank / Verb Conjugation questions
            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(1)
                    .questionType("FILL_IN_BLANK")
                    .prompt("Điền dạng đúng của từ trong ngoặc để hoàn thành câu:")
                    .baseWord("submit")
                    .question("The marketing team ________ (submit) three revised reports since Mr. Henderson took over.")
                    .translation("Đội ngũ tiếp thị đã nộp 3 bản báo cáo sửa đổi kể từ khi ông Henderson tiếp quản.")
                    .options(List.of("has submitted", "have submitted", "submitted", "submitting"))
                    .correctAnswer("has submitted")
                    .explanation("Chủ ngữ 'The marketing team' là danh từ tập hợp số ít, kết hợp với mệnh đề 'since + S + V(quá khứ)', ta chia thì Hiện tại hoàn thành dạng 'has submitted'.")
                    .grammarTip("Since + mốc thời gian -> Mệnh đề chính chia Hiện tại hoàn thành.")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(2)
                    .questionType("FILL_IN_BLANK")
                    .prompt("Điền dạng đúng của động từ trong ngoặc:")
                    .baseWord("work")
                    .question("Ms. Lawson ________ (work) at the financial firm for more than ten years.")
                    .translation("Bà Lawson đã làm việc tại công ty tài chính này được hơn mười năm.")
                    .options(List.of("has worked", "have worked", "worked", "is working"))
                    .correctAnswer("has worked")
                    .explanation("Dấu hiệu 'for more than ten years' (khoảng thời gian) chỉ một hành động bắt đầu trong quá khứ và vẫn đang tiếp diễn, ta chia thì Hiện tại hoàn thành 'has worked'.")
                    .grammarTip("'For + khoảng thời gian' -> Dấu hiệu kinh điển của Hiện tại hoàn thành.")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(3)
                    .questionType("FILL_IN_BLANK")
                    .prompt("Chia động từ trong ngoặc theo đúng ngữ cảnh:")
                    .baseWord("approve")
                    .question("The project proposal has already ________ (approve) by the board of directors.")
                    .translation("Đề xuất dự án đã được phê duyệt bởi hội đồng quản trị.")
                    .options(List.of("been approved", "approved", "approving", "be approved"))
                    .correctAnswer("been approved")
                    .explanation("Đề xuất dự án 'được phê duyệt' (thể bị động của Hiện tại hoàn thành): S + have/has + been + V3/ed ('been approved').")
                    .grammarTip("Bị động Hiện tại hoàn thành: have/has + BEEN + V3/ed.")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(4)
                    .questionType("FILL_IN_BLANK")
                    .prompt("Điền dạng đúng của từ trong ngoặc:")
                    .baseWord("finalize")
                    .question("The regional director ________ (not / finalize) the contract terms yet.")
                    .translation("Giám đốc khu vực vẫn chưa hoàn tất các điều khoản hợp đồng.")
                    .options(List.of("has not finalized", "have not finalized", "did not finalize", "is not finalizing"))
                    .correctAnswer("has not finalized")
                    .explanation("Dấu hiệu 'yet' ở cuối câu mang nghĩa 'vẫn chưa', câu ở thể phủ định của thì Hiện tại hoàn thành: 'has not finalized'.")
                    .grammarTip("'Yet' đứng cuối câu phủ định Hiện tại hoàn thành.")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(5)
                    .questionType("FILL_IN_BLANK")
                    .prompt("Điền dạng đúng của từ trong ngoặc:")
                    .baseWord("receive")
                    .question("We ________ (receive) multiple inquiries from international investors this morning.")
                    .translation("Chúng tôi đã nhận được nhiều thắc mắc từ các nhà đầu tư quốc tế sáng nay.")
                    .options(List.of("have received", "has received", "received", "are receiving"))
                    .correctAnswer("have received")
                    .explanation("Chủ ngữ 'We' số nhiều đi với 'have + V3' ('have received').")
                    .grammarTip("We / They / You + have + V3; He / She / It + has + V3.")
                    .build());
        } else if (isErrorCorrection) {
            // Error Identification and Correction questions
            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(1)
                    .questionType("ERROR_CORRECTION")
                    .prompt("Tìm phần gạch chân bị sai ngữ pháp trong câu:")
                    .question("The committee [A] has decided [B] to postpone the conference because [C] they has not received [D] all registrations.")
                    .translation("Ủy ban đã quyết định hoãn hội nghị vì họ chưa nhận được tất cả các đăng ký.")
                    .options(List.of("A. has decided", "B. to postpone", "C. they has not received", "D. all registrations"))
                    .correctAnswer("C. they has not received")
                    .correctIndex(2)
                    .correctedWord("they have not received")
                    .explanation("Chủ ngữ 'they' là đại từ số nhiều nên trợ động từ phải là 'have', không thể dùng 'has'. Sửa thành 'they have not received'.")
                    .grammarTip("Lỗi hòa hợp chủ vị (Subject-Verb Agreement): 'They + have', không dùng 'They + has'.")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(2)
                    .questionType("ERROR_CORRECTION")
                    .prompt("Tìm một vị trí sai ngữ pháp trong câu:")
                    .question("Mr. Reynolds [A] has worked as [B] a senior consultant [C] since five years [D] at our headquarters.")
                    .translation("Ông Reynolds đã làm việc với tư cách là cố vấn cao cấp trong năm năm tại trụ sở của chúng tôi.")
                    .options(List.of("A. has worked", "B. a senior consultant", "C. since five years", "D. at our headquarters"))
                    .correctAnswer("C. since five years")
                    .correctIndex(2)
                    .correctedWord("for five years")
                    .explanation("'five years' là một khoảng thời gian (duration), bắt buộc phải dùng giới từ 'for' thay vì 'since'. Sửa thành 'for five years'.")
                    .grammarTip("'For + khoảng thời gian', 'Since + mốc thời gian'.")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(3)
                    .questionType("ERROR_CORRECTION")
                    .prompt("Tìm một vị trí sai ngữ pháp trong câu:")
                    .question("The newly appointed manager [A] have already [B] implemented several [C] productive cost-saving [D] measures.")
                    .translation("Người quản lý mới được bổ nhiệm đã thực hiện một số biện pháp tiết kiệm chi phí hiệu quả.")
                    .options(List.of("A. have already", "B. implemented", "C. productive", "D. measures"))
                    .correctAnswer("A. have already")
                    .correctIndex(0)
                    .correctedWord("has already")
                    .explanation("Chủ ngữ 'The newly appointed manager' là danh từ số ít (ngôi thứ 3 số ít), trợ động từ phải là 'has', không dùng 'have'. Sửa thành 'has already'.")
                    .grammarTip("Chủ ngữ số ít 'manager' -> dùng 'has'.")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(4)
                    .questionType("ERROR_CORRECTION")
                    .prompt("Tìm vị trí sai ngữ pháp:")
                    .question("The software update [A] was completely [B] install [C] by our technical support [D] specialists.")
                    .translation("Bản cập nhật phần mềm đã được cài đặt hoàn toàn bởi các chuyên gia hỗ trợ kỹ thuật của chúng tôi.")
                    .options(List.of("A. was completely", "B. install", "C. by our", "D. specialists"))
                    .correctAnswer("B. install")
                    .correctIndex(1)
                    .correctedWord("installed")
                    .explanation("Cấu trúc bị động ở Quá khứ đơn yêu cầu Quá khứ phân từ: was + Adv + V3/ed. Động từ 'install' phải đổi thành 'installed'.")
                    .grammarTip("Bị động: was/were + V3/ed.")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(5)
                    .questionType("ERROR_CORRECTION")
                    .prompt("Tìm vị trí sai ngữ pháp:")
                    .question("Although [A] the severe weather conditions, [B] the delivery flight [C] departed [D] exactly on schedule.")
                    .translation("Mặc dù điều kiện thời tiết khắc nghiệt, chuyến bay giao hàng vẫn khởi hành đúng lịch trình.")
                    .options(List.of("A. Although", "B. the delivery flight", "C. departed", "D. exactly on schedule"))
                    .correctAnswer("A. Although")
                    .correctIndex(0)
                    .correctedWord("Despite / In spite of")
                    .explanation("'the severe weather conditions' là một cụm danh từ (Noun Phrase). Phía trước phải dùng giới từ 'Despite' hoặc 'In spite of', không thể dùng liên từ 'Although' (chỉ đi với mệnh đề S + V).")
                    .grammarTip("Despite / In spite of + Noun Phrase; Although / Even though + S + V.")
                    .build());
        } else if (isMixed) {
            // Mixed Quiz: combine multiple choice, fill in blank, and error correction
            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(1)
                    .questionType("MULTIPLE_CHOICE")
                    .prompt("Chọn phương án chính xác nhất để hoàn thành câu:")
                    .question("The marketing department ________ three comprehensive quarterly reports since Mr. Henderson took over as director.")
                    .translation("Phòng tiếp thị đã nộp 3 bản báo cáo quý toàn diện kể từ khi ông Henderson tiếp quản vị trí giám đốc.")
                    .options(List.of("submits", "submitted", "has submitted", "is submitting"))
                    .correctAnswer("has submitted")
                    .correctIndex(2)
                    .explanation("Câu có mệnh đề 'since + S + V(quá khứ đơn)', nên mệnh đề chính bắt buộc dùng thì Hiện tại hoàn thành (has + V3/ed).")
                    .grammarTip("Since + mốc/mệnh đề quá khứ -> Hiện tại hoàn thành.")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(2)
                    .questionType("FILL_IN_BLANK")
                    .prompt("Điền dạng đúng của động từ trong ngoặc:")
                    .baseWord("work")
                    .question("Ms. Lawson ________ (work) at the financial firm for more than ten years.")
                    .translation("Bà Lawson đã làm việc tại công ty tài chính này được hơn mười năm.")
                    .options(List.of("has worked", "have worked", "worked", "is working"))
                    .correctAnswer("has worked")
                    .explanation("Dấu hiệu 'for more than ten years' (khoảng thời gian) chỉ hành động bắt đầu trong quá khứ và kéo dài tới hiện tại -> chia 'has worked'.")
                    .grammarTip("'For + khoảng thời gian' -> Hiện tại hoàn thành.")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(3)
                    .questionType("ERROR_CORRECTION")
                    .prompt("Tìm phần gạch chân bị sai ngữ pháp trong câu:")
                    .question("The committee [A] has decided [B] to postpone the conference because [C] they has not received [D] all registrations.")
                    .translation("Ủy ban đã quyết định hoãn hội nghị vì họ chưa nhận được tất cả các đăng ký.")
                    .options(List.of("A. has decided", "B. to postpone", "C. they has not received", "D. all registrations"))
                    .correctAnswer("C. they has not received")
                    .correctIndex(2)
                    .correctedWord("they have not received")
                    .explanation("Chủ ngữ 'they' là đại từ số nhiều nên trợ động từ phải là 'have', không thể dùng 'has'. Sửa thành 'they have not received'.")
                    .grammarTip("They / We / You + have, He / She / It + has.")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(4)
                    .questionType("MULTIPLE_CHOICE")
                    .prompt("Chọn từ thích hợp điền vào chỗ trống:")
                    .question("The IT technicians have not resolved the network connection error ________.")
                    .translation("Các kỹ thuật viên IT vẫn chưa khắc phục được sự cố kết nối mạng.")
                    .options(List.of("already", "yet", "still", "just"))
                    .correctAnswer("yet")
                    .correctIndex(1)
                    .explanation("Trong câu phủ định của thì Hiện tại hoàn thành, trạng từ 'yet' thường đứng ở cuối câu mang nghĩa 'vẫn chưa'.")
                    .grammarTip("'Yet' đứng cuối câu phủ định (?) hoặc nghi vấn (-).")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(5)
                    .questionType("FILL_IN_BLANK")
                    .prompt("Chia động từ trong ngoặc theo dạng bị động:")
                    .baseWord("approve")
                    .question("The project proposal has already ________ (approve) by the board of directors.")
                    .translation("Đề xuất dự án đã được phê duyệt bởi hội đồng quản trị.")
                    .options(List.of("been approved", "approved", "approving", "be approved"))
                    .correctAnswer("been approved")
                    .explanation("Thể bị động của Hiện tại hoàn thành: S + have/has + been + V3/ed ('been approved').")
                    .grammarTip("Bị động Hiện tại hoàn thành: have/has + BEEN + V3/ed.")
                    .build());
        } else {
            // Default Multiple Choice Questions
            if (topicLower.contains("present perfect") || topicLower.contains("hiện tại hoàn thành")) {
                pool.add(GrammarExerciseQuestionDto.builder()
                        .id(1)
                        .questionType("MULTIPLE_CHOICE")
                        .prompt("Chọn phương án chính xác nhất để hoàn thành câu:")
                    .question("The marketing department ________ three comprehensive quarterly reports since Mr. Henderson took over as director.")
                    .translation("Phòng tiếp thị đã nộp 3 bản báo cáo quý toàn diện kể từ khi ông Henderson tiếp quản vị trí giám đốc.")
                    .options(List.of("submits", "submitted", "has submitted", "is submitting"))
                    .correctAnswer("has submitted")
                    .correctIndex(2)
                    .explanation("Câu có mệnh đề 'since + S + V(quá khứ đơn)' (since Mr. Henderson took over), nên mệnh đề chính bắt buộc dùng thì Hiện tại hoàn thành (has + V3/ed). Chủ ngữ 'The marketing department' là danh từ số ít.")
                    .grammarTip("Gặp 'since + mốc/mệnh đề quá khứ' là chọn ngay thì Hiện tại hoàn thành (have/has + V3).")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(2)
                    .questionType("MULTIPLE_CHOICE")
                    .prompt("Chọn giới từ thích hợp:")
                    .question("Ms. Lawson has worked at the financial firm ________ more than ten years.")
                    .translation("Bà Lawson đã làm việc tại công ty tài chính này được hơn mười năm.")
                    .options(List.of("since", "for", "during", "while"))
                    .correctAnswer("for")
                    .correctIndex(1)
                    .explanation("'more than ten years' là một khoảng thời gian (duration), do đó ta sử dụng giới từ 'for' đi với thì Hiện tại hoàn thành. Giới từ 'since' chỉ đi với mốc thời gian cụ thể.")
                    .grammarTip("'For + khoảng thời gian', 'Since + mốc thời gian'.")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(3)
                    .questionType("MULTIPLE_CHOICE")
                    .prompt("Chọn trạng từ thích hợp:")
                    .question("The IT technicians have not resolved the network connection error ________.")
                    .translation("Các kỹ thuật viên IT vẫn chưa khắc phục được sự cố kết nối mạng.")
                    .options(List.of("already", "yet", "still", "just"))
                    .correctAnswer("yet")
                    .correctIndex(1)
                    .explanation("Trong câu phủ định của thì Hiện tại hoàn thành, trạng từ 'yet' thường đứng ở cuối câu mang nghĩa 'vẫn chưa'.")
                    .grammarTip("'Yet' đứng cuối câu phủ định (?) hoặc nghi vấn (-). 'Already' dùng trong câu khẳng định (+).")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(4)
                    .questionType("MULTIPLE_CHOICE")
                    .prompt("Chọn dạng đúng của động từ:")
                    .question("We ________ several inquiries from potential overseas investors this morning.")
                    .translation("Chúng tôi đã nhận được một vài thắc mắc từ các nhà đầu tư nước ngoài tiềm năng vào sáng nay.")
                    .options(List.of("have received", "are received", "receiving", "has received"))
                    .correctAnswer("have received")
                    .correctIndex(0)
                    .explanation("Chủ ngữ 'We' là ngôi thứ nhất số nhiều, đi với trợ động từ 'have' + V3/ed 'received' để diễn tả hành động vừa mới diễn ra có liên hệ với hiện tại.")
                    .grammarTip("Chủ ngữ số nhiều (We, They, You, N plural) + HAVE + V3; Chủ ngữ số ít (He, She, It, N singular) + HAS + V3.")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(5)
                    .questionType("MULTIPLE_CHOICE")
                    .prompt("Chọn trợ động từ phù hợp:")
                    .question("The regional sales director ________ already finalized the agreement with our new supplier.")
                    .translation("Giám đốc kinh doanh khu vực đã hoàn tất thỏa thuận với nhà cung cấp mới của chúng ta.")
                    .options(List.of("has", "have", "is", "was"))
                    .correctAnswer("has")
                    .correctIndex(0)
                    .explanation("Chủ ngữ 'The regional sales director' là ngôi thứ 3 số ít, kết hợp với trạng từ 'already' và quá khứ phân từ 'finalized', ta cần trợ động từ 'has'.")
                    .grammarTip("Cấu trúc khẳng định: S + have/has + already + V3/ed.")
                    .build());
        } else if (topicLower.contains("passive") || topicLower.contains("bị động")) {
            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(1)
                    .question("The annual shareholder meeting will ________ in the Grand Ballroom tomorrow.")
                    .translation("Cuộc họp đại hội cổ đông thường niên sẽ được tổ chức tại Phòng Đại Tiệc vào ngày mai.")
                    .options(List.of("hold", "be held", "held", "holding"))
                    .correctAnswer("be held")
                    .correctIndex(1)
                    .explanation("Chủ ngữ 'The annual shareholder meeting' là vật thể không tự thực hiện hành động 'tổ chức' mà 'được tổ chức'. Sau động từ khuyết thiếu 'will', thể bị động có dạng 'will + be + V3/ed' (be held).")
                    .grammarTip("Bị động của Modal Verbs: S + Modal Verb (will/can/must/should) + BE + V3/ed.")
                    .questionType("MULTIPLE_CHOICE")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(2)
                    .question("All confidential client documents must ________ in the secure filing cabinet.")
                    .translation("Tất cả tài liệu mật của khách hàng phải được lưu trữ trong tủ hồ sơ an toàn.")
                    .options(List.of("store", "be stored", "stored", "storing"))
                    .correctAnswer("be stored")
                    .correctIndex(1)
                    .explanation("Chủ ngữ 'All confidential client documents' chịu tác động của hành động lưu trữ. Cấu trúc bị động với 'must': must + be + V3/ed ('be stored').")
                    .grammarTip("Xác định chủ ngữ là người thực hiện hay vật bị tác động để quyết định chọn thể Bị động.")
                    .questionType("MULTIPLE_CHOICE")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(3)
                    .question("The new software update was successfully ________ by our development team yesterday.")
                    .translation("Bản cập nhật phần mềm mới đã được cài đặt thành công bởi đội ngũ phát triển vào ngày hôm qua.")
                    .options(List.of("install", "installing", "installed", "installs"))
                    .correctAnswer("installed")
                    .correctIndex(2)
                    .explanation("Cấu trúc bị động ở Quá khứ đơn: S + was/were + (Adv) + V3/ed. Ở đây ta cần quá khứ phân từ 'installed'.")
                    .grammarTip("Giữa 'was/were' và 'V3/ed' thường được chèn thêm một trạng từ (Adv) chỉ cách thức (e.g. successfully, properly).")
                    .questionType("MULTIPLE_CHOICE")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(4)
                    .question("Defective items can ________ to the manufacturer within 30 days of purchase.")
                    .translation("Các sản phẩm lỗi có thể được trả lại cho nhà sản xuất trong vòng 30 ngày kể từ khi mua.")
                    .options(List.of("return", "be returned", "returning", "returned"))
                    .correctAnswer("be returned")
                    .correctIndex(1)
                    .explanation("Chủ ngữ 'Defective items' (các mặt hàng lỗi) được trả lại, dùng cấu trúc bị động 'can be + V3/ed' ('can be returned').")
                    .grammarTip("Khi chủ ngữ là đồ vật đứng đầu câu, 90% câu Part 5 TOEIC sẽ chia ở dạng Bị Động.")
                    .questionType("MULTIPLE_CHOICE")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(5)
                    .question("The proposal has already ________ by the executive committee.")
                    .translation("Bản đề xuất đã được phê duyệt bởi ban điều hành.")
                    .options(List.of("been approved", "approved", "approving", "approve"))
                    .correctAnswer("been approved")
                    .correctIndex(0)
                    .explanation("Bị động của thì Hiện tại hoàn thành: S + have/has + been + V3/ed. Bản đề xuất được phê duyệt nên dùng 'has been approved'.")
                    .grammarTip("Bị động Hiện tại hoàn thành: have/has + BEEN + V3/ed.")
                    .questionType("MULTIPLE_CHOICE")
                    .build());
        } else if (topicLower.contains("conditional") || topicLower.contains("điều kiện") || topicLower.contains("if")) {
            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(1)
                    .question("If the delivery is delayed, the customer service team ________ the client immediately.")
                    .translation("Nếu việc giao hàng bị chậm trễ, đội ngũ chăm sóc khách hàng sẽ thông báo cho khách hàng ngay lập tức.")
                    .options(List.of("notifies", "will notify", "notified", "would notify"))
                    .correctAnswer("will notify")
                    .correctIndex(1)
                    .explanation("Câu điều kiện loại 1 (sự việc có thể xảy ra ở hiện tại/tương lai): Mệnh đề If dùng thì Hiện tại đơn ('is delayed'), mệnh đề chính dùng 'will + V nguyên mẫu' ('will notify').")
                    .grammarTip("Công thức Điều kiện loại 1: If + S + V(hiện tại đơn), S + will/can + V(nguyên mẫu).")
                    .questionType("MULTIPLE_CHOICE")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(2)
                    .question("If we ________ a larger advertising budget, we would launch a nationwide campaign.")
                    .translation("Nếu chúng ta có ngân sách quảng cáo lớn hơn, chúng ta sẽ khởi động một chiến dịch toàn quốc.")
                    .options(List.of("have", "had", "have had", "will have"))
                    .correctAnswer("had")
                    .correctIndex(1)
                    .explanation("Mệnh đề chính dùng 'would launch' (would + V-inf), đây là câu điều kiện loại 2 (giả định không có thật ở hiện tại). Mệnh đề If chia ở Quá khứ đơn ('had').")
                    .grammarTip("Điều kiện loại 2: If + S + V2/ed (were), S + would/could + V-inf.")
                    .questionType("MULTIPLE_CHOICE")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(3)
                    .question("Unless we receive your confirmation by 5:00 PM, your reservation ________ canceled.")
                    .translation("Trừ khi chúng tôi nhận được xác nhận của bạn trước 5 giờ chiều, đặt chỗ của bạn sẽ bị hủy.")
                    .options(List.of("will be", "is", "would be", "was"))
                    .correctAnswer("will be")
                    .correctIndex(0)
                    .explanation("'Unless' tương đương 'If ... not'. Mệnh đề Unless dùng Hiện tại đơn ('receive'), mệnh đề chính dùng tương lai đơn bị động 'will be + V3/ed' ('will be canceled').")
                    .grammarTip("Unless = If not. Sau Unless không dùng phủ định (not).")
                    .questionType("MULTIPLE_CHOICE")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(4)
                    .question("Had Mr. Jenkins ________ the contract terms, he would not have signed the agreement.")
                    .translation("Nếu ông Jenkins đọc kỹ các điều khoản hợp đồng, ông ấy đã không ký thỏa thuận.")
                    .options(List.of("read", "reading", "reads", "been read"))
                    .correctAnswer("read")
                    .correctIndex(0)
                    .explanation("Cấu trúc đảo ngữ câu điều kiện loại 3: 'Had + S + V3/ed, S + would have + V3/ed'. Động từ 'read' ở dạng quá khứ phân từ vẫn viết là 'read'.")
                    .grammarTip("Đảo ngữ điều kiện loại 3: Had + S + V3/ed, S + would have + V3/ed.")
                    .questionType("MULTIPLE_CHOICE")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(5)
                    .question("Should you ________ any technical assistance, please do not hesitate to contact our helpdesk.")
                    .translation("Nếu bạn cần bất kỳ sự hỗ trợ kỹ thuật nào, xin vui lòng liên hệ với bộ phận trợ giúp của chúng tôi.")
                    .options(List.of("require", "requires", "required", "requiring"))
                    .correctAnswer("require")
                    .correctIndex(0)
                    .explanation("Cấu trúc đảo ngữ câu điều kiện loại 1: 'Should + S + V(nguyên mẫu), ...'. Do đó ta chọn động từ nguyên mẫu không 'to' ('require').")
                    .grammarTip("Đảo ngữ điều kiện loại 1: Should + S + V(bare-inf) = If + S + V(present).")
                    .questionType("MULTIPLE_CHOICE")
                    .build());
        } else {
            // General TOEIC Grammar Questions tailored to the current topic
            String topicName = topic.getTopic();
            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(1)
                    .question(String.format("The auditor carefully examined the financial statements to ensure they ________ compliant with international accounting standards.", ""))
                    .translation("Kiểm toán viên đã kiểm tra cẩn thận các báo cáo tài chính để đảm bảo chúng tuân thủ các chuẩn mực kế toán quốc tế.")
                    .options(List.of("is", "was", "were", "are"))
                    .correctAnswer("were")
                    .correctIndex(2)
                    .explanation("Mệnh đề chính ở thì Quá khứ đơn ('examined'), mệnh đề phụ phía sau cần hòa hợp thì ở quá khứ. Chủ ngữ 'they' (thay thế cho financial statements) là số nhiều nên dùng 'were'.")
                    .grammarTip("Quy tắc hòa hợp thì: Mệnh đề chính ở quá khứ thì mệnh đề phụ lùi về thì quá khứ tương ứng.")
                    .questionType("MULTIPLE_CHOICE")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(2)
                    .question("Employees wishing to participate in the leadership seminar must submit their applications ________ Friday afternoon.")
                    .translation("Những nhân viên muốn tham gia hội thảo lãnh đạo phải nộp đơn đăng ký trước chiều thứ Sáu.")
                    .options(List.of("by", "until", "during", "while"))
                    .correctAnswer("by")
                    .correctIndex(0)
                    .explanation("Giới từ 'by' diễn tả hành động phải hoàn thành muộn nhất trước một thời điểm cụ thể ('trước chiều thứ Sáu'). Trong khi 'until' dùng cho hành động kéo dài liên tục đến một thời điểm.")
                    .grammarTip("'By + mốc thời gian': Muộn nhất trước lúc đó (Deadlines). 'Until + mốc thời gian': Kéo dài tới lúc đó.")
                    .questionType("MULTIPLE_CHOICE")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(3)
                    .question("The executive committee requested that all division managers ________ a detailed progress report.")
                    .translation("Ban điều hành yêu cầu tất cả các giám đốc bộ phận nộp báo cáo tiến độ chi tiết.")
                    .options(List.of("submit", "submits", "submitted", "submitting"))
                    .correctAnswer("submit")
                    .correctIndex(0)
                    .explanation("Cấu trúc Thức giả định (Subjunctive mood): 'S + request/require/suggest/recommend + that + S + (should) + V(nguyên mẫu)'. Động từ luôn ở dạng nguyên mẫu không chia ('submit').")
                    .grammarTip("Subjunctive: S + V(khuyên/bảo/yêu cầu) + that + S + V(nguyên mẫu không 'to').")
                    .questionType("MULTIPLE_CHOICE")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(4)
                    .question("Despite ________ severe traffic congestion, the keynote speaker arrived at the conference hall on time.")
                    .translation("Mặc dù gặp phải tình trạng ùn tắc giao thông nghiêm trọng, diễn giả chính vẫn đến hội trường đúng giờ.")
                    .options(List.of("experiencing", "experienced", "experience", "experiences"))
                    .correctAnswer("experiencing")
                    .correctIndex(0)
                    .explanation("Sau giới từ 'Despite' (hoặc 'In spite of'), ta cần một Danh từ (Noun) hoặc Danh động từ (V-ing). Do đó ta chọn 'experiencing'.")
                    .grammarTip("'Despite / In spite of + V-ing/Noun phrase'; 'Although / Even though + S + V'.")
                    .questionType("MULTIPLE_CHOICE")
                    .build());

            pool.add(GrammarExerciseQuestionDto.builder()
                    .id(5)
                    .question(String.format("Please review the core principles of %s to ensure flawless application in professional writing and examination.", topicName))
                    .translation(String.format("Vui lòng xem lại các nguyên tắc cốt lõi của %s để đảm bảo áp dụng chuẩn xác trong văn bản chuyên nghiệp và bài thi.", topicName))
                    .options(List.of("accurate", "accurately", "accuracy", "accurateness"))
                    .correctAnswer("accurately")
                    .correctIndex(1)
                    .explanation("Đứng trước động từ hoặc bổ nghĩa cho động từ/hành động 'apply' ta cần một trạng từ (Adverb) có đuôi -ly ('accurately').")
                    .grammarTip("Cấu trúc từ loại: V + Adv hoặc Adv + V để chỉ cách thức thực hiện hành động.")
                    .questionType("MULTIPLE_CHOICE")
                    .build());
            }
        }

        // Adjust count
        List<GrammarExerciseQuestionDto> result = new ArrayList<>();
        for (int i = 0; i < Math.min(count, pool.size()); i++) {
            GrammarExerciseQuestionDto q = pool.get(i);
            q.setId(i + 1);
            result.add(q);
        }
        return result;
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
