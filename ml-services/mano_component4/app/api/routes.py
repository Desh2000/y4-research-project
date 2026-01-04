# app/api/routes.py
# All API endpoints

"""
API Routes
==========
This file contains all the API endpoints for the clustering system.
"""

from fastapi import APIRouter, HTTPException
from typing import Dict, List, Optional
from pydantic import BaseModel
from app.core.recommender import RecommenderService

from app.core.scoring import ScoringService
from app.core.clustering import ClusteringService
from app.core.combined_service import CombinedAnalysisService

recommender = RecommenderService()
# Create router
router = APIRouter()

# Initialize services
scorer = ScoringService()
combined_service = CombinedAnalysisService()

# Try to load the clustering model
try:
    combined_service.load_clustering_model()
    print("✓ Clustering model loaded for API")
except:
    print("⚠ Clustering model not loaded - some features disabled")


# ============================================
# REQUEST/RESPONSE MODELS
# ============================================

class UserDataInput(BaseModel):
    """Input model for user data."""
    user_id: str

    # Body metrics
    heart_rate: Optional[float] = None
    sleep_hours: Optional[float] = None
    sleep_quality: Optional[float] = None
    steps: Optional[int] = None
    hrv: Optional[float] = None
    exercise_days: Optional[int] = None

    # Behavior metrics
    phone_usage_hours: Optional[float] = None
    routine_stability: Optional[float] = None
    work_hours: Optional[float] = None
    social_media_hours: Optional[float] = None

    # Emotional metrics
    stress_level: Optional[float] = None
    anxiety_level: Optional[float] = None
    depression_level: Optional[float] = None
    mood_score: Optional[float] = None

    # Social metrics
    messages_sent: Optional[int] = None
    friends_contacted: Optional[int] = None
    social_support_score: Optional[float] = None
    family_relationship: Optional[float] = None

    class Config:
        # Allow extra fields
        extra = 'allow'


class BatchUserInput(BaseModel):
    """Input model for multiple users."""
    users: List[UserDataInput]


class RecommendationRequest(BaseModel):
    """Request model for recommendations."""
    # Scores can be provided directly
    overall_score: Optional[float] = None
    stress_level: Optional[str] = None
    body_score: Optional[float] = None
    behavior_score: Optional[float] = None
    emotional_score: Optional[float] = None
    social_score: Optional[float] = None

    # Preferences
    num_recommendations: Optional[int] = 3
    difficulty_preference: Optional[str] = 'easy'
    max_duration_minutes: Optional[int] = None
    exclude_categories: Optional[List[str]] = None

# ============================================
# STATUS ENDPOINTS
# ============================================

@router.get("/status")
def get_status():
    """
    Get system status.
    """
    return {
        "scoring_service": "ready",
        "clustering_service": "ready" if combined_service.model_loaded else "model not loaded",
        "recommendation_service": "coming soon"
    }


# ============================================
# SCORING ENDPOINTS
# ============================================

@router.post("/score")
def calculate_score(user_data: UserDataInput):
    """
    Calculate stress/resilience scores for a user.

    Returns overall score, category scores, and interpretation.
    """
    try:
        # Convert to dictionary
        data_dict = user_data.dict()

        # Calculate scores
        result = scorer.calculate_overall_score(data_dict)

        # Get interpretation
        interpretation = scorer.get_score_interpretation(result)

        return {
            "success": True,
            "user_id": user_data.user_id,
            "scores": {
                "overall": result['overall_score'],
                "stress_level": result['stress_level'],
                "categories": result['category_scores']
            },
            "interpretation": interpretation,
            "areas_of_concern": result['areas_of_concern'],
            "calculated_at": result['calculated_at']
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/score/batch")
def calculate_scores_batch(batch_input: BatchUserInput):
    """
    Calculate scores for multiple users.
    """
    try:
        results = []
        for user_data in batch_input.users:
            data_dict = user_data.dict()
            result = scorer.calculate_overall_score(data_dict)
            results.append({
                "user_id": user_data.user_id,
                "overall_score": result['overall_score'],
                "stress_level": result['stress_level'],
                "categories": result['category_scores']
            })

        return {
            "success": True,
            "count": len(results),
            "results": results
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/score/category/{category}")
def calculate_category_score(category: str, user_data: UserDataInput):
    """
    Calculate score for a specific category.

    Categories: body, behavior, emotional, social
    """
    valid_categories = ['body', 'behavior', 'emotional', 'social']

    if category not in valid_categories:
        raise HTTPException(
            status_code=400,
            detail=f"Invalid category. Must be one of: {valid_categories}"
        )

    try:
        data_dict = user_data.dict()

        if category == 'body':
            score, details = scorer.calculate_body_score(data_dict)
        elif category == 'behavior':
            score, details = scorer.calculate_behavior_score(data_dict)
        elif category == 'emotional':
            score, details = scorer.calculate_emotional_score(data_dict)
        elif category == 'social':
            score, details = scorer.calculate_social_score(data_dict)

        return {
            "success": True,
            "user_id": user_data.user_id,
            "category": category,
            "score": round(score, 2),
            "details": details
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/recommend")
def get_recommendations(request: RecommendationRequest):
    """
    Get activity recommendations based on user's scores.

    Provide either:
    - Individual category scores (body_score, behavior_score, etc.)
    - Or overall_score and stress_level
    """
    try:
        # Build scores dictionary
        scores = {
            'overall_score': request.overall_score or 50,
            'stress_level': request.stress_level or 'medium',
            'category_scores': {
                'body': request.body_score or 50,
                'behavior': request.behavior_score or 50,
                'emotional': request.emotional_score or 50,
                'social': request.social_score or 50
            }
        }

        # Get recommendations
        result = recommender.get_recommendations(
            scores=scores,
            num_recommendations=request.num_recommendations,
            difficulty_preference=request.difficulty_preference,
            max_duration_minutes=request.max_duration_minutes,
            exclude_categories=request.exclude_categories or []
        )

        return result

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/recommend/from-analysis")
def get_recommendations_from_analysis(user_data: UserDataInput):
    """
    Get recommendations by first analyzing user data.

    This endpoint:
    1. Calculates scores from raw data
    2. Gets recommendations based on scores
    """
    try:
        # Calculate scores first
        data_dict = user_data.dict()
        score_result = scorer.calculate_overall_score(data_dict)

        # Get recommendations
        rec_result = recommender.get_recommendations(score_result)

        return {
            "success": True,
            "user_id": user_data.user_id,
            "scores": {
                "overall": score_result['overall_score'],
                "stress_level": score_result['stress_level'],
                "categories": score_result['category_scores']
            },
            "recommendations": rec_result['recommendations'],
            "identified_problems": rec_result['identified_problems']
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/recommend/quick")
def get_quick_recommendations(num: int = 3):
    """
    Get quick activities (under 10 minutes).
    Good for users with limited time.
    """
    try:
        activities = recommender.get_quick_activities(num)
        return {
            "success": True,
            "count": len(activities),
            "max_duration": 10,
            "activities": activities
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/recommend/by-duration/{max_minutes}")
def get_recommendations_by_duration(max_minutes: int):
    """
    Get activities that fit within a time limit.
    """
    try:
        activities = recommender.get_activities_by_duration(max_minutes)
        return {
            "success": True,
            "count": len(activities),
            "max_duration": max_minutes,
            "activities": activities
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/activities")
def get_all_activities():
    """
    Get all available activities.
    """
    try:
        from data.activities import get_all_activities
        activities = get_all_activities()
        return {
            "success": True,
            "count": len(activities),
            "activities": activities
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/activities/{activity_id}")
def get_activity_details(activity_id: str):
    """
    Get details for a specific activity.
    """
    try:
        activity = recommender.get_activity_details(activity_id)
        if activity:
            return {
                "success": True,
                "activity": activity
            }
        else:
            raise HTTPException(status_code=404, detail="Activity not found")
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/activities/search/{query}")
def search_activities(query: str):
    """
    Search activities by keyword.
    """
    try:
        results = recommender.search_activities(query)
        return {
            "success": True,
            "query": query,
            "count": len(results),
            "activities": results
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/activities/categories")
def get_activity_categories():
    """
    Get all activity categories with counts.
    """
    try:
        categories = recommender.get_all_categories()
        return {
            "success": True,
            "categories": categories
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# ============================================
# ANALYSIS ENDPOINTS (Scoring + Clustering)
# ============================================

@router.post("/analyze")
def analyze_user(user_data: UserDataInput):
    """
    Perform complete analysis on a user.

    This includes:
    - All scores
    - Stress level
    - Peer group assignment
    - Interpretation
    """
    try:
        data_dict = user_data.dict()
        result = combined_service.analyze_user(data_dict)

        return {
            "success": True,
            "analysis": result
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/analyze/batch")
def analyze_users_batch(batch_input: BatchUserInput):
    """
    Analyze multiple users.
    """
    try:
        users_data = [user.dict() for user in batch_input.users]
        results = combined_service.analyze_batch(users_data)

        return {
            "success": True,
            "count": len(results),
            "analyses": results
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/compare")
def compare_users(user1: UserDataInput, user2: UserDataInput):
    """
    Compare two users' scores and groups.
    """
    try:
        result = combined_service.compare_users(user1.dict(), user2.dict())

        return {
            "success": True,
            "comparison": result
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# ============================================
# GROUP ENDPOINTS
# ============================================

@router.get("/groups")
def get_all_groups():
    """
    Get information about all peer groups.
    """
    try:
        groups = combined_service.get_group_summary()

        return {
            "success": True,
            "groups": groups
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# ============================================
# HEALTH INFORMATION ENDPOINTS
# ============================================

@router.get("/info/weights")
def get_scoring_weights():
    """
    Get the weights used for scoring calculations.
    """
    return {
        "weights": scorer.weights,
        "description": {
            "body": "Physical health indicators (heart rate, sleep, exercise)",
            "behavior": "Daily habits (phone usage, routine, work hours)",
            "emotional": "Mental state (stress, anxiety, mood)",
            "social": "Social connections (friends, support, relationships)"
        }
    }


@router.get("/info/stress-levels")
def get_stress_level_info():
    """
    Get information about stress level categories.
    """
    return {
        "levels": {
            "very_low": {
                "score_range": "80-100",
                "description": "Excellent mental health",
                "color": "green"
            },
            "low": {
                "score_range": "60-79",
                "description": "Good mental health with minor areas to improve",
                "color": "light_green"
            },
            "medium": {
                "score_range": "40-59",
                "description": "Moderate concerns, some attention needed",
                "color": "yellow"
            },
            "high": {
                "score_range": "20-39",
                "description": "Significant stress, support recommended",
                "color": "orange"
            },
            "very_high": {
                "score_range": "0-19",
                "description": "Critical level, immediate support needed",
                "color": "red"
            }
        }
    }