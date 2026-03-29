package com.financeapp.controller;

import com.financeapp.model.Alert;
import com.financeapp.model.Budget;
import com.financeapp.service.AlertService;
import com.financeapp.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budget")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;
    private final AlertService alertService;

    @PostMapping("/create")
    public ResponseEntity<Budget> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(budgetService.createBudget(
                Long.valueOf(body.get("userId").toString()),
                Double.parseDouble(body.get("limit").toString()),
                body.get("month").toString(),
                Integer.parseInt(body.get("year").toString()),
                body.get("strategy").toString()
        ));
    }

    @PutMapping("/{id}/spend")
    public ResponseEntity<Budget> spend(@PathVariable Long id,
                                        @RequestParam double amount) {
        return ResponseEntity.ok(budgetService.addSpending(id, amount));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Budget>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(budgetService.getBudgetsByUser(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/suggest")
    public ResponseEntity<Map<String, Double>> suggest(
            @RequestParam String strategy, @RequestParam double income) {
        return ResponseEntity.ok(
                Map.of("limit", budgetService.getSuggestedLimit(strategy, income)));
    }

    @GetMapping("/alerts/{userId}")
    public ResponseEntity<List<Alert>> alerts(@PathVariable Long userId) {
        return ResponseEntity.ok(alertService.getUnseenAlerts(userId));
    }

    @PutMapping("/alerts/{alertId}/seen")
    public ResponseEntity<Void> markSeen(@PathVariable Long alertId) {
        alertService.markSeen(alertId);
        return ResponseEntity.ok().build();
    }
}