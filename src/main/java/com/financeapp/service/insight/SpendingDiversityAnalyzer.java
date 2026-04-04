package com.financeapp.service.insight;

import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * Concrete analyzer: measures spending spread across categories.
 * A high concentration (1-2 categories dominating) is flagged as a risk.
 * Example output: "Food & Rent account for 85% of your total spending."
 */
@Component
public class SpendingDiversityAnalyzer extends InsightAnalyzer {

    @Override
    protected boolean validateData(Map<String, Double> expenseByCategory,
                                   double totalIncome, double totalExpense) {
        return expenseByCategory != null
                && expenseByCategory.size() >= 2
                && totalExpense > 0;
    }

    @Override
    protected double analyze(Map<String, Double> expenseByCategory,
                              double totalIncome, double totalExpense) {
        // Find the top-2 categories' combined share
        double top2Sum = expenseByCategory.values().stream()
                .sorted((a, b) -> Double.compare(b, a))
                .limit(2)
                .mapToDouble(Double::doubleValue)
                .sum();

        return (top2Sum / totalExpense) * 100.0;
    }

    @Override
    protected String formatMessage(Map<String, Double> expenseByCategory,
                                   double totalIncome, double totalExpense, double value) {

        // Get top 2 category names for the message
        String[] topTwo = expenseByCategory.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(2)
                .map(Map.Entry::getKey)
                .toArray(String[]::new);

        String cats = topTwo.length == 2
                ? topTwo[0] + " & " + topTwo[1]
                : topTwo[0];

        if (value >= 80) {
            return String.format(
                    "\u26A0 %s account for %.1f%% of your spending. Consider diversifying.",
                    cats, value);
        } else {
            return String.format(
                    "%s are your top spending areas at %.1f%% of total expenses.",
                    cats, value);
        }
    }

    @Override
    protected String getInsightType() {
        return "SPENDING_DIVERSITY";
    }
}
