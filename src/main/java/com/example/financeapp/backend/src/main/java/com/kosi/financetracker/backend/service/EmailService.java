package com.kosi.financetracker.backend.service;

import com.kosi.financetracker.backend.model.EmailVerification;
import com.kosi.financetracker.backend.model.User;
import com.kosi.financetracker.backend.repository.EmailVerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailVerificationRepository verificationRepository;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.otp-expiration-minutes:15}")
    private int otpExpirationMinutes;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    private final Random random = new SecureRandom();

    /**
     * Generate a 6-digit numeric OTP
     */
    public String generateOTP() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Generates an OTP, saves it to the DB, prints it to Console (for debugging),
     * and attempts to send the email.
     */
    @Transactional
    public void sendVerificationOTP(User user, String verificationType) {
        String otp = generateOTP();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpirationMinutes);

        // 1. Invalidate any previous unused OTPs for this user to keep DB clean
        verificationRepository.findByUserIdAndUsedFalse(user.getId())
            .forEach(v -> {
                v.setUsed(true);
                verificationRepository.save(v);
            });

        // 2. Create and Save the new verification record
        EmailVerification verification = new EmailVerification(
            user.getId(),
            user.getEmail(),
            otp,
            expiresAt,
            verificationType
        );
        verificationRepository.save(verification);

        // 3. DEBUG LOG: Print to console so you can see it without checking email
        System.out.println("==================================================");
        System.out.println(">>> [EMAIL DEBUG] To: " + user.getEmail());
        System.out.println(">>> [EMAIL DEBUG] Type: " + verificationType);
        System.out.println(">>> [EMAIL DEBUG] OTP CODE: " + otp);
        System.out.println("==================================================");

        // 4. Send the actual email
        try {
            sendOTPEmail(user.getEmail(), user.getName(), otp, verificationType);
            System.out.println(">>> [EMAIL SERVICE] Email sent successfully.");
        } catch (MessagingException e) {
            // We log the error but do NOT throw it. 
            // This ensures the user is still registered even if SMTP fails.
            System.err.println(">>> [EMAIL SERVICE ERROR] Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Verifies if an OTP is valid, matches the email, and is not expired.
     */
    @Transactional
    public boolean verifyOTP(String email, String otp) {
        var verificationOpt = verificationRepository.findByEmailAndOtpAndUsedFalse(email, otp);
        
        if (verificationOpt.isEmpty()) {
            return false;
        }

        EmailVerification verification = verificationOpt.get();
        
        if (verification.isExpired()) {
            return false;
        }

        // Mark as used so it cannot be used again
        verification.setUsed(true);
        verificationRepository.save(verification);

        return true;
    }

    /**
     * Sends a 2FA login code (if you are using email for 2FA instead of Google Auth)
     */
    @Transactional
    public void send2FACode(User user, String code) {
        // Debug Log
        System.out.println(">>> [2FA DEBUG] Sending 2FA code to " + user.getEmail() + ": " + code);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("FinanceTracker - Your 2FA Login Code");

            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; background: #f9f9f9; padding: 20px; border-radius: 10px;">
                        <h2 style="color: #2563eb; text-align: center;">Two-Factor Authentication</h2>
                        <p>Hello %s,</p>
                        <p>Here is your login code:</p>
                        <div style="background: white; padding: 15px; text-align: center; border-radius: 5px; border: 1px solid #ddd;">
                            <h1 style="margin: 0; color: #2563eb; letter-spacing: 5px;">%s</h1>
                        </div>
                        <p style="font-size: 12px; color: #666; margin-top: 20px;">
                            If you did not try to log in, please secure your account immediately.
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(user.getName(), code);

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println(">>> [EMAIL ERROR] Failed to send 2FA code: " + e.getMessage());
        }
    }

    /**
     * Sends a Password Reset Link
     */
    public void sendPasswordResetEmail(User user, String resetToken) {
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
        
        // Debug Log
        System.out.println(">>> [RESET DEBUG] Link for " + user.getEmail() + ": " + resetLink);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("FinanceTracker - Password Reset Request");

            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; background: #f9f9f9; padding: 20px; border-radius: 10px;">
                        <h2 style="color: #2563eb;">Reset Your Password</h2>
                        <p>Hello %s,</p>
                        <p>We received a request to reset your password. Click the button below to proceed:</p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" style="background-color: #2563eb; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold;">Reset Password</a>
                        </div>
                        <p style="font-size: 12px; color: #666;">
                            Or copy this link: <br> %s
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(user.getName(), resetLink, resetLink);

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println(">>> [EMAIL ERROR] Failed to send password reset: " + e.getMessage());
        }
    }

    /**
     * Cleans up expired OTPs from the database.
     * Can be scheduled using @Scheduled if enabled in main class.
     */
    @Transactional
    public void cleanupExpiredOTPs() {
        verificationRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

    // ================= PRIVATE HELPERS =================

    private void sendOTPEmail(String to, String name, String otp, String verificationType) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        
        String subject = getSubjectForType(verificationType);
        helper.setSubject(subject);

        String htmlContent = buildOTPEmailTemplate(name, otp, verificationType);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private String buildOTPEmailTemplate(String name, String otp, String verificationType) {
        String title = getTitleForType(verificationType);
        String description = getDescriptionForType(verificationType);

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); overflow: hidden; }
                    .header { background-color: #2563eb; color: #ffffff; padding: 20px; text-align: center; }
                    .content { padding: 30px; }
                    .otp-box { background-color: #f3f4f6; border: 2px dashed #2563eb; border-radius: 8px; padding: 20px; text-align: center; margin: 25px 0; }
                    .otp-code { font-size: 36px; font-weight: bold; color: #2563eb; letter-spacing: 8px; margin: 10px 0; }
                    .footer { background-color: #f8fafc; padding: 20px; text-align: center; font-size: 12px; color: #64748b; }
                    .warning { background-color: #fffbeb; border-left: 4px solid #f59e0b; padding: 15px; font-size: 14px; color: #92400e; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1 style="margin:0; font-size: 24px;">FinanceTracker</h1>
                    </div>
                    <div class="content">
                        <h2 style="color: #1e293b; margin-top: 0;">%s</h2>
                        <p>Hello <strong>%s</strong>,</p>
                        <p>%s</p>
                        
                        <div class="otp-box">
                            <div style="font-size: 12px; text-transform: uppercase; letter-spacing: 1px; color: #64748b;">Verification Code</div>
                            <div class="otp-code">%s</div>
                            <div style="font-size: 14px; color: #64748b;">Expires in %d minutes</div>
                        </div>
                        
                        <div class="warning">
                            <strong>Security Notice:</strong> Never share this code with anyone. Our support team will never ask for it.
                        </div>
                    </div>
                    <div class="footer">
                        <p>If you didn't request this email, you can safely ignore it.</p>
                        <p>&copy; 2024 FinanceTracker App. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(title, name, description, otp, otpExpirationMinutes);
    }

    private String getSubjectForType(String type) {
        if (type == null) return "FinanceTracker - Verification Code";
        return switch (type) {
            case "registration" -> "Verify your email address";
            case "forgot-password" -> "Reset your password";
            case "change-email" -> "Verify new email address";
            default -> "FinanceTracker - Verification Code";
        };
    }

    private String getTitleForType(String type) {
        if (type == null) return "Verification Code";
        return switch (type) {
            case "registration" -> "Welcome to FinanceTracker!";
            case "forgot-password" -> "Password Reset Request";
            case "change-email" -> "Confirm Email Change";
            default -> "Verification Required";
        };
    }

    private String getDescriptionForType(String type) {
        if (type == null) return "Please use the code below to verify your action.";
        return switch (type) {
            case "registration" -> "Thank you for creating an account. To complete your registration and verify your email address, please use the code below.";
            case "forgot-password" -> "We received a request to reset your password. Enter the following code to proceed.";
            case "change-email" -> "You have requested to update your email address. Please use the code below to verify this change.";
            default -> "Please use the code below to verify your action.";
        };
    }
}
