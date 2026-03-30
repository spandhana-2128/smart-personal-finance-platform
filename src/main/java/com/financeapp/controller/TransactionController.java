package com.financeapp.controller;

import com.financeapp.model.Transaction;
import com.financeapp.service.CategoryService;
import com.financeapp.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final CategoryService categoryService;

    @PostMapping("/income")
    public ResponseEntity<Transaction> addIncome(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(transactionService.addIncome(
                Long.valueOf(body.get("userId").toString()),
                Double.parseDouble(body.get("amount").toString()),
                LocalDate.parse(body.get("date").toString()),
                body.get("description").toString(),
                body.getOrDefault("currency", "INR").toString(),
                body.get("source").toString(),
                Boolean.parseBoolean(body.getOrDefault("isRecurring", "false").toString()),
                body.getOrDefault("frequency", "").toString()
        ));
    }

    @PostMapping("/expense")
    public ResponseEntity<Transaction> addExpense(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(transactionService.addExpense(
                Long.valueOf(body.get("userId").toString()),
                Long.valueOf(body.get("categoryId").toString()),
                Double.parseDouble(body.get("amount").toString()),
                LocalDate.parse(body.get("date").toString()),
                body.get("description").toString(),
                body.getOrDefault("currency", "INR").toString(),
                body.getOrDefault("paymentMethod", "CASH").toString(),
                Boolean.parseBoolean(body.getOrDefault("isRecurring", "false").toString()),
                body.getOrDefault("frequency", "").toString()
        ));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getAll(@PathVariable Long userId) {
        return ResponseEntity.ok(transactionService.getByUser(userId));
    }

    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<Map<String, Double>> getSummary(@PathVariable Long userId) {
        return ResponseEntity.ok(transactionService.getExpenseByCategory(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
}