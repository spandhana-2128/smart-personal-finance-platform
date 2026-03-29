package com.financeapp.service;

import com.financeapp.model.Budget;
import com.financeapp.model.User;
import com.financeapp.repository.BudgetRepository;
import com.financeapp.repository.UserRepository;
import com.financeapp.service.strategy.BudgetingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final AlertService alertService;
    private final Map<String, BudgetingStrategy> strategies;

    public Budget createBudget(Long userId, double limit,
                               String month, int year, String strategyType) {
        User user = userRepository.findById(userId).orElseThrow();
        Budget budget = Budget.builder()
                .user(user).monthlyLimit(limit)
                .currentSpending(0.0).month(month)
                .year(year).strategyType(strategyType)
                .build();
        return budgetRepository.save(budget);
    }

    public Budget addSpending(Long budgetId, double amount) {
        Budget budget = budgetRepository.findById(budgetId).orElseThrow();
        budget.setCurrentSpending(budget.getCurrentSpending() + amount);
        budgetRepository.save(budget);
        alertService.onBudgetUpdated(
                budget.getUser().getUserId(),
                budget.getCurrentSpending(),
                budget.getMonthlyLimit(),
                budget.getStrategyType());
        return budget;
    }

    public List<Budget> getBudgetsByUser(Long userId) {
        return budgetRepository.findByUserUserId(userId);
    }

    public void deleteBudget(Long budgetId) {
        budgetRepository.deleteById(budgetId);
    }

    public double getSuggestedLimit(String strategyType, double income) {
        BudgetingStrategy s = strategies.get(strategyType);
        if (s == null) throw new IllegalArgumentException("Unknown strategy");
        return s.getSuggestedLimit(income);
    }
}