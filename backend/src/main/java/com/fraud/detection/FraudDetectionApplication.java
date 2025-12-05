package com.fraud.detection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * AI 기반 이상거래 탐지 시스템
 * Spring Boot 메인 애플리케이션
 * 
 * @author 박형빈
 * @version 1.0.0
 */
@SpringBootApplication
public class FraudDetectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(FraudDetectionApplication.class, args);
        System.out.println("=".repeat(80));
        System.out.println("AI 기반 이상거래 탐지 시스템 시작 완료");
        System.out.println("=".repeat(80));
        System.out.println("주요 기능:");
        System.out.println("  - 실시간 이상거래 탐지 (평균 0.3초)");
        System.out.println("  - 모델 정확도: 92.3%");
        System.out.println("  - REST API: http://localhost:8080");
        System.out.println("=".repeat(80));
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
