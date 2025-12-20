# AI 기반 실시간 이상거래 탐지 시스템 (FDS)

[![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Python](https://img.shields.io/badge/Python-3.11-3776AB?logo=python&logoColor=white)](https://www.python.org/)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.108-009688?logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)

Spring Boot와 Python(FastAPI)을 연동한 마이크로서비스 아키텍처 기반의 이상거래 탐지 시스템입니다. 결제 요청이 들어오면 학습된 Random Forest 모델이 실시간으로 사기(Fraud) 확률을 분석하여 응답합니다.

---

## 목차 (Table of Contents)

- [프로젝트 개요](#-프로젝트-개요)
- [주요 기능](#-주요-기능)
- [기술 스택](#️-기술-스택-tech-stack)
- [시스템 아키텍처](#️-시스템-아키텍처)
- [프로젝트 구조](#-프로젝트-구조)
- [실행 방법](#-실행-방법-getting-started)
- [API 명세서](#-api-명세서-endpoints)
- [ML 모델 상세](#-ml-모델-상세)
- [테스트 예시](#-테스트-예시)
- [모니터링 및 로그](#-모니터링-및-로그)
- [트러블슈팅](#-트러블슈팅)
- [개선 제안 및 향후 계획](#-개선-제안-및-향후-계획)

---

## 프로젝트 개요

### 프로젝트명
**AI 기반 실시간 이상거래 탐지 시스템 (Fraud Detection System)**

### 목적
결제 시스템에서 발생하는 이상거래를 머신러닝 모델을 통해 실시간으로 탐지하고, 위험도를 분석하여 사기 거래를 사전에 차단하는 시스템입니다.

### 주요 특징
- **실시간 분석**: 평균 0.3초 이내 응답
- **높은 정확도**: Random Forest 모델 기반 92.3% 정확도
- **마이크로서비스**: Spring Boot + FastAPI 분리 아키텍처
- **원클릭 배포**: Docker Compose 기반 자동화
- **데이터 저장**: MySQL 기반 거래 이력 및 분석 결과 저장

---

## 주요 기능

### 1. 실시간 거래 분석
- 15개 특성(Feature)을 기반으로 한 AI 예측
- 이상거래 확률 및 위험도 레벨 (LOW/MEDIUM/HIGH) 산정
- 거래 데이터 자동 저장 및 이력 관리

### 2. 이상거래 탐지
- 다양한 거래 패턴 분석 (시간대, 금액, 속도, 기기 변경 등)
- SMOTE 기법을 통한 불균형 데이터 처리
- 실시간 사기 확률 계산

### 3. 통계 및 대시보드
- 전체 거래 대비 이상거래 비율 통계
- 최근 거래 내역 조회
- 높은 위험도 거래 목록 확인
- 기간별 이상거래 조회

### 4. ML 모델 서빙
- FastAPI 기반 독립적인 ML 서비스
- 배치 예측 지원 (최대 1000건)
- 모델 정보 및 성능 지표 조회

---

## 기술 스택 (Tech Stack)

| 구분 | 기술 (Technology) | 버전 | 설명 |
|:--|:--|:--|:--|
| **Backend** | Java | 17 | 백엔드 언어 |
| | Spring Boot | 3.2 | REST API 서버, 비즈니스 로직 처리 |
| | Spring Data JPA | 3.2 | ORM, 데이터베이스 접근 계층 |
| | Lombok | - | 코드 간소화 |
| **ML Service** | Python | 3.11 | 머신러닝 서비스 언어 |
| | FastAPI | 0.108 | ML 모델 서빙 프레임워크 |
| | Scikit-learn | 1.3.2 | 머신러닝 모델 (Random Forest) |
| | Pandas | 2.1.4 | 데이터 처리 |
| | Imbalanced-learn | 0.11.0 | SMOTE 불균형 데이터 처리 |
| **Database** | MySQL | 8.0 | 거래 내역 및 탐지 결과 저장 |
| **Infrastructure** | Docker | - | 컨테이너화 |
| | Docker Compose | - | 멀티 컨테이너 오케스트레이션 |
| **Build Tools** | Gradle | - | Java 프로젝트 빌드 도구 |

---
```
 API
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     Spring Boot Backend (Port 8080)                 │
│  ┌───────────────┐  ┌──────────────┐  ┌───────────────────────┐     │
│  │  Controller   │→ │   Service    │→ │  TransactionRepo      │     │
│  │  (REST API)   │  │  (Business)  │  │  (JPA Repository)     │     │
│  └───────────────┘  └──────┬───────┘  └───────────┬───────────┘     │
└────────────────────────────┼─────────────────── ──┼─────────────────┘
                             │                      │
                             │ RestTemplate         │ JDBC
                             ▼                      ▼
    ┌────────────────────────────────┐   ┌─────────────────────────┐
    │   Python ML Service (8000)     │   │   MySQL Database        │
    │  ┌──────────────────────────┐  │   │  ┌───────────────────┐  │
    │  │  FastAPI Server          │  │   │  │  transactions     │  │
    │  │  - /api/predict          │  │   │  │  - id             │  │
    │  │  - /api/batch-predict    │  │   │  │  - amount         │  │
    │  │  - /api/model-info       │  │   │  │  - is_fraud       │  │
    │  └───────────┬──────────────┘  │   │  │  - fraud_prob     │  │
    │              │                 │   │  │  - created_at     │  │
    │              ▼                 │   │  └───────────────────┘  │
    │  ┌──────────────────────────┐  │   │                         │
    │  │  ML Model                │  │   └─────────────────────────┘
    │  │  - Random Forest         │  │
    │  │  - fraud_detection.pkl   │  │
    │  │  - model_metadata.json   │  │
    │  └──────────────────────────┘  │
    └────────────────────────────────┘
```

### 동작 흐름 (Flow)

1. **클라이언트 요청**: 사용자가 POST `/api/fraud-detection/analyze`로 거래 데이터 전송
2. **백엔드 처리**: Spring Boot Controller가 요청을 받아 Service로 전달
3. **ML 예측**: Service가 RestTemplate을 통해 Python ML API 호출
4. **모델 추론**: FastAPI가 Random Forest 모델을 사용해 이상거래 확률 계산
5. **응답 전달**: ML 결과를 백엔드로 반환
6. **데이터 저장**: 백엔드가 거래 정보와 예측 결과를 MySQL에 저장
7. **최종 응답**: 클라이언트에게 분석 결과 반환

---

## 프로젝트 구조

```bash
fraud-detection-system/
├── backend/                           # Spring Boot 백엔드 소스
│   ├── src/main/
│   │   ├── java/com/fraud/detection/
│   │   │   ├── controller/
│   │   │   │   └── FraudDetectionController.java     # REST API 컨트롤러
│   │   │   ├── dto/
│   │   │   │   ├── TransactionAnalysisRequest.java   # 요청 DTO
│   │   │   │   ├── TransactionAnalysisResponse.java  # 응답 DTO
│   │   │   │   └── MLApiResponse.java                # ML API 응답 DTO
│   │   │   ├── entity/
│   │   │   │   └── Transaction.java                  # 거래 엔티티
│   │   │   ├── repository/
│   │   │   │   └── TransactionRepository.java        # JPA Repository
│   │   │   ├── service/
│   │   │   │   └── FraudDetectionService.java        # 비즈니스 로직
│   │   │   └── FraudDetectionApplication.java        # Spring Boot 메인
│   │   └── resources/
│   │       └── application.yml                       # Spring 설정 파일
│   ├── build.gradle                                  # Gradle 빌드 설정
│   └── Dockerfile.backend                            # 백엔드 Docker 이미지
│
├── ml-service/                        # Python ML 서비스 소스
│   ├── ml_api.py                                     # FastAPI 서버 코드
│   ├── train_model.py                                # 모델 학습 스크립트
│   ├── requirements.txt                              # Python 의존성
│   └── Dockerfile.ml                                 # ML 서버 Docker 이미지
│
├── docker-compose.yml                 # Docker Compose 설정
├── README.md                          # 프로젝트 설명서 (본 문서)
└── .gitignore                         # Git 제외 파일 목록

```

---

## 실행 방법 (Getting Started)

### 사전 요구 사항 (Prerequisites)

시스템 실행을 위해 다음 소프트웨어가 설치되어 있어야 합니다:

- **Docker Desktop**: [다운로드](https://www.docker.com/products/docker-desktop/)
- **Docker Compose**: Docker Desktop에 포함되어 있음

> **참고**: Java, Python, MySQL을 별도로 설치할 필요 **없습니다**. Docker가 모든 것을 처리합니다.

---

### 1. 프로젝트 클론 (Clone)

```bash
git clone https://github.com/PHB-1994/fraud-detection-system.git
cd fraud-detection-system
```

---

### 2. Docker Compose 실행

프로젝트 루트 디렉토리에서 다음 명령어를 실행합니다:

```bash
docker-compose up --build -d
```

**명령어 설명**:
- `--build`: Docker 이미지를 새로 빌드
- `-d`: 백그라운드에서 실행 (Detached mode)

**최초 실행 시**: ML 모델 학습 및 Docker 이미지 빌드로 인해 **1~2분** 소요될 수 있습니다.

---

### 3. 실행 상태 확인

다음 명령어로 모든 서비스가 정상적으로 실행 중인지 확인합니다:

```bash
docker-compose ps
```

**예상 출력**:
```
NAME                          STATUS
fraud-detection-backend       Up (healthy)
fraud-detection-ml-api        Up
fraud-detection-mysql         Up (healthy)
```

모든 서비스의 상태가 `Up` 또는 `Up (healthy)`이면 정상입니다.

---

### 4. 서비스 접속 확인

#### 백엔드 API (Spring Boot)
브라우저에서 다음 URL로 접속:
```
http://localhost:8080/api/fraud-detection/health
```

**예상 응답**:
```json
{
  "status": "UP",
  "service": "Fraud Detection API",
  "version": "1.0.0",
  "timestamp": "2024-XX-XXTXX:XX:XX"
}
```

#### ML API (FastAPI)
브라우저에서 Swagger UI 문서 페이지 접속:
```
http://localhost:8000/docs
```

파란색/초록색 버튼이 있는 **Swagger UI** 페이지가 표시되면 정상입니다.

#### 데이터베이스 (MySQL)
MySQL 클라이언트 또는 DBeaver 등으로 접속:
- **Host**: `localhost`
- **Port**: `3307` (주의: 3306이 아닌 3307)
- **Username**: `root`
- **Password**: `fraud_password`
- **Database**: `fraud_detection`

---

### 로그 확인 (실시간 모니터링)

실시간으로 서비스 로그를 보려면:

```bash
docker-compose logs -f
```

특정 서비스만 보려면:
```bash
docker-compose logs -f backend      # 백엔드만
docker-compose logs -f ml-api       # ML 서비스만
docker-compose logs -f mysql        # DB만
```

종료: `Ctrl + C`

---

### 서비스 종료

```bash
docker-compose down
```

데이터베이스 볼륨까지 삭제하려면:
```bash
docker-compose down -v
```

---

## API 명세서 (Endpoints)

### Backend API (Port 8080)

#### 1. 거래 분석 (단일)
**실시간으로 거래를 분석하고 결과를 저장합니다.**

- **Endpoint**: `POST /api/fraud-detection/analyze`
- **요청 본문** (JSON):
```json
{
  "amount": 75000,
  "transactionCount1h": 2,
  "transactionCount24h": 5,
  "differentMerchants24h": 3,
  "avgTransactionAmount": 50000,
  "timeSinceLastTransaction": 3600,
  "isWeekend": 0,
  "isNightTime": 0,
  "merchantRiskScore": 0.25,
  "cardAgeDays": 365,
  "transactionVelocity": 1.5,
  "amountDeviation": 0.8,
  "crossBorder": 0,
  "deviceChange": 0,
  "ipChange": 0
}
```

- **응답** (JSON):
```json
{
  "transactionId": 1,
  "isFraud": false,
  "fraudProbability": 0.12,
  "riskLevel": "LOW",
  "message": "정상 거래입니다",
  "analyzedAt": "2024-12-15T14:30:00"
}
```

---

#### 2. 이상거래 목록 조회
**탐지된 이상거래 목록을 반환합니다.**

- **Endpoint**: `GET /api/fraud-detection/fraud-transactions`
- **응답**: Transaction 배열

---

#### 3. 기간별 이상거래 조회
**특정 기간 동안의 이상거래를 조회합니다.**

- **Endpoint**: `GET /api/fraud-detection/fraud-transactions/period`
- **Query Parameters**:
  - `start`: 시작 시간 (ISO-8601 형식, 예: `2024-12-01T00:00:00`)
  - `end`: 종료 시간 (ISO-8601 형식)
- **예시**:
```
GET /api/fraud-detection/fraud-transactions/period?start=2024-12-01T00:00:00&end=2024-12-15T23:59:59
```

---

#### 4. 통계 조회
**전체 거래 대비 이상거래 통계를 반환합니다.**

- **Endpoint**: `GET /api/fraud-detection/statistics`
- **응답**:
```json
{
  "total_transactions": 1000,
  "fraud_transactions": 50,
  "fraud_rate": "5.00%",
  "normal_transactions": 950
}
```

---

#### 5. 최근 거래 조회
**최근 10건의 거래 내역을 반환합니다.**

- **Endpoint**: `GET /api/fraud-detection/recent-transactions`
- **응답**: Transaction 배열 (최대 10개)

---

#### 6. 높은 위험도 거래 조회
**위험도가 HIGH인 거래 목록을 반환합니다.**

- **Endpoint**: `GET /api/fraud-detection/high-risk-transactions`
- **응답**: Transaction 배열 (위험도 HIGH, 확률 내림차순)

---

### ML Service API (Port 8000)

#### 1. 모델 예측 (단일)
**순수 ML 예측만 수행합니다 (DB 저장 없음).**

- **Endpoint**: `POST /api/predict`
- **요청/응답**: Backend API의 `analyze`와 동일하지만 DB 저장은 하지 않음

---

#### 2. 배치 예측
**한 번에 여러 건의 거래를 예측합니다 (최대 1000건).**

- **Endpoint**: `POST /api/batch-predict`
- **요청**:
```json
{
  "transactions": [
    { /* transaction 1 */ },
    { /* transaction 2 */ },
    ...
  ]
}
```

---

#### 3. 모델 정보 조회
**학습된 모델의 성능 지표를 반환합니다.**

- **Endpoint**: `GET /api/model-info`
- **응답**:
```json
{
  "model_type": "RandomForestClassifier",
  "training_date": "2024-12-15T10:00:00",
  "performance": {
    "accuracy": 0.923,
    "precision": 0.897,
    "recall": 0.856,
    "f1_score": 0.876,
    "auc_roc": 0.95
  },
  "parameters": {
    "n_estimators": 200,
    "max_depth": 20
  }
}
```

---

#### 4. 헬스체크
**ML 서비스의 상태를 확인합니다.**

- **Endpoint**: `GET /health`
- **응답**:
```json
{
  "status": "healthy",
  "model_loaded": true,
  "model_version": "2024-12-15T10:00:00",
  "timestamp": "2024-12-15T14:30:00"
}
```

---

## ML 모델 상세

### 모델 알고리즘
**Random Forest Classifier** (Scikit-learn)

### 모델 하이퍼파라미터
- `n_estimators`: 200 (트리 개수)
- `max_depth`: 20 (최대 깊이)
- `min_samples_split`: 5
- `min_samples_leaf`: 2
- `random_state`: 42

### 학습 데이터
- **데이터 크기**: 10,000건 (합성 데이터)
- **정상 거래**: 9,500건 (95%)
- **이상 거래**: 500건 (5%)
- **Train/Test 비율**: 80% / 20%

### 불균형 데이터 처리
**SMOTE (Synthetic Minority Over-sampling Technique)** 적용
- 소수 클래스(이상거래)를 오버샘플링하여 클래스 불균형 해소

### 모델 성능 지표
| 지표 | 값 |
|:--|:--|
| **Accuracy** | 92.3% |
| **Precision** | 89.7% |
| **Recall** | 85.6% |
| **F1-Score** | 87.6% |
| **AUC-ROC** | 0.95 |

### 주요 특성 (Features) - 총 15개

| 순위 | 특성명 | 설명 | 중요도 |
|:--:|:--|:--|:--|
| 1 | `amount` | 거래 금액 | ⭐⭐⭐⭐⭐ |
| 2 | `merchant_risk_score` | 가맹점 위험도 점수 (0~1) | ⭐⭐⭐⭐ |
| 3 | `amount_deviation` | 평균 대비 금액 편차 | ⭐⭐⭐⭐ |
| 4 | `transaction_velocity` | 거래 속도 지표 | ⭐⭐⭐ |
| 5 | `transaction_count_1h` | 1시간 내 거래 횟수 | ⭐⭐⭐ |
| 6 | `card_age_days` | 카드 사용 일수 | ⭐⭐ |
| 7 | `cross_border` | 해외 거래 여부 (0/1) | ⭐⭐ |
| 8 | `is_night_time` | 야간 시간대 여부 (0/1) | ⭐⭐ |
| 9 | `ip_change` | IP 주소 변경 여부 (0/1) | ⭐ |
| 10 | `device_change` | 기기 변경 여부 (0/1) | ⭐ |
| ... | ... | ... | ... |

---

## 테스트 예시

### 1. Swagger UI를 통한 테스트 (가장 쉬움)

1. 브라우저에서 `http://localhost:8000/docs` 접속
2. `POST /api/predict` 클릭
3. **Try it out** 버튼 클릭
4. 아래 샘플 데이터를 Request body에 붙여넣기:

#### 정상 거래 샘플
```json
{
  "amount": 50000,
  "transaction_count_1h": 1,
  "transaction_count_24h": 3,
  "different_merchants_24h": 2,
  "avg_transaction_amount": 45000,
  "time_since_last_transaction": 7200,
  "is_weekend": 0,
  "is_night_time": 0,
  "merchant_risk_score": 0.15,
  "card_age_days": 500,
  "transaction_velocity": 1.2,
  "amount_deviation": 0.5,
  "cross_border": 0,
  "device_change": 0,
  "ip_change": 0
}
```

**예상 결과**: `"is_fraud": false`, `"risk_level": "LOW"`

---

#### 이상 거래 샘플
```json
{
  "amount": 750000,
  "transaction_count_1h": 10,
  "transaction_count_24h": 25,
  "different_merchants_24h": 15,
  "avg_transaction_amount": 50000,
  "time_since_last_transaction": 60,
  "is_weekend": 0,
  "is_night_time": 1,
  "merchant_risk_score": 0.8,
  "card_age_days": 30,
  "transaction_velocity": 5.5,
  "amount_deviation": 8.0,
  "cross_border": 1,
  "device_change": 1,
  "ip_change": 1
}
```

**예상 결과**: `"is_fraud": true`, `"risk_level": "HIGH"`, `"fraud_probability": 0.85+`

---

### 2. cURL을 통한 테스트

```bash
curl -X POST "http://localhost:8080/api/fraud-detection/analyze" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 750000,
    "transactionCount1h": 10,
    "transactionCount24h": 25,
    "differentMerchants24h": 15,
    "avgTransactionAmount": 50000,
    "timeSinceLastTransaction": 60,
    "isWeekend": 0,
    "isNightTime": 1,
    "merchantRiskScore": 0.8,
    "cardAgeDays": 30,
    "transactionVelocity": 5.5,
    "amountDeviation": 8.0,
    "crossBorder": 1,
    "deviceChange": 1,
    "ipChange": 1
  }'
```

---

### 3. Postman을 통한 테스트

1. Postman 실행
2. **New Request** → **POST** 선택
3. URL: `http://localhost:8080/api/fraud-detection/analyze`
4. **Body** → **raw** → **JSON** 선택
5. 위의 샘플 JSON 데이터 붙여넣기
6. **Send** 클릭

---

## 모니터링 및 로그

### 실시간 로그 확인

```bash
# 전체 서비스 로그
docker-compose logs -f

# 백엔드만
docker-compose logs -f backend

# ML 서비스만
docker-compose logs -f ml-api

# MySQL만
docker-compose logs -f mysql
```

### 주요 로그 메시지

#### 정상 실행 로그
```
[Backend]  AI 기반 이상거래 탐지 시스템 시작 완료
[ML API]   ✓ 모델 로드 완료 (정확도: 92.3%)
[MySQL]    mysqld: ready for connections
```

#### 거래 분석 로그
```
[Backend]  거래 분석 요청 수신 - 금액: 750000
[ML API]   예측 완료 - 이상거래: True, 확률: 0.856
[Backend]  거래 저장 완료 - ID: 1
```

#### 이상거래 탐지 로그
```
[Backend]  이상거래 탐지 - ID: 1, 확률: 85.6%
```

---

## 트러블슈팅

### 문제 1: "Port already in use" 오류

**증상**:
```
Error: bind: address already in use
```

**원인**: 3306, 8000, 8080 포트가 이미 다른 프로그램에서 사용 중

**해결 방법**:
1. 사용 중인 포트 확인:
   ```bash
   # Windows
   netstat -ano | findstr :8080
   
   # Mac/Linux
   lsof -i :8080
   ```

2. 해당 프로세스 종료 후 재시작
   
   **또는**
   
3. `docker-compose.yml`에서 포트 변경:
   ```yaml
   ports:
     - "8081:8080"  # 8080 대신 8081 사용
   ```

---

### 문제 2: ML 모델이 로드되지 않음

**증상**:
```
모델이 로드되지 않았습니다 (503 Service Unavailable)
```

**해결 방법**:
1. ML 컨테이너 재시작:
   ```bash
   docker-compose restart ml-api
   ```

2. ML 컨테이너 로그 확인:
   ```bash
   docker-compose logs ml-api
   ```

3. 모델 파일 확인:
   ```bash
   docker exec -it fraud-detection-ml-api ls -la
   # fraud_detection_model.pkl 파일이 있는지 확인
   ```

---

### 문제 3: 데이터베이스 연결 실패

**증상**:
```
Could not connect to MySQL
```

**해결 방법**:
1. MySQL 컨테이너 상태 확인:
   ```bash
   docker-compose ps mysql
   ```

2. MySQL이 `healthy` 상태인지 확인:
   ```bash
   docker-compose logs mysql | grep "ready for connections"
   ```

3. 전체 재시작:
   ```bash
   docker-compose down
   docker-compose up -d
   ```

---

### 문제 4: Docker 이미지 빌드 실패

**해결 방법**:
```bash
# 캐시 없이 완전히 새로 빌드
docker-compose build --no-cache

# 기존 이미지/컨테이너 모두 제거 후 재시작
docker-compose down
docker system prune -a
docker-compose up --build -d
```

---

## 개선 제안 및 향후 계획

### 코드 품질 개선 사항

#### 1. 금액 데이터 타입 변경
**현황**: `Double` 사용  
**제안**: 금융 도메인에서는 `BigDecimal` 사용 권장 (부동소수점 오차 방지)

```java
// 현재
private Double amount;

// 개선안
private BigDecimal amount;
```

---

#### 2. RestTemplate 타임아웃 설정
**현황**: 타임아웃 미설정  
**제안**: ML 서버 무응답 시 백엔드 무한 대기 방지

```java
@Bean
public RestTemplate restTemplate() {
    HttpComponentsClientHttpRequestFactory factory = 
        new HttpComponentsClientHttpRequestFactory();
    factory.setConnectTimeout(5000);  // 5초
    factory.setReadTimeout(5000);     // 5초
    return new RestTemplate(factory);
}
```

---

#### 3. 보안 강화
**현황**:
- DB 비밀번호 하드코딩
- CORS `origins = "*"` (모든 도메인 허용)

**제안**:
```yaml
# .env 파일로 분리
MYSQL_ROOT_PASSWORD=your_secure_password
```

```java
// CORS 특정 도메인만 허용
@CrossOrigin(origins = "https://yourdomain.com")
```

---

## Acknowledgments

- Scikit-learn 커뮤니티
- FastAPI 프레임워크
- Spring Boot 생태계
- Docker 및 오픈소스 커뮤니티
