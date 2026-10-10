package com.englishlearning.repository;

import com.englishlearning.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

    Optional<PasswordResetOtp> findTopByEmailAndIsUsedFalseAndExpiryTimeAfterOrderByCreatedAtDesc(
            String email, LocalDateTime now
    );

    Optional<PasswordResetOtp> findTopByEmailAndOtpCodeAndIsUsedFalseAndExpiryTimeAfterOrderByCreatedAtDesc(
            String email, String otpCode, LocalDateTime now
    );
}
