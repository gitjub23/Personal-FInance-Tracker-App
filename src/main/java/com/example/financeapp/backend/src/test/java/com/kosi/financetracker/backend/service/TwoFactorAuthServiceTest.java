package com.kosi.financetracker.backend.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TwoFactorAuthServiceTest {

    private final TwoFactorAuthService service = new TwoFactorAuthService();

    // Test 6: Verify a correct backup code works
    @Test
    public void testVerifyBackupCodeSuccess() {
        String storedCodes = "12345678,87654321,11223344";
        String inputCode = "87654321";

        assertTrue(service.verifyBackupCode(storedCodes, inputCode), "Should return true for a valid existing code");
    }

    // Test 7: Verify backup code removal logic
    @Test
    public void testRemoveBackupCode() {
        String storedCodes = "12345678,87654321,11223344";
        String codeToRemove = "87654321";

        String updatedCodes = service.removeBackupCode(storedCodes, codeToRemove);

        // Code should be gone
        assertFalse(updatedCodes.contains("87654321"));
        // Other codes should remain
        assertTrue(updatedCodes.contains("12345678"));
        assertTrue(updatedCodes.contains("11223344"));
    }
}