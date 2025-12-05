package com.fraud.detection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

/**
 * 거래 분석 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAnalysisRequest {

    @NotNull(message = "거래 금액은 필수입니다")
    @Positive(message = "거래 금액은 양수여야 합니다")
    private Double amount;

    @NotNull
    @Min(0)
    private Integer transactionCount1h;

    @NotNull
    @Min(0)
    private Integer transactionCount24h;

    @NotNull
    @Min(0)
    private Integer differentMerchants24h;

    @NotNull
    @Positive
    private Double avgTransactionAmount;

    @NotNull
    @Min(0)
    private Double timeSinceLastTransaction;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer isWeekend;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer isNightTime;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double merchantRiskScore;

    @NotNull
    @Min(0)
    private Double cardAgeDays;

    @NotNull
    @Positive
    private Double transactionVelocity;

    @NotNull
    @Positive
    private Double amountDeviation;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer crossBorder;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer deviceChange;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer ipChange;
}
