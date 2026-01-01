# app/models/schemas.py
# Data structures are defined here

"""
Schemas Module
==============
What is this file?
Defines the SHAPE of our data. Like templates/forms.
Why we need it?
To make sure data is in the correct format.

Defines the structure of data used in the system.
Using Pydantic for data validation.
"""

from pydantic import BaseModel
from typing import List, Optional
from datetime import datetime


# ============================================
# USER RELATED SCHEMAS
# ============================================

class UserBase(BaseModel):
    """Base user information."""
    user_id: str
    name: Optional[str] = None
    age: Optional[int] = None
    occupation: Optional[str] = None


class UserScores(BaseModel):
    """User's calculated scores."""
    body_score: float = 0.0
    behavior_score: float = 0.0
    emotional_score: float = 0.0
    social_score: float = 0.0
    overall_score: float = 0.0
    stress_level: str = "unknown"  # low, medium, high, very_high


class UserInput(BaseModel):
    """
    Input data for a user.
    This is what we receive from other components or input system.
    """
    user_id: str

    # Body signals
    heart_rate: Optional[float] = None
    sleep_hours: Optional[float] = None
    steps: Optional[int] = None

    # Behavior
    phone_usage_hours: Optional[float] = None
    routine_stability: Optional[float] = None  # 0 to 100

    # Emotional
    stress_self_report: Optional[int] = None  # 1 to 10
    mood_score: Optional[int] = None  # 1 to 10

    # Social
    messages_sent: Optional[int] = None
    friends_contacted: Optional[int] = None


# ============================================
# GROUP RELATED SCHEMAS
# ============================================

class GroupInfo(BaseModel):
    """Information about a group."""
    group_id: int
    group_name: str
    description: str
    member_count: int
    average_stress: float


class GroupMember(BaseModel):
    """A member in a group."""
    user_id: str
    similarity_score: float  # How similar to group center


# ============================================
# ACTIVITY RELATED SCHEMAS
# ============================================

class Activity(BaseModel):
    """An activity that can be recommended."""
    activity_id: str
    name: str
    category: str  # sleep, stress_relief, social, energy, positive_thinking
    difficulty: str  # easy, medium, hard
    duration_minutes: int
    description: str


class ActivityRecommendation(BaseModel):
    """A recommended activity for a user."""
    activity: Activity
    reason: str  # Why this is recommended
    priority: int  # 1 = highest priority


# ============================================
# API RESPONSE SCHEMAS
# ============================================

class ClusteringResult(BaseModel):
    """Result of clustering a user."""
    user_id: str
    assigned_group: int
    group_name: str
    confidence: float  # How confident the assignment is (0 to 1)
    similar_users: List[str]


class ScoringResult(BaseModel):
    """Result of scoring a user."""
    user_id: str
    scores: UserScores
    calculated_at: datetime


class RecommendationResult(BaseModel):
    """Result of getting recommendations."""
    user_id: str
    recommendations: List[ActivityRecommendation]
    generated_at: datetime