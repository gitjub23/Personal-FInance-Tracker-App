package com.kosi.financetracker.backend.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BudgetTest {

    // Test 1: Verify standard percentage calculation
    @Test
    public void testPercentageUsedNormalCase() {
        Budget budget = new Budget();
        budget.setLimitAmount(1000.0);
        budget.setSpent(500.0);

        assertEquals(50.0, budget.getPercentageUsed(), "500 out of 1000 should be 50%");
    }

    // Test 2: Edge Case - Verify we don't crash if limit is 0
    @Test
    public void testPercentageUsedZeroLimit() {
        Budget budget = new Budget();
        budget.setLimitAmount(0.0);
        budget.setSpent(100.0);

        assertEquals(0.0, budget.getPercentageUsed(), "Should return 0% if limit is 0 to avoid divide-by-zero errors");
    }

    // Test 3: Verify Over Budget Logic
    @Test
    public void testIsOverBudget() {
        Budget budget = new Budget();
        budget.setLimitAmount(100.0);
        budget.setSpent(150.0);

        assertTrue(budget.isOverBudget(), "Should be true when spent > limit");
    }

    // Test 4: Verify 'Near Limit' Logic (Warning zone > 80%)
    @Test
    public void testIsNearLimit() {
        Budget budget = new Budget();
        budget.setLimitAmount(100.0);
        budget.setSpent(85.0); // 85% used

        assertTrue(budget.isNearLimit(), "Should be true when spending is between 80% and 100%");
    }

    // Test 5: Verify Not Near Limit (Safe zone)
    @Test
    public void testIsNotNearLimit() {
        Budget budget = new Budget();
        budget.setLimitAmount(100.0);
        budget.setSpent(50.0); // 50% used

        assertFalse(budget.isNearLimit(), "Should be false when spending is under 80%");
    }
}