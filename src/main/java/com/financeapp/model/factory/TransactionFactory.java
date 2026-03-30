package com.financeapp.model.factory;

import com.financeapp.model.*;
import java.time.LocalDate;

public class TransactionFactory {

    public static Transaction create(String type, User user, double amount,
                                     LocalDate date, String description,
                                     String currency, boolean isRecurring,
                                     String frequency, Category category,
                                     String source, String paymentMethod) {
        if ("INCOME".equalsIgnoreCase(type)) {
            return Income.builder()
                    .user(user).amount(amount).date(date)
                    .description(description).currency(currency)
                    .isRecurring(isRecurring).frequency(frequency)
                    .source(source)
                    .build();
        } else if ("EXPENSE".equalsIgnoreCase(type)) {
            return Expense.builder()
                    .user(user).amount(amount).date(date)
                    .description(description).currency(currency)
                    .isRecurring(isRecurring).frequency(frequency)
                    .category(category).paymentMethod(paymentMethod)
                    .build();
        }
        throw new IllegalArgumentException("Unknown transaction type: " + type);
    }
}