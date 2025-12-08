package com.kosi.financetracker.backend.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EmailServiceTest {

    private final EmailService emailService = new EmailService();

    // Test 8: Verify OTP format
    @Test
    public void testGenerateOTP() {
        String otp = emailService.generateOTP();

        assertNotNull(otp);
        assertEquals(6, otp.length(), "OTP must be exactly 6 digits");
        assertTrue(otp.matches("\\d+"), "OTP must contain only numbers");
    }
}