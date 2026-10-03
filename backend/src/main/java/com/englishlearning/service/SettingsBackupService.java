package com.englishlearning.service;

import com.englishlearning.common.ResourceNotFoundException;
import com.englishlearning.dto.grammar.GrammarFilterRequest;
import com.englishlearning.dto.grammar.GrammarRequest;
import com.englishlearning.dto.listening.ListeningFilterRequest;
import com.englishlearning.dto.listening.ListeningRequest;
import com.englishlearning.dto.settings.BackupDataDto;
import com.englishlearning.dto.settings.UserSettingDto;
import com.englishlearning.dto.speaking.SpeakingFilterRequest;
import com.englishlearning.dto.speaking.SpeakingRequest;
import com.englishlearning.dto.vocabulary.VocabularyFilterRequest;
import com.englishlearning.dto.vocabulary.VocabularyRequest;
import com.englishlearning.entity.User;
import com.englishlearning.entity.UserSetting;
import com.englishlearning.repository.UserRepository;
import com.englishlearning.repository.UserSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingsBackupService {

    private final UserSettingRepository userSettingRepository;
    private final UserRepository userRepository;
    private final VocabularyService vocabularyService;
    private final GrammarService grammarService;
    private final ListeningService listeningService;
    private final SpeakingService speakingService;

    @Transactional(readOnly = true)
    public UserSettingDto getUserSettings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserSetting setting = userSettingRepository.findByUserId(userId)
                .orElseGet(() -> UserSetting.builder()
                        .user(user)
                        .theme("LIGHT")
                        .language("vi")
                        .timezone("Asia/Ho_Chi_Minh")
                        .dailyLearningTarget(30)
                        .reviewEnabled(true)
                        .build());

        return UserSettingDto.builder()
                .id(setting.getId())
                .theme(setting.getTheme())
                .language(setting.getLanguage())
                .timezone(setting.getTimezone())
                .dailyLearningTarget(setting.getDailyLearningTarget())
                .reviewEnabled(setting.getReviewEnabled())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public UserSettingDto updateUserSettings(Long userId, UserSettingDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (StringUtils.hasText(dto.getDisplayName())) {
            user.setDisplayName(dto.getDisplayName().trim());
            userRepository.save(user);
        }

        UserSetting setting = userSettingRepository.findByUserId(userId)
                .orElseGet(() -> UserSetting.builder().user(user).build());

        if (StringUtils.hasText(dto.getTheme())) setting.setTheme(dto.getTheme());
        if (StringUtils.hasText(dto.getLanguage())) setting.setLanguage(dto.getLanguage());
        if (StringUtils.hasText(dto.getTimezone())) setting.setTimezone(dto.getTimezone());
        if (dto.getDailyLearningTarget() != null && dto.getDailyLearningTarget() > 0) {
            setting.setDailyLearningTarget(dto.getDailyLearningTarget());
        }
        if (dto.getReviewEnabled() != null) setting.setReviewEnabled(dto.getReviewEnabled());

        UserSetting saved = userSettingRepository.save(setting);

        return UserSettingDto.builder()
                .id(saved.getId())
                .theme(saved.getTheme())
                .language(saved.getLanguage())
                .timezone(saved.getTimezone())
                .dailyLearningTarget(saved.getDailyLearningTarget())
                .reviewEnabled(saved.getReviewEnabled())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .build();
    }

    @Transactional(readOnly = true)
    public BackupDataDto exportData(Long userId) {
        UserSettingDto settings = getUserSettings(userId);

        VocabularyFilterRequest vf = new VocabularyFilterRequest();
        vf.setSize(10000);
        var vocabs = vocabularyService.getVocabularies(userId, vf).getItems();

        GrammarFilterRequest gf = new GrammarFilterRequest();
        gf.setSize(10000);
        var grammars = grammarService.getGrammars(userId, gf).getItems();

        ListeningFilterRequest lf = new ListeningFilterRequest();
        lf.setSize(10000);
        var listenings = listeningService.getListeningLessons(userId, lf).getItems();

        SpeakingFilterRequest sf = new SpeakingFilterRequest();
        sf.setSize(10000);
        var speakings = speakingService.getSpeakingLessons(userId, sf).getItems();

        return BackupDataDto.builder()
                .version("1.0.0")
                .exportDate(LocalDateTime.now())
                .settings(settings)
                .vocabularies(vocabs)
                .grammars(grammars)
                .listenings(listenings)
                .speakings(speakings)
                .build();
    }

    @Transactional
    public void importData(Long userId, BackupDataDto backup) {
        if (backup == null) return;

        if (backup.getSettings() != null) {
            updateUserSettings(userId, backup.getSettings());
        }

        if (backup.getVocabularies() != null) {
            for (var v : backup.getVocabularies()) {
                VocabularyRequest req = VocabularyRequest.builder()
                        .word(v.getWord())
                        .meaning(v.getMeaning())
                        .pronunciation(v.getPronunciation())
                        .partOfSpeech(v.getPartOfSpeech())
                        .level(v.getLevel())
                        .note(v.getNote())
                        .status(v.getStatus())
                        .examples(v.getExamples())
                        .build();
                try {
                    vocabularyService.createVocabulary(userId, req);
                } catch (Exception e) {
                    log.warn("Skipping vocab during import: {}", e.getMessage());
                }
            }
        }

        if (backup.getGrammars() != null) {
            for (var g : backup.getGrammars()) {
                GrammarRequest req = GrammarRequest.builder()
                        .topic(g.getTopic())
                        .level(g.getLevel())
                        .structure(g.getStructure())
                        .positiveStructure(g.getPositiveStructure())
                        .negativeStructure(g.getNegativeStructure())
                        .questionStructure(g.getQuestionStructure())
                        .usage(g.getUsage())
                        .signalWords(g.getSignalWords())
                        .commonMistakes(g.getCommonMistakes())
                        .note(g.getNote())
                        .status(g.getStatus())
                        .examples(g.getExamples())
                        .build();
                try {
                    grammarService.createGrammar(userId, req);
                } catch (Exception e) {
                    log.warn("Skipping grammar during import: {}", e.getMessage());
                }
            }
        }

        if (backup.getListenings() != null) {
            for (var l : backup.getListenings()) {
                ListeningRequest req = ListeningRequest.builder()
                        .title(l.getTitle())
                        .description(l.getDescription())
                        .url(l.getUrl())
                        .durationSeconds(l.getDurationSeconds())
                        .level(l.getLevel())
                        .note(l.getNote())
                        .status(l.getStatus())
                        .learnedAt(l.getLearnedAt())
                        .build();
                try {
                    listeningService.createListeningLesson(userId, req);
                } catch (Exception e) {
                    log.warn("Skipping listening during import: {}", e.getMessage());
                }
            }
        }

        if (backup.getSpeakings() != null) {
            for (var s : backup.getSpeakings()) {
                SpeakingRequest req = SpeakingRequest.builder()
                        .title(s.getTitle())
                        .topic(s.getTopic())
                        .description(s.getDescription())
                        .url(s.getUrl())
                        .durationSeconds(s.getDurationSeconds())
                        .level(s.getLevel())
                        .note(s.getNote())
                        .status(s.getStatus())
                        .practicedAt(s.getPracticedAt())
                        .build();
                try {
                    speakingService.createSpeakingLesson(userId, req);
                } catch (Exception e) {
                    log.warn("Skipping speaking during import: {}", e.getMessage());
                }
            }
        }
    }
}
