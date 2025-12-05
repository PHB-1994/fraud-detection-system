package com.fraud.detection.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ML API 응답 DTO
 * FastAPI 서버로부터 받는 예측 결과
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MLApiResponse {

    @JsonProperty("is_fraud")
    private Boolean isFraud;

    @JsonProperty("fraud_probability")
    private Double fraudProbability;

    @JsonProperty("risk_level")
    private String riskLevel;

    @JsonProperty("timestamp")
    private String timestamp;
}
