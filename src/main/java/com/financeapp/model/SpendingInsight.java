package com.financeapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "spending_insights")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpendingInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long insightId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // e.g. "TOP_CATEGORY", "MONTHLY_TREND", "SAVINGS_RATE"
    private String insightType;

    // Human-readable insight text
    @Column(length = 512)
    private String message;

    // Supporting numeric value (e.g. percentage, amount)
    private Double value;

    private LocalDateTime generatedAt;
}
