FROM python:3.11-slim

WORKDIR /app

# 시스템 의존성 설치
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    && rm -rf /var/lib/apt/lists/*

# Python 의존성 복사 및 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 애플리케이션 코드 복사
COPY train_model.py .
COPY ml_api.py .

# 모델 학습 (빌드 시점에 한 번 실행)
RUN python train_model.py

# 포트 노출
EXPOSE 8000

# FastAPI 서버 실행
CMD ["uvicorn", "ml_api:app", "--host", "0.0.0.0", "--port", "8000"]