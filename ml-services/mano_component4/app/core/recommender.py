# app/core/recommender.py
# Activity recommendation logic

"""
Recommender Module
==================
This module recommends activities to users based on:
- Their current problems (from scores)
- Activity effectiveness
- Difficulty level
- Time available

How it works:
1. Analyze user's scores to identify problems
2. Find activities that target those problems
3. Rank activities by relevance and effectiveness
4. Return top recommendations
"""

import sys
import os
from typing import Dict, List, Optional, Tuple
from datetime import datetime

# Add parent directory to path
sys.path.append(os.path.dirname(os.path.dirname(os.path.dirname(__file__))))

from data.activities import (
    ACTIVITIES_DATABASE,
    get_activities_for_problem,
    get_activity_by_id,
    get_activities_by_category
)
from config import MAX_RECOMMENDATIONS


class RecommenderService:
    """
    Service class for recommending activities to users.

    This service:
    - Identifies user's problem areas from scores
    - Matches problems to helpful activities
    - Ranks activities by relevance
    - Returns personalized recommendations
    """

    def __init__(self):
        """Initialize the recommender service."""
        self.activities = ACTIVITIES_DATABASE
        self.max_recommendations = MAX_RECOMMENDATIONS

        # Problem mapping: score category -> possible problems
        self.problem_mapping = {
            'body': {
                'low_threshold': 40,
                'problems': ['physical_health', 'low_energy', 'fatigue', 'sleep_issues'],
                'categories': ['physical', 'sleep']
            },
            'behavior': {
                'low_threshold': 40,
                'problems': ['poor_routine', 'daily_habits', 'screen_addiction', 'chaos'],
                'categories': ['routine']
            },
            'emotional': {
                'low_threshold': 40,
                'problems': ['emotional_wellbeing', 'high_stress', 'anxiety', 'bad_mood',
                             'negative_thinking', 'depression'],
                'categories': ['stress_relief', 'emotional', 'mindfulness']
            },
            'social': {
                'low_threshold': 40,
                'problems': ['social_connections', 'loneliness', 'isolation'],
                'categories': ['social']
            }
        }

        # Stress level to urgency mapping
        self.urgency_mapping = {
            'very_high': {'urgency': 5, 'include_professional': True},
            'high': {'urgency': 4, 'include_professional': True},
            'medium': {'urgency': 3, 'include_professional': False},
            'low': {'urgency': 2, 'include_professional': False},
            'very_low': {'urgency': 1, 'include_professional': False}
        }

    def identify_problems(self, scores: Dict) -> List[Dict]:
        """
        Identify user's problems based on their scores.

        Args:
            scores: Dictionary with category scores and overall info
                   Expected format from ScoringService:
                   {
                       'overall_score': 45,
                       'stress_level': 'medium',
                       'category_scores': {'body': 70, 'behavior': 45, ...}
                   }

        Returns:
            List of identified problems with severity
        """
        problems = []

        # Get category scores
        category_scores = scores.get('category_scores', scores)

        # Check each category
        for category, config in self.problem_mapping.items():
            score = category_scores.get(category, 50)
            threshold = config['low_threshold']

            if score < threshold:
                # Calculate severity (lower score = higher severity)
                if score < 20:
                    severity = 'critical'
                    priority = 5
                elif score < 30:
                    severity = 'high'
                    priority = 4
                elif score < 40:
                    severity = 'medium'
                    priority = 3
                else:
                    severity = 'low'
                    priority = 2

                problems.append({
                    'category': category,
                    'score': score,
                    'severity': severity,
                    'priority': priority,
                    'problem_types': config['problems'],
                    'activity_categories': config['categories']
                })

        # Sort by priority (highest first)
        problems.sort(key=lambda x: x['priority'], reverse=True)

        return problems

    def find_matching_activities(self, problems: List[Dict],
                                 difficulty_preference: str = 'easy') -> List[Dict]:
        """
        Find activities that match the identified problems.

        Args:
            problems: List of identified problems
            difficulty_preference: 'easy', 'medium', or 'hard'

        Returns:
            List of matching activities with relevance scores
        """
        activity_scores = {}  # activity_id -> score

        for problem in problems:
            problem_priority = problem['priority']

            # Search by problem types
            for problem_type in problem['problem_types']:
                matching = get_activities_for_problem(problem_type)
                for activity in matching:
                    activity_id = activity['id']
                    if activity_id not in activity_scores:
                        activity_scores[activity_id] = {
                            'activity': activity,
                            'relevance_score': 0,
                            'matched_problems': []
                        }
                    # Add to relevance score based on problem priority
                    activity_scores[activity_id]['relevance_score'] += problem_priority * 10
                    if problem_type not in activity_scores[activity_id]['matched_problems']:
                        activity_scores[activity_id]['matched_problems'].append(problem_type)

            # Also search by activity category
            for category in problem['activity_categories']:
                category_activities = get_activities_by_category(category)
                for activity in category_activities:
                    activity_id = activity['id']
                    if activity_id not in activity_scores:
                        activity_scores[activity_id] = {
                            'activity': activity,
                            'relevance_score': 0,
                            'matched_problems': []
                        }
                    activity_scores[activity_id]['relevance_score'] += problem_priority * 5

        # Convert to list and add activity effectiveness
        result = []
        for activity_id, data in activity_scores.items():
            activity = data['activity']

            # Calculate final score
            final_score = data['relevance_score']

            # Bonus for matching difficulty preference
            if activity['difficulty'] == difficulty_preference:
                final_score += 15
            elif activity['difficulty'] == 'easy':  # Easy is always good
                final_score += 10

            # Add effectiveness score
            final_score += activity.get('effectiveness_score', 70) * 0.3

            # Bonus for scientific backing
            if activity.get('scientific_backing', False):
                final_score += 5

            result.append({
                'activity': activity,
                'relevance_score': round(final_score, 2),
                'matched_problems': data['matched_problems']
            })

        # Sort by relevance score (highest first)
        result.sort(key=lambda x: x['relevance_score'], reverse=True)

        return result

    def get_recommendations(self, scores: Dict,
                            num_recommendations: int = None,
                            difficulty_preference: str = 'easy',
                            max_duration_minutes: int = None,
                            exclude_categories: List[str] = None) -> Dict:
        """
        Get personalized activity recommendations for a user.

        This is the MAIN method to call.

        Args:
            scores: User's scores from ScoringService
            num_recommendations: How many activities to recommend
            difficulty_preference: 'easy', 'medium', or 'hard'
            max_duration_minutes: Maximum time user has available
            exclude_categories: Categories to exclude (e.g., ['physical'])

        Returns:
            Dictionary with recommendations and reasoning
        """
        if num_recommendations is None:
            num_recommendations = self.max_recommendations

        if exclude_categories is None:
            exclude_categories = []

        # Step 1: Identify problems
        problems = self.identify_problems(scores)

        # If no problems found, return general wellness activities
        if not problems:
            return self._get_general_recommendations(num_recommendations)

        # Step 2: Find matching activities
        matching_activities = self.find_matching_activities(problems, difficulty_preference)

        # Step 3: Filter by constraints
        filtered = []
        for item in matching_activities:
            activity = item['activity']

            # Filter by duration
            if max_duration_minutes and activity['duration_minutes'] > max_duration_minutes:
                continue

            # Filter by excluded categories
            if activity['category'] in exclude_categories:
                continue

            filtered.append(item)

        # Step 4: Check if professional help should be included
        stress_level = scores.get('stress_level', 'medium')
        urgency_config = self.urgency_mapping.get(stress_level, {'include_professional': False})

        # Step 5: Select top recommendations
        recommendations = []
        seen_categories = set()

        for item in filtered:
            activity = item['activity']

            # Try to get variety in categories (but not strictly required)
            category = activity['category']

            # Skip if we already have 2 from this category
            category_count = sum(1 for r in recommendations if r['activity']['category'] == category)
            if category_count >= 2:
                continue

            recommendations.append({
                'activity_id': activity['id'],
                'activity': activity,
                'relevance_score': item['relevance_score'],
                'matched_problems': item['matched_problems'],
                'why_recommended': self._generate_recommendation_reason(
                    activity, item['matched_problems']
                )
            })

            if len(recommendations) >= num_recommendations:
                break

        # Step 6: Add professional help if needed
        if urgency_config['include_professional'] and stress_level in ['very_high', 'high']:
            professional = get_activity_by_id('professional_001')
            if professional:
                # Check if not already in recommendations
                if not any(r['activity_id'] == 'professional_001' for r in recommendations):
                    recommendations.insert(0, {
                        'activity_id': professional['id'],
                        'activity': professional,
                        'relevance_score': 100,
                        'matched_problems': ['high_stress', 'professional_support'],
                        'why_recommended': "Given your current stress levels, professional support could be very helpful.",
                        'priority': 'high'
                    })

        # Build final response
        return {
            'success': True,
            'user_stress_level': stress_level,
            'identified_problems': [
                {
                    'category': p['category'],
                    'severity': p['severity'],
                    'score': p['score']
                } for p in problems
            ],
            'recommendations': recommendations,
            'total_matching_activities': len(matching_activities),
            'filters_applied': {
                'difficulty_preference': difficulty_preference,
                'max_duration_minutes': max_duration_minutes,
                'excluded_categories': exclude_categories
            },
            'generated_at': datetime.now().isoformat()
        }

    def _generate_recommendation_reason(self, activity: Dict, matched_problems: List[str]) -> str:
        """
        Generate a human-readable reason for the recommendation.

        Args:
            activity: The activity being recommended
            matched_problems: Problems this activity addresses

        Returns:
            Explanation string
        """
        category = activity['category']
        name = activity['name']

        # Create reason based on category and problems
        reasons = {
            'stress_relief': f"'{name}' can help reduce your stress levels",
            'sleep': f"'{name}' can improve your sleep quality",
            'physical': f"'{name}' can boost your physical wellbeing and energy",
            'social': f"'{name}' can help you feel more connected",
            'emotional': f"'{name}' can help improve your emotional state",
            'mindfulness': f"'{name}' can help calm your mind",
            'routine': f"'{name}' can help establish healthier habits",
            'professional': f"'{name}' provides expert support for your situation"
        }

        base_reason = reasons.get(category, f"'{name}' matches your current needs")

        # Add problem-specific detail
        if matched_problems:
            if 'high_stress' in matched_problems or 'anxiety' in matched_problems:
                base_reason += " and reduce anxiety"
            if 'loneliness' in matched_problems:
                base_reason += " and reduce feelings of loneliness"
            if 'sleep_issues' in matched_problems:
                base_reason += " and help you sleep better"

        return base_reason + "."

    def _get_general_recommendations(self, num_recommendations: int) -> Dict:
        """
        Get general wellness recommendations when no specific problems identified.

        Args:
            num_recommendations: How many to return

        Returns:
            General wellness recommendations
        """
        # Select well-rounded activities
        general_activities = [
            get_activity_by_id('mindful_001'),  # Mindful breathing
            get_activity_by_id('physical_002'),  # Walking
            get_activity_by_id('emotional_001'),  # Gratitude journaling
        ]

        recommendations = []
        for activity in general_activities[:num_recommendations]:
            if activity:
                recommendations.append({
                    'activity_id': activity['id'],
                    'activity': activity,
                    'relevance_score': 70,
                    'matched_problems': ['general_wellness'],
                    'why_recommended': f"'{activity['name']}' is great for maintaining overall wellbeing."
                })

        return {
            'success': True,
            'user_stress_level': 'low',
            'identified_problems': [],
            'message': "Your scores look good! Here are some activities to maintain your wellbeing.",
            'recommendations': recommendations,
            'generated_at': datetime.now().isoformat()
        }

    def get_activity_details(self, activity_id: str) -> Optional[Dict]:
        """
        Get full details for a specific activity.

        Args:
            activity_id: The activity ID

        Returns:
            Full activity details or None
        """
        return get_activity_by_id(activity_id)

    def get_activities_by_duration(self, max_minutes: int) -> List[Dict]:
        """
        Get all activities that fit within a time limit.

        Args:
            max_minutes: Maximum duration in minutes

        Returns:
            List of activities
        """
        return [a for a in self.activities if a['duration_minutes'] <= max_minutes]

    def get_quick_activities(self, num: int = 3) -> List[Dict]:
        """
        Get quick activities (under 10 minutes).

        Args:
            num: How many to return

        Returns:
            List of quick activities
        """
        quick = [a for a in self.activities if a['duration_minutes'] <= 10]
        quick.sort(key=lambda x: x['effectiveness_score'], reverse=True)
        return quick[:num]

    def search_activities(self, query: str) -> List[Dict]:
        """
        Search activities by keyword.

        Args:
            query: Search term

        Returns:
            Matching activities
        """
        query = query.lower()
        results = []

        for activity in self.activities:
            # Search in name, description, and tags
            if (query in activity['name'].lower() or
                    query in activity['description'].lower() or
                    any(query in tag for tag in activity['tags'])):
                results.append(activity)

        return results

    def get_all_categories(self) -> List[Dict]:
        """
        Get list of all activity categories with counts.

        Returns:
            List of categories with activity counts
        """
        categories = {}
        for activity in self.activities:
            cat = activity['category']
            if cat not in categories:
                categories[cat] = {'name': cat, 'count': 0, 'activities': []}
            categories[cat]['count'] += 1
            categories[cat]['activities'].append(activity['name'])

        return list(categories.values())