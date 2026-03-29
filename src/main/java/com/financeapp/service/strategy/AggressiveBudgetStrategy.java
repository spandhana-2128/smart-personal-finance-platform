package com.financeapp.service.strategy;

import org.springframework.stereotype.Component;

@Component("AGGRESSIVE")
public class AggressiveBudgetStrategy implements BudgetingStrategy {

    @Override
    public double getWarningThreshold() { return 85.0; }

    @Override
    public double getSuggestedLimit(double income) { return income * 0.80; }

    @Override
    public String getStrategyName() { return "Aggressive (80% of income)"; }
}