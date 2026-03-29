package com.financeapp.service;

public interface BudgetAlertObserver {
    void onBudgetUpdated(Long userId, double currentSpending,
                         double limit, String strategy);
}