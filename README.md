# Manō: Component 2 — Risk Prediction System

![Status](https://img.shields.io/badge/Status-Production_Ready-success)
![Python](https://img.shields.io/badge/Python-3.9%2B-blue)
![Framework](https://img.shields.io/badge/Framework-TensorFlow%20%7C%20FastAPI-orange)
![License](https://img.shields.io/badge/License-MIT-green)

> **Project ID:** 25-26J-163  
> **Component:** Stress & Cognitive Risk Prediction Engine  
> **Author:** Keerthi K.K.D.D

---

## 📖 Table of Contents
1. [Overview](#-overview)
2. [Historical Evolution & Design Pivots](#-historical-evolution--design-pivots)
3. [System Architecture](#-system-architecture)
4. [The Model: LSTM + Temporal Attention](#-the-model-lstm--temporal-attention)
5. [Data Pipeline](#-data-pipeline)
6. [Installation & Setup](#-installation--setup)
7. [API Usage](#-api-usage)
8. [Ethical Considerations](#-ethical-considerations)
9. [References & Attribution](#-references--attribution)

---

## 🔭 Overview

**Component 2** of the Manō ecosystem is the analytical core responsible for **Cognitive Risk Assessment**. Unlike generic wellness trackers that display static graphs, this system acts as a predictive engine. It ingests multimodal biometric data (Sleep, Heart Rate, Activity) to forecast a user's mental state.

### Core Capability
The system classifies users into three clinically actionable categories:
* 🟢 **Low Risk (Optimal):** User is in Flow State. No intervention needed.
* 🟡 **Medium Risk (Warning):** Early signs of cognitive fatigue. Chatbot triggers "Micro-Interventions."
* 🔴 **High Risk (Critical):** Immediate danger of burnout. Chatbot triggers "De-escalation Protocols."

---

## 📜 Historical Evolution & Design Pivots

This project did not start with its current architecture. Below is the transparent record of the engineering journey, including failures and pivots.

### Phase 1: The "Naive" Regression Approach (Day 1)
* **Initial Vision:** A simple regression model to predict a "Stress Score" from 0.0 to 10.0 based on daily sleep duration.
* **The Implementation:** A Feed-Forward Neural Network (Dense Layers).
* **The Failure:**
    1.  **Ambiguity:** The downstream Chatbot (Component 3) struggled to interpret scores. Does a score of `6.2` require help? What about `6.8`?
    2.  **Lack of Context:** The model treated every day as an isolated event. It failed to recognize that *accumulated* sleep debt (3 days of poor sleep) is worse than one bad night.
* **The Pivot:** We moved away from regression to **Classification** (Low/Med/High) to provide deterministic signals to the Chatbot.

### Phase 2: The Standard LSTM (The "Black Box" Era)
* **The Idea:** Use a Long Short-Term Memory (LSTM) network to solve the "Context" problem. LSTMs have memory cells to track history.
* **The Implementation:** A vanilla LSTM layer processing 3-day sequences.
* **The Limitation:** While accuracy improved, the model was opaque. When it predicted "High Risk," we couldn't tell *why*. Was it the sleep drop on Monday? Or the Heart Rate spike on Tuesday?
* **Why this mattered:** In mental health, "Explainability" is a safety requirement. We cannot intervene without knowing the cause.

### Phase 3: The Current Solution (Attention Mechanism)
* **The Breakthrough:** We integrated a **Temporal Attention Layer** on top of the LSTM.
* **How it works:** The Attention mechanism assigns a "weight" (0.0 to 1.0) to each time step in the user's history.
* **Result:** The model not only predicts risk but inherently "points" to the specific day or feature that triggered the risk. This allows the system to say: *"High Risk detected due to sleep anomaly on Day T-1."*

---

## 🏗 System Architecture

The system follows a **Microservice Architecture** deployed via FastAPI. It is decoupled from the frontend, ensuring that the heavy ML processing does not lag the user interface.



### Component Breakdown
1.  **Data Ingestion Layer:** Receives raw JSON from client apps/wearables.
2.  **Preprocessing Engine:**
    * Loads saved **Scalers** (`scaler.pkl`) to normalize numerical inputs (0-1).
    * Loads saved **Label Encoders** (`encoders.pkl`) to translate text (e.g., "Nurse", "Male") into tensors.
3.  **Inference Engine:**
    * Loads the trained `.keras` model.
    * Runs the 3-Class prediction logic.
4.  **API Gateway:** Exposes endpoints via Swagger/OpenAPI for the Frontend.

---

## 🧠 The Model: LSTM + Temporal Attention

The heart of Component 2 is a custom Keras model designed for **Multi-Task Sequence Learning**.

### Technical Specs
* **Input Shape:** `(Batch_Size, Time_Steps, Features)` -> Typically `(1, 1, 12)` for snapshots.
* **Layer 1 (Recurrent):** `LSTM(64 units, return_sequences=True)`
    * *Purpose:* Captures the temporal dynamics of physiological markers.
* **Layer 2 (Regularization):** `Dropout(0.3)`
    * *Purpose:* Prevents overfitting to synthetic patterns.
* **Layer 3 (The Brain):** `Attention()` layer using **Self-Attention** `[x, x]`.
    * *Purpose:* Calculates context vectors to prioritize critical signal spikes.
* **Output Head:** `Dense(3, activation='softmax')`.
    * *Purpose:* Outputs a probability distribution summing to 1.0.

### Why 3 Classes?
This stratification was chosen to align with the **Triagging Protocol** of the Manō Chatbot:
1.  **Class 0 (Low):** Passive Monitoring.
2.  **Class 1 (Med):** Suggestive prompt ("You seem tired, take a break?").
3.  **Class 2 (High):** Directive prompt ("Stop working immediately. Let's do a breathing exercise.").

---

## 🔌 Data Pipeline

### Source Data
The primary training data is derived from the **Sleep Health and Lifestyle Dataset** (Open Source).
* **Dataset Path:** `data/Sleep_health_and_lifestyle_dataset.csv`
* **Key Features:** Sleep Duration, Quality, Heart Rate, Steps, BMI, Occupation.

### Synthetic Augmentation (Component 1 Integration)
To mitigate the lack of massive real-world mental health datasets (a common constraint in this domain), we utilize **Statistical Seeding**:
1.  We extract distributions (Mean, Std Dev) from the real CSV.
2.  We generate synthetic longitudinal sequences that respect these biological correlations.
3.  This allows us to train the LSTM on "Trajectory" data even if the source CSV is cross-sectional (snapshot-based).

---

## 💻 Installation & Setup

### Prerequisites
* Python 3.8+
* `pip` / `virtualenv`

### 1. Clone & Install
```bash
git clone [https://github.com/your-repo/mano-backend.git](https://github.com/your-repo/mano-backend.git)
cd mano_backend

# Create virtual environment (Optional but Recommended)
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt