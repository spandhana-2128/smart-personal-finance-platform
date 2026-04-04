package com.financeapp.service.insight;

import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * Concrete analyzer: computes the user's savings rate for the month.
 * Example output: "Great job! You saved 34.5% of your income this month."
 */
@Component
public class SavingsRateAnalyzer extends InsightAnalyzer {

    @Override
    protected boolean validateData(Map<String, Double> expenseByCategory,
                                   double totalIncome, double totalExpense) {
        return totalIncome > 0;
    }

    @Override
    protected double analyze(Map<String, Double> expenseByCategory,
                              double totalIncome, double totalExpense) {
        double saved = totalIncome - totalExpense;
        return (saved / totalIncome) * 100.0;
    }

    @Override
    protected String formatMessage(Map<String, Double> expenseByCategory,
                                   double totalIncome, double totalExpense, double value) {
        double saved = totalIncome - totalExpense;

        if (value < 0) {
            return String.format(
                    "\u26A0 You overspent by \u20B9%.2f this month (%.1f%% over income).",
                    Math.abs(saved), Math.abs(value));
        } else if (value < 10) {
            return String.format(
                    "You saved %.1f%% of your income (\u20B9%.2f). Try to save at least 20%%.",
                    value, saved);
        } else if (value < 30) {
            return String.format(
                    "Good effort! You saved %.1f%% of your income (\u20B9%.2f) this month.",
                    value, saved);
        } else {
            return String.format(
                    "Great job! You saved %.1f%% of your income (\u20B9%.2f) this month.",
                    value, saved);
        }
    }

    @Override
    protected String getInsightType() {
        return "SAVINGS_RATE";
    }
}
