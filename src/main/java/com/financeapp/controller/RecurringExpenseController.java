package com.financeapp.controller;

import com.financeapp.model.Transaction;
import com.financeapp.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Minor Use Case: Recurring Expense Automation
 *
 * Reuses the isRecurring flag already stored by Tanish's Transaction module.
 * Provides an endpoint to view all recurring transactions for a user,
 * which the UI displays so the user is aware of automatic charges.
 */
@RestController
@RequestMapping("/api/recurring")
@RequiredArgsConstructor
public class RecurringExpenseController {

    private final TransactionRepository transactionRepository;

    /** Get all recurring transactions for a user. */
    @GetMapping("/{userId}")
    public ResponseEntity<List<Transaction>> getRecurring(@PathVariable Long userId) {
        List<Transaction> recurring =
                transactionRepository.findByUserUserIdAndIsRecurringTrue(userId);
        return ResponseEntity.ok(recurring);
    }
}
