package com.financeapp.service;

import com.financeapp.model.Notification;
import com.financeapp.repository.NotificationRepository;
import com.financeapp.service.observer.Observer;
import org.springframework.stereotype.Service;

@Service
public class NotificationService implements Observer {

    private final NotificationRepository repo;

    public NotificationService(NotificationRepository repo) {
        this.repo = repo;
    }

    @Override
    public void update(String message) {
        repo.save(new Notification(message));
    }
}