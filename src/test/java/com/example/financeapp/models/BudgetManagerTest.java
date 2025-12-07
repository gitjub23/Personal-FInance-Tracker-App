package com.example.financeapp.models;

import com.example.financeapp.database.Database;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BudgetManagerTest {

    private static final int TEST_USER_ID = 9999;
    private static BudgetManager budgetManager;

    @BeforeAll
    static void initDb() {
        // Use the dedicated test database
        Database.useTestDatabase();
        budgetManager = new BudgetManager();
    }

    @BeforeEach
    void cleanBudgets() throws Exception {
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate("DELETE FROM budgets WHERE user_id = " + TEST_USER_ID);
        }
    }

    @Test
    void addBudget_shouldPersistBudgetForUser() {
        budgetManager.addBudget(TEST_USER_ID, "Food", 200.0);

        List<Budget> budgets = budgetManager.getBudgetsForUser(TEST_USER_ID);
        assertEquals(1, budgets.size());

        Budget b = budgets.get(0);
        assertEquals("Food", b.getCategory());
        assertEquals(200.0, b.getMonthlyLimit(), 0.001);
        assertEquals(TEST_USER_ID, b.getUserId());
    }

    @Test
    void updateBudget_shouldChangeMonthlyLimit() {
        budgetManager.addBudget(TEST_USER_ID, "Transport", 100.0);

        List<Budget> budgets = budgetManager.getBudgetsForUser(TEST_USER_ID);
        assertEquals(1, budgets.size());

        Budget b = budgets.get(0);
        b.setMonthlyLimit(150.0);

        boolean ok = budgetManager.updateBudget(b);
        assertTrue(ok);

        List<Budget> updated = budgetManager.getBudgetsForUser(TEST_USER_ID);
        assertEquals(1, updated.size());
        assertEquals(150.0, updated.get(0).getMonthlyLimit(), 0.001);
    }

    @Test
    void deleteBudget_shouldRemoveBudget() {
        budgetManager.addBudget(TEST_USER_ID, "Entertainment", 50.0);

        List<Budget> budgets = budgetManager.getBudgetsForUser(TEST_USER_ID);
        assertEquals(1, budgets.size());

        int id = budgets.get(0).getId();
        boolean ok = budgetManager.deleteBudget(id);
        assertTrue(ok);

        List<Budget> afterDelete = budgetManager.getBudgetsForUser(TEST_USER_ID);
        assertTrue(afterDelete.isEmpty());
    }

    @Test
    void addMultipleBudgets_shouldKeepThemSeparateByCategory() {
        budgetManager.addBudget(TEST_USER_ID, "Food", 200.0);
        budgetManager.addBudget(TEST_USER_ID, "Subscriptions", 40.0);

        List<Budget> budgets = budgetManager.getBudgetsForUser(TEST_USER_ID);
        assertEquals(2, budgets.size());

        Budget food = budgets.stream()
                .filter(b -> b.getCategory().equalsIgnoreCase("Food"))
                .findFirst()
                .orElseThrow();

        Budget subs = budgets.stream()
                .filter(b -> b.getCategory().equalsIgnoreCase("Subscriptions"))
                .findFirst()
                .orElseThrow();

        assertEquals(200.0, food.getMonthlyLimit(), 0.001);
        assertEquals(40.0, subs.getMonthlyLimit(), 0.001);
    }
}