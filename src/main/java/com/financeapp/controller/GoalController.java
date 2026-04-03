package com.financeapp.controller;

import com.financeapp.model.Goal;
import com.financeapp.service.GoalService;
import com.financeapp.service.NotificationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService, NotificationService notificationService) {
        this.goalService = goalService;
        goalService.addObserver(notificationService);
    }

    @PostMapping
    public Goal createGoal(@RequestBody Goal goal) {
        return goalService.createGoal(goal);
    }

    @PutMapping("/{id}/add")
    public Goal addSavings(@PathVariable Long id, @RequestParam double amount) {
        return goalService.addSavings(id, amount);
    }
}