package com.example.financeapp.models;

import com.example.financeapp.database.Database;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TransactionManager using a dedicated test user id.
 */
class TransactionManagerTest {

    private static final int TEST_USER_ID = 9999;
    private TransactionManager manager;

    @BeforeAll
    static void initDb() {
        // Switch to a dedicated test DB before any tests run
        Database.useTestDatabase();
    }

    @BeforeEach
    void setUp() throws SQLException {
        manager = new TransactionManager();

        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate("DELETE FROM transactions WHERE user_id = " + TEST_USER_ID);
        }
    }

    // Helper to build a Transaction matching your TransactionManager schema
    private Transaction tx(double amount, boolean isIncome, String category, LocalDate date) {
        Transaction t = new Transaction();
        t.setUserId(TEST_USER_ID);
        t.setAmount(amount);
        t.setIncome(isIncome);
        t.setCategory(category);
        t.setDate(date);
        t.setPaymentMethod(null);
        t.setNotes("");
        t.setRecurring(false);
        t.setRecurrenceRule(null);
        return t;
    }

    // -------------------------------------------------------------
    // TESTS
    // -------------------------------------------------------------

    @Test
    void addAndGetTransactions_shouldPersistAndReturnAllForUser() {
        LocalDate today = LocalDate.now();

        Transaction t1 = tx(3000, true, "Salary", today);
        Transaction t2 = tx(-150, false, "Food", today);

        manager.addTransaction(t1);
        manager.addTransaction(t2);

        List<Transaction> list = manager.getTransactionsForUser(TEST_USER_ID);

        assertEquals(2, list.size(), "Expected 2 transactions for test user.");

        boolean hasSalary = list.stream()
                .anyMatch(t -> t.getCategory().equals("Salary") && t.getAmount() == 3000);

        boolean hasFood = list.stream()
                .anyMatch(t -> t.getCategory().equals("Food") && t.getAmount() == -150);

        assertTrue(hasSalary, "Missing Salary transaction");
        assertTrue(hasFood, "Missing Food transaction");
    }

    @Test
    void monthlyTotals_shouldCalculateIncomeAndExpensesSeparately() {
        YearMonth ym = YearMonth.now();
        LocalDate d1 = ym.atDay(5);
        LocalDate d2 = ym.atDay(10);
        LocalDate d3 = ym.atDay(15);

        // Current month
        manager.addTransaction(tx(3000, true, "Salary", d1));          // income
        manager.addTransaction(tx(-150, false, "Food", d2));          // expense
        manager.addTransaction(tx(-50, false, "Transport", d3));      // expense

        // Previous month (should NOT be counted)
        YearMonth previous = ym.minusMonths(1);
        manager.addTransaction(tx(2500, true, "Salary", previous.atDay(1)));

        double income = manager.getTotalIncomeForMonth(TEST_USER_ID, ym);
        double expenses = manager.getTotalExpenseForMonth(TEST_USER_ID, ym);

        assertEquals(3000.0, income, 0.001, "Income for current month should be 3000");
        assertEquals(-200.0, expenses, 0.001, "Expenses for current month should be -200 (sum of -150 and -50)");
    }

    @Test
    void categoryTotals_shouldReturnCorrectTotalsForExpenses() {
        YearMonth ym = YearMonth.now();
        LocalDate d1 = ym.atDay(3);
        LocalDate d2 = ym.atDay(7);
        LocalDate d3 = ym.atDay(12);

        // All in current month
        manager.addTransaction(tx(-100, false, "Food", d1));
        manager.addTransaction(tx(-50, false, "Food", d2));
        manager.addTransaction(tx(-30, false, "Entertainment", d3));

        // Income (should not be included in expense totals)
        manager.addTransaction(tx(2000, true, "Salary", d1));

        Map<String, Double> totals =
                manager.getCategoryTotalsForMonth(TEST_USER_ID, ym, false); // false = expenses

        // Stored amounts are negative; SUM(amount) will be negative too.
        // Your dashboard uses this as positive values, so adapt expectations accordingly.
        // If you prefer positive values, you can take Math.abs in your UI.
        assertEquals(-150.0,
                totals.getOrDefault("Food", 0.0),
                0.001,
                "Food total should be -150 (sum of -100 and -50)");

        assertEquals(-30.0,
                totals.getOrDefault("Entertainment", 0.0),
                0.001,
                "Entertainment total should be -30");
    }

    @Test
    void recentTransactions_shouldReturnMostRecentFirst() {
        YearMonth ym = YearMonth.now();

        LocalDate d1 = ym.atDay(1);
        LocalDate d2 = ym.atDay(10);
        LocalDate d3 = ym.atDay(20);

        manager.addTransaction(tx(-10, false, "Oldest", d1));
        manager.addTransaction(tx(-20, false, "Middle", d2));
        manager.addTransaction(tx(-30, false, "Newest", d3));

        List<Transaction> recent = manager.getRecentTransactions(TEST_USER_ID, 2);

        assertEquals(2, recent.size(), "Should only return 2 most recent transactions");

        // Newest date first
        assertEquals(d3, recent.get(0).getDate());
        assertEquals(d2, recent.get(1).getDate());
    }
}