package com.kosi.financetracker.backend.repository;

import com.kosi.financetracker.backend.model.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    
    Optional<EmailVerification> findByEmailAndOtpAndUsedFalse(String email, String otp);
    
    Optional<EmailVerification> findByUserIdAndOtpAndUsedFalse(Long userId, String otp);
    
    List<EmailVerification> findByEmailAndUsedFalse(String email);
    
    List<EmailVerification> findByUserIdAndUsedFalse(Long userId);
    
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
    
    void deleteByUserId(Long userId);
}