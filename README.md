# Manō Framework - Component 4
## Community-Driven Resilience Clustering System: A Dynamic Gaussian Mixture Model Approach for Peer Support in Mental Health Frameworks

[![Project Status](https://img.shields.io/badge/status-active-success.svg)]()
[![Python Version](https://img.shields.io/badge/python-3.9%2B-blue.svg)]()
[![License](https://img.shields.io/badge/license-MIT-green.svg)]()
[![Framework](https://img.shields.io/badge/framework-FastAPI-009688.svg)]()

> **Project ID:** 25-26J-163  
> **Institution:** Sri Lanka Institute of Information Technology  
> **Department:** Information Technology, Faculty of Computing  
> **Developer:** SHALINDA D.G.M (IT22317308)  
> **Supervisor:** Dr. Sanvitha Kasthuriarachchi  
> **Timeline:** August 2025 - December 2025

---

## Table of Contents

- [Overview](#overview)
- [The Problem We're Solving](#the-problem-were-solving)
- [Architecture](#architecture)
- [Core Services](#core-services)
- [Data Pipeline](#data-pipeline)
- [Machine Learning Models](#machine-learning-models)
- [Installation & Setup](#installation--setup)
- [Usage Examples](#usage-examples)
- [API Reference](#api-reference)
- [Design Decisions & Rationale](#design-decisions--rationale)
- [Limitations & Known Issues](#limitations--known-issues)
- [Evaluation & Metrics](#evaluation--metrics)
- [Integration with Manō Ecosystem](#integration-with-manō-ecosystem)
- [Ethical Considerations](#ethical-considerations)
- [Future Work](#future-work)
- [Contributing](#contributing)
- [References](#references)
- [License](#license)

---

## Overview

Component 4 of the Manō Framework is a **real-time, adaptive mental health support system** that uses Gaussian Mixture Models (GMM) to dynamically cluster users into peer support communities based on evolving resilience profiles. Unlike traditional static mental health interventions, this system continuously learns from multimodal data streams to provide personalized activity recommendations and foster meaningful peer connections.

### What Makes This Different?

| Traditional Approach | Our System |
|---------------------|------------|
| Static demographic grouping | Dynamic, real-time clustering |
| Manual community assignment | Automated GMM-based matching |
| Single data source (surveys) | Multimodal: wearables, social media, chatbot, behavioral |
| Batch processing | Continuous learning with <5 min latency |
| Individual-focused | Community-driven with peer support |

### Key Capabilities

- **🧠 Intelligent Scoring**: Multi-dimensional resilience assessment across body, behavior, emotional, and social domains
- **👥 Dynamic Clustering**: Real-time peer group formation using probabilistic GMM clustering
- **🎯 Personalized Recommendations**: Evidence-based activity suggestions tailored to individual needs
- **📊 Continuous Adaptation**: Model updates within 5 minutes of significant data changes
- **🔒 Privacy-First**: GDPR/HIPAA compliant with AES-256 encryption

---

## The Problem We're Solving

### Global Mental Health Crisis

- **13%** of the world population suffers from mental health disorders ([WHO, 2022](https://www.who.int/news-room/fact-sheets/detail/mental-disorders))
- **$2.5 trillion** annual economic burden, projected to reach **$6 trillion by 2030**
- **$100-200** per therapy session makes professional help unaffordable for most
- **Weeks to months** waiting time for professional consultation
- **Lack of community support** in digital interventions

### Research Gap

Current digital mental health platforms suffer from:

1. **Static Community Assignment**: Groups based on demographics/diagnosis, ignoring dynamic recovery patterns
2. **Limited Data Integration**: Reliance on self-reported surveys, missing rich behavioral signals
3. **No Real-Time Adaptation**: Batch processing prevents timely response to user state changes
4. **Individual-Centric Focus**: Neglecting the proven benefits of peer support and community dynamics
5. **Insufficient Evaluation**: Lack of standardized metrics for community effectiveness

### Our Solution

A **dynamic, multimodal, real-time clustering system** that:
- Processes diverse data sources (wearables, social media, chatbot interactions)
- Adapts community structures within minutes of pattern changes
- Balances similarity with beneficial diversity in peer matching
- Provides evidence-based activity recommendations
- Measures impact through validated psychological scales

---

## Architecture

### System Design Philosophy

The system follows a **modular, microservices-inspired architecture** with clear separation of concerns:
```
┌─────────────────────────────────────────────────────────────────┐
│                        USER INTERFACE                            │
│              (Web Dashboard / Mobile App / API)                  │
└────────────────────────┬────────────────────────────────────────┘
                         │
┌────────────────────────┴────────────────────────────────────────┐
│                     COMBINED SERVICE                             │
│            (Orchestrates all three core services)                │
└─────┬──────────────────┬──────────────────┬──────────────────────┘
      │                  │                  │
┌─────▼─────┐    ┌──────▼──────┐    ┌─────▼──────────┐
│  SCORING  │    │ CLUSTERING  │    │ RECOMMENDER    │
│  SERVICE  │    │   SERVICE   │    │    SERVICE     │
└─────┬─────┘    └──────┬──────┘    └─────┬──────────┘
      │                  │                  │
      └──────────────────┼──────────────────┘
                         │
┌────────────────────────┴────────────────────────────────────────┐
│                     DATA LAYER                                   │
│  ┌──────────┐  ┌───────────┐  ┌──────────┐  ┌──────────┐      │
│  │ Wearables│  │Social Media│  │ Chatbot  │  │ Surveys  │      │
│  └──────────┘  └───────────┘  └──────────┘  └──────────┘      │
└──────────────────────────────────────────────────────────────────┘
```

### Directory Structure
```
mano_component4/
│
├── app/                          # Main application
│   ├── __init__.py              # Package initializer
│   ├── main.py                  # FastAPI entry point
│   │
│   ├── api/                     # API layer
│   │   └── routes.py           # REST endpoints
│   │
│   ├── core/                    # Business logic
│   │   ├── scoring.py          # Resilience scoring engine
│   │   ├── clustering.py       # GMM clustering engine
│   │   ├── recommender.py      # Activity recommendation engine
│   │   └── combined_service.py # Service orchestrator
│   │
│   ├── models/                  # Data models
│   │   └── schemas.py          # Pydantic schemas
│   │
│   └── utils/                   # Utilities
│       └── helpers.py          # Helper functions
│
├── data/                        # Data assets
│   ├── activities.py           # 21 evidence-based activities
│   ├── download_datasets.py    # Training data generator
│   └── gmm_training_data.csv   # Synthetic training data
│
├── ml_models/                   # Trained models
│   └── gmm_model.pkl           # Serialized GMM model
│
├── tests/                       # Test suite
│   ├── test_scoring.py
│   ├── test_clustering.py
│   ├── test_recommender.py
│   └── test_integration.py
│
├── config.py                    # Configuration settings
├── requirements.txt             # Python dependencies
├── test_full_system.py         # End-to-end system test
├── docker-compose.yml          # Container orchestration
└── README.md                    # This file
```

---

## Core Services

### 1. Scoring Service (`app/core/scoring.py`)

**Purpose**: Transforms raw multimodal data into normalized resilience scores.

**How It Works**:
1. **Data Normalization**: Converts different scales (heart rate, sleep hours, stress level) to uniform 0-100 scale
2. **Category Scoring**: Aggregates metrics into four domains:
   - **Body** (20%): Heart rate, sleep, physical activity
   - **Behavior** (20%): Phone usage, work hours, routine stability
   - **Emotional** (35%): Stress, anxiety, mood (highest weight for mental health focus)
   - **Social** (25%): Friend contact, social support, community participation
3. **Weighted Overall Score**: Combines categories with research-backed weights
4. **Stress Level Classification**: Maps scores to interpretable levels

**Design Decision**: Why These Weights?
- **Emotional (35%)**: This is a mental health system—emotional state is most critical
- **Social (25%)**: Peer support research shows social connection strongly predicts outcomes
- **Body & Behavior (20% each)**: Important but secondary to psychological factors

**Ideal Range Methodology**:
Each metric uses a trapezoidal membership function:
```
Score = 0   |████|         |████|   Score = 0
Score = 50  |    |████|    |    |   Score = 50  
Score = 100 |    |    |████|    |   Score = 100
            min  min  max  max
            bad  good good bad
```

Example for heart rate: `(50, 60, 80, 120)`
- Below 50 bpm: Bradycardia → Score 0
- 50-60 bpm: Transition zone → Score 50
- 60-80 bpm: Optimal range → Score 100
- 80-120 bpm: Transition zone → Score 50
- Above 120 bpm: Tachycardia → Score 0

**Limitations**:
- Assumes linear interpolation between thresholds
- Ideal ranges based on general population norms (may not fit all demographics)
- Self-reported metrics (stress, mood) subject to response bias

### 2. Clustering Service (`app/core/clustering.py`)

**Purpose**: Groups users into peer support communities using probabilistic clustering.

**Algorithm Choice: Why GMM?**

We evaluated multiple clustering approaches:

| Algorithm | Pros | Cons | Decision |
|-----------|------|------|----------|
| K-Means | Fast, simple | Hard boundaries, assumes spherical clusters | ❌ Too rigid |
| DBSCAN | Finds arbitrary shapes | Requires density tuning, no probabilities | ❌ Hard to interpret |
| Hierarchical | Creates dendrograms | Not scalable, no online updates | ❌ Can't adapt |
| **GMM** | **Soft clustering, probabilistic, handles overlaps** | **Computationally intensive** | ✅ **CHOSEN** |

**GMM Advantages for Mental Health**:
1. **Soft Clustering**: Users can belong to multiple communities (e.g., 70% "Stressed Workers", 30% "Sleep Deprived")
2. **Probabilistic Interpretation**: Uncertainty quantification for cluster assignment confidence
3. **Flexible Shape**: Gaussian components can model various resilience profile distributions
4. **Theoretical Foundation**: Expectation-Maximization (EM) algorithm has convergence guarantees

**Implementation Details**:
```python
# Automatic Component Selection
for n_components in range(2, 15):
    gmm = GaussianMixture(n_components=n_components, 
                          covariance_type='full',  # Allows elliptical clusters
                          random_state=42)
    gmm.fit(training_data)
    bic_scores.append(gmm.bic(training_data))  # Bayesian Information Criterion
    
optimal_k = np.argmin(bic_scores) + 2  # Typically converges to 5-7 clusters
```

**Why 5 Clusters?**
- 2-3 clusters: Oversimplified (just "good" vs "bad")
- 5-7 clusters: Sweet spot balancing granularity and interpretability
- 10+ clusters: Overfitting, sparse communities

**Real-Time Update Mechanism**:
```python
# Incremental EM for online learning
def update_gmm(self, new_data_batch):
    # 1. Compute responsibilities (E-step)
    responsibilities = self.model.predict_proba(new_data_batch)
    
    # 2. Update sufficient statistics
    self.update_statistics(new_data_batch, responsibilities)
    
    # 3. Recompute parameters (M-step)
    self.model = self.recompute_parameters()
    
    # 4. Check convergence
    if self.has_converged():
        return self.model
```

**Cluster Stability Monitoring**:
- **Silhouette Coefficient** (target >0.5): Measures cluster cohesion vs separation
- **Davies-Bouldin Index** (target <1.0): Lower is better, ratio of within-cluster to between-cluster distances
- **Concept Drift Detection**: Triggers retraining when distribution shift detected

**Limitations**:
- GMM assumes Gaussian distributions (may not perfectly fit all user data)
- Sensitive to initialization (mitigated by multiple random restarts)
- Requires tuning of number of components (automated via BIC but imperfect)

### 3. Recommender Service (`app/core/recommender.py`)

**Purpose**: Suggests personalized activities based on identified problem areas.

**Recommendation Strategy**:
```
┌─────────────────────────────────────────────────────────────┐
│ Step 1: Problem Identification                               │
│ → Scores < 40 flagged as problems                           │
│ → Priority: emotional (critical if <30) > social > behavior  │
└──────────────────┬──────────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────────┐
│ Step 2: Activity Filtering                                   │
│ → Match problem tags with activity targets                   │
│ → Consider user difficulty preference (easy/medium/hard)     │
└──────────────────┬──────────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────────┐
│ Step 3: Relevance Scoring                                    │
│ → Problem severity × activity effectiveness                  │
│ → Boost if activity targets multiple problems                │
│ → Penalize if user recently tried (avoid fatigue)           │
└──────────────────┬──────────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────────┐
│ Step 4: Diversity Optimization                               │
│ → Ensure variety across categories (stress, sleep, social)   │
│ → Balance duration (mix of 5-min and 30-min activities)     │
│ → Return top 3-5 recommendations                             │
└──────────────────────────────────────────────────────────────┘
```

**Activity Database Structure**:

21 evidence-based activities across 7 categories:

| Category | Activities | Evidence Base |
|----------|-----------|---------------|
| **Stress Relief** (3) | Deep breathing, Progressive muscle relaxation, Grounding 5-4-3-2-1 | [Kabat-Zinn, 1990](https://pubmed.ncbi.nlm.nih.gov/2693686/) |
| **Sleep** (3) | Sleep hygiene checklist, Body scan meditation, Wind-down routine | [Irish et al., 2015](https://pubmed.ncbi.nlm.nih.gov/25454674/) |
| **Physical** (3) | Morning stretch, 15-min walk, Desk stretches | [Schuch et al., 2016](https://pubmed.ncbi.nlm.nih.gov/27650251/) |
| **Social** (3) | Call a friend, Join support group, Family check-in | [Holt-Lunstad et al., 2010](https://pubmed.ncbi.nlm.nih.gov/20668659/) |
| **Emotional** (3) | Gratitude journaling, Mood tracking, Cognitive reframing | [Emmons & McCullough, 2003](https://pubmed.ncbi.nlm.nih.gov/12585811/) |
| **Mindfulness** (3) | Mindful breathing, Mindful eating, Loving-kindness meditation | [Khoury et al., 2013](https://pubmed.ncbi.nlm.nih.gov/23796855/) |
| **Routine** (3) | Morning routine, Digital detox hour, Weekly planning | [Martela & Steger, 2016](https://www.frontiersin.org/articles/10.3389/fpsyg.2016.00259) |

**Design Decision: Why Not Use Collaborative Filtering?**

We considered but ultimately rejected pure collaborative filtering because:
- **Cold Start Problem**: New users have no activity history
- **Sparsity**: In pilot tests, <10% activity completion rates led to sparse matrices
- **Interpretability**: Content-based filtering provides explainable recommendations

Our hybrid approach:
1. **Content-Based (Primary)**: Match problem tags with activity targets
2. **Collaborative Filtering (Secondary)**: Boost activities popular in user's cluster
3. **Temporal Optimization**: Recommend based on time-of-day preferences (morning stretches at 7am, sleep hygiene at 9pm)

**Limitations**:
- Activity database manually curated (requires periodic expert review)
- No automatic discovery of new activities
- Effectiveness ratings based on literature, not real-world user outcomes (yet)

### 4. Combined Service (`app/core/combined_service.py`)

**Purpose**: Orchestrates all three services into a unified analysis pipeline.
```python
def get_full_analysis(self, user_data):
    # 1. Calculate resilience scores
    scores = self.scorer.calculate_overall_score(user_data)
    
    # 2. Get human-readable interpretation
    interpretation = self.scorer.get_score_interpretation(scores)
    
    # 3. Assign to peer group
    cluster = self.clusterer.predict(scores)
    
    # 4. Generate recommendations
    recommendations = self.recommender.get_recommendations(
        scores=scores,
        cluster_id=cluster['group_id'],
        user_preferences=user_data.get('preferences', {})
    )
    
    # 5. Aggregate and return
    return {
        'scores': scores,
        'interpretation': interpretation,
        'peer_group': cluster,
        'recommendations': recommendations,
        'timestamp': datetime.utcnow()
    }
```

**Why This Architecture?**

**Modularity**: Each service can be developed, tested, and deployed independently
**Extensibility**: Easy to add new services (e.g., goal tracking, progress visualization)
**Testability**: Unit tests for each service, integration tests for combined service
**Scalability**: Services can run on separate containers for horizontal scaling

---

## Data Pipeline

### Data Sources
```
┌──────────────────────────────────────────────────────────────┐
│                    MULTIMODAL DATA SOURCES                    │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  1. WEARABLE DEVICES (Continuous)                            │
│     • Heart rate (bpm)              → Body metrics            │
│     • Sleep hours & quality         → Body metrics            │
│     • Steps per day                 → Behavior metrics        │
│     • Active minutes                → Behavior metrics        │
│                                                               │
│  2. MOBILE APP (Event-driven)                                │
│     • Phone usage hours             → Behavior metrics        │
│     • App categories used           → Behavior metrics        │
│     • Screen time patterns          → Behavior metrics        │
│                                                               │
│  3. SOCIAL MEDIA (Batch processing)                          │
│     • Post sentiment analysis       → Emotional metrics       │
│     • Interaction frequency         → Social metrics          │
│     • Community engagement          → Social metrics          │
│     • Emotional expression patterns → Emotional metrics       │
│                                                               │
│  4. CHATBOT INTERACTIONS (Real-time)                         │
│     • Conversation sentiment        → Emotional metrics       │
│     • Help-seeking behavior         → Emotional metrics       │
│     • Coping strategy indicators    → Emotional metrics       │
│     • Risk score from Component 3   → Overall risk            │
│                                                               │
│  5. VALIDATED SURVEYS (Periodic)                             │
│     • CD-RISC-25 (Resilience)       → Ground truth            │
│     • BRS (Brief Resilience Scale)  → Ground truth            │
│     • SSQ-6 (Social Support)        → Social metrics          │
│     • PHQ-9 (Depression screening)  → Emotional metrics       │
│                                                               │
└──────────────────────────────────────────────────────────────┘
```

### Feature Engineering Pipeline

**Phase 1: Data Collection & Cleaning**
```python
# Example: Wearable data preprocessing
def preprocess_wearable_data(raw_data):
    # 1. Handle missing values
    data = raw_data.fillna(method='ffill', limit=3)  # Forward fill up to 3 hours
    
    # 2. Remove physiologically impossible values
    data = data[
        (data['heart_rate'] >= 40) & (data['heart_rate'] <= 200) &
        (data['sleep_hours'] >= 0) & (data['sleep_hours'] <= 16)
    ]
    
    # 3. Detect and remove outliers (3-sigma rule)
    data = remove_outliers(data, threshold=3)
    
    # 4. Resample to hourly frequency
    data = data.resample('1H').mean()
    
    return data
```

**Phase 2: Feature Extraction**

**Emotional Regulation Features**:
- **Sentiment Variance**: Standard deviation of daily sentiment scores (stability indicator)
- **Emotional Recovery Time**: Time to return to baseline after negative event
- **Affect Intensity**: Average absolute sentiment score (emotional reactivity)

**Social Connectivity Features**:
- **Network Centrality**: Betweenness/degree centrality in interaction graph
- **Interaction Frequency**: Messages/calls per day
- **Social Support Utilization**: Ratio of help-seeking to offering support

**Behavioral Adaptation Features**:
- **Routine Stability**: Autocorrelation of daily activity patterns
- **Sleep Consistency**: Standard deviation of sleep onset/wake times
- **Activity Diversity**: Shannon entropy of activity categories

**Phase 3: Normalization & Aggregation**
```python
# Min-max normalization to 0-100 scale
def normalize_metric(value, min_bad, min_good, max_good, max_bad):
    if value <= min_bad or value >= max_bad:
        return 0
    elif value >= min_good and value <= max_good:
        return 100
    elif value < min_good:
        return 50 * (value - min_bad) / (min_good - min_bad)
    else:  # value > max_good
        return 50 * (max_bad - value) / (max_bad - max_good)
```

### Data Quality Considerations

**Missing Data Strategy**:
1. **Wearables**: Forward fill up to 3 hours, then flag as missing
2. **Social Media**: Not everyone uses social media → treat as optional
3. **Surveys**: Required every 2 weeks, send reminders

**Privacy Safeguards**:
- All data encrypted at rest (AES-256) and in transit (TLS 1.3)
- Personally identifiable information (PII) hashed before storage
- Social media content analyzed locally, only features stored
- Users can delete their data at any time (GDPR Right to Erasure)

**Data Retention**:
- Raw sensor data: 30 days
- Aggregated daily features: 1 year
- Model predictions: Indefinitely (for research)

---

## Machine Learning Models

### Gaussian Mixture Model (GMM)

**Mathematical Foundation**:

A GMM assumes data is generated from a mixture of K Gaussian distributions:
```
p(x) = Σ(k=1 to K) π_k * N(x | μ_k, Σ_k)

where:
  π_k = mixing coefficient (probability of cluster k)
  μ_k = mean vector of cluster k
  Σ_k = covariance matrix of cluster k
  N(x | μ, Σ) = Multivariate Gaussian PDF
```

**Expectation-Maximization (EM) Algorithm**:
```python
def fit_gmm(data, n_components):
    # Initialize parameters randomly
    means = random_sample(data, n_components)
    covariances = [np.eye(data.shape[1])] * n_components
    weights = [1/n_components] * n_components
    
    for iteration in range(max_iterations):
        # E-step: Compute responsibilities
        responsibilities = compute_posterior_probabilities(
            data, means, covariances, weights
        )
        
        # M-step: Update parameters
        weights = responsibilities.mean(axis=0)
        means = (responsibilities.T @ data) / responsibilities.sum(axis=0)
        covariances = compute_covariances(data, means, responsibilities)
        
        # Check convergence
        if log_likelihood_change < tolerance:
            break
    
    return GMMModel(means, covariances, weights)
```

**Why EM Works**: 
- EM is guaranteed to converge to a local maximum of the log-likelihood
- Alternating E and M steps is computationally efficient
- Naturally handles missing data (unlike K-means)

**Model Selection**:

We use **Bayesian Information Criterion (BIC)** to select optimal number of clusters:
```
BIC = -2 * log(L) + k * log(n)

where:
  L = likelihood of the model
  k = number of parameters (means, covariances, weights)
  n = number of data points
```

Lower BIC = Better model (penalizes complexity)

**Training Process**:

1. **Data Generation** (`data/download_datasets.py`):
```python
   # Generate synthetic training data with realistic distributions
   n_samples = 10000
   features = ['body_score', 'behavior_score', 'emotional_score', 'social_score']
   
   # Simulate 5 archetypes
   archetypes = {
       'healthy': {'body': 80, 'behavior': 75, 'emotional': 85, 'social': 80},
       'stressed': {'body': 60, 'behavior': 50, 'emotional': 30, 'social': 40},
       'sleep_deprived': {'body': 40, 'behavior': 45, 'emotional': 50, 'social': 60},
       'isolated': {'body': 70, 'behavior': 65, 'emotional': 55, 'social': 25},
       'high_risk': {'body': 30, 'behavior': 25, 'emotional': 20, 'social': 30}
   }
   
   # Add Gaussian noise
   training_data = generate_samples(archetypes, noise_std=10)
```

2. **Model Training**:
```python
   from sklearn.mixture import GaussianMixture
   
   # Load training data
   data = pd.read_csv('data/gmm_training_data.csv')
   
   # Train GMM with optimal components
   gmm = GaussianMixture(
       n_components=5,
       covariance_type='full',  # Full covariance allows elliptical clusters
       n_init=10,               # Multiple random initializations
       max_iter=200,
       random_state=42
   )
   
   gmm.fit(data[features])
   
   # Save trained model
   joblib.dump(gmm, 'ml_models/gmm_model.pkl')
```

3. **Model Validation**:
```python
   # Silhouette score (higher is better, range [-1, 1])
   silhouette_avg = silhouette_score(data, gmm.predict(data))
   # Expected: >0.5
   
   # Davies-Bouldin index (lower is better)
   db_index = davies_bouldin_score(data, gmm.predict(data))
   # Expected: <1.0
```

**Incremental Updates**:

For real-time adaptation, we implement **online GMM**:
```python
class OnlineGMM:
    def __init__(self, base_model):
        self.model = base_model
        self.sufficient_stats = self._initialize_stats()
        
    def partial_fit(self, new_batch):
        # E-step: Compute responsibilities for new data
        responsibilities = self.model.predict_proba(new_batch)
        
        # Update sufficient statistics
        self.sufficient_stats['sum_resp'] += responsibilities.sum(axis=0)
        self.sufficient_stats['sum_x'] += responsibilities.T @ new_batch
        self.sufficient_stats['sum_xx'] += ... # Covariance update
        
        # M-step: Recompute parameters from statistics
        self.model.means_ = self.sufficient_stats['sum_x'] / self.sufficient_stats['sum_resp']
        self.model.covariances_ = ... # From sum_xx
        self.model.weights_ = self.sufficient_stats['sum_resp'] / self.sufficient_stats['sum_resp'].sum()
```

**Limitations & Failure Modes**:

1. **Initialization Sensitivity**: 
   - Problem: EM can converge to poor local optima
   - Mitigation: Run 10 random initializations, keep best
   - Open Issue: Still no guarantee of global optimum

2. **Assumes Gaussian Distributions**:
   - Problem: Real user data may have heavy tails or multimodality
   - Mitigation: Transform skewed features (log/sqrt)
   - Open Issue: Non-parametric alternatives (DPGMM) too slow for real-time

3. **Curse of Dimensionality**:
   - Problem: Full covariance matrices have O(d²) parameters
   - Mitigation: Currently using 4 features (body, behavior, emotional, social)
   - Future: Dimensionality reduction (PCA) if features expand

4. **Concept Drift**:
   - Problem: User distributions change over time (seasonality, pandemics)
   - Mitigation: Detect drift using Kolmogorov-Smirnov test, retrain if p<0.05
   - Open Issue: How to handle sudden population shifts?

---

## Installation & Setup

### Prerequisites

- **Python**: 3.9 or higher
- **OS**: Ubuntu 20.04 LTS, macOS 10.15+, or Windows 10/11
- **Hardware**: 
  - Development: 8GB RAM minimum, 16GB recommended
  - Production: 16GB RAM, GPU optional for faster clustering

### Installation Steps

#### 1. Clone Repository
```bash
git clone https://github.com/your-org/mano-component4.git
cd mano-component4
```

#### 2. Create Virtual Environment
```bash
# Using venv
python3.9 -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Or using conda
conda create -n mano-c4 python=3.9
conda activate mano-c4
```

#### 3. Install Dependencies
```bash
pip install -r requirements.txt
```

**Core Dependencies**:
```
fastapi==0.104.1          # Web framework
uvicorn==0.24.0          # ASGI server
scikit-learn==1.3.2      # Machine learning
pandas==2.1.3            # Data manipulation
numpy==1.26.2            # Numerical computing
pydantic==2.5.0          # Data validation
python-multipart==0.0.6  # File uploads
joblib==1.3.2            # Model serialization
```

#### 4. Generate Training Data
```bash
python data/download_datasets.py
```

This creates `data/gmm_training_
