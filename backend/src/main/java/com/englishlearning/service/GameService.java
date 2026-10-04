package com.englishlearning.service;

import com.englishlearning.common.ResourceNotFoundException;
import com.englishlearning.dto.game.*;
import com.englishlearning.entity.*;
import com.englishlearning.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final GameQuestionRepository gameQuestionRepository;
    private final GameQuestionOptionRepository gameQuestionOptionRepository;
    private final GameSessionRepository gameSessionRepository;
    private final GameAnswerRepository gameAnswerRepository;
    private final VocabularyRepository vocabularyRepository;
    private final GrammarTopicRepository grammarTopicRepository;
    private final UserRepository userRepository;
    private final LearningActivityRepository learningActivityRepository;
    private final DailyLearningStatisticRepository dailyLearningStatisticRepository;
    private final StudyStreakRepository studyStreakRepository;

    @Transactional(readOnly = true)
    public List<GameDto> getAvailableGames() {
        return gameRepository.findAll().stream()
                .filter(g -> g.getIsActive() != null && g.getIsActive())
                .map(g -> GameDto.builder()
                        .id(g.getId())
                        .code(g.getCode())
                        .name(g.getName())
                        .description(g.getDescription())
                        .gameType(g.getGameType())
                        .isActive(g.getIsActive())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public GameSessionStartResponse startGameSession(Long userId, String gameCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Game game = gameRepository.findByCode(gameCode.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy game với mã: " + gameCode));

        List<Vocabulary> vocabList = vocabularyRepository.findByUserId(userId);
        List<GrammarTopic> grammarList = grammarTopicRepository.findByUserId(userId);

        List<GameQuestionDto> questionDtos = new ArrayList<>();

        switch (game.getCode().toUpperCase()) {
            case "FLASHCARD":
                questionDtos = generateFlashcardQuestions(game, vocabList);
                break;
            case "MULTIPLE_CHOICE":
                questionDtos = generateMultipleChoiceQuestions(game, vocabList);
                break;
            case "WORD_MEANING":
                questionDtos = generateWordMeaningQuestions(game, vocabList);
                break;
            case "SENTENCE_COMPLETION":
                questionDtos = generateSentenceCompletionQuestions(game, vocabList);
                break;
            case "GRAMMAR_QUIZ":
                questionDtos = generateGrammarQuizQuestions(game, grammarList);
                break;
            case "AIRPLANE_SHOOTER":
                questionDtos = generateMultipleChoiceQuestions(game, vocabList);
                break;
            default:
                questionDtos = generateMultipleChoiceQuestions(game, vocabList);
                break;
        }

        if (questionDtos.isEmpty()) {
            throw new ResourceNotFoundException("Chưa có đủ dữ liệu học tập để bắt đầu game này. Vui lòng thêm từ vựng hoặc ngữ pháp trước!");
        }

        GameSession session = GameSession.builder()
                .user(user)
                .game(game)
                .totalQuestions(questionDtos.size())
                .correctAnswers(0)
                .wrongAnswers(0)
                .score(BigDecimal.ZERO)
                .startedAt(LocalDateTime.now())
                .build();
        session = gameSessionRepository.save(session);

        return GameSessionStartResponse.builder()
                .sessionId(session.getId())
                .gameId(game.getId())
                .gameCode(game.getCode())
                .gameName(game.getName())
                .totalQuestions(questionDtos.size())
                .startedAt(session.getStartedAt())
                .questions(questionDtos)
                .build();
    }

    @Transactional
    public GameResultResponse submitGameAnswers(Long userId, GameAnswerSubmitRequest request) {
        GameSession session = gameSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên chơi với id: " + request.getSessionId()));

        if (!session.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Không có quyền nộp kết quả phiên chơi này");
        }

        int correctCount = 0;
        int wrongCount = 0;

        if (request.getAnswers() != null) {
            for (GameAnswerSubmitRequest.SingleAnswerDto aDto : request.getAnswers()) {
                boolean isCorrect = aDto.getIsCorrect() != null && aDto.getIsCorrect();
                if (isCorrect) {
                    correctCount++;
                } else {
                    wrongCount++;
                }

                GameQuestion question = null;
                if (aDto.getQuestionId() != null) {
                    question = gameQuestionRepository.findById(aDto.getQuestionId()).orElse(null);
                }

                GameAnswer answer = GameAnswer.builder()
                        .gameSession(session)
                        .question(question)
                        .selectedOptionId(aDto.getSelectedOptionId())
                        .answerText(aDto.getAnswerText())
                        .isCorrect(isCorrect)
                        .answeredAt(LocalDateTime.now())
                        .build();
                gameAnswerRepository.save(answer);
            }
        }

        int total = correctCount + wrongCount;
        if (total == 0) total = session.getTotalQuestions() != null ? session.getTotalQuestions() : 1;

        BigDecimal score = BigDecimal.valueOf(correctCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);

        LocalDateTime now = LocalDateTime.now();
        session.setCorrectAnswers(correctCount);
        session.setWrongAnswers(wrongCount);
        session.setTotalQuestions(total);
        session.setScore(score);
        session.setCompletedAt(now);
        gameSessionRepository.save(session);

        // Learning Activity
        learningActivityRepository.save(LearningActivity.builder()
                .user(session.getUser())
                .activityType("PLAY_GAME")
                .contentType("GAME")
                .contentId(session.getGame().getId())
                .title("Chơi game: " + session.getGame().getName())
                .description("Điểm số: " + score + "% (" + correctCount + "/" + total + " câu đúng)")
                .activityDate(now)
                .build());

        // Update Daily Stat & Streak
        updateDailyLearningStats(session.getUser());
        updateStudyStreak(session.getUser());

        return GameResultResponse.builder()
                .sessionId(session.getId())
                .gameCode(session.getGame().getCode())
                .gameName(session.getGame().getName())
                .totalQuestions(total)
                .correctAnswers(correctCount)
                .wrongAnswers(wrongCount)
                .score(score)
                .startedAt(session.getStartedAt())
                .completedAt(now)
                .build();
    }

    private List<GameQuestionDto> generateFlashcardQuestions(Game game, List<Vocabulary> vocabList) {
        if (vocabList.isEmpty()) return Collections.emptyList();

        List<Vocabulary> pool = new ArrayList<>(vocabList);
        Collections.shuffle(pool);
        int limit = Math.min(10, pool.size());

        List<GameQuestionDto> questions = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            Vocabulary v = pool.get(i);
            String exampleStr = (v.getExamples() != null && !v.getExamples().isEmpty())
                    ? v.getExamples().get(0).getExampleSentence() : "";

            // Save question in database
            GameQuestion gq = GameQuestion.builder()
                    .game(game)
                    .userId(v.getUser().getId())
                    .contentType("VOCABULARY")
                    .contentId(v.getId())
                    .questionText(v.getWord())
                    .explanation(exampleStr)
                    .difficulty(v.getLevel())
                    .isActive(true)
                    .build();
            gq = gameQuestionRepository.save(gq);

            questions.add(GameQuestionDto.builder()
                    .id(gq.getId())
                    .gameId(game.getId())
                    .contentType("VOCABULARY")
                    .contentId(v.getId())
                    .questionText(v.getWord())
                    .prompt(v.getPronunciation() != null ? v.getPronunciation() : v.getPartOfSpeech())
                    .targetAnswer(v.getMeaning())
                    .explanation(exampleStr)
                    .difficulty(v.getLevel())
                    .options(Collections.emptyList())
                    .build());
        }
        return questions;
    }

    private List<GameQuestionDto> generateMultipleChoiceQuestions(Game game, List<Vocabulary> vocabList) {
        if (vocabList.isEmpty()) return Collections.emptyList();

        List<Vocabulary> pool = new ArrayList<>(vocabList);
        Collections.shuffle(pool);
        int limit = Math.min(10, pool.size());

        List<GameQuestionDto> questions = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            Vocabulary v = pool.get(i);

            GameQuestion gq = GameQuestion.builder()
                    .game(game)
                    .userId(v.getUser().getId())
                    .contentType("VOCABULARY")
                    .contentId(v.getId())
                    .questionText("Nghĩa của từ \"" + v.getWord() + "\" là gì?")
                    .explanation(v.getWord() + " (" + (v.getPronunciation() != null ? v.getPronunciation() : "") + "): " + v.getMeaning())
                    .difficulty(v.getLevel())
                    .isActive(true)
                    .build();
            gq = gameQuestionRepository.save(gq);

            // 4 Options: 1 correct + 3 distractors
            List<String> optionsText = new ArrayList<>();
            optionsText.add(v.getMeaning());

            List<Vocabulary> otherVocabs = new ArrayList<>(vocabList);
            otherVocabs.remove(v);
            Collections.shuffle(otherVocabs);
            for (int d = 0; d < Math.min(3, otherVocabs.size()); d++) {
                optionsText.add(otherVocabs.get(d).getMeaning());
            }

            // Fill fake distractors if total vocabs < 4
            String[] fallbackDistractors = {"hoàn thành xuất sắc", "thực hiện thường xuyên", "chuẩn bị kỹ lưỡng", "giải thích cặn kẽ"};
            int fbIdx = 0;
            while (optionsText.size() < 4) {
                optionsText.add(fallbackDistractors[fbIdx % fallbackDistractors.length]);
                fbIdx++;
            }

            Collections.shuffle(optionsText);

            List<GameQuestionOptionDto> optionDtos = new ArrayList<>();
            for (int o = 0; o < optionsText.size(); o++) {
                String text = optionsText.get(o);
                boolean isCorrect = text.equals(v.getMeaning());

                GameQuestionOption option = GameQuestionOption.builder()
                        .question(gq)
                        .optionText(text)
                        .isCorrect(isCorrect)
                        .displayOrder(o + 1)
                        .build();
                option = gameQuestionOptionRepository.save(option);

                optionDtos.add(GameQuestionOptionDto.builder()
                        .id(option.getId())
                        .optionText(text)
                        .isCorrect(isCorrect)
                        .displayOrder(o + 1)
                        .build());
            }

            questions.add(GameQuestionDto.builder()
                    .id(gq.getId())
                    .gameId(game.getId())
                    .contentType("VOCABULARY")
                    .contentId(v.getId())
                    .questionText("Nghĩa của từ \"" + v.getWord() + "\" là gì?")
                    .prompt(v.getPronunciation())
                    .targetAnswer(v.getMeaning())
                    .explanation(v.getMeaning())
                    .difficulty(v.getLevel())
                    .options(optionDtos)
                    .build());
        }
        return questions;
    }

    private List<GameQuestionDto> generateWordMeaningQuestions(Game game, List<Vocabulary> vocabList) {
        if (vocabList.isEmpty()) return Collections.emptyList();

        List<Vocabulary> pool = new ArrayList<>(vocabList);
        Collections.shuffle(pool);
        int limit = Math.min(10, pool.size());

        List<GameQuestionDto> questions = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            Vocabulary v = pool.get(i);

            GameQuestion gq = GameQuestion.builder()
                    .game(game)
                    .userId(v.getUser().getId())
                    .contentType("VOCABULARY")
                    .contentId(v.getId())
                    .questionText("Nhập từ tiếng Anh có nghĩa là: \"" + v.getMeaning() + "\"")
                    .explanation("Đáp án chính xác: " + v.getWord() + " (" + (v.getPronunciation() != null ? v.getPronunciation() : "") + ")")
                    .difficulty(v.getLevel())
                    .isActive(true)
                    .build();
            gq = gameQuestionRepository.save(gq);

            questions.add(GameQuestionDto.builder()
                    .id(gq.getId())
                    .gameId(game.getId())
                    .contentType("VOCABULARY")
                    .contentId(v.getId())
                    .questionText("Nhập từ tiếng Anh có nghĩa là: \"" + v.getMeaning() + "\"")
                    .prompt(v.getPartOfSpeech() != null ? "[" + v.getPartOfSpeech() + "]" : null)
                    .targetAnswer(v.getWord())
                    .explanation(v.getWord())
                    .difficulty(v.getLevel())
                    .options(Collections.emptyList())
                    .build());
        }
        return questions;
    }

    private List<GameQuestionDto> generateSentenceCompletionQuestions(Game game, List<Vocabulary> vocabList) {
        if (vocabList.isEmpty()) return Collections.emptyList();

        List<Vocabulary> pool = new ArrayList<>(vocabList);
        Collections.shuffle(pool);

        List<GameQuestionDto> questions = new ArrayList<>();
        for (Vocabulary v : pool) {
            if (questions.size() >= 10) break;

            String sentence = null;
            if (v.getExamples() != null && !v.getExamples().isEmpty()) {
                sentence = v.getExamples().get(0).getExampleSentence();
            }

            if (!org.springframework.util.StringUtils.hasText(sentence)) {
                sentence = "She tried to " + v.getWord() + " her goals everyday.";
            }

            // Replace word with ______
            String blanked = sentence.replaceAll("(?i)\\b" + java.util.regex.Pattern.quote(v.getWord()) + "\\b", "______");
            if (!blanked.contains("______")) {
                blanked = sentence + " -> (Điền từ: ______ )";
            }

            GameQuestion gq = GameQuestion.builder()
                    .game(game)
                    .userId(v.getUser().getId())
                    .contentType("VOCABULARY")
                    .contentId(v.getId())
                    .questionText(blanked)
                    .explanation("Câu hoàn chỉnh: " + sentence + " (Nghĩa: " + v.getMeaning() + ")")
                    .difficulty(v.getLevel())
                    .isActive(true)
                    .build();
            gq = gameQuestionRepository.save(gq);

            List<String> optionsText = new ArrayList<>();
            optionsText.add(v.getWord());

            List<Vocabulary> otherVocabs = new ArrayList<>(vocabList);
            otherVocabs.remove(v);
            Collections.shuffle(otherVocabs);
            for (int d = 0; d < Math.min(3, otherVocabs.size()); d++) {
                optionsText.add(otherVocabs.get(d).getWord());
            }

            String[] fallbackWords = {"develop", "maintain", "accomplish", "understand"};
            int fbIdx = 0;
            while (optionsText.size() < 4) {
                optionsText.add(fallbackWords[fbIdx % fallbackWords.length]);
                fbIdx++;
            }

            Collections.shuffle(optionsText);

            List<GameQuestionOptionDto> optionDtos = new ArrayList<>();
            for (int o = 0; o < optionsText.size(); o++) {
                String text = optionsText.get(o);
                boolean isCorrect = text.equalsIgnoreCase(v.getWord());

                GameQuestionOption option = GameQuestionOption.builder()
                        .question(gq)
                        .optionText(text)
                        .isCorrect(isCorrect)
                        .displayOrder(o + 1)
                        .build();
                option = gameQuestionOptionRepository.save(option);

                optionDtos.add(GameQuestionOptionDto.builder()
                        .id(option.getId())
                        .optionText(text)
                        .isCorrect(isCorrect)
                        .displayOrder(o + 1)
                        .build());
            }

            questions.add(GameQuestionDto.builder()
                    .id(gq.getId())
                    .gameId(game.getId())
                    .contentType("VOCABULARY")
                    .contentId(v.getId())
                    .questionText(blanked)
                    .prompt("Chọn từ phù hợp nhất điền vào câu")
                    .targetAnswer(v.getWord())
                    .explanation(sentence)
                    .difficulty(v.getLevel())
                    .options(optionDtos)
                    .build());
        }
        return questions;
    }

    private List<GameQuestionDto> generateGrammarQuizQuestions(Game game, List<GrammarTopic> grammarList) {
        if (grammarList.isEmpty()) return Collections.emptyList();

        List<GrammarTopic> pool = new ArrayList<>(grammarList);
        Collections.shuffle(pool);
        int limit = Math.min(10, pool.size());

        List<GameQuestionDto> questions = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            GrammarTopic g = pool.get(i);

            String qText = "Cấu trúc nào sau đây là của chủ đề: \"" + g.getTopic() + "\"?";
            String correctStructure = g.getStructure() != null ? g.getStructure() : "S + V";

            GameQuestion gq = GameQuestion.builder()
                    .game(game)
                    .userId(g.getUser().getId())
                    .contentType("GRAMMAR")
                    .contentId(g.getId())
                    .questionText(qText)
                    .explanation(g.getTopic() + " -> " + correctStructure + " (" + (g.getUsage() != null ? g.getUsage() : "") + ")")
                    .difficulty(g.getLevel())
                    .isActive(true)
                    .build();
            gq = gameQuestionRepository.save(gq);

            List<String> optionsText = new ArrayList<>();
            optionsText.add(correctStructure);

            List<GrammarTopic> otherGrammars = new ArrayList<>(grammarList);
            otherGrammars.remove(g);
            Collections.shuffle(otherGrammars);
            for (int d = 0; d < Math.min(3, otherGrammars.size()); d++) {
                String str = otherGrammars.get(d).getStructure();
                if (str != null && !optionsText.contains(str)) {
                    optionsText.add(str);
                }
            }

            String[] fallbackStructures = {"S + V(s/es)", "S + have/has + V3", "S + will + V-inf", "S + was/were + V-ing"};
            int fbIdx = 0;
            while (optionsText.size() < 4) {
                String fb = fallbackStructures[fbIdx % fallbackStructures.length];
                if (!optionsText.contains(fb)) {
                    optionsText.add(fb);
                } else {
                    optionsText.add(fb + " (Pattern " + (fbIdx + 1) + ")");
                }
                fbIdx++;
            }

            Collections.shuffle(optionsText);

            List<GameQuestionOptionDto> optionDtos = new ArrayList<>();
            for (int o = 0; o < optionsText.size(); o++) {
                String text = optionsText.get(o);
                boolean isCorrect = text.equals(correctStructure);

                GameQuestionOption option = GameQuestionOption.builder()
                        .question(gq)
                        .optionText(text)
                        .isCorrect(isCorrect)
                        .displayOrder(o + 1)
                        .build();
                option = gameQuestionOptionRepository.save(option);

                optionDtos.add(GameQuestionOptionDto.builder()
                        .id(option.getId())
                        .optionText(text)
                        .isCorrect(isCorrect)
                        .displayOrder(o + 1)
                        .build());
            }

            questions.add(GameQuestionDto.builder()
                    .id(gq.getId())
                    .gameId(game.getId())
                    .contentType("GRAMMAR")
                    .contentId(g.getId())
                    .questionText(qText)
                    .prompt(g.getLevel())
                    .targetAnswer(correctStructure)
                    .explanation(g.getUsage())
                    .difficulty(g.getLevel())
                    .options(optionDtos)
                    .build());
        }
        return questions;
    }

    private void updateDailyLearningStats(User user) {
        LocalDate today = LocalDate.now();
        DailyLearningStatistic stat = dailyLearningStatisticRepository.findByUserIdAndStatisticDate(user.getId(), today)
                .orElseGet(() -> DailyLearningStatistic.builder()
                        .user(user)
                        .statisticDate(today)
                        .build());

        stat.setGameCount(stat.getGameCount() + 1);
        stat.setTotalLearningCount(stat.getTotalLearningCount() + 1);
        dailyLearningStatisticRepository.save(stat);
    }

    private void updateStudyStreak(User user) {
        LocalDate today = LocalDate.now();
        StudyStreak streak = studyStreakRepository.findByUserId(user.getId())
                .orElseGet(() -> StudyStreak.builder().user(user).build());

        if (streak.getLastStudyDate() == null) {
            streak.setCurrentStreak(1);
            streak.setLongestStreak(Math.max(1, streak.getLongestStreak()));
            streak.setLastStudyDate(today);
        } else if (streak.getLastStudyDate().equals(today.minusDays(1))) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
            streak.setLongestStreak(Math.max(streak.getCurrentStreak(), streak.getLongestStreak()));
            streak.setLastStudyDate(today);
        } else if (!streak.getLastStudyDate().equals(today)) {
            streak.setCurrentStreak(1);
            streak.setLastStudyDate(today);
        }
        studyStreakRepository.save(streak);
    }
}
