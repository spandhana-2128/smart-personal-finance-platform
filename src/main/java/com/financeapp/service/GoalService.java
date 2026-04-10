package com.financeapp.service;

import com.financeapp.model.Goal;
import com.financeapp.repository.GoalRepository;
import com.financeapp.service.observer.Observer;
import com.financeapp.service.observer.Subject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GoalService implements Subject {

    private final GoalRepository repo;
    private final List<Observer> observers = new ArrayList<>();

    public GoalService(GoalRepository repo) {
        this.repo = repo;
    }

    // ✅ Create Goal
    public Goal createGoal(Goal goal) {
        return repo.save(goal);
    }

    // ✅ Add Savings
    public Goal addSavings(Long id, double amount) {
        Goal goal = repo.findById(id).orElseThrow();

        goal.setSavedAmount(goal.getSavedAmount() + amount);

        // 🔥 Trigger notification
        if (goal.getSavedAmount() >= goal.getTargetAmount()) {
            notifyObservers("Goal achieved: " + goal.getName());
        }

        return repo.save(goal);
    }

    // 🔥 NEW METHOD (VERY IMPORTANT FOR TEAM INTEGRATION)
    public List<Goal> getGoalsByUser(Long userId) {
        return repo.findByUserUserId(userId);
    }

    // Observer methods
    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void notifyObservers(String message) {
        for (Observer o : observers) {
            o.update(message);
        }
    }

    public void deleteGoal(Long id) {
        repo.deleteById(id);
    }
}