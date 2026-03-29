package com.financeapp.service;

import com.financeapp.model.Alert;
import com.financeapp.model.User;
import com.financeapp.repository.AlertRepository;
import com.financeapp.repository.UserRepository;
import com.financeapp.service.strategy.BudgetingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AlertService implements BudgetAlertObserver {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final Map<String, BudgetingStrategy> strategies;

    @Override
    public void onBudgetUpdated(Long userId, double currentSpending,
                                double limit, String strategyType) {
        BudgetingStrategy strategy = strategies.get(strategyType);
        if (strategy == null) return;

        double pct = (currentSpending / limit) * 100;
        User user = userRepository.findById(userId).orElseThrow();

        if (pct >= 100) {
            saveAlert(user, "EXCEEDED! Spent Rs." + String.format("%.2f", currentSpending)
                    + " of Rs." + String.format("%.2f", limit) + " limit.", "EXCEEDED");
        } else if (pct >= strategy.getWarningThreshold()) {
            saveAlert(user, "Warning: " + String.format("%.1f", pct)
                    + "% of budget used.", "WARNING");
        }
    }

    private void saveAlert(User user, String message, String type) {
        alertRepository.save(Alert.builder()
                .user(user).message(message).alertType(type)
                .createdAt(LocalDateTime.now()).seen(false).build());
    }

    public List<Alert> getUnseenAlerts(Long userId) {
        return alertRepository.findByUserUserIdAndSeenFalse(userId);
    }

    public void markSeen(Long alertId) {
        alertRepository.findById(alertId).ifPresent(a -> {
            a.setSeen(true);
            alertRepository.save(a);
        });
    }
}