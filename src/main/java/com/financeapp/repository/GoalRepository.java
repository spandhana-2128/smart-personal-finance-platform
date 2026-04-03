package com.financeapp.repository;

import com.financeapp.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    // 🔥 Important for user-based queries
    List<Goal> findByUserUserId(Long userId);
}