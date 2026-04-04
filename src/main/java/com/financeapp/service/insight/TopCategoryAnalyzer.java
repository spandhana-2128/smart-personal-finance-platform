package com.financeapp.service.insight;

import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * Concrete analyzer: finds the category where the user spends the most.
 * Example output: "You spend the most on Food — ₹4,500.00 this month."
 */
@Component
public class TopCategoryAnalyzer extends InsightAnalyzer {

    // No instance variable - Spring beans are singletons, so state must be local
    // topCategory is derived inside formatMessage from expenseByCategory directly

    @Override
    protected boolean validateData(Map<String, Double> expenseByCategory,
                                   double totalIncome, double totalExpense) {
        return expenseByCategory != null && !expenseByCategory.isEmpty();
    }

    @Override
    protected double analyze(Map<String, Double> expenseByCategory,
                              double totalIncome, double totalExpense) {
        return expenseByCategory.values().stream()
                .max(Double::compareTo)
                .orElse(0.0);
    }

    @Override
    protected String formatMessage(Map<String, Double> expenseByCategory,
                                   double totalIncome, double totalExpense, double value) {
        // Re-derive top category name safely (no shared state)
        String topCategory = expenseByCategory.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");

        return String.format(
                "You spend the most on %s \u2014 \u20B9%.2f this month.", topCategory, value);
    }

    @Override
    protected String getInsightType() {
        return "TOP_CATEGORY";
    }
}

