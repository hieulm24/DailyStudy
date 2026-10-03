package com.englishlearning.config;

import com.englishlearning.entity.*;
import com.englishlearning.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;
    private final StudyStreakRepository studyStreakRepository;
    private final GameRepository gameRepository;
    private final VocabularyRepository vocabularyRepository;
    private final GrammarTopicRepository grammarTopicRepository;
    private final ListeningLessonRepository listeningLessonRepository;
    private final SpeakingLessonRepository speakingLessonRepository;
    private final ReviewItemRepository reviewItemRepository;
    private final LearningActivityRepository learningActivityRepository;
    private final DailyLearningStatisticRepository dailyLearningStatisticRepository;
    private final DailyTaskRepository dailyTaskRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default-user.email:hieulm24@gmail.com}")
    private String defaultEmail;

    @Value("${app.default-user.password:L@nhminhhieudeptrai.1}")
    private String defaultPassword;

    @Value("${app.default-user.display-name:Lãnh Minh Hiếu}")
    private String defaultDisplayName;

    @Value("${app.seed-sample-data:false}")
    private boolean seedSampleData;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking initial application data...");

        // 1. Seed Games
        seedGames();

        // 2. Seed Default User
        User user = seedDefaultUser();

        // 3. Seed Sample Data only if explicitly enabled in configuration
        if (seedSampleData && vocabularyRepository.countByUserId(user.getId()) == 0) {
            seedSampleData(user);
        }

        // 4. Seed Sample Daily Tasks if empty
        if (dailyTaskRepository.countByUserId(user.getId()) == 0) {
            seedDailyTasks(user);
        }

        log.info("Application data initialization completed successfully.");
    }

    private void seedGames() {
        if (!gameRepository.existsByCode("FLASHCARD")) {
            gameRepository.save(Game.builder()
                    .code("FLASHCARD")
                    .name("Flashcard Ôn Tập")
                    .description("Lật thẻ ôn từ vựng với Spaced Repetition (Quên, Khó, Nhớ, Rất dễ)")
                    .gameType("VOCABULARY")
                    .isActive(true)
                    .build());
        }

        if (!gameRepository.existsByCode("MULTIPLE_CHOICE")) {
            gameRepository.save(Game.builder()
                    .code("MULTIPLE_CHOICE")
                    .name("Trắc Nghiệm 4 Lựa Chọn")
                    .description("Chọn nghĩa tiếng Việt chính xác nhất cho từ vựng")
                    .gameType("VOCABULARY")
                    .isActive(true)
                    .build());
        }

        if (!gameRepository.existsByCode("WORD_MEANING")) {
            gameRepository.save(Game.builder()
                    .code("WORD_MEANING")
                    .name("Gõ Từ Theo Nghĩa")
                    .description("Nhìn nghĩa tiếng Việt và gõ lại chính xác từ vựng tiếng Anh")
                    .gameType("VOCABULARY")
                    .isActive(true)
                    .build());
        }

        if (!gameRepository.existsByCode("SENTENCE_COMPLETION")) {
            gameRepository.save(Game.builder()
                    .code("SENTENCE_COMPLETION")
                    .name("Điền Từ Vào Câu")
                    .description("Lựa chọn từ vựng phù hợp để điền vào chỗ trống trong câu ví dụ")
                    .gameType("VOCABULARY")
                    .isActive(true)
                    .build());
        }

        if (!gameRepository.existsByCode("GRAMMAR_QUIZ")) {
            gameRepository.save(Game.builder()
                    .code("GRAMMAR_QUIZ")
                    .name("Trắc Nghiệm Ngữ Pháp")
                    .description("Kiểm tra khả năng vận dụng các cấu trúc ngữ pháp đã học")
                    .gameType("GRAMMAR")
                    .isActive(true)
                    .build());
        }
    }

    private User seedDefaultUser() {
        return userRepository.findByEmail(defaultEmail).orElseGet(() -> {
            log.info("Creating default user: {}", defaultEmail);
            User newUser = User.builder()
                    .email(defaultEmail)
                    .passwordHash(passwordEncoder.encode(defaultPassword))
                    .displayName(defaultDisplayName)
                    .isActive(true)
                    .build();
            newUser = userRepository.save(newUser);

            userSettingRepository.save(UserSetting.builder()
                    .user(newUser)
                    .theme("LIGHT")
                    .language("vi")
                    .timezone("Asia/Ho_Chi_Minh")
                    .dailyLearningTarget(30)
                    .reviewEnabled(true)
                    .build());

            studyStreakRepository.save(StudyStreak.builder()
                    .user(newUser)
                    .currentStreak(1)
                    .longestStreak(1)
                    .lastStudyDate(LocalDate.now())
                    .build());

            return newUser;
        });
    }

    private void seedSampleData(User user) {
        log.info("Seeding initial English learning sample data for user: {}", user.getEmail());

        // Vocabularies
        createVocabulary(user, "abandon", "từ bỏ, ruồng bỏ", "/əˈbæn.dən/", "verb", "B2",
                "He decided to abandon the plan after seeing the high costs.", "Anh ấy quyết định từ bỏ kế hoạch sau khi thấy chi phí cao.");

        createVocabulary(user, "accommodate", "cung cấp chỗ ở, đáp ứng nhu cầu", "/əˈkɑː.mə.deɪt/", "verb", "B2",
                "The hotel can accommodate up to 500 guests.", "Khách sạn có thể chứa đến 500 khách.");

        createVocabulary(user, "beneficial", "có lợi, mang lại lợi ích", "/ˌben.əˈfɪʃ.əl/", "adjective", "B1",
                "Regular exercise is beneficial to health.", "Tập thể dục đều đặn rất có lợi cho sức khỏe.");

        createVocabulary(user, "collaborate", "hợp tác, cộng tác", "/kəˈlæb.ə.reɪt/", "verb", "B2",
                "Two teams will collaborate on this international project.", "Hai đội sẽ hợp tác trong dự án quốc tế này.");

        createVocabulary(user, "determine", "xác định, quyết tâm", "/dɪˈtɜːr.mɪn/", "verb", "B1",
                "Your attitude will determine your success.", "Thái độ của bạn sẽ quyết định thành công của bạn.");

        createVocabulary(user, "efficient", "hiệu quả, có năng suất", "/ɪˈfɪʃ.ənt/", "adjective", "B1",
                "We need to find a more efficient way to process data.", "Chúng ta cần tìm cách xử lý dữ liệu hiệu quả hơn.");

        createVocabulary(user, "fluctuate", "dao động, biến động", "/ˈflʌk.tʃu.eɪt/", "verb", "C1",
                "Prices fluctuate depending on supply and demand.", "Giá cả biến động tùy thuộc vào cung và cầu.");

        createVocabulary(user, "guarantee", "bảo đảm, cam kết", "/ˌɡær.ənˈtiː/", "verb", "B1",
                "We guarantee high quality service for every customer.", "Chúng tôi cam kết chất lượng dịch vụ cao cho mọi khách hàng.");

        createVocabulary(user, "hesitate", "ngập ngừng, do dự", "/ˈhez.ə.teɪt/", "verb", "B1",
                "Do not hesitate to contact us if you need help.", "Đừng ngần ngại liên hệ với chúng tôi nếu bạn cần giúp đỡ.");

        createVocabulary(user, "implement", "triển khai, thực thi", "/ˈɪm.plə.ment/", "verb", "B2",
                "The company plans to implement the new policy next month.", "Công ty dự định triển khai chính sách mới vào tháng sau.");

        // Grammars
        createGrammar(user, "Present Simple", "A1", "S + V(s/es)", "She reads books every night.", "She doesn't read books.", "Does she read books?",
                "Diễn tả thói quen, chân lý hiển nhiên, sự thật hoặc lịch trình cố định.", "always, usually, often, sometimes, every day",
                "Quên thêm s/es cho ngôi thứ 3 số ít.");

        createGrammar(user, "Present Perfect", "B1", "S + have/has + V3/ed", "I have finished my homework.", "I haven't finished my homework.", "Have you finished your homework?",
                "Diễn tả hành động xảy ra trong quá khứ kéo dài đến hiện tại hoặc vừa mới xảy ra để lại kết quả.", "already, yet, just, since, for, ever, never",
                "Dùng nhầm với thì Quá khứ đơn (Past Simple) khi có thời gian xác định.");

        createGrammar(user, "First Conditional (Điều kiện loại 1)", "B1", "If + S + V(hiện tại đơn), S + will + V(nguyên mẫu)",
                "If it rains tomorrow, we will stay at home.", "If you don't hurry, you will miss the train.", "What will you do if you pass the exam?",
                "Diễn tả điều kiện có thể xảy ra ở hiện tại hoặc tương lai.", "if, unless, as long as", "Dùng will ở cả hai mệnh đề.");

        createGrammar(user, "Passive Voice (Câu bị động)", "B1", "S + be + V3/ed (+ by O)",
                "The report was sent by the manager.", "The house was not built in 1990.", "Was the email delivered yesterday?",
                "Nhấn mạnh vào đối tượng chịu tác động của hành động thay vì người thực hiện.", "by, with", "Chia sai thì của động từ to be.");

        createGrammar(user, "Used to vs Be used to", "B2", "Used to + V / Be used to + V-ing",
                "I used to wake up late when I was young.", "I am used to waking up early now.", "Are you used to the cold weather?",
                "Used to chỉ thói quen trong quá khứ đã chấm dứt; Be used to chỉ sự quen thuộc với việc gì ở hiện tại.", "in the past, now",
                "Nhầm lẫn giữa V-nguyên thể và V-ing sau used to.");

        // Listenings
        createListening(user, "BBC 6 Minute English - The Power of Reading", "B1", 360, "https://www.bbc.co.uk/learningenglish/english/features/6-minute-english",
                "Luyện nghe về lợi ích của việc đọc sách mỗi ngày đối với trí nhớ.", "Nghe được khoảng 80%, cần chú ý từ vựng chuyên ngành.");

        createListening(user, "TED Talk - How to speak so that people want to listen", "B2", 600, "https://www.ted.com/talks/julian_treasure_how_to_speak_so_that_people_want_to_listen",
                "Bài nói nổi tiếng của Julian Treasure về 7 lỗi khi nói và 4 nền tảng giao tiếp.", "Giọng Anh - Anh rất chuẩn, nội dung thực tế.");

        createListening(user, "VOA Learning English - Technology Report", "A2", 300, "https://learningenglish.voanews.com",
                "Bản tin công nghệ phát âm chậm rãi, rõ ràng.", "Phù hợp để shadow theo từng câu.");

        // Speakings
        createSpeaking(user, "Describe your hometown", "IELTS Part 2", "B1", 180, "",
                "Mô tả về quê hương: vị trí địa lý, con người, món ăn đặc sản và điểm yêu thích.", "Cần luyện thêm ngữ điệu và phát âm âm đuôi /s/, /ed/.");

        createSpeaking(user, "Why English is important for your career", "Daily Topic", "B2", 240, "",
                "Trình bày lý do tiếng Anh mở ra nhiều cơ hội làm việc và kết nối bạn bè quốc tế.", "Đã áp dụng tốt các từ vựng collaborate và beneficial.");

        createSpeaking(user, "My favorite hobby and daily routine", "General English", "A2", 150, "",
                "Nói về thói quen đọc sách và học lập trình vào mỗi buổi sáng.", "Lưu loát, không bị vấp nhiều.");

        // Daily Stat for today
        DailyLearningStatistic stat = dailyLearningStatisticRepository.findByUserIdAndStatisticDate(user.getId(), LocalDate.now())
                .orElseGet(() -> DailyLearningStatistic.builder()
                        .user(user)
                        .statisticDate(LocalDate.now())
                        .build());
        stat.setVocabularyCount(10);
        stat.setGrammarCount(5);
        stat.setListeningCount(3);
        stat.setSpeakingCount(3);
        stat.setTotalLearningCount(21);
        stat.setTotalLearningSeconds(1800);
        dailyLearningStatisticRepository.save(stat);
    }

    private void createVocabulary(User user, String word, String meaning, String pronunciation, String pos, String level, String sentence, String sentenceMeaning) {
        Vocabulary vocab = Vocabulary.builder()
                .user(user)
                .word(word)
                .meaning(meaning)
                .pronunciation(pronunciation)
                .partOfSpeech(pos)
                .level(level)
                .status("LEARNING")
                .masteryLevel(1)
                .reviewCount(1)
                .lastReviewedAt(LocalDateTime.now().minusHours(2))
                .nextReviewAt(LocalDateTime.now().plusDays(1))
                .build();

        Vocabulary saved = vocabularyRepository.save(vocab);

        VocabularyExample example = VocabularyExample.builder()
                .vocabulary(saved)
                .exampleSentence(sentence)
                .meaning(sentenceMeaning)
                .isPrimary(true)
                .build();
        saved.getExamples().add(example);
        vocabularyRepository.save(saved);

        // Review item
        reviewItemRepository.save(ReviewItem.builder()
                .user(user)
                .contentType("VOCABULARY")
                .contentId(saved.getId())
                .reviewStatus("ACTIVE")
                .masteryLevel(1)
                .reviewCount(1)
                .currentIntervalDays(1)
                .lastReviewedAt(LocalDateTime.now().minusHours(2))
                .nextReviewAt(LocalDateTime.now().minusHours(1)) // due today for review test
                .build());

        learningActivityRepository.save(LearningActivity.builder()
                .user(user)
                .activityType("ADD_VOCABULARY")
                .contentType("VOCABULARY")
                .contentId(saved.getId())
                .title("Đã thêm từ vựng: " + word)
                .description(meaning)
                .activityDate(LocalDateTime.now())
                .build());
    }

    private void createGrammar(User user, String topic, String level, String structure, String positive, String negative, String question, String usage, String signals, String mistakes) {
        GrammarTopic grammar = GrammarTopic.builder()
                .user(user)
                .topic(topic)
                .level(level)
                .structure(structure)
                .positiveStructure(positive)
                .negativeStructure(negative)
                .questionStructure(question)
                .usage(usage)
                .signalWords(signals)
                .commonMistakes(mistakes)
                .status("LEARNING")
                .masteryLevel(1)
                .reviewCount(1)
                .lastReviewedAt(LocalDateTime.now().minusHours(3))
                .nextReviewAt(LocalDateTime.now().plusDays(1))
                .build();

        GrammarTopic saved = grammarTopicRepository.save(grammar);

        GrammarExample example = GrammarExample.builder()
                .grammarTopic(saved)
                .exampleSentence(positive)
                .meaning("Ví dụ câu khẳng định cho cấu trúc: " + topic)
                .isPrimary(true)
                .build();
        saved.getExamples().add(example);
        grammarTopicRepository.save(saved);

        // Review item
        reviewItemRepository.save(ReviewItem.builder()
                .user(user)
                .contentType("GRAMMAR")
                .contentId(saved.getId())
                .reviewStatus("ACTIVE")
                .masteryLevel(1)
                .reviewCount(1)
                .currentIntervalDays(1)
                .lastReviewedAt(LocalDateTime.now().minusHours(3))
                .nextReviewAt(LocalDateTime.now().minusHours(1)) // due today for review test
                .build());

        learningActivityRepository.save(LearningActivity.builder()
                .user(user)
                .activityType("LEARN_GRAMMAR")
                .contentType("GRAMMAR")
                .contentId(saved.getId())
                .title("Đã học ngữ pháp: " + topic)
                .description(structure)
                .activityDate(LocalDateTime.now())
                .build());
    }

    private void createListening(User user, String title, String level, int duration, String url, String desc, String note) {
        ListeningLesson lesson = ListeningLesson.builder()
                .user(user)
                .title(title)
                .level(level)
                .durationSeconds(duration)
                .url(url)
                .description(desc)
                .note(note)
                .status("COMPLETED")
                .listenedCount(1)
                .learnedAt(LocalDateTime.now())
                .build();
        ListeningLesson saved = listeningLessonRepository.save(lesson);

        learningActivityRepository.save(LearningActivity.builder()
                .user(user)
                .activityType("LISTEN")
                .contentType("LISTENING")
                .contentId(saved.getId())
                .title("Nghe bài: " + title)
                .description(desc)
                .durationSeconds(duration)
                .activityDate(LocalDateTime.now())
                .build());
    }

    private void createSpeaking(User user, String title, String topic, String level, int duration, String url, String desc, String note) {
        SpeakingLesson lesson = SpeakingLesson.builder()
                .user(user)
                .title(title)
                .topic(topic)
                .level(level)
                .durationSeconds(duration)
                .url(url)
                .description(desc)
                .note(note)
                .status("COMPLETED")
                .practiceCount(1)
                .practicedAt(LocalDateTime.now())
                .build();
        SpeakingLesson saved = speakingLessonRepository.save(lesson);

        learningActivityRepository.save(LearningActivity.builder()
                .user(user)
                .activityType("SPEAK")
                .contentType("SPEAKING")
                .contentId(saved.getId())
                .title("Luyện nói: " + title)
                .description(topic)
                .durationSeconds(duration)
                .activityDate(LocalDateTime.now())
                .build());
    }

    private void seedDailyTasks(User user) {
        log.info("Seeding sample daily tasks for user: {}", user.getEmail());
        LocalDate today = LocalDate.now();

        // 6 Tasks for Today with estimated times
        dailyTaskRepository.save(DailyTask.builder()
                .user(user)
                .taskDate(today)
                .title("Học 20 từ vựng TOEIC chủ đề Business & Contracts")
                .description("Học các từ vựng mới, luyện phát âm và lấy ví dụ câu thực tế")
                .category("ENGLISH")
                .priority("HIGH")
                .status("COMPLETED")
                .isCompleted(true)
                .displayOrder(1)
                .estimatedTime("45 phút")
                .completedAt(LocalDateTime.now())
                .build());

        dailyTaskRepository.save(DailyTask.builder()
                .user(user)
                .taskDate(today)
                .title("Luyện nghe BBC 6 Minute English + Chép chính tả")
                .description("Nghe bài The Power of Reading và ghi chú lại từ mới")
                .category("ENGLISH")
                .priority("MEDIUM")
                .status("IN_PROGRESS")
                .isCompleted(false)
                .displayOrder(2)
                .estimatedTime("30 phút")
                .build());

        dailyTaskRepository.save(DailyTask.builder()
                .user(user)
                .taskDate(today)
                .title("Ôn tập Flashcards theo thuật toán Spaced Repetition (SRS)")
                .description("Hoàn thành tất cả các từ vựng và ngữ pháp đến hạn ôn hôm nay")
                .category("ENGLISH")
                .priority("URGENT")
                .status("TODO")
                .isCompleted(false)
                .displayOrder(3)
                .estimatedTime("20 phút")
                .build());

        dailyTaskRepository.save(DailyTask.builder()
                .user(user)
                .taskDate(today)
                .title("Xử lý báo cáo và họp tiến độ dự án Gateway BIDV")
                .description("Review code gateway, kiểm thử tích hợp và gửi báo cáo tuần")
                .category("WORK")
                .priority("HIGH")
                .status("TODO")
                .isCompleted(false)
                .displayOrder(4)
                .estimatedTime("2 giờ")
                .build());

        dailyTaskRepository.save(DailyTask.builder()
                .user(user)
                .taskDate(today)
                .title("Chạy bộ 3km & Rèn luyện thể lực buổi chiều")
                .description("Duy trì sức khỏe và giải tỏa căng thẳng sau giờ làm việc")
                .category("HEALTH")
                .priority("MEDIUM")
                .status("TODO")
                .isCompleted(false)
                .displayOrder(5)
                .estimatedTime("45 phút")
                .build());

        dailyTaskRepository.save(DailyTask.builder()
                .user(user)
                .taskDate(today)
                .title("Đọc 10 trang sách hoặc bài báo tiếng Anh (The Economist)")
                .description("Nâng cao vốn từ vựng học thuật và kỹ năng đọc hiểu")
                .category("STUDY")
                .priority("LOW")
                .status("TODO")
                .isCompleted(false)
                .displayOrder(6)
                .estimatedTime("30 phút")
                .build());

        // Previous day sample tasks
        LocalDate yesterday = today.minusDays(1);
        dailyTaskRepository.save(DailyTask.builder()
                .user(user)
                .taskDate(yesterday)
                .title("Luyện đề TOEIC Listening Part 1 & Part 2")
                .category("ENGLISH")
                .priority("HIGH")
                .status("COMPLETED")
                .isCompleted(true)
                .displayOrder(1)
                .estimatedTime("1 giờ")
                .completedAt(LocalDateTime.now().minusDays(1))
                .build());

        dailyTaskRepository.save(DailyTask.builder()
                .user(user)
                .taskDate(yesterday)
                .title("Luyện phát âm theo phương pháp Shadowing")
                .category("ENGLISH")
                .priority("MEDIUM")
                .status("COMPLETED")
                .isCompleted(true)
                .displayOrder(2)
                .estimatedTime("30 phút")
                .completedAt(LocalDateTime.now().minusDays(1))
                .build());

        dailyTaskRepository.save(DailyTask.builder()
                .user(user)
                .taskDate(yesterday)
                .title("Cập nhật tài liệu kỹ thuật dự án")
                .category("WORK")
                .priority("MEDIUM")
                .status("COMPLETED")
                .isCompleted(true)
                .displayOrder(3)
                .estimatedTime("1.5 giờ")
                .completedAt(LocalDateTime.now().minusDays(1))
                .build());
    }
}
