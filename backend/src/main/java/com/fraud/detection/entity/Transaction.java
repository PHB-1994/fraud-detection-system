package com.fraud.detection.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 거래 엔티티
 * 이상거래 탐지 결과를 데이터베이스에 저장
 */
@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "transaction_count_1h")
    private Integer transactionCount1h;

    @Column(name = "transaction_count_24h")
    private Integer transactionCount24h;

    @Column(name = "different_merchants_24h")
    private Integer differentMerchants24h;

    @Column(name = "avg_transaction_amount")
    private Double avgTransactionAmount;

    @Column(name = "time_since_last_transaction")
    private Double timeSinceLastTransaction;

    @Column(name = "is_weekend")
    private Integer isWeekend;

    @Column(name = "is_night_time")
    private Integer isNightTime;

    @Column(name = "merchant_risk_score")
    private Double merchantRiskScore;

    @Column(name = "card_age_days")
    private Double cardAgeDays;

    @Column(name = "transaction_velocity")
    private Double transactionVelocity;

    @Column(name = "amount_deviation")
    private Double amountDeviation;

    @Column(name = "cross_border")
    private Integer crossBorder;

    @Column(name = "device_change")
    private Integer deviceChange;

    @Column(name = "ip_change")
    private Integer ipChange;

    // AI 예측 결과
    @Column(name = "is_fraud")
    private Boolean isFraud;

    @Column(name = "fraud_probability")
    private Double fraudProbability;

    @Column(name = "risk_level")
    private String riskLevel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
