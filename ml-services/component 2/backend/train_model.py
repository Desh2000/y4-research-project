"""
Script: train_model.py
Description: Trains the 3-Class LSTM model and saves ALL artifacts (Model + Scaler + Encoders).
"""
import pandas as pd
import numpy as np
import joblib
import os
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import MinMaxScaler, LabelEncoder
from tensorflow.keras.models import Model
from tensorflow.keras.layers import Input, LSTM, Dense, Attention, GlobalAveragePooling1D, Dropout
from tensorflow.keras.optimizers import Adam

# CONFIG
DATA_PATH = 'data/Sleep_health_and_lifestyle_dataset.csv'
ARTIFACTS_DIR = 'model_artifacts'
os.makedirs(ARTIFACTS_DIR, exist_ok=True)

print(">>> Loading Data...")
try:
    df = pd.read_csv(DATA_PATH)
except FileNotFoundError:
    print(f"❌ Error: CSV not found at {DATA_PATH}")
    exit()

# 1. CLEANING
df['Sleep Disorder'] = df['Sleep Disorder'].fillna('None')
if 'Person ID' in df.columns: df = df.drop(columns=['Person ID'])
if 'Blood Pressure' in df.columns:
    df[['BP_Sys', 'BP_Dia']] = df['Blood Pressure'].str.split('/', expand=True).astype(float)
    df = df.drop(columns=['Blood Pressure'])

# 2. ENCODING & SAVING ENCODERS
print(">>> Encoding Data...")
label_encoders = {}
for col in df.select_dtypes(include=['object']).columns:
    le = LabelEncoder()
    df[col] = le.fit_transform(df[col])
    label_encoders[col] = le

joblib.dump(label_encoders, os.path.join(ARTIFACTS_DIR, 'encoders.pkl'))

# 3. TARGET MAPPING (3 Classes)
y_raw = df['Stress Level'].values
y = np.array([0 if v <= 4 else 1 if v <= 6 else 2 for v in y_raw])

# 4. SCALING & SAVING SCALER
print(">>> Scaling Data...")
X_raw = df.drop(columns=['Stress Level']).values
scaler = MinMaxScaler()
X_scaled = scaler.fit_transform(X_raw)

joblib.dump(scaler, os.path.join(ARTIFACTS_DIR, 'scaler.pkl'))

# 5. RESHAPE & TRAIN
X_lstm = X_scaled.reshape((X_scaled.shape[0], 1, X_scaled.shape[1]))
X_train, X_test, y_train, y_test = train_test_split(X_lstm, y, test_size=0.2, random_state=42)

print(">>> Building Model...")
inputs = Input(shape=(1, X_train.shape[2]))
x = LSTM(64, return_sequences=True)(inputs)
x = Dropout(0.3)(x)
attn = Attention()([x, x])
x = GlobalAveragePooling1D()(attn)
outputs = Dense(3, activation='softmax')(x)

model = Model(inputs, outputs)
model.compile(optimizer=Adam(0.001), loss='sparse_categorical_crossentropy', metrics=['accuracy'])

print(">>> Training (This may take a moment)...")
model.fit(X_train, y_train, validation_data=(X_test, y_test), epochs=40, batch_size=16, verbose=0)

model.save(os.path.join(ARTIFACTS_DIR, 'risk_model_3class.keras'))
print("\n✅ SUCCESS: 'risk_model_3class.keras', 'scaler.pkl', and 'encoders.pkl' saved!")