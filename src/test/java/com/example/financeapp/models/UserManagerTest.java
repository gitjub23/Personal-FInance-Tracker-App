package com.example.financeapp.models;

import com.example.financeapp.database.Database;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {

    private static UserManager userManager;

    @BeforeAll
    static void initDb() {
        Database.useTestDatabase();
        userManager = new UserManager();
    }

    @BeforeEach
    void cleanUsers() throws Exception {
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate("DELETE FROM users");
        }
    }

    @Test
    void registerAndLogin_shouldWorkWithEmailAndUsername() {
        boolean ok = userManager.register("john", "john@example.com", "secret123", "");
        assertTrue(ok);

        assertNotNull(userManager.login("john", "secret123"));
        assertNotNull(userManager.login("john@example.com", "secret123"));
    }

    @Test
    void register_shouldRejectDuplicateEmailIgnoringCase() {
        userManager.register("john", "john@example.com", "a", "");
        boolean ok = userManager.register("jane", "JOHN@example.com", "b", "");

        assertFalse(ok, "Duplicate email (case-insensitive) should fail");
    }

    @Test
    void resetPassword_shouldAllowLoginWithNewPassword() {
        userManager.register("john", "john@example.com", "oldpass", "");

        boolean ok = userManager.resetPassword("john@example.com", "newpass");
        assertTrue(ok);

        assertNull(userManager.login("john@example.com", "oldpass"));
        assertNotNull(userManager.login("john@example.com", "newpass"));
    }
}