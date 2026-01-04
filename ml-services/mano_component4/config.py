# config.py
# All configuration settings for the application

"""
Configuration Settings
======================
What is this file?
Central place for ALL settings.
Why we need it?
Change settings in ONE place instead of hunting through many files.

Central place for all settings and constants.
"""

# ============================================
# APPLICATION SETTINGS
# ============================================

APP_NAME = "Mano Component 4"
APP_VERSION = "1.0.0"
DEBUG = True  # Set to False in production


# ============================================
# GMM CLUSTERING SETTINGS
# ============================================

# Number of clusters (groups) to create
NUM_CLUSTERS = 5

# Minimum members per group
MIN_GROUP_SIZE = 8

# Maximum members per group
MAX_GROUP_SIZE = 12


# How many activities to recommend by default
MAX_RECOMMENDATIONS = 3

# Activity difficulty levels
DIFFICULTY_LEVELS = ["easy", "medium", "hard"]


# ============================================
# SCORING WEIGHTS
# ============================================

# How much each factor contributes to overall score
# Must add up to 1.0 (100%)
SCORING_WEIGHTS = {
    "body": 0.20,       # 20% - heart rate, sleep, exercise
    "behavior": 0.20,   # 20% - phone usage, routine
    "emotional": 0.35,  # 35% - self-reported stress, mood
    "social": 0.25      # 25% - social interactions
}


# ============================================
# STRESS LEVEL THRESHOLDS
# ============================================

# Score thresholds for categorizing stress levels
# Higher score = healthier/better
STRESS_THRESHOLDS = {
    "very_low": 80,    # Score >= 80: Very healthy
    "low": 60,         # Score >= 60: Doing okay
    "medium": 40,      # Score >= 40: Some problems
    "high": 20,        # Score >= 20: Needs help
    "very_high": 0     # Score < 20: Urgent attention
}


# ============================================
# ACTIVITY SETTINGS
# ============================================

# Maximum recommendations per user
MAX_RECOMMENDATIONS = 3

# Activity categories
ACTIVITY_CATEGORIES = [
    "sleep",
    "stress_relief",
    "social",
    "energy",
    "positive_thinking"
]


# ============================================
# API SETTINGS
# ============================================

# API host and port
API_HOST = "127.0.0.1"
API_PORT = 8000


# ============================================
# DATA PATHS
# ============================================

# Where to save/load data
DATA_DIR = "data"
ML_MODELS_DIR = "ml_models"

## Step 5: Create requirements.txt

### File 18: `requirements.txt`

# **Location:** `requirements.txt` (in root folder)
# ```
# # requirements.txt
# # List of all required Python packages
#
# # Core packages
# numpy>=1.24.0
# pandas>=2.0.0
#
# # Machine Learning
# scikit-learn>=1.3.0
#
# # API Framework
# fastapi>=0.104.0
# uvicorn>=0.24.0
#
# # Data validation
# pydantic>=2.0.0
#
# # HTTP client (for connecting to other components)
# httpx>=0.25.0
#
# # Date/time handling
# python-dateutil>=2.8.0