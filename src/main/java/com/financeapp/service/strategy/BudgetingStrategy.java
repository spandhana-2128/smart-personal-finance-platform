package com.financeapp.service.strategy;

public interface BudgetingStrategy {
    double getWarningThreshold();
    double getSuggestedLimit(double income);
    String getStrategyName();
}