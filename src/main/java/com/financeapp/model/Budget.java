package com.financeapp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "budgets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long budgetId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Double monthlyLimit;
    private Double currentSpending;
    private String month;
    private int year;
    private String strategyType;

    @Transient
    public double getSpendingPercentage() {
        if (monthlyLimit == 0) return 0;
        return (currentSpending / monthlyLimit) * 100;
    }
}