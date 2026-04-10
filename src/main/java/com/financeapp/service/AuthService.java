package com.financeapp.service;

import com.financeapp.model.User;
import com.financeapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public Optional<User> login(String email, String password) {
        return userRepository.findByEmailAndPassword(email, password);
    }

    public User register(String name, String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        return userRepository.save(User.builder()
                .name(name).email(email).password(password)
                .preferredCurrency("INR").role("USER")
                .build());
    }
}