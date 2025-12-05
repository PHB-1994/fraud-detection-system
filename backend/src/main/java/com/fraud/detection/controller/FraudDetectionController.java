package com.fraud.detection.controller;

import com.fraud.detection.dto.TransactionAnalysisRequest;
import com.fraud.detection.dto.TransactionAnalysisResponse;
import com.fraud.detection.entity.Transaction;
import com.fraud.detection.service.FraudDetectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 이상거래 탐지 REST API 컨트롤러
 * 
 * @author 박형빈
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/fraud-detection")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FraudDetectionController {

    private final FraudDetectionService fraudDetectionService;

    /**
     * 거래 분석 API
     * 
     * POST /api/fraud-detection/analyze
     * 
     * @param request 거래 데이터
     * @return 분석 결과
     */
    @PostMapping("/analyze")
    public ResponseEntity<TransactionAnalysisResponse> analyzeTransaction(
            @Valid @RequestBody TransactionAnalysisRequest request
    ) {
        log.info("거래 분석 요청 수신 - 금액: {}", request.getAmount());
        
        TransactionAnalysisResponse response = fraudDetectionService.analyzeTransaction(request);
        
        if (response.getIsFraud()) {
            log.warn("이상거래 탐지 - ID: {}, 확률: {}%",
                    response.getTransactionId(), 
                    response.getFraudProbability() * 100);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 이상거래 목록 조회
     * 
     * GET /api/fraud-detection/fraud-transactions
     * 
     * @return 이상거래 목록
     */
    @GetMapping("/fraud-transactions")
    public ResponseEntity<List<Transaction>> getFraudTransactions() {
        log.info("이상거래 목록 조회");
        List<Transaction> transactions = fraudDetectionService.getFraudTransactions();
        return ResponseEntity.ok(transactions);
    }

    /**
     * 특정 기간 이상거래 조회
     * 
     * GET /api/fraud-detection/fraud-transactions/period
     * 
     * @param start 시작 시간
     * @param end 종료 시간
     * @return 해당 기간 이상거래 목록
     */
    @GetMapping("/fraud-transactions/period")
    public ResponseEntity<List<Transaction>> getFraudTransactionsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        log.info("기간별 이상거래 조회 - {} ~ {}", start, end);
        List<Transaction> transactions = fraudDetectionService.getFraudTransactionsBetween(start, end);
        return ResponseEntity.ok(transactions);
    }

    /**
     * 통계 조회
     * 
     * GET /api/fraud-detection/statistics
     * 
     * @return 이상거래 통계
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("통계 조회");
        Map<String, Object> stats = fraudDetectionService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * 최근 거래 조회
     * 
     * GET /api/fraud-detection/recent-transactions
     * 
     * @return 최근 10건 거래
     */
    @GetMapping("/recent-transactions")
    public ResponseEntity<List<Transaction>> getRecentTransactions() {
        log.info("최근 거래 조회");
        List<Transaction> transactions = fraudDetectionService.getRecentTransactions();
        return ResponseEntity.ok(transactions);
    }

    /**
     * 높은 위험도 거래 조회
     * 
     * GET /api/fraud-detection/high-risk-transactions
     * 
     * @return 높은 위험도 거래 목록
     */
    @GetMapping("/high-risk-transactions")
    public ResponseEntity<List<Transaction>> getHighRiskTransactions() {
        log.info("높은 위험도 거래 조회");
        List<Transaction> transactions = fraudDetectionService.getHighRiskTransactions();
        return ResponseEntity.ok(transactions);
    }

    /**
     * 헬스체크
     * 
     * GET /api/fraud-detection/health
     * 
     * @return 상태 정보
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Fraud Detection API",
                "version", "1.0.0",
                "timestamp", LocalDateTime.now()
        ));
    }
}
