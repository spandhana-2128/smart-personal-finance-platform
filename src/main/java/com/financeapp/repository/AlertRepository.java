package com.financeapp.repository;

import com.financeapp.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByUserUserIdAndSeenFalse(Long userId);
}