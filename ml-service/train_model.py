"""
AI 기반 이상거래 탐지 모델 학습 스크립트
"""

import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, roc_auc_score, confusion_matrix
from imblearn.over_sampling import SMOTE
import joblib
import json
from datetime import datetime

def generate_sample_data(n_samples=10000):
    """
    샘플 거래 데이터 생성
    실제 PG 서비스의 거래 패턴을 시뮬레이션
    """
    np.random.seed(42)
    
    # 정상 거래 (95%)
    n_normal = int(n_samples * 0.95)
    normal_data = {
        'amount': np.random.normal(50000, 20000, n_normal).clip(1000, 500000),
        'transaction_count_1h': np.random.poisson(2, n_normal),
        'transaction_count_24h': np.random.poisson(5, n_normal),
        'different_merchants_24h': np.random.poisson(3, n_normal),
        'avg_transaction_amount': np.random.normal(45000, 15000, n_normal).clip(5000, 200000),
        'time_since_last_transaction': np.random.exponential(3600, n_normal),
        'is_weekend': np.random.choice([0, 1], n_normal, p=[0.7, 0.3]),
        'is_night_time': np.random.choice([0, 1], n_normal, p=[0.8, 0.2]),
        'merchant_risk_score': np.random.uniform(0.1, 0.4, n_normal),
        'card_age_days': np.random.normal(365, 200, n_normal).clip(30, 3650),
        'transaction_velocity': np.random.normal(1.5, 0.5, n_normal).clip(0.5, 5),
        'amount_deviation': np.random.normal(0.8, 0.3, n_normal).clip(0.1, 2),
        'cross_border': np.random.choice([0, 1], n_normal, p=[0.95, 0.05]),
        'device_change': np.random.choice([0, 1], n_normal, p=[0.9, 0.1]),
        'ip_change': np.random.choice([0, 1], n_normal, p=[0.85, 0.15]),
        'is_fraud': np.zeros(n_normal)
    }
    
    # 이상 거래 (5%)
    n_fraud = n_samples - n_normal
    fraud_data = {
        'amount': np.random.normal(150000, 50000, n_fraud).clip(50000, 1000000),
        'transaction_count_1h': np.random.poisson(8, n_fraud),
        'transaction_count_24h': np.random.poisson(20, n_fraud),
        'different_merchants_24h': np.random.poisson(10, n_fraud),
        'avg_transaction_amount': np.random.normal(120000, 40000, n_fraud).clip(50000, 500000),
        'time_since_last_transaction': np.random.exponential(600, n_fraud),
        'is_weekend': np.random.choice([0, 1], n_fraud, p=[0.5, 0.5]),
        'is_night_time': np.random.choice([0, 1], n_fraud, p=[0.3, 0.7]),
        'merchant_risk_score': np.random.uniform(0.6, 0.95, n_fraud),
        'card_age_days': np.random.normal(90, 50, n_fraud).clip(1, 365),
        'transaction_velocity': np.random.normal(5, 2, n_fraud).clip(3, 15),
        'amount_deviation': np.random.normal(3, 1, n_fraud).clip(2, 10),
        'cross_border': np.random.choice([0, 1], n_fraud, p=[0.4, 0.6]),
        'device_change': np.random.choice([0, 1], n_fraud, p=[0.3, 0.7]),
        'ip_change': np.random.choice([0, 1], n_fraud, p=[0.2, 0.8]),
        'is_fraud': np.ones(n_fraud)
    }
    
    # 데이터 결합
    df_normal = pd.DataFrame(normal_data)
    df_fraud = pd.DataFrame(fraud_data)
    df = pd.concat([df_normal, df_fraud], ignore_index=True)
    
    # 셔플
    df = df.sample(frac=1, random_state=42).reset_index(drop=True)
    
    return df

def train_model():
    """
    Random Forest 모델 학습 및 저장
    """
    print("=" * 80)
    print("AI 기반 이상거래 탐지 모델 학습 시작")
    print("=" * 80)
    
    # 1. 데이터 생성
    print("\n[1/5] 데이터 생성 중...")
    df = generate_sample_data(10000)
    print(f"✓ 총 {len(df)}개 거래 데이터 생성")
    print(f"  - 정상 거래: {(df['is_fraud']==0).sum()}개 ({(df['is_fraud']==0).sum()/len(df)*100:.1f}%)")
    print(f"  - 이상 거래: {(df['is_fraud']==1).sum()}개 ({(df['is_fraud']==1).sum()/len(df)*100:.1f}%)")
    
    # 2. 특성과 타겟 분리
    print("\n[2/5] 데이터 전처리 중...")
    X = df.drop('is_fraud', axis=1)
    y = df['is_fraud']
    
    # Train/Test 분리
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )
    print(f"✓ Train: {len(X_train)}개, Test: {len(X_test)}개")
    
    # 3. SMOTE 적용 (불균형 데이터 해결)
    print("\n[3/5] SMOTE 적용 중...")
    smote = SMOTE(random_state=42)
    X_train_balanced, y_train_balanced = smote.fit_resample(X_train, y_train)
    print(f"✓ SMOTE 후 Train 데이터: {len(X_train_balanced)}개")
    print(f"  - 정상: {(y_train_balanced==0).sum()}개")
    print(f"  - 이상: {(y_train_balanced==1).sum()}개")
    
    # 4. Random Forest 모델 학습
    print("\n[4/5] Random Forest 모델 학습 중...")
    model = RandomForestClassifier(
        n_estimators=200,
        max_depth=20,
        min_samples_split=5,
        min_samples_leaf=2,
        random_state=42,
        n_jobs=-1
    )
    model.fit(X_train_balanced, y_train_balanced)
    print("✓ 모델 학습 완료")
    
    # 5. 모델 평가
    print("\n[5/5] 모델 평가 중...")
    y_pred = model.predict(X_test)
    y_pred_proba = model.predict_proba(X_test)[:, 1]
    
    accuracy = accuracy_score(y_test, y_pred)
    precision = precision_score(y_test, y_pred)
    recall = recall_score(y_test, y_pred)
    f1 = f1_score(y_test, y_pred)
    auc_roc = roc_auc_score(y_test, y_pred_proba)
    
    print("\n" + "=" * 80)
    print("모델 성능 지표")
    print("=" * 80)
    print(f"Accuracy:  {accuracy*100:.1f}%")
    print(f"Precision: {precision*100:.1f}%")
    print(f"Recall:    {recall*100:.1f}%")
    print(f"F1-Score:  {f1*100:.1f}%")
    print(f"AUC-ROC:   {auc_roc:.2f}")
    
    # Confusion Matrix
    cm = confusion_matrix(y_test, y_pred)
    print("\n혼동 행렬:")
    print(f"  TN: {cm[0][0]:4d}  FP: {cm[0][1]:4d}")
    print(f"  FN: {cm[1][0]:4d}  TP: {cm[1][1]:4d}")
    
    # Feature Importance
    print("\n주요 특성 중요도:")
    feature_importance = pd.DataFrame({
        'feature': X.columns,
        'importance': model.feature_importances_
    }).sort_values('importance', ascending=False)
    
    for idx, row in feature_importance.head(5).iterrows():
        print(f"  {row['feature']:30s}: {row['importance']:.4f}")
    
    # 6. 모델 저장
    print("\n모델 저장 중...")
    joblib.dump(model, 'fraud_detection_model.pkl')
    
    # 메타데이터 저장
    metadata = {
        'model_type': 'RandomForestClassifier',
        'n_estimators': 200,
        'max_depth': 20,
        'training_date': datetime.now().isoformat(),
        'accuracy': float(accuracy),
        'precision': float(precision),
        'recall': float(recall),
        'f1_score': float(f1),
        'auc_roc': float(auc_roc),
        'feature_names': X.columns.tolist()
    }
    
    with open('model_metadata.json', 'w', encoding='utf-8') as f:
        json.dump(metadata, f, indent=2, ensure_ascii=False)
    
    print("✓ 모델 저장 완료: fraud_detection_model.pkl")
    print("✓ 메타데이터 저장 완료: model_metadata.json")
    print("\n" + "=" * 80)
    print("학습 완료")
    print("=" * 80)
    
    return model, metadata

if __name__ == "__main__":
    train_model()
