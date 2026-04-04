package com.financeapp.service;

import com.financeapp.model.Expense;
import com.financeapp.model.Income;
import com.financeapp.model.SpendingInsight;
import com.financeapp.model.Transaction;
import com.financeapp.model.User;
import com.financeapp.repository.InsightRepository;
import com.financeapp.repository.TransactionRepository;
import com.financeapp.repository.UserRepository;
import com.financeapp.service.insight.InsightAnalyzer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InsightService {

    private final InsightRepository insightRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    // Spring injects ALL InsightAnalyzer beans automatically (Template Method participants)
    private final List<InsightAnalyzer> analyzers;

    /**
     * Generate fresh insights for a user for the current month.
     * Deletes old insights first, then runs every analyzer.
     */
    @Transactional
    public List<SpendingInsight> generateInsights(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Clear stale insights
        insightRepository.deleteByUserUserId(userId);

        // Fetch this month's transactions
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end   = LocalDate.now();
        List<Transaction> transactions =
                transactionRepository.findByUserUserIdAndDateBetween(userId, start, end);

        // Aggregate expense by category
        Map<String, Double> expenseByCategory = transactions.stream()
                .filter(t -> t instanceof Expense)
                .map(t -> (Expense) t)
                .filter(e -> e.getCategory() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().getCategoryName(),
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        double totalExpense = transactions.stream()
                .filter(t -> t instanceof Expense)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalIncome = transactions.stream()
                .filter(t -> t instanceof Income)
                .mapToDouble(Transaction::getAmount)
                .sum();

        // Run every analyzer via Template Method pattern
        List<SpendingInsight> insights = analyzers.stream()
                .map(analyzer -> analyzer.generate(user, expenseByCategory,
                        totalIncome, totalExpense))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return insightRepository.saveAll(insights);
    }

    /** Retrieve previously generated insights for a user. */
    public List<SpendingInsight> getInsights(Long userId) {
        return insightRepository.findByUserUserIdOrderByGeneratedAtDesc(userId);
    }
}
