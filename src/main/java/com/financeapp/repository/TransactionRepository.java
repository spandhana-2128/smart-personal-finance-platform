package com.financeapp.repository;

import com.financeapp.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserUserId(Long userId);

    @Query("SELECT t FROM Transaction t WHERE t.user.userId = :userId AND TYPE(t) = :type")
    List<Transaction> findByUserUserIdAndType(@Param("userId") Long userId,
                                              @Param("type") Class<?> type);

    List<Transaction> findByUserUserIdAndIsRecurringTrue(Long userId);

    @Query("SELECT t FROM Transaction t WHERE t.user.userId = :userId " +
           "AND t.date BETWEEN :start AND :end")
    List<Transaction> findByUserUserIdAndDateBetween(@Param("userId") Long userId,
                                                     @Param("start") LocalDate start,
                                                     @Param("end") LocalDate end);
}