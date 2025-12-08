package com.example.financeapp.models;

import com.example.financeapp.database.Database;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticsServiceTest {

    private static final int TEST_USER_ID = 9999;
    private static TransactionManager tm;
    private static AnalyticsService analytics;

    @BeforeAll
    static void initDb() {
        Database.useTestDatabase();
        tm = new TransactionManager();
        analytics = new AnalyticsService(tm);
    }

    @BeforeEach
    void clean() throws Exception {
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate("DELETE FROM transactions WHERE user_id = " + TEST_USER_ID);
        }
    }

    @Test
    void noData_shouldReturnFriendlyMessage() {
        List<String> recs = analytics.generateMonthlyRecommendations(
                TEST_USER_ID, YearMonth.now());
        assertFalse(recs.isEmpty());
        assertTrue(recs.get(0).toLowerCase().contains("not enough data"));
    }

    @Test
    void higherSpendingOnCategory_shouldGenerateInsight() {
        YearMonth now = YearMonth.now();
        YearMonth last = now.minusMonths(1);

        tm.addTransaction(buildTx(-100, "Food", last.atDay(5)));
        tm.addTransaction(buildTx(-200, "Food", now.atDay(5)));

        List<String> recs = analytics.generateMonthlyRecommendations(TEST_USER_ID, now);
        assertTrue(recs.stream().anyMatch(r -> r.contains("Food")));
    }

    private Transaction buildTx(double amount, String category, LocalDate date) {
        Transaction t = new Transaction();
        t.setUserId(TEST_USER_ID);
        t.setAmount(amount);
        t.setIncome(amount > 0);
        t.setCategory(category);
        t.setDate(date);
        t.setPaymentMethod(null);
        t.setNotes("");
        t.setRecurring(false);
        t.setRecurrenceRule(null);
        tm.addTransaction(t);
        return t;
    }
}