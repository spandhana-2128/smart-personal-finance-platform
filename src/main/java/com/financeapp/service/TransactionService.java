package com.financeapp.service;

import com.financeapp.model.Income;
import com.financeapp.model.Expense;
import com.financeapp.model.Transaction;
import com.financeapp.model.User;
import com.financeapp.model.Category;  // ← must be explicitly imported
import com.financeapp.model.factory.TransactionFactory;
import com.financeapp.repository.CategoryRepository;
import com.financeapp.repository.TransactionRepository;
import com.financeapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetService budgetService;

    public Transaction addIncome(Long userId, double amount, LocalDate date,
                                  String description, String currency,
                                  String source, boolean isRecurring, String frequency) {
        User user = userRepository.findById(userId).orElseThrow();
        Transaction t = TransactionFactory.create(
                "INCOME", user, amount, date, description,
                currency, isRecurring, frequency, null, source, null
        );
        t.recordTransaction();
        return transactionRepository.save(t);
    }

    public Transaction addExpense(Long userId, Long categoryId, double amount,
                                   LocalDate date, String description, String currency,
                                   String paymentMethod, boolean isRecurring, String frequency) {
        User user = userRepository.findById(userId).orElseThrow();
        Category category = categoryRepository.findById(categoryId).orElseThrow();
        Transaction t = TransactionFactory.create(
                "EXPENSE", user, amount, date, description,
                currency, isRecurring, frequency, category, null, paymentMethod
        );
        t.recordTransaction();
        Transaction saved = transactionRepository.save(t);
        budgetService.addSpendingByUserCurrentMonth(userId, amount);
        return saved;
    }

    public List<Transaction> getByUser(Long userId) {
        return transactionRepository.findByUserUserId(userId);
    }

    public List<Transaction> getIncomeByUser(Long userId) {
        return transactionRepository.findByUserUserIdAndType(userId, Income.class);
    }

    public List<Transaction> getExpenseByUser(Long userId) {
        return transactionRepository.findByUserUserIdAndType(userId, Expense.class);
    }

    // Called by insights module - returns category name → total spent
    public Map<String, Double> getExpenseByCategory(Long userId) {
        List<Transaction> expenses = transactionRepository
                .findByUserUserIdAndType(userId, Expense.class);
        return expenses.stream()
                .map(t -> (Expense) t)
                .filter(e -> e.getCategory() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().getCategoryName(),
                        Collectors.summingDouble(Transaction::getAmount)
                ));
    }

    // Called by budget module on merge - total expenses for current month
    public double getTotalExpensesForCurrentMonth(Long userId) {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = LocalDate.now();
        return transactionRepository
                .findByUserUserIdAndDateBetween(userId, start, end)
                .stream()
                .filter(t -> t instanceof Expense)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }
}