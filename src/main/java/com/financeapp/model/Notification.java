package com.financeapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;
    private LocalDateTime timestamp;

    public Notification() {}

    public Notification(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public String getMessage() { return message; }

    public LocalDateTime getTimestamp() { return timestamp; }
}