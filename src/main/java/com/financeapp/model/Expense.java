package com.financeapp.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("EXPENSE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Expense extends Transaction {

    private String paymentMethod;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Override
    public void recordTransaction() {
        // Handled by TransactionService
    }
}