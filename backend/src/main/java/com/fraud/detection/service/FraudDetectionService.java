package com.fraud.detection.service;

import com.fraud.detection.dto.MLApiResponse;
import com.fraud.detection.dto.TransactionAnalysisRequest;
import com.fraud.detection.dto.TransactionAnalysisResponse;
import com.fraud.detection.entity.Transaction;
import com.fraud.detection.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 이상거래 탐지 서비스
 * ML API를 호출하여 거래를 분석하고 결과를 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;

    @Value("${ml.api.url:http://localhost:8000}")
    private String mlApiUrl;

    /**
     * 거래 분석 (단일)
     * 
     * @param request 거래 데이터
     * @return 분석 결과
     */
    @Transactional
    public TransactionAnalysisResponse analyzeTransaction(TransactionAnalysisRequest request) {
        log.info("거래 분석 시작 - 금액: {}", request.getAmount());

        try {
            // 1. ML API 호출
            long startTime = System.currentTimeMillis();
            MLApiResponse mlResponse = callMLApi(request);
            long elapsedTime = System.currentTimeMillis() - startTime;

            log.info("ML API 응답 완료 - 소요 시간: {}ms, 이상거래: {}, 확률: {}",
                    elapsedTime, mlResponse.getIsFraud(), mlResponse.getFraudProbability());

            // 2. 거래 엔티티 생성 및 저장
            Transaction transaction = buildTransaction(request, mlResponse);
            Transaction savedTransaction = transactionRepository.save(transaction);

            log.info("거래 저장 완료 - ID: {}", savedTransaction.getId());

            // 3. 응답 생성
            return TransactionAnalysisResponse.success(
                    savedTransaction.getId(),
                    mlResponse.getIsFraud(),
                    mlResponse.getFraudProbability(),
                    mlResponse.getRiskLevel()
            );

        } catch (Exception e) {
            log.error("거래 분석 중 오류 발생", e);
            throw new RuntimeException("거래 분석 실패: " + e.getMessage(), e);
        }
    }

    /**
     * ML API 호출
     */
    private MLApiResponse callMLApi(TransactionAnalysisRequest request) {
        String url = mlApiUrl + "/api/predict";

        // 요청 바디 생성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("amount", request.getAmount());
        requestBody.put("transaction_count_1h", request.getTransactionCount1h());
        requestBody.put("transaction_count_24h", request.getTransactionCount24h());
        requestBody.put("different_merchants_24h", request.getDifferentMerchants24h());
        requestBody.put("avg_transaction_amount", request.getAvgTransactionAmount());
        requestBody.put("time_since_last_transaction", request.getTimeSinceLastTransaction());
        requestBody.put("is_weekend", request.getIsWeekend());
        requestBody.put("is_night_time", request.getIsNightTime());
        requestBody.put("merchant_risk_score", request.getMerchantRiskScore());
        requestBody.put("card_age_days", request.getCardAgeDays());
        requestBody.put("transaction_velocity", request.getTransactionVelocity());
        requestBody.put("amount_deviation", request.getAmountDeviation());
        requestBody.put("cross_border", request.getCrossBorder());
        requestBody.put("device_change", request.getDeviceChange());
        requestBody.put("ip_change", request.getIpChange());

        // API 호출
        return restTemplate.postForObject(url, requestBody, MLApiResponse.class);
    }

    /**
     * Transaction 엔티티 빌드
     */
    private Transaction buildTransaction(TransactionAnalysisRequest request, MLApiResponse mlResponse) {
        return Transaction.builder()
                .amount(request.getAmount())
                .transactionCount1h(request.getTransactionCount1h())
                .transactionCount24h(request.getTransactionCount24h())
                .differentMerchants24h(request.getDifferentMerchants24h())
                .avgTransactionAmount(request.getAvgTransactionAmount())
                .timeSinceLastTransaction(request.getTimeSinceLastTransaction())
                .isWeekend(request.getIsWeekend())
                .isNightTime(request.getIsNightTime())
                .merchantRiskScore(request.getMerchantRiskScore())
                .cardAgeDays(request.getCardAgeDays())
                .transactionVelocity(request.getTransactionVelocity())
                .amountDeviation(request.getAmountDeviation())
                .crossBorder(request.getCrossBorder())
                .deviceChange(request.getDeviceChange())
                .ipChange(request.getIpChange())
                .isFraud(mlResponse.getIsFraud())
                .fraudProbability(mlResponse.getFraudProbability())
                .riskLevel(mlResponse.getRiskLevel())
                .build();
    }

    /**
     * 이상거래 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Transaction> getFraudTransactions() {
        return transactionRepository.findByIsFraudTrue();
    }

    /**
     * 특정 기간 이상거래 조회
     */
    @Transactional(readOnly = true)
    public List<Transaction> getFraudTransactionsBetween(LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findFraudTransactionsBetween(start, end);
    }

    /**
     * 통계 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics() {
        long totalCount = transactionRepository.count();
        long fraudCount = transactionRepository.countFraudTransactions();
        double fraudRate = totalCount > 0 ? (double) fraudCount / totalCount * 100 : 0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("total_transactions", totalCount);
        stats.put("fraud_transactions", fraudCount);
        stats.put("fraud_rate", String.format("%.2f%%", fraudRate));
        stats.put("normal_transactions", totalCount - fraudCount);

        return stats;
    }

    /**
     * 최근 거래 조회
     */
    @Transactional(readOnly = true)
    public List<Transaction> getRecentTransactions() {
        return transactionRepository.findTop10ByOrderByCreatedAtDesc();
    }

    /**
     * 높은 위험도 거래 조회
     */
    @Transactional(readOnly = true)
    public List<Transaction> getHighRiskTransactions() {
        return transactionRepository.findHighRiskTransactions();
    }
}
