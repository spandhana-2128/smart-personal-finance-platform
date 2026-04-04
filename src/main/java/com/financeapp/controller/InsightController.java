package com.financeapp.controller;

import com.financeapp.model.SpendingInsight;
import com.financeapp.service.InsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Spending Insights.
 *
 * Endpoints:
 *   POST /api/insights/generate/{userId}  — generate fresh insights
 *   GET  /api/insights/{userId}           — retrieve saved insights
 */
@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightController {

    private final InsightService insightService;

    /** Generate (or regenerate) insights for the given user. */
    @PostMapping("/generate/{userId}")
    public ResponseEntity<List<SpendingInsight>> generate(@PathVariable Long userId) {
        List<SpendingInsight> insights = insightService.generateInsights(userId);
        return ResponseEntity.ok(insights);
    }

    /** Retrieve the most recently generated insights for the given user. */
    @GetMapping("/{userId}")
    public ResponseEntity<List<SpendingInsight>> getInsights(@PathVariable Long userId) {
        return ResponseEntity.ok(insightService.getInsights(userId));
    }
}
