package com.financeapp.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("INCOME")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Income extends Transaction {

    private String source;

    @Override
    public void recordTransaction() {
        // Handled by TransactionService
    }
}