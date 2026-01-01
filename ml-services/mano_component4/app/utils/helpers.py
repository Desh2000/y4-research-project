# app/utils/helpers.py
# Helper functions go here

"""
Helpers Module
==============
What is this file?
Contains small helper functions used everywhere.
Why we need it?
Avoid writing same code multiple times.

Utility functions used across the application.
"""

from datetime import datetime
from typing import List, Dict, Any


def get_current_timestamp():
    """
    Get current date and time.

    Returns:
        Current datetime
    """
    return datetime.now()


def normalize_score(value: float, min_val: float, max_val: float) -> float:
    """
    Normalize a value to 0-100 scale.

    Args:
        value: The value to normalize
        min_val: Minimum possible value
        max_val: Maximum possible value

    Returns:
        Normalized value between 0 and 100
    """
    if max_val == min_val:
        return 50.0  # Return middle if no range

    normalized = ((value - min_val) / (max_val - min_val)) * 100

    # Ensure it's between 0 and 100
    return max(0.0, min(100.0, normalized))


def get_stress_level(score: float) -> str:
    """
    Convert numerical score to stress level category.

    Args:
        score: Overall score (0-100, higher = better/healthier)

    Returns:
        Stress level as string
    """
    if score >= 80:
        return "very_low"
    elif score >= 60:
        return "low"
    elif score >= 40:
        return "medium"
    elif score >= 20:
        return "high"
    else:
        return "very_high"


def validate_user_input(data: Dict[str, Any]) -> Dict[str, Any]:
    """
    Validate and clean user input data.

    Args:
        data: Raw user data dictionary

    Returns:
        Cleaned data dictionary
    """
    cleaned = {}

    # List of expected fields and their valid ranges
    validations = {
        "heart_rate": (40, 200),
        "sleep_hours": (0, 24),
        "steps": (0, 100000),
        "phone_usage_hours": (0, 24),
        "routine_stability": (0, 100),
        "stress_self_report": (1, 10),
        "mood_score": (1, 10),
        "messages_sent": (0, 10000),
        "friends_contacted": (0, 1000)
    }

    for field, (min_val, max_val) in validations.items():
        if field in data and data[field] is not None:
            value = data[field]
            # Clamp value to valid range
            cleaned[field] = max(min_val, min(max_val, value))

    return cleaned


def format_group_name(group_id: int, characteristics: List[str]) -> str:
    """
    Generate a human-readable group name.

    Args:
        group_id: Numerical group ID
        characteristics: Main characteristics of the group

    Returns:
        Formatted group name
    """
    if not characteristics:
        return f"Group {group_id}"

    return f"Group {group_id}: {' & '.join(characteristics[:2])}"