package com.fraud.detection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 거래 분석 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAnalysisResponse {

    private Long transactionId;
    private Boolean isFraud;
    private Double fraudProbability;
    private String riskLevel;
    private String message;
    private LocalDateTime analyzedAt;

    /**
     * 성공 응답 생성
     */
    public static TransactionAnalysisResponse success(
            Long transactionId,
            Boolean isFraud,
            Double fraudProbability,
            String riskLevel
    ) {
        return TransactionAnalysisResponse.builder()
                .transactionId(transactionId)
                .isFraud(isFraud)
                .fraudProbability(fraudProbability)
                .riskLevel(riskLevel)
                .message(isFraud ? "이상거래가 탐지되었습니다" : "정상 거래입니다")
                .analyzedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 위험도 레벨 설명
     */
    public String getRiskLevelDescription() {
        switch (riskLevel) {
            case "LOW":
                return "낮은 위험도 (정상 거래 가능성 높음)";
            case "MEDIUM":
                return "중간 위험도 (추가 확인 필요)";
            case "HIGH":
                return "높은 위험도 (즉시 차단 권장)";
            default:
                return "알 수 없음";
        }
    }
}
