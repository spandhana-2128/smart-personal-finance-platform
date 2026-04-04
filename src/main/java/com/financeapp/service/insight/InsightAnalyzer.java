package com.financeapp.service.insight;

import com.financeapp.model.SpendingInsight;
import com.financeapp.model.User;

import java.util.Map;
import java.util.List;

/**
 * Template Method Pattern (Behavioral Design Pattern)
 *
 * Defines the fixed skeleton of the insight-generation algorithm:
 *   1. validateData()   — check if enough data exists
 *   2. analyze()        — compute the insight value
 *   3. formatMessage()  — produce a human-readable message
 *   4. buildInsight()   — assemble the SpendingInsight entity
 *
 * Concrete subclasses override steps 1-3 but never change the overall flow.
 */
public abstract class InsightAnalyzer {

    // ── Template method — final so subclasses cannot change the flow ──────
    public final SpendingInsight generate(User user,
                                          Map<String, Double> expenseByCategory,
                                          double totalIncome,
                                          double totalExpense) {

        if (!validateData(expenseByCategory, totalIncome, totalExpense)) {
            return null; // not enough data — skip this insight
        }

        double value   = analyze(expenseByCategory, totalIncome, totalExpense);
        String message = formatMessage(expenseByCategory, totalIncome, totalExpense, value);

        return buildInsight(user, message, value);
    }

    // ── Steps to override ─────────────────────────────────────────────────

    /** Return false if there is insufficient data to generate this insight. */
    protected abstract boolean validateData(Map<String, Double> expenseByCategory,
                                            double totalIncome,
                                            double totalExpense);

    /** Compute the key numeric value for this insight. */
    protected abstract double analyze(Map<String, Double> expenseByCategory,
                                      double totalIncome,
                                      double totalExpense);

    /** Turn the numeric value into a user-facing sentence. */
    protected abstract String formatMessage(Map<String, Double> expenseByCategory,
                                            double totalIncome,
                                            double totalExpense,
                                            double value);

    /** The insight type tag stored in the DB (e.g. "TOP_CATEGORY"). */
    protected abstract String getInsightType();

    // ── Hook used by template method ──────────────────────────────────────
    private SpendingInsight buildInsight(User user, String message, double value) {
        return SpendingInsight.builder()
                .user(user)
                .insightType(getInsightType())
                .message(message)
                .value(value)
                .generatedAt(java.time.LocalDateTime.now())
                .build();
    }
}
