package com.fraud.detection.repository;

import com.fraud.detection.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 거래 데이터 Repository
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * 이상거래만 조회
     */
    List<Transaction> findByIsFraudTrue();

    /**
     * 위험도 레벨별 조회
     */
    List<Transaction> findByRiskLevel(String riskLevel);

    /**
     * 특정 기간 이상거래 조회
     */
    @Query("SELECT t FROM Transaction t WHERE t.isFraud = true " +
           "AND t.createdAt BETWEEN :startDate AND :endDate")
    List<Transaction> findFraudTransactionsBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 이상거래 통계
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.isFraud = true")
    Long countFraudTransactions();

    /**
     * 최근 거래 조회
     */
    List<Transaction> findTop10ByOrderByCreatedAtDesc();

    /**
     * 높은 위험도 거래 조회
     */
    @Query("SELECT t FROM Transaction t WHERE t.riskLevel = 'HIGH' " +
           "ORDER BY t.fraudProbability DESC")
    List<Transaction> findHighRiskTransactions();
}
