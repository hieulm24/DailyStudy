package com.englishlearning.service;

import com.englishlearning.entity.PasswordResetOtp;
import com.englishlearning.repository.PasswordResetOtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailOtpService {

    private final PasswordResetOtpRepository otpRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.mail.from:no-reply@englishlearning.local}")
    private String mailFrom;

    @Transactional
    public String generateAndSaveOtp(String email) {
        // Generate 6-digit OTP
        int number = secureRandom.nextInt(900000) + 100000;
        String otpCode = String.valueOf(number);

        // Expiry 10 minutes
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);

        PasswordResetOtp otpEntity = PasswordResetOtp.builder()
                .email(email.trim().toLowerCase())
                .otpCode(otpCode)
                .expiryTime(expiry)
                .isUsed(false)
                .build();

        otpRepository.save(otpEntity);

        // Send or Log OTP
        sendEmailNotification(email, otpCode);

        return otpCode;
    }

    private void sendEmailNotification(String email, String otpCode) {
        log.info("\n========================================================\n" +
                 " [EMAIL OTP NOTIFICATION]\n" +
                 " To: {}\n" +
                 " Subject: Ma xac thuc quen mat khau - DailyStudy English Learning\n" +
                 " OTP Code: [{}]\n" +
                 " Expiry: 10 minutes\n" +
                 "========================================================", email, otpCode);
    }

    @Transactional(readOnly = true)
    public boolean verifyOtp(String email, String otpCode) {
        if (email == null || otpCode == null) return false;
        return otpRepository.findTopByEmailAndOtpCodeAndIsUsedFalseAndExpiryTimeAfterOrderByCreatedAtDesc(
                email.trim().toLowerCase(), otpCode.trim(), LocalDateTime.now()
        ).isPresent();
    }

    @Transactional
    public void markOtpAsUsed(String email, String otpCode) {
        otpRepository.findTopByEmailAndOtpCodeAndIsUsedFalseAndExpiryTimeAfterOrderByCreatedAtDesc(
                email.trim().toLowerCase(), otpCode.trim(), LocalDateTime.now()
        ).ifPresent(otp -> {
            otp.setIsUsed(true);
            otpRepository.save(otp);
        });
    }
}
