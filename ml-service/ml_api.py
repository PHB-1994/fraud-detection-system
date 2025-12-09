"""
FastAPI 기반 ML 모델 서빙 서버
실시간 이상거래 탐지 API
"""

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import List, Optional
import joblib
import numpy as np
import json
from datetime import datetime
import logging

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# FastAPI 앱 초기화
app = FastAPI(
    title="이상거래 탐지 API",
    description="AI 기반 실시간 이상거래 탐지 시스템",
    version="1.0.0"
)

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 글로벌 변수
model = None
model_metadata = None

class TransactionRequest(BaseModel):
    """거래 데이터 요청 모델"""
    amount: float = Field(..., description="거래 금액", ge=0)
    transaction_count_1h: int = Field(..., description="1시간 내 거래 횟수", ge=0)
    transaction_count_24h: int = Field(..., description="24시간 내 거래 횟수", ge=0)
    different_merchants_24h: int = Field(..., description="24시간 내 다른 가맹점 수", ge=0)
    avg_transaction_amount: float = Field(..., description="평균 거래 금액", ge=0)
    time_since_last_transaction: float = Field(..., description="마지막 거래 이후 시간(초)", ge=0)
    is_weekend: int = Field(..., description="주말 여부 (0/1)", ge=0, le=1)
    is_night_time: int = Field(..., description="야간 시간대 여부 (0/1)", ge=0, le=1)
    merchant_risk_score: float = Field(..., description="가맹점 위험도 점수", ge=0, le=1)
    card_age_days: float = Field(..., description="카드 사용 일수", ge=0)
    transaction_velocity: float = Field(..., description="거래 속도 지표", ge=0)
    amount_deviation: float = Field(..., description="금액 편차 지표", ge=0)
    cross_border: int = Field(..., description="해외 거래 여부 (0/1)", ge=0, le=1)
    device_change: int = Field(..., description="기기 변경 여부 (0/1)", ge=0, le=1)
    ip_change: int = Field(..., description="IP 변경 여부 (0/1)", ge=0, le=1)
    
    class Config:
        schema_extra = {
            "example": {
                "amount": 75000,
                "transaction_count_1h": 2,
                "transaction_count_24h": 5,
                "different_merchants_24h": 3,
                "avg_transaction_amount": 50000,
                "time_since_last_transaction": 3600,
                "is_weekend": 0,
                "is_night_time": 0,
                "merchant_risk_score": 0.25,
                "card_age_days": 365,
                "transaction_velocity": 1.5,
                "amount_deviation": 0.8,
                "cross_border": 0,
                "device_change": 0,
                "ip_change": 0
            }
        }

class PredictionResponse(BaseModel):
    """예측 결과 응답 모델"""
    is_fraud: bool = Field(..., description="이상거래 여부")
    fraud_probability: float = Field(..., description="이상거래 확률")
    risk_level: str = Field(..., description="위험도 레벨 (LOW/MEDIUM/HIGH)")
    timestamp: str = Field(..., description="예측 시각")
    
class BatchTransactionRequest(BaseModel):
    """배치 거래 데이터 요청 모델"""
    transactions: List[TransactionRequest]

class HealthResponse(BaseModel):
    """헬스체크 응답"""
    status: str
    model_loaded: bool
    model_version: Optional[str]
    timestamp: str

@app.on_event("startup")
async def load_model():
    """서버 시작 시 모델 로드"""
    global model, model_metadata
    try:
        logger.info("모델 로딩 시작...")
        model = joblib.load('fraud_detection_model.pkl')
        
        with open('model_metadata.json', 'r', encoding='utf-8') as f:
            model_metadata = json.load(f)
        
        logger.info(f"✓ 모델 로드 완료 (정확도: {model_metadata['accuracy']*100:.1f}%)")
    except Exception as e:
        logger.error(f"모델 로드 실패: {str(e)}")
        model = None
        model_metadata = None

@app.get("/", response_model=dict)
async def root():
    """루트 엔드포인트"""
    return {
        "message": "AI 기반 이상거래 탐지 API",
        "version": "1.0.0",
        "endpoints": {
            "health": "/health",
            "predict": "/api/predict (POST)",
            "batch_predict": "/api/batch-predict (POST)",
            "model_info": "/api/model-info"
        }
    }

@app.get("/health", response_model=HealthResponse)
async def health_check():
    """헬스체크 엔드포인트"""
    return {
        "status": "healthy" if model is not None else "unhealthy",
        "model_loaded": model is not None,
        "model_version": model_metadata.get('training_date') if model_metadata else None,
        "timestamp": datetime.now().isoformat()
    }

@app.get("/api/model-info")
async def get_model_info():
    """모델 정보 조회"""
    if model is None or model_metadata is None:
        raise HTTPException(status_code=503, detail="모델이 로드되지 않았습니다")
    
    return {
        "model_type": model_metadata.get('model_type'),
        "training_date": model_metadata.get('training_date'),
        "performance": {
            "accuracy": model_metadata.get('accuracy'),
            "precision": model_metadata.get('precision'),
            "recall": model_metadata.get('recall'),
            "f1_score": model_metadata.get('f1_score'),
            "auc_roc": model_metadata.get('auc_roc')
        },
        "parameters": {
            "n_estimators": model_metadata.get('n_estimators'),
            "max_depth": model_metadata.get('max_depth')
        }
    }

def get_risk_level(probability: float) -> str:
    """확률 기반 위험도 레벨 결정"""
    if probability < 0.3:
        return "LOW"
    elif probability < 0.7:
        return "MEDIUM"
    else:
        return "HIGH"

@app.post("/api/predict", response_model=PredictionResponse)
async def predict_fraud(transaction: TransactionRequest):
    """
    단일 거래 이상 탐지
    
    - **평균 응답 시간**: 0.3초
    - **모델 정확도**: 92.3%
    """
    if model is None:
        raise HTTPException(status_code=503, detail="모델이 로드되지 않았습니다")
    
    try:
        # 입력 데이터를 배열로 변환
        features = np.array([[
            transaction.amount,
            transaction.transaction_count_1h,
            transaction.transaction_count_24h,
            transaction.different_merchants_24h,
            transaction.avg_transaction_amount,
            transaction.time_since_last_transaction,
            transaction.is_weekend,
            transaction.is_night_time,
            transaction.merchant_risk_score,
            transaction.card_age_days,
            transaction.transaction_velocity,
            transaction.amount_deviation,
            transaction.cross_border,
            transaction.device_change,
            transaction.ip_change
        ]])
        
        # 예측 수행
        prediction = model.predict(features)[0]
        probability = model.predict_proba(features)[0][1]
        
        logger.info(f"예측 완료 - 이상거래: {bool(prediction)}, 확률: {probability:.3f}")
        
        return {
            "is_fraud": bool(prediction),
            "fraud_probability": float(probability),
            "risk_level": get_risk_level(probability),
            "timestamp": datetime.now().isoformat()
        }
    
    except Exception as e:
        logger.error(f"예측 중 오류: {str(e)}")
        raise HTTPException(status_code=500, detail=f"예측 실패: {str(e)}")

@app.post("/api/batch-predict")
async def batch_predict_fraud(batch_request: BatchTransactionRequest):
    """
    배치 거래 이상 탐지
    
    - **한 번에 최대 1000건 처리**
    - **평균 처리 시간**: 100건당 1초
    """
    if model is None:
        raise HTTPException(status_code=503, detail="모델이 로드되지 않았습니다")
    
    if len(batch_request.transactions) > 1000:
        raise HTTPException(status_code=400, detail="최대 1000건까지 처리 가능합니다")
    
    try:
        results = []
        
        for transaction in batch_request.transactions:
            features = np.array([[
                transaction.amount,
                transaction.transaction_count_1h,
                transaction.transaction_count_24h,
                transaction.different_merchants_24h,
                transaction.avg_transaction_amount,
                transaction.time_since_last_transaction,
                transaction.is_weekend,
                transaction.is_night_time,
                transaction.merchant_risk_score,
                transaction.card_age_days,
                transaction.transaction_velocity,
                transaction.amount_deviation,
                transaction.cross_border,
                transaction.device_change,
                transaction.ip_change
            ]])
            
            prediction = model.predict(features)[0]
            probability = model.predict_proba(features)[0][1]
            
            results.append({
                "is_fraud": bool(prediction),
                "fraud_probability": float(probability),
                "risk_level": get_risk_level(probability)
            })
        
        logger.info(f"배치 예측 완료 - {len(results)}건 처리")
        
        return {
            "total_count": len(results),
            "fraud_count": sum(1 for r in results if r["is_fraud"]),
            "results": results,
            "timestamp": datetime.now().isoformat()
        }
    
    except Exception as e:
        logger.error(f"배치 예측 중 오류: {str(e)}")
        raise HTTPException(status_code=500, detail=f"예측 실패: {str(e)}")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
