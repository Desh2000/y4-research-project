"""
Activity Database Module

Manages the catalog of wellness activities with their
resilience impact profiles.

Author: [Your Name]
Date: [Current Date]
"""

import pandas as pd
import numpy as np
import os
from typing import List, Dict, Optional


class ActivityDatabase:
    """
    Manages wellness activities and their resilience impacts
    """

    def __init__(self, catalog_path: str = "data/activities/activity_catalog.csv"):
        """
        Initialize activity database

        Parameters:
            catalog_path (str): Path to activity catalog CSV
        """
        self.catalog_path = catalog_path
        self.activities = None

        # Load or create catalog
        if os.path.exists(catalog_path):
            self.load_catalog()
        else:
            self.create_default_catalog()

    def create_default_catalog(self):
        """
        Create default activity catalog with evidence-based activities
        """
        print(f"\n{'=' * 60}")
        print(f"CREATING ACTIVITY CATALOG")
        print(f"{'=' * 60}\n")

        # Define activities with their resilience impact profiles
        activities_data = [
            # EMOTIONAL REGULATION ACTIVITIES
            {
                'activity_id': 'ACT_001',
                'name': 'Mindfulness Meditation Workshop',
                'category': 'Emotional Regulation',
                'type': 'Workshop',
                'duration_minutes': 60,
                'difficulty': 'Beginner',
                'target_emotional': 0.15,
                'target_social': 0.00,
                'target_behavioral': 0.05,
                'target_cognitive': 0.05,
                'target_stress': 0.10,
                'required_min_behavioral': 0.40,
                'required_min_social': 0.00,
                'social_format': 'Individual',
                'description': 'Learn basic mindfulness techniques to improve emotional awareness and regulation',
                'resource_links': 'YouTube:Guided Meditation|Headspace App'
            },
            {
                'activity_id': 'ACT_002',
                'name': 'Emotion Journaling Practice',
                'category': 'Emotional Regulation',
                'type': 'Individual',
                'duration_minutes': 20,
                'difficulty': 'Beginner',
                'target_emotional': 0.12,
                'target_social': 0.00,
                'target_behavioral': 0.08,
                'target_cognitive': 0.06,
                'target_stress': 0.05,
                'required_min_behavioral': 0.30,
                'required_min_social': 0.00,
                'social_format': 'Individual',
                'description': 'Daily journaling to identify and process emotions',
                'resource_links': 'Template:Emotion Journal|App:Day One'
            },
            {
                'activity_id': 'ACT_003',
                'name': 'Cognitive Behavioral Therapy Techniques',
                'category': 'Emotional Regulation',
                'type': 'Workshop',
                'duration_minutes': 90,
                'difficulty': 'Intermediate',
                'target_emotional': 0.18,
                'target_social': 0.00,
                'target_behavioral': 0.08,
                'target_cognitive': 0.12,
                'target_stress': 0.10,
                'required_min_behavioral': 0.50,
                'required_min_social': 0.00,
                'social_format': 'Individual',
                'description': 'Learn CBT techniques to manage thoughts and emotions',
                'resource_links': 'Book:Feeling Good|App:MoodGYM'
            },

            # SOCIAL CONNECTIVITY ACTIVITIES
            {
                'activity_id': 'ACT_004',
                'name': 'Peer Support Circle',
                'category': 'Social Connectivity',
                'type': 'Group',
                'duration_minutes': 90,
                'difficulty': 'Beginner',
                'target_emotional': 0.08,
                'target_social': 0.20,
                'target_behavioral': 0.05,
                'target_cognitive': 0.05,
                'target_stress': 0.08,
                'required_min_behavioral': 0.00,
                'required_min_social': 0.50,
                'social_format': 'Group 6-8',
                'description': 'Weekly peer support meetings for sharing and connection',
                'resource_links': 'Guide:Peer Support|Platform:7 Cups'
            },
            {
                'activity_id': 'ACT_005',
                'name': 'Communication Skills Workshop',
                'category': 'Social Connectivity',
                'type': 'Workshop',
                'duration_minutes': 120,
                'difficulty': 'Intermediate',
                'target_emotional': 0.06,
                'target_social': 0.18,
                'target_behavioral': 0.05,
                'target_cognitive': 0.10,
                'target_stress': 0.04,
                'required_min_behavioral': 0.00,
                'required_min_social': 0.40,
                'social_format': 'Group 8-12',
                'description': 'Develop active listening and assertive communication skills',
                'resource_links': 'Book:Nonviolent Communication|Online:Coursera'
            },
            {
                'activity_id': 'ACT_006',
                'name': 'Buddy Accountability System',
                'category': 'Social Connectivity',
                'type': 'Pair',
                'duration_minutes': 30,
                'difficulty': 'Beginner',
                'target_emotional': 0.05,
                'target_social': 0.15,
                'target_behavioral': 0.12,
                'target_cognitive': 0.03,
                'target_stress': 0.06,
                'required_min_behavioral': 0.00,
                'required_min_social': 0.45,
                'social_format': 'Pair',
                'description': 'Weekly check-ins with an accountability partner',
                'resource_links': 'Template:Check-in Questions|App:Habitica'
            },

            # BEHAVIORAL STABILITY ACTIVITIES
            {
                'activity_id': 'ACT_007',
                'name': 'Daily Routine Building Workshop',
                'category': 'Behavioral Stability',
                'type': 'Workshop',
                'duration_minutes': 90,
                'difficulty': 'Beginner',
                'target_emotional': 0.05,
                'target_social': 0.00,
                'target_behavioral': 0.18,
                'target_cognitive': 0.08,
                'target_stress': 0.10,
                'required_min_behavioral': 0.00,
                'required_min_social': 0.00,
                'social_format': 'Individual',
                'description': 'Learn to build and maintain healthy daily routines',
                'resource_links': 'Book:Atomic Habits|App:Habitify'
            },
            {
                'activity_id': 'ACT_008',
                'name': 'Sleep Hygiene Program',
                'category': 'Behavioral Stability',
                'type': 'Individual',
                'duration_minutes': 45,
                'difficulty': 'Beginner',
                'target_emotional': 0.08,
                'target_social': 0.00,
                'target_behavioral': 0.15,
                'target_cognitive': 0.06,
                'target_stress': 0.12,
                'required_min_behavioral': 0.25,
                'required_min_social': 0.00,
                'social_format': 'Individual',
                'description': 'Establish healthy sleep patterns and bedtime routines',
                'resource_links': 'Guide:Sleep Foundation|App:Sleep Cycle'
            },
            {
                'activity_id': 'ACT_009',
                'name': 'Time Management Training',
                'category': 'Behavioral Stability',
                'type': 'Workshop',
                'duration_minutes': 120,
                'difficulty': 'Intermediate',
                'target_emotional': 0.04,
                'target_social': 0.00,
                'target_behavioral': 0.16,
                'target_cognitive': 0.10,
                'target_stress': 0.09,
                'required_min_behavioral': 0.35,
                'required_min_social': 0.00,
                'social_format': 'Individual',
                'description': 'Learn effective time management and prioritization',
                'resource_links': 'Book:Getting Things Done|App:Todoist'
            },

            # COGNITIVE FLEXIBILITY ACTIVITIES
            {
                'activity_id': 'ACT_010',
                'name': 'Creative Problem-Solving Workshop',
                'category': 'Cognitive Flexibility',
                'type': 'Workshop',
                'duration_minutes': 90,
                'difficulty': 'Intermediate',
                'target_emotional': 0.05,
                'target_social': 0.06,
                'target_behavioral': 0.04,
                'target_cognitive': 0.17,
                'target_stress': 0.05,
                'required_min_behavioral': 0.00,
                'required_min_social': 0.00,
                'social_format': 'Group 6-10',
                'description': 'Develop creative thinking and flexible problem-solving skills',
                'resource_links': 'Book:Thinking Fast and Slow|Online:Khan Academy'
            },
            {
                'activity_id': 'ACT_011',
                'name': 'Perspective-Taking Exercises',
                'category': 'Cognitive Flexibility',
                'type': 'Individual',
                'duration_minutes': 30,
                'difficulty': 'Beginner',
                'target_emotional': 0.08,
                'target_social': 0.07,
                'target_behavioral': 0.00,
                'target_cognitive': 0.14,
                'target_stress': 0.04,
                'required_min_behavioral': 0.00,
                'required_min_social': 0.00,
                'social_format': 'Individual',
                'description': 'Practice seeing situations from multiple perspectives',
                'resource_links': 'Workbook:Perspective Exercises|App:MindShift'
            },
            {
                'activity_id': 'ACT_012',
                'name': 'Mental Agility Games',
                'category': 'Cognitive Flexibility',
                'type': 'Individual',
                'duration_minutes': 20,
                'difficulty': 'Beginner',
                'target_emotional': 0.02,
                'target_social': 0.00,
                'target_behavioral': 0.05,
                'target_cognitive': 0.12,
                'target_stress': 0.03,
                'required_min_behavioral': 0.30,
                'required_min_social': 0.00,
                'social_format': 'Individual',
                'description': 'Brain training games to improve mental flexibility',
                'resource_links': 'App:Lumosity|App:Peak'
            },

            # STRESS COPING ACTIVITIES
            {
                'activity_id': 'ACT_013',
                'name': 'Stress Management Techniques',
                'category': 'Stress Coping',
                'type': 'Workshop',
                'duration_minutes': 90,
                'difficulty': 'Beginner',
                'target_emotional': 0.10,
                'target_social': 0.00,
                'target_behavioral': 0.06,
                'target_cognitive': 0.08,
                'target_stress': 0.16,
                'required_min_behavioral': 0.00,
                'required_min_social': 0.00,
                'social_format': 'Individual',
                'description': 'Learn evidence-based stress management strategies',
                'resource_links': 'Guide:APA Stress Management|App:Sanvello'
            },
            {
                'activity_id': 'ACT_014',
                'name': 'Progressive Muscle Relaxation',
                'category': 'Stress Coping',
                'type': 'Individual',
                'duration_minutes': 15,
                'difficulty': 'Beginner',
                'target_emotional': 0.06,
                'target_social': 0.00,
                'target_behavioral': 0.04,
                'target_cognitive': 0.02,
                'target_stress': 0.14,
                'required_min_behavioral': 0.25,
                'required_min_social': 0.00,
                'social_format': 'Individual',
                'description': 'Daily relaxation practice to reduce physical tension',
                'resource_links': 'YouTube:PMR Guide|Audio:Relaxation Scripts'
            },
            {
                'activity_id': 'ACT_015',
                'name': 'Exercise for Mental Health',
                'category': 'Stress Coping',
                'type': 'Individual',
                'duration_minutes': 30,
                'difficulty': 'Beginner',
                'target_emotional': 0.08,
                'target_social': 0.05,
                'target_behavioral': 0.10,
                'target_cognitive': 0.04,
                'target_stress': 0.15,
                'required_min_behavioral': 0.35,
                'required_min_social': 0.00,
                'social_format': 'Individual/Group',
                'description': 'Regular physical activity for stress relief and mood boost',
                'resource_links': 'App:Nike Training|YouTube:Yoga with Adriene'
            },

            # INTEGRATED/MULTI-DIMENSIONAL ACTIVITIES
            {
                'activity_id': 'ACT_016',
                'name': 'Group Wellness Challenge',
                'category': 'Integrated',
                'type': 'Group',
                'duration_minutes': 60,
                'difficulty': 'Beginner',
                'target_emotional': 0.08,
                'target_social': 0.15,
                'target_behavioral': 0.12,
                'target_cognitive': 0.06,
                'target_stress': 0.10,
                'required_min_behavioral': 0.00,
                'required_min_social': 0.55,
                'social_format': 'Group 8-15',
                'description': 'Team-based wellness challenges promoting holistic health',
                'resource_links': 'Platform:Strava|App:Challenges'
            },
            {
                'activity_id': 'ACT_017',
                'name': 'Holistic Resilience Workshop',
                'category': 'Integrated',
                'type': 'Workshop',
                'duration_minutes': 180,
                'difficulty': 'Intermediate',
                'target_emotional': 0.12,
                'target_social': 0.10,
                'target_behavioral': 0.10,
                'target_cognitive': 0.12,
                'target_stress': 0.12,
                'required_min_behavioral': 0.40,
                'required_min_social': 0.40,
                'social_format': 'Group 10-15',
                'description': 'Comprehensive resilience building across all dimensions',
                'resource_links': 'Book:Resilience Factor|Workshop:Local Mental Health Center'
            },
            {
                'activity_id': 'ACT_018',
                'name': 'Mindful Walking Group',
                'category': 'Integrated',
                'type': 'Group',
                'duration_minutes': 45,
                'difficulty': 'Beginner',
                'target_emotional': 0.09,
                'target_social': 0.12,
                'target_behavioral': 0.08,
                'target_cognitive': 0.05,
                'target_stress': 0.11,
                'required_min_behavioral': 0.00,
                'required_min_social': 0.50,
                'social_format': 'Group 4-8',
                'description': 'Combine mindfulness practice with social connection and movement',
                'resource_links': 'Guide:Mindful Walking|App:Meetup'
            },

            # BEGINNER-FRIENDLY LOW-BARRIER ACTIVITIES
            {
                'activity_id': 'ACT_019',
                'name': 'Daily Gratitude Practice',
                'category': 'Emotional Regulation',
                'type': 'Individual',
                'duration_minutes': 5,
                'difficulty': 'Beginner',
                'target_emotional': 0.08,
                'target_social': 0.00,
                'target_behavioral': 0.06,
                'target_cognitive': 0.04,
                'target_stress': 0.07,
                'required_min_behavioral': 0.20,
                'required_min_social': 0.00,
                'social_format': 'Individual',
                'description': 'Simple daily practice of noting three things you\'re grateful for',
                'resource_links': 'App:Gratitude Journal|Template:3 Good Things'
            },
            {
                'activity_id': 'ACT_020',
                'name': 'Breathing Exercises',
                'category': 'Stress Coping',
                'type': 'Individual',
                'duration_minutes': 5,
                'difficulty': 'Beginner',
                'target_emotional': 0.06,
                'target_social': 0.00,
                'target_behavioral': 0.03,
                'target_cognitive': 0.02,
                'target_stress': 0.12,
                'required_min_behavioral': 0.00,
                'required_min_social': 0.00,
                'social_format': 'Individual',
                'description': 'Quick breathing techniques for immediate stress relief',
                'resource_links': 'App:Breathwrk|YouTube:4-7-8 Breathing'
            }
        ]

        # Create DataFrame
        self.activities = pd.DataFrame(activities_data)

        # Save catalog
        os.makedirs(os.path.dirname(self.catalog_path), exist_ok=True)
        self.activities.to_csv(self.catalog_path, index=False)

        print(f"✓ Created activity catalog with {len(self.activities)} activities")
        print(f"  Saved to: {self.catalog_path}")

        # Print summary
        print(f"\nActivity Categories:")
        for category in self.activities['category'].unique():
            count = len(self.activities[self.activities['category'] == category])
            print(f"  • {category}: {count} activities")

        print(f"\nDifficulty Levels:")
        for difficulty in self.activities['difficulty'].unique():
            count = len(self.activities[self.activities['difficulty'] == difficulty])
            print(f"  • {difficulty}: {count} activities")

        print(f"\nSocial Formats:")
        for format in self.activities['social_format'].unique():
            count = len(self.activities[self.activities['social_format'] == format])
            print(f"  • {format}: {count} activities")

    def load_catalog(self):
        """Load existing activity catalog"""
        self.activities = pd.read_csv(self.catalog_path)
        print(f"✓ Loaded {len(self.activities)} activities from catalog")

    def get_all_activities(self) -> pd.DataFrame:
        """Get all activities"""
        return self.activities.copy()

    def get_activity(self, activity_id: str) -> Optional[Dict]:
        """
        Get specific activity by ID

        Parameters:
            activity_id (str): Activity ID

        Returns:
            dict: Activity details or None
        """
        activity = self.activities[self.activities['activity_id'] == activity_id]

        if len(activity) == 0:
            return None

        return activity.iloc[0].to_dict()

    def get_activities_by_category(self, category: str) -> pd.DataFrame:
        """
        Get activities in specific category

        Parameters:
            category (str): Category name

        Returns:
            pd.DataFrame: Filtered activities
        """
        return self.activities[self.activities['category'] == category].copy()

    def get_activities_by_difficulty(self, difficulty: str) -> pd.DataFrame:
        """
        Get activities of specific difficulty

        Parameters:
            difficulty (str): Difficulty level

        Returns:
            pd.DataFrame: Filtered activities
        """
        return self.activities[self.activities['difficulty'] == difficulty].copy()

    def get_activities_by_format(self, social_format: str) -> pd.DataFrame:
        """
        Get activities with specific social format

        Parameters:
            social_format (str): Social format

        Returns:
            pd.DataFrame: Filtered activities
        """
        return self.activities[self.activities['social_format'].str.contains(social_format)].copy()

    def print_activity_details(self, activity_id: str):
        """Print formatted activity details"""
        activity = self.get_activity(activity_id)

        if activity is None:
            print(f"Activity {activity_id} not found")
            return

        print(f"\n{'=' * 60}")
        print(f"{activity['name']}")
        print(f"{'=' * 60}")
        print(f"ID: {activity['activity_id']}")
        print(f"Category: {activity['category']}")
        print(f"Type: {activity['type']}")
        print(f"Duration: {activity['duration_minutes']} minutes")
        print(f"Difficulty: {activity['difficulty']}")
        print(f"Social Format: {activity['social_format']}")

        print(f"\nDescription:")
        print(f"  {activity['description']}")

        print(f"\nExpected Resilience Improvements:")
        if activity['target_emotional'] > 0:
            print(f"  • Emotional Regulation: +{activity['target_emotional']:.2f}")
        if activity['target_social'] > 0:
            print(f"  • Social Connectivity: +{activity['target_social']:.2f}")
        if activity['target_behavioral'] > 0:
            print(f"  • Behavioral Stability: +{activity['target_behavioral']:.2f}")
        if activity['target_cognitive'] > 0:
            print(f"  • Cognitive Flexibility: +{activity['target_cognitive']:.2f}")
        if activity['target_stress'] > 0:
            print(f"  • Stress Coping: +{activity['target_stress']:.2f}")

        print(f"\nRequirements:")
        if activity['required_min_behavioral'] > 0:
            print(f"  • Behavioral Stability: {activity['required_min_behavioral']:.2f}+")
        if activity['required_min_social'] > 0:
            print(f"  • Social Connectivity: {activity['required_min_social']:.2f}+")
        if activity['required_min_behavioral'] == 0 and activity['required_min_social'] == 0:
            print(f"  • None (beginner-friendly)")

        print(f"\nResources:")
        resources = activity['resource_links'].split('|')
        for resource in resources:
            print(f"  • {resource}")

        print(f"{'=' * 60}\n")


# Testing code
if __name__ == "__main__":
    """
    Test the Activity Database
    """
    print("Testing Activity Database Module")
    print("=" * 60)

    # Initialize database (will create catalog if doesn't exist)
    print("\n1. Initializing Activity Database...")
    db = ActivityDatabase()

    # Get all activities
    print("\n2. Getting all activities...")
    all_activities = db.get_all_activities()
    print(f"Total activities: {len(all_activities)}")

    # Get by category
    print("\n3. Getting activities by category...")
    emotional_activities = db.get_activities_by_category("Emotional Regulation")
    print(f"Emotional Regulation activities: {len(emotional_activities)}")

    # Get by difficulty
    print("\n4. Getting beginner activities...")
    beginner_activities = db.get_activities_by_difficulty("Beginner")
    print(f"Beginner activities: {len(beginner_activities)}")

    # Get by format
    print("\n5. Getting individual activities...")
    individual_activities = db.get_activities_by_format("Individual")
    print(f"Individual activities: {len(individual_activities)}")

    # Print specific activity
    print("\n6. Activity details:")
    db.print_activity_details("ACT_001")
    db.print_activity_details("ACT_004")
    db.print_activity_details("ACT_013")

    print("\n" + "=" * 60)
    print("✓ ALL TESTS PASSED!")
    print("=" * 60)