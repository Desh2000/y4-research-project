# 🧠 Manō: Holistic Framework for Personalized and Community-Driven Cognitive, Emotional, and Resilient Vitality

### *"A Complete AI-Driven Mental Health Ecosystem — From Data Generation to Real-Time Intervention"*

<div align="center">

[![MIT License](https://img.shields.io/badge/License-MIT-green.svg)](https://choosealicense.com/licenses/mit/)
[![Python 3.10+](https://img.shields.io/badge/python-3.10+-blue.svg)](https://www.python.org/downloads/)
[![PyTorch](https://img.shields.io/badge/PyTorch-%23EE4C2C.svg?style=flat&logo=PyTorch&logoColor=white)](https://pytorch.org/)
[![FastAPI](https://img.shields.io/badge/FastAPI-005571?style=flat&logo=fastapi)](https://fastapi.tiangolo.com/)
[![React](https://img.shields.io/badge/React-20232A?style=flat&logo=react&logoColor=61DAFB)](https://reactjs.org/)
[![CUDA](https://img.shields.io/badge/CUDA-11.8%20%7C%2012.1-76B900?logo=nvidia)](https://developer.nvidia.com/cuda-toolkit)

<img src="https://img.shields.io/badge/Status-Production%20Ready-success" alt="Status">
<img src="https://img.shields.io/badge/Version-1.0.0-blue" alt="Version">
<img src="https://img.shields.io/badge/Research-Project%20ID%2025--26J--163-important" alt="Research Project">

---

**Manō** is a comprehensive, modular AI framework designed to revolutionize digital mental health support. It integrates four interconnected components that work in harmony to generate synthetic mental health data, predict cognitive risks, provide empathetic conversational support, and foster community resilience — all while preserving user privacy and adhering to ethical AI principles.

[Features](#-key-features) •
[Architecture](#🏗️-system-architecture-overview) •
[Components](#-component-breakdown) •
[Installation](#-installation--deployment) •
[Usage](#-usage--demonstration) •
[Results](#-results--performance-metrics)

---

</div>

## 📑 Table of Contents

- [🌟 Introduction](#-introduction)
- [🏗️ System Architecture Overview](#🏗️-system-architecture-overview)
- [🧩 Component Breakdown](#-component-breakdown)
- [🔬 Technical Innovations & Decisions](#-technical-innovations--decisions)
- [📊 Results & Performance Metrics](#-results--performance-metrics)
- [💻 Installation & Deployment](#-installation--deployment)
- [⏯️ Usage & Demonstration](#-usage--demonstration)
- [📂 Project Structure](#-project-structure)
- [🤝 Team & Contributions](#-team--contributions)
- [📜 License & Citation](#-license--citation)
- [📚 References](#-references)

---

## 🌟 Introduction

### 🎯 **The Mental Health Crisis & Our Solution**

> **13%** of the global population suffers from mental health disorders, yet **only 2%** of healthcare budgets are allocated to mental health services. Traditional solutions face critical barriers: **data scarcity**, **privacy concerns**, **lack of personalization**, and **inaccessible support**.

**Manō** addresses these challenges through a **privacy-first, AI-driven ecosystem** that:

<div align="center">

| ✅ | **Generates** realistic synthetic mental health data (no real patient data required) |
| ✅ | **Predicts** cognitive and emotional risks with clinical accuracy |
| ✅ | **Provides** empathetic, context-aware conversational support |
| ✅ | **Connects** users with peer communities for sustainable resilience |
| ✅ | **Operates** on consumer hardware (RTX 3050 Ti laptop GPU) |
| ✅ | **Maintains** 100% privacy compliance (HIPAA/GDPR ready) |

</div>

---

## 🏗️ System Architecture Overview

<div align="center">

### **End-to-End Data Flow & Component Integration**

```mermaid
flowchart TD
    A[📊 Real World Data Sources] --> B[🔒 Component 1: Synthetic Data Engine]
    
    subgraph B[Component 1 - Privacy-Preserving Data Generation]
        B1[🎲 CTGAN - Static Demographics]
        B2[⏰ TimeGAN - Temporal Biometrics]
        B3[🏥 Rule Engine - Medical Labeling]
    end
    
    B --> C[📦 Fused Synthetic Dataset<br/>10,000 patients]
    
    C --> D[🧠 Component 2: Risk Prediction System]
    D --> E[⚠️ Risk Classification<br/>Low/Medium/High]
    
    E --> F[💬 Component 3: Empathetic Chatbot]
    F --> G[🤖 Personalized Interventions]
    
    C --> H[👥 Component 4: Resilience Clustering]
    E --> H
    F --> H
    
    H --> I[🔄 Community Feedback Loop]
    I --> B
    
    G --> J[📱 User Applications<br/>Mobile & Web]
    H --> J
</div>
