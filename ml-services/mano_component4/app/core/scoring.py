# app/core/scoring.py
# Stress and resilience score calculation

"""
Scoring Module
==============
This module calculates resilience/stress scores for users.

How it works:
1. Takes raw user data (heart rate, sleep, mood, etc.)
2. Calculates 4 category scores (body, behavior, emotional, social)
3. Combines into overall resilience score
4. Determines stress level category

Higher score = Better/Healthier (less stress)
Lower score = Worse (more stress)
"""

import numpy as np
from typing import Dict, Optional, Tuple
from datetime import datetime

# Import configuration
import sys
import os

sys.path.append(os.path.dirname(os.path.dirname(os.path.dirname(__file__))))
from config import SCORING_WEIGHTS, STRESS_THRESHOLDS


class ScoringService:
    """
    Service class for calculating user resilience/stress scores.

    This service:
    - Calculates individual category scores
    - Combines them into overall score
    - Determines stress level
    """

    def __init__(self):
        """
        Initialize the scoring service.
        """
        # Weights for each category (from config)
        self.weights = SCORING_WEIGHTS

        # Thresholds for stress levels (from config)
        self.thresholds = STRESS_THRESHOLDS

        # Define ideal ranges for each metric
        # Format: (min_bad, min_good, max_good, max_bad)
        # Values in the "good" range get high scores
        self.ideal_ranges = {
            # Body metrics
            'heart_rate': (50, 60, 80, 120),  # 60-80 is ideal
            'resting_heart_rate': (45, 55, 75, 100),  # 55-75 is ideal
            'sleep_hours': (4, 7, 9, 12),  # 7-9 is ideal
            'sleep_quality': (1, 7, 10, 10),  # 7-10 is ideal (scale 1-10)
            'steps': (0, 6000, 15000, 30000),  # 6000-15000 is ideal
            'hrv': (10, 40, 80, 100),  # 40-80 is ideal (heart rate variability)
            'exercise_days': (0, 3, 6, 7),  # 3-6 days is ideal

            # Behavior metrics
            'phone_usage_hours': (0, 0, 4, 16),  # 0-4 hours is ideal
            'screen_time_hours': (0, 0, 6, 18),  # 0-6 hours is ideal
            'routine_stability': (0, 70, 100, 100),  # 70-100% is ideal
            'work_hours': (0, 6, 9, 16),  # 6-9 hours is ideal
            'social_media_hours': (0, 0, 2, 12),  # 0-2 hours is ideal

            # Emotional metrics (for these, lower is better for stress/anxiety/depression)
            'stress_level': (1, 1, 4, 10),  # 1-4 is ideal (scale 1-10)
            'anxiety_level': (1, 1, 4, 10),  # 1-4 is ideal
            'depression_level': (1, 1, 4, 10),  # 1-4 is ideal
            'mood_score': (1, 6, 10, 10),  # 6-10 is ideal
            'stress_self_report': (1, 1, 4, 10),  # 1-4 is ideal

            # Social metrics
            'messages_sent': (0, 10, 100, 500),  # 10-100 per week is ideal
            'friends_contacted': (0, 3, 20, 100),  # 3-20 per week is ideal
            'social_support_score': (1, 6, 10, 10),  # 6-10 is ideal
            'family_relationship': (1, 6, 10, 10),  # 6-10 is ideal
            'friends_count': (0, 5, 50, 500),  # 5-50 is ideal
        }

        # Map metrics to categories
        self.category_metrics = {
            'body': [
                'heart_rate', 'resting_heart_rate', 'sleep_hours',
                'sleep_quality', 'steps', 'hrv', 'exercise_days',
                'daily_steps', 'sleep_duration_mins', 'active_minutes'
            ],
            'behavior': [
                'phone_usage_hours', 'screen_time_hours', 'routine_stability',
                'work_hours', 'social_media_hours', 'work_hours_per_day'
            ],
            'emotional': [
                'stress_level', 'anxiety_level', 'depression_level',
                'mood_score', 'stress_self_report', 'stress_score',
                'positive_emotion', 'negative_emotion'
            ],
            'social': [
                'messages_sent', 'friends_contacted', 'social_support_score',
                'family_relationship', 'friends_count', 'social_score'
            ]
        }

        # Metrics where LOWER is BETTER (inverted scoring)
        self.lower_is_better = [
            'stress_level', 'anxiety_level', 'depression_level',
            'stress_self_report', 'phone_usage_hours', 'screen_time_hours',
            'social_media_hours', 'heart_rate', 'resting_heart_rate',
            'stress_score', 'negative_emotion', 'wearable_stress'
        ]

    def _normalize_value(self, value: float, metric_name: str) -> float:
        """
        Normalize a value to 0-100 scale based on ideal ranges.

        Args:
            value: The raw value
            metric_name: Name of the metric

        Returns:
            Normalized score (0-100)
        """
        # Get ideal range for this metric
        if metric_name not in self.ideal_ranges:
            # If we don't have a range, use a default normalization
            # Assume it's already on a reasonable scale
            if metric_name in self.lower_is_better:
                # For lower-is-better metrics, invert
                return max(0, min(100, 100 - value))
            else:
                return max(0, min(100, value))

        min_bad, min_good, max_good, max_bad = self.ideal_ranges[metric_name]

        # Check if this metric is "lower is better"
        invert = metric_name in self.lower_is_better

        # Calculate score
        if min_good <= value <= max_good:
            # In ideal range = 100 points
            score = 100
        elif value < min_good:
            # Below ideal range
            if value <= min_bad:
                score = 0
            else:
                # Linear interpolation from min_bad (0) to min_good (100)
                score = ((value - min_bad) / (min_good - min_bad)) * 100
        else:
            # Above ideal range
            if value >= max_bad:
                score = 0
            else:
                # Linear interpolation from max_good (100) to max_bad (0)
                score = ((max_bad - value) / (max_bad - max_good)) * 100

        # Ensure score is in 0-100 range
        score = max(0, min(100, score))

        return score

    def _get_metric_value(self, data: Dict, possible_names: list) -> Optional[float]:
        """
        Get a metric value from data, trying multiple possible field names.

        Args:
            data: Dictionary of user data
            possible_names: List of possible field names for this metric

        Returns:
            The value if found, None otherwise
        """
        for name in possible_names:
            if name in data and data[name] is not None:
                try:
                    return float(data[name])
                except (ValueError, TypeError):
                    continue
        return None

    def calculate_body_score(self, data: Dict) -> Tuple[float, Dict]:
        """
        Calculate body/physical health score.

        Args:
            data: Dictionary with user's body metrics

        Returns:
            Tuple of (score, details)
        """
        scores = []
        details = {}

        # Heart Rate
        hr = self._get_metric_value(data, ['heart_rate', 'resting_heart_rate', 'avg_heart_rate'])
        if hr is not None:
            # For heart rate, being in normal range is good
            # Too high = stressed, too low = potential issue
            hr_score = self._normalize_value(hr, 'heart_rate')
            # Invert because lower heart rate (within reason) is better
            hr_score = 100 - hr_score if hr > 80 else hr_score
            scores.append(hr_score)
            details['heart_rate'] = {'value': hr, 'score': hr_score}

        # Sleep Hours
        sleep = self._get_metric_value(data, ['sleep_hours', 'sleep_duration_mins'])
        if sleep is not None:
            # Convert minutes to hours if needed
            if sleep > 24:
                sleep = sleep / 60
            sleep_score = self._normalize_value(sleep, 'sleep_hours')
            scores.append(sleep_score)
            details['sleep_hours'] = {'value': sleep, 'score': sleep_score}

        # Sleep Quality
        sleep_qual = self._get_metric_value(data, ['sleep_quality', 'sleep_score'])
        if sleep_qual is not None:
            sq_score = self._normalize_value(sleep_qual, 'sleep_quality')
            scores.append(sq_score)
            details['sleep_quality'] = {'value': sleep_qual, 'score': sq_score}

        # Steps
        steps = self._get_metric_value(data, ['steps', 'daily_steps'])
        if steps is not None:
            steps_score = self._normalize_value(steps, 'steps')
            scores.append(steps_score)
            details['steps'] = {'value': steps, 'score': steps_score}

        # Heart Rate Variability (HRV)
        hrv = self._get_metric_value(data, ['hrv', 'heart_rate_variability', 'hrv_score'])
        if hrv is not None:
            hrv_score = self._normalize_value(hrv, 'hrv')
            scores.append(hrv_score)
            details['hrv'] = {'value': hrv, 'score': hrv_score}

        # Exercise Days
        exercise = self._get_metric_value(data, ['exercise_days', 'exercise_days_per_week', 'active_minutes'])
        if exercise is not None:
            if exercise > 7:  # Probably active_minutes
                exercise = min(7, exercise / 30)  # Convert to approximate days
            ex_score = self._normalize_value(exercise, 'exercise_days')
            scores.append(ex_score)
            details['exercise'] = {'value': exercise, 'score': ex_score}

        # Calculate average score
        if scores:
            body_score = sum(scores) / len(scores)
        else:
            body_score = 50.0  # Default to middle if no data

        return body_score, details

    def calculate_behavior_score(self, data: Dict) -> Tuple[float, Dict]:
        """
        Calculate behavior/habits score.

        Args:
            data: Dictionary with user's behavior metrics

        Returns:
            Tuple of (score, details)
        """
        scores = []
        details = {}

        # Phone Usage
        phone = self._get_metric_value(data, ['phone_usage_hours', 'screen_time_hours'])
        if phone is not None:
            # Lower phone usage is better
            phone_score = max(0, 100 - (phone * 8))  # 0 hours = 100, 12.5 hours = 0
            scores.append(phone_score)
            details['phone_usage'] = {'value': phone, 'score': phone_score}

        # Routine Stability
        routine = self._get_metric_value(data, ['routine_stability'])
        if routine is not None:
            routine_score = routine  # Already 0-100
            scores.append(routine_score)
            details['routine_stability'] = {'value': routine, 'score': routine_score}

        # Work Hours
        work = self._get_metric_value(data, ['work_hours', 'work_hours_per_day'])
        if work is not None:
            # 6-9 hours is ideal
            if 6 <= work <= 9:
                work_score = 100
            elif work < 6:
                work_score = (work / 6) * 100
            else:  # work > 9
                work_score = max(0, 100 - ((work - 9) * 15))
            scores.append(work_score)
            details['work_hours'] = {'value': work, 'score': work_score}

        # Social Media Hours
        social_media = self._get_metric_value(data, ['social_media_hours'])
        if social_media is not None:
            # Lower is better, 0-2 is ideal
            sm_score = max(0, 100 - (social_media * 15))
            scores.append(sm_score)
            details['social_media'] = {'value': social_media, 'score': sm_score}

        # Work-Life Balance (if directly provided)
        wlb = self._get_metric_value(data, ['work_life_balance', 'work_balance_score'])
        if wlb is not None:
            wlb_score = wlb * 10 if wlb <= 10 else wlb  # Convert 1-10 to 0-100
            scores.append(wlb_score)
            details['work_life_balance'] = {'value': wlb, 'score': wlb_score}

        # Calculate average score
        if scores:
            behavior_score = sum(scores) / len(scores)
        else:
            behavior_score = 50.0  # Default to middle if no data

        return behavior_score, details

    def calculate_emotional_score(self, data: Dict) -> Tuple[float, Dict]:
        """
        Calculate emotional/mental health score.

        Args:
            data: Dictionary with user's emotional metrics

        Returns:
            Tuple of (score, details)
        """
        scores = []
        details = {}

        # Stress Level (lower is better)
        stress = self._get_metric_value(data, ['stress_level', 'stress_self_report', 'stress_score'])
        if stress is not None:
            # Convert to 0-100 where higher = better
            if stress <= 10:  # 1-10 scale
                stress_score = (10 - stress) * 10  # 1 = 90, 10 = 0
            else:  # Already 0-100 scale
                stress_score = 100 - stress
            scores.append(stress_score)
            details['stress'] = {'value': stress, 'score': stress_score}

        # Anxiety Level (lower is better)
        anxiety = self._get_metric_value(data, ['anxiety_level', 'anxiety_score'])
        if anxiety is not None:
            if anxiety <= 10:
                anxiety_score = (10 - anxiety) * 10
            else:
                anxiety_score = 100 - anxiety
            scores.append(anxiety_score)
            details['anxiety'] = {'value': anxiety, 'score': anxiety_score}

        # Depression Level (lower is better)
        depression = self._get_metric_value(data, ['depression_level', 'depression_score'])
        if depression is not None:
            if depression <= 10:
                depression_score = (10 - depression) * 10
            else:
                depression_score = 100 - depression
            scores.append(depression_score)
            details['depression'] = {'value': depression, 'score': depression_score}

        # Mood Score (higher is better)
        mood = self._get_metric_value(data, ['mood_score'])
        if mood is not None:
            mood_score = mood * 10 if mood <= 10 else mood
            scores.append(mood_score)
            details['mood'] = {'value': mood, 'score': mood_score}

        # Positive Emotion (higher is better)
        positive = self._get_metric_value(data, ['positive_emotion', 'positive_sentiment'])
        if positive is not None:
            pos_score = positive if positive <= 100 else positive * 100
            scores.append(pos_score)
            details['positive_emotion'] = {'value': positive, 'score': pos_score}

        # Negative Emotion (lower is better)
        negative = self._get_metric_value(data, ['negative_emotion', 'negative_sentiment'])
        if negative is not None:
            neg_score = 100 - (negative if negative <= 100 else negative * 100)
            scores.append(neg_score)
            details['negative_emotion'] = {'value': negative, 'score': neg_score}

        # Calculate average score
        if scores:
            emotional_score = sum(scores) / len(scores)
        else:
            emotional_score = 50.0  # Default to middle if no data

        return emotional_score, details

    def calculate_social_score(self, data: Dict) -> Tuple[float, Dict]:
        """
        Calculate social connection score.

        Args:
            data: Dictionary with user's social metrics

        Returns:
            Tuple of (score, details)
        """
        scores = []
        details = {}

        # Messages Sent
        messages = self._get_metric_value(data, ['messages_sent'])
        if messages is not None:
            # 10-100 messages per week is ideal
            if messages < 10:
                msg_score = (messages / 10) * 100
            elif messages <= 100:
                msg_score = 100
            else:
                msg_score = max(50, 100 - ((messages - 100) * 0.5))
            scores.append(msg_score)
            details['messages'] = {'value': messages, 'score': msg_score}

        # Friends Contacted
        friends = self._get_metric_value(data, ['friends_contacted', 'friends_count'])
        if friends is not None:
            if friends == 0:
                friends_score = 0
            elif friends < 5:
                friends_score = (friends / 5) * 80
            elif friends <= 20:
                friends_score = 80 + ((friends - 5) / 15) * 20
            else:
                friends_score = 100
            scores.append(friends_score)
            details['friends'] = {'value': friends, 'score': friends_score}

        # Social Support Score
        support = self._get_metric_value(data, ['social_support_score', 'social_score'])
        if support is not None:
            support_score = support * 10 if support <= 10 else support
            scores.append(support_score)
            details['social_support'] = {'value': support, 'score': support_score}

        # Family Relationship
        family = self._get_metric_value(data, ['family_relationship'])
        if family is not None:
            family_score = family * 10 if family <= 10 else family
            scores.append(family_score)
            details['family'] = {'value': family, 'score': family_score}

        # Calculate average score
        if scores:
            social_score = sum(scores) / len(scores)
        else:
            social_score = 50.0  # Default to middle if no data

        return social_score, details

    def calculate_overall_score(self, data: Dict) -> Dict:
        """
        Calculate the overall resilience score.

        This is the MAIN method that combines all category scores.

        Args:
            data: Dictionary with all user data

        Returns:
            Dictionary with all scores and details
        """
        # Calculate each category score
        body_score, body_details = self.calculate_body_score(data)
        behavior_score, behavior_details = self.calculate_behavior_score(data)
        emotional_score, emotional_details = self.calculate_emotional_score(data)
        social_score, social_details = self.calculate_social_score(data)

        # Calculate weighted overall score
        overall_score = (
                body_score * self.weights['body'] +
                behavior_score * self.weights['behavior'] +
                emotional_score * self.weights['emotional'] +
                social_score * self.weights['social']
        )

        # Determine stress level
        stress_level = self._get_stress_level(overall_score)

        # Determine which areas need attention
        areas_of_concern = []
        if body_score < 40:
            areas_of_concern.append('physical_health')
        if behavior_score < 40:
            areas_of_concern.append('daily_habits')
        if emotional_score < 40:
            areas_of_concern.append('emotional_wellbeing')
        if social_score < 40:
            areas_of_concern.append('social_connections')

        return {
            'overall_score': round(overall_score, 2),
            'stress_level': stress_level,
            'category_scores': {
                'body': round(body_score, 2),
                'behavior': round(behavior_score, 2),
                'emotional': round(emotional_score, 2),
                'social': round(social_score, 2)
            },
            'category_details': {
                'body': body_details,
                'behavior': behavior_details,
                'emotional': emotional_details,
                'social': social_details
            },
            'areas_of_concern': areas_of_concern,
            'weights_used': self.weights,
            'calculated_at': datetime.now().isoformat()
        }

    def _get_stress_level(self, score: float) -> str:
        """
        Convert numerical score to stress level category.

        Args:
            score: Overall score (0-100, higher = healthier)

        Returns:
            Stress level category
        """
        if score >= self.thresholds['very_low']:
            return 'very_low'
        elif score >= self.thresholds['low']:
            return 'low'
        elif score >= self.thresholds['medium']:
            return 'medium'
        elif score >= self.thresholds['high']:
            return 'high'
        else:
            return 'very_high'

    def get_score_interpretation(self, score_result: Dict) -> Dict:
        """
        Get human-readable interpretation of scores.

        Args:
            score_result: Result from calculate_overall_score()

        Returns:
            Dictionary with interpretations
        """
        overall = score_result['overall_score']
        stress = score_result['stress_level']
        categories = score_result['category_scores']

        # Overall interpretation
        if stress == 'very_low':
            overall_interpretation = "Excellent! You're doing great. Keep up the healthy habits."
        elif stress == 'low':
            overall_interpretation = "Good job! You're managing well with minor areas to improve."
        elif stress == 'medium':
            overall_interpretation = "You're coping but some areas need attention. Consider small changes."
        elif stress == 'high':
            overall_interpretation = "You're showing signs of stress. It's important to take action and seek support."
        else:
            overall_interpretation = "You need immediate support. Please reach out to a professional or trusted person."

        # Category interpretations
        category_interpretations = {}

        for category, score in categories.items():
            if score >= 80:
                level = "excellent"
                advice = "Keep doing what you're doing!"
            elif score >= 60:
                level = "good"
                advice = "You're doing well with room for improvement."
            elif score >= 40:
                level = "moderate"
                advice = "This area needs some attention."
            elif score >= 20:
                level = "concerning"
                advice = "Focus on improving this area."
            else:
                level = "critical"
                advice = "This area needs immediate attention."

            category_interpretations[category] = {
                'level': level,
                'score': score,
                'advice': advice
            }

        return {
            'overall_score': overall,
            'stress_level': stress,
            'overall_interpretation': overall_interpretation,
            'category_interpretations': category_interpretations,
            'priority_areas': score_result.get('areas_of_concern', [])
        }