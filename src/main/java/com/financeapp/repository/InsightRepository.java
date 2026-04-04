package com.financeapp.repository;

import com.financeapp.model.SpendingInsight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface InsightRepository extends JpaRepository<SpendingInsight, Long> {

    List<SpendingInsight> findByUserUserIdOrderByGeneratedAtDesc(Long userId);

    @Modifying
    @Transactional
    void deleteByUserUserId(Long userId);
}
