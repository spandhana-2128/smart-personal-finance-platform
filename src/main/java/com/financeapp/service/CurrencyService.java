package com.financeapp.service;

import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class CurrencyService {

    // Static rates relative to INR — no API key needed
    private static final Map<String, Double> RATES_FROM_INR = Map.of(
            "INR", 1.0,
            "USD", 0.012,
            "EUR", 0.011,
            "GBP", 0.0095,
            "JPY", 1.78,
            "AED", 0.044
    );

    public double convert(double amount, String fromCurrency, String toCurrency) {
        double inrAmount = amount / RATES_FROM_INR.getOrDefault(fromCurrency, 1.0);
        return inrAmount * RATES_FROM_INR.getOrDefault(toCurrency, 1.0);
    }

    public java.util.List<String> getSupportedCurrencies() {
        return new java.util.ArrayList<>(RATES_FROM_INR.keySet());
    }
}