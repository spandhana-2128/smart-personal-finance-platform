package com.financeapp.service.strategy;

import org.springframework.stereotype.Component;

@Component("CONSERVATIVE")
public class ConservativeBudgetStrategy implements BudgetingStrategy {

    @Override
    public double getWarningThreshold() { return 70.0; }

    @Override
    public double getSuggestedLimit(double income) { return income * 0.50; }

    @Override
    public String getStrategyName() { return "Conservative (50% of income)"; }
}