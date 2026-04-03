package com.financeapp.service.observer;

public interface Subject {
    void addObserver(Observer observer);
    void notifyObservers(String message);
}