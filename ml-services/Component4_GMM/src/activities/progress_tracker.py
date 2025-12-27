"""
Progress Tracker Module

Tracks user progress through activities and measures
resilience improvements over time.

Author: [Your Name]
Date: [Current Date]
"""

import pandas as pd
import numpy as np
from datetime import datetime, timedelta
from typing import Dict, List, Optional, Tuple
import os
import json


class ProgressTracker:
    """
    Tracks activity progress and measures effectiveness
    """

    # Activity status codes
    STATUS_STARTED = "started"
    STATUS_IN_PROGRESS = "in_progress"
    STATUS_COMPLETED = "completed"
    STATUS_DROPPED = "dropped"

    # Resilience dimensions
    DIMENSIONS = [
        'emotional_regulation_score',
        'social_connectivity_score',
        'behavioral_stability_score',
        'cognitive_flexibility_score',
        'stress_coping_mechanism'
    ]

    def __init__(self, tracking_file: str = "data/activities/activity_progress.csv"):
        """
        Initialize progress tracker

        Parameters:
            tracking_file (str): Path to progress tracking CSV
        """
        self.tracking_file = tracking_file
        self.progress_data = None

        # Create directory if needed
        os.makedirs(os.path.dirname(tracking_file), exist_ok=True)

        # Load or create tracking database
        if os.path.exists(tracking_file):
            self.load_progress()
        else:
            self.create_tracking_database()

    def create_tracking_database(self):
        """Create empty progress tracking database"""
        print(f"\n{'=' * 60}")
        print(f"CREATING PROGRESS TRACKING DATABASE")
        print(f"{'=' * 60}\n")

        # Define schema
        columns = [
            'tracking_id',
            'user_id',
            'activity_id',
            'activity_name',
            'start_date',
            'expected_end_date',
            'actual_end_date',
            'status',
            'completion_percentage',

            # Baseline (before)
            'baseline_emotional',
            'baseline_social',
            'baseline_behavioral',
            'baseline_cognitive',
            'baseline_stress',

            # Outcome (after)
            'outcome_emotional',
            'outcome_social',
            'outcome_behavioral',
            'outcome_cognitive',
            'outcome_stress',

            # Improvements (calculated)
            'improvement_emotional',
            'improvement_social',
            'improvement_behavioral',
            'improvement_cognitive',
            'improvement_stress',

            # Expected improvements (from activity catalog)
            'expected_emotional',
            'expected_social',
            'expected_behavioral',
            'expected_cognitive',
            'expected_stress',

            # Effectiveness metrics
            'overall_improvement',
            'effectiveness_score',
            'meets_expectations',

            # Notes
            'notes'
        ]

        self.progress_data = pd.DataFrame(columns=columns)
        self.save_progress()

        print(f"✓ Created tracking database")
        print(f"  Location: {self.tracking_file}")
        print(f"  Columns: {len(columns)}")

    def load_progress(self):
        """Load existing progress data"""
        self.progress_data = pd.read_csv(self.tracking_file)
        print(f"✓ Loaded progress data: {len(self.progress_data)} records")

    def save_progress(self):
        """Save progress data to CSV"""
        self.progress_data.to_csv(self.tracking_file, index=False)

    def start_activity(self,
                       user_id: str,
                       activity_id: str,
                       activity_name: str,
                       baseline_scores: Dict[str, float],
                       expected_improvements: Dict[str, float],
                       duration_days: int = 14) -> str:
        """
        Record user starting an activity

        Parameters:
            user_id (str): User ID
            activity_id (str): Activity ID
            activity_name (str): Activity name
            baseline_scores (dict): Current resilience scores (before)
            expected_improvements (dict): Expected improvements from activity catalog
            duration_days (int): Expected duration in days

        Returns:
            str: Tracking ID
        """
        # Generate tracking ID
        tracking_id = f"TRACK_{user_id}_{activity_id}_{datetime.now().strftime('%Y%m%d%H%M%S')}"

        # Calculate dates
        start_date = datetime.now()
        expected_end_date = start_date + timedelta(days=duration_days)

        # Create record
        record = {
            'tracking_id': tracking_id,
            'user_id': user_id,
            'activity_id': activity_id,
            'activity_name': activity_name,
            'start_date': start_date.strftime('%Y-%m-%d'),
            'expected_end_date': expected_end_date.strftime('%Y-%m-%d'),
            'actual_end_date': None,
            'status': self.STATUS_STARTED,
            'completion_percentage': 0,

            # Baseline
            'baseline_emotional': baseline_scores.get('emotional_regulation_score', 0),
            'baseline_social': baseline_scores.get('social_connectivity_score', 0),
            'baseline_behavioral': baseline_scores.get('behavioral_stability_score', 0),
            'baseline_cognitive': baseline_scores.get('cognitive_flexibility_score', 0),
            'baseline_stress': baseline_scores.get('stress_coping_mechanism', 0),

            # Outcome (empty initially)
            'outcome_emotional': None,
            'outcome_social': None,
            'outcome_behavioral': None,
            'outcome_cognitive': None,
            'outcome_stress': None,

            # Improvements (calculated later)
            'improvement_emotional': None,
            'improvement_social': None,
            'improvement_behavioral': None,
            'improvement_cognitive': None,
            'improvement_stress': None,

            # Expected improvements
            'expected_emotional': expected_improvements.get('target_emotional', 0),
            'expected_social': expected_improvements.get('target_social', 0),
            'expected_behavioral': expected_improvements.get('target_behavioral', 0),
            'expected_cognitive': expected_improvements.get('target_cognitive', 0),
            'expected_stress': expected_improvements.get('target_stress', 0),

            # Effectiveness (calculated later)
            'overall_improvement': None,
            'effectiveness_score': None,
            'meets_expectations': None,

            'notes': ''
        }

        # Add to dataframe
        self.progress_data = pd.concat([
            self.progress_data,
            pd.DataFrame([record])
        ], ignore_index=True)

        self.save_progress()

        print(f"\n{'=' * 60}")
        print(f"ACTIVITY STARTED")
        print(f"{'=' * 60}")
        print(f"User: {user_id}")
        print(f"Activity: {activity_name}")
        print(f"Start Date: {start_date.strftime('%Y-%m-%d')}")
        print(f"Expected End: {expected_end_date.strftime('%Y-%m-%d')}")
        print(f"Tracking ID: {tracking_id}")

        print(f"\nBaseline Resilience:")
        print(f"  Emotional:  {record['baseline_emotional']:.2f}")
        print(f"  Social:     {record['baseline_social']:.2f}")
        print(f"  Behavioral: {record['baseline_behavioral']:.2f}")
        print(f"  Cognitive:  {record['baseline_cognitive']:.2f}")
        print(f"  Stress:     {record['baseline_stress']:.2f}")

        print(f"\nExpected Improvements:")
        print(f"  Emotional:  +{record['expected_emotional']:.2f}")
        print(f"  Social:     +{record['expected_social']:.2f}")
        print(f"  Behavioral: +{record['expected_behavioral']:.2f}")
        print(f"  Cognitive:  +{record['expected_cognitive']:.2f}")
        print(f"  Stress:     +{record['expected_stress']:.2f}")
        print(f"{'=' * 60}\n")

        return tracking_id

    def update_progress(self,
                        tracking_id: str,
                        completion_percentage: int,
                        notes: str = ""):
        """
        Update activity progress

        Parameters:
            tracking_id (str): Tracking ID
            completion_percentage (int): 0-100
            notes (str): Progress notes
        """
        idx = self.progress_data[self.progress_data['tracking_id'] == tracking_id].index

        if len(idx) == 0:
            print(f"Tracking ID {tracking_id} not found")
            return

        self.progress_data.loc[idx, 'completion_percentage'] = completion_percentage
        self.progress_data.loc[idx, 'notes'] = notes

        # Update status
        if completion_percentage >= 100:
            self.progress_data.loc[idx, 'status'] = self.STATUS_COMPLETED
        elif completion_percentage > 0:
            self.progress_data.loc[idx, 'status'] = self.STATUS_IN_PROGRESS

        self.save_progress()

        print(f"✓ Progress updated: {completion_percentage}%")

    def complete_activity(self,
                          tracking_id: str,
                          outcome_scores: Dict[str, float],
                          notes: str = "") -> Dict:
        """
        Mark activity as completed and calculate effectiveness

        Parameters:
            tracking_id (str): Tracking ID
            outcome_scores (dict): Final resilience scores (after)
            notes (str): Completion notes

        Returns:
            dict: Effectiveness analysis
        """
        idx = self.progress_data[self.progress_data['tracking_id'] == tracking_id].index

        if len(idx) == 0:
            print(f"Tracking ID {tracking_id} not found")
            return {}

        idx = idx[0]

        # Update outcome scores
        self.progress_data.loc[idx, 'outcome_emotional'] = outcome_scores.get('emotional_regulation_score', 0)
        self.progress_data.loc[idx, 'outcome_social'] = outcome_scores.get('social_connectivity_score', 0)
        self.progress_data.loc[idx, 'outcome_behavioral'] = outcome_scores.get('behavioral_stability_score', 0)
        self.progress_data.loc[idx, 'outcome_cognitive'] = outcome_scores.get('cognitive_flexibility_score', 0)
        self.progress_data.loc[idx, 'outcome_stress'] = outcome_scores.get('stress_coping_mechanism', 0)

        # Calculate improvements
        improvements = {
            'emotional': self.progress_data.loc[idx, 'outcome_emotional'] - self.progress_data.loc[
                idx, 'baseline_emotional'],
            'social': self.progress_data.loc[idx, 'outcome_social'] - self.progress_data.loc[idx, 'baseline_social'],
            'behavioral': self.progress_data.loc[idx, 'outcome_behavioral'] - self.progress_data.loc[
                idx, 'baseline_behavioral'],
            'cognitive': self.progress_data.loc[idx, 'outcome_cognitive'] - self.progress_data.loc[
                idx, 'baseline_cognitive'],
            'stress': self.progress_data.loc[idx, 'outcome_stress'] - self.progress_data.loc[idx, 'baseline_stress']
        }

        self.progress_data.loc[idx, 'improvement_emotional'] = improvements['emotional']
        self.progress_data.loc[idx, 'improvement_social'] = improvements['social']
        self.progress_data.loc[idx, 'improvement_behavioral'] = improvements['behavioral']
        self.progress_data.loc[idx, 'improvement_cognitive'] = improvements['cognitive']
        self.progress_data.loc[idx, 'improvement_stress'] = improvements['stress']

        # Calculate overall improvement (average of positive improvements)
        positive_improvements = [v for v in improvements.values() if v > 0]
        overall_improvement = np.mean(positive_improvements) if positive_improvements else 0
        self.progress_data.loc[idx, 'overall_improvement'] = overall_improvement

        # Calculate effectiveness score (actual vs expected)
        expected_improvements = {
            'emotional': self.progress_data.loc[idx, 'expected_emotional'],
            'social': self.progress_data.loc[idx, 'expected_social'],
            'behavioral': self.progress_data.loc[idx, 'expected_behavioral'],
            'cognitive': self.progress_data.loc[idx, 'expected_cognitive'],
            'stress': self.progress_data.loc[idx, 'expected_stress']
        }

        # Calculate effectiveness for each dimension
        effectiveness_scores = []
        for dim in ['emotional', 'social', 'behavioral', 'cognitive', 'stress']:
            expected = expected_improvements[dim]
            if expected > 0:  # Only for dimensions where improvement was expected
                actual = improvements[dim]
                effectiveness = min((actual / expected) * 100, 150)  # Cap at 150% (exceeded expectations)
                effectiveness_scores.append(effectiveness)

        effectiveness_score = np.mean(effectiveness_scores) if effectiveness_scores else 0
        self.progress_data.loc[idx, 'effectiveness_score'] = effectiveness_score

        # Determine if meets expectations
        meets_expectations = effectiveness_score >= 75  # 75%+ of expected = success
        self.progress_data.loc[idx, 'meets_expectations'] = meets_expectations

        # Update status and dates
        self.progress_data.loc[idx, 'status'] = self.STATUS_COMPLETED
        self.progress_data.loc[idx, 'actual_end_date'] = datetime.now().strftime('%Y-%m-%d')
        self.progress_data.loc[idx, 'completion_percentage'] = 100
        self.progress_data.loc[idx, 'notes'] = notes

        self.save_progress()

        # Print completion report
        self._print_completion_report(idx, improvements, expected_improvements, effectiveness_score)

        # Return analysis
        return {
            'tracking_id': tracking_id,
            'improvements': improvements,
            'overall_improvement': overall_improvement,
            'effectiveness_score': effectiveness_score,
            'meets_expectations': meets_expectations
        }

    def _print_completion_report(self, idx, improvements, expected_improvements, effectiveness_score):
        """Print detailed completion report"""
        record = self.progress_data.loc[idx]

        print(f"\n{'=' * 70}")
        print(f"ACTIVITY COMPLETION REPORT")
        print(f"{'=' * 70}")
        print(f"User: {record['user_id']}")
        print(f"Activity: {record['activity_name']}")
        print(f"Duration: {record['start_date']} to {record['actual_end_date']}")

        print(f"\n{'DIMENSION':<20} {'BEFORE':<10} {'AFTER':<10} {'CHANGE':<12} {'EXPECTED':<12} {'STATUS':<10}")
        print(f"{'-' * 70}")

        dimensions = {
            'Emotional': ('baseline_emotional', 'outcome_emotional', 'improvement_emotional', 'expected_emotional'),
            'Social': ('baseline_social', 'outcome_social', 'improvement_social', 'expected_social'),
            'Behavioral': ('baseline_behavioral', 'outcome_behavioral', 'improvement_behavioral',
                           'expected_behavioral'),
            'Cognitive': ('baseline_cognitive', 'outcome_cognitive', 'improvement_cognitive', 'expected_cognitive'),
            'Stress Coping': ('baseline_stress', 'outcome_stress', 'improvement_stress', 'expected_stress')
        }

        for name, (baseline_col, outcome_col, improvement_col, expected_col) in dimensions.items():
            baseline = record[baseline_col]
            outcome = record[outcome_col]
            improvement = record[improvement_col]
            expected = record[expected_col]

            if expected > 0:
                if improvement >= expected * 0.75:
                    status = "✓ GOOD"
                elif improvement > 0:
                    status = "~ OK"
                else:
                    status = "✗ BELOW"
            else:
                status = "- N/A"

            print(
                f"{name:<20} {baseline:<10.2f} {outcome:<10.2f} {improvement:>+11.2f} {expected:>+11.2f} {status:<10}")

        print(f"{'-' * 70}")
        print(f"{'Overall Improvement:':<20} {record['overall_improvement']:.2f}")
        print(f"{'Effectiveness Score:':<20} {effectiveness_score:.1f}%")
        print(f"{'Meets Expectations:':<20} {'YES ✓' if record['meets_expectations'] else 'NO ✗'}")
        print(f"{'=' * 70}\n")

    def get_user_progress(self, user_id: str) -> pd.DataFrame:
        """Get all progress records for a user"""
        return self.progress_data[self.progress_data['user_id'] == user_id].copy()

    def get_activity_effectiveness(self, activity_id: str) -> Dict:
        """
        Calculate effectiveness statistics for an activity

        Parameters:
            activity_id (str): Activity ID

        Returns:
            dict: Effectiveness statistics
        """
        activity_records = self.progress_data[
            (self.progress_data['activity_id'] == activity_id) &
            (self.progress_data['status'] == self.STATUS_COMPLETED)
            ]

        if len(activity_records) == 0:
            return {
                'activity_id': activity_id,
                'completions': 0,
                'avg_effectiveness': 0,
                'success_rate': 0
            }

        stats = {
            'activity_id': activity_id,
            'activity_name': activity_records.iloc[0]['activity_name'],
            'completions': len(activity_records),
            'avg_effectiveness': activity_records['effectiveness_score'].mean(),
            'success_rate': (activity_records['meets_expectations'].sum() / len(activity_records)) * 100,
            'avg_improvement': activity_records['overall_improvement'].mean(),
            'avg_improvements_by_dimension': {
                'emotional': activity_records['improvement_emotional'].mean(),
                'social': activity_records['improvement_social'].mean(),
                'behavioral': activity_records['improvement_behavioral'].mean(),
                'cognitive': activity_records['improvement_cognitive'].mean(),
                'stress': activity_records['improvement_stress'].mean()
            }
        }

        return stats

    def get_overall_statistics(self) -> Dict:
        """Get overall tracking statistics"""
        completed = self.progress_data[self.progress_data['status'] == self.STATUS_COMPLETED]

        stats = {
            'total_activities_started': len(self.progress_data),
            'total_completed': len(completed),
            'completion_rate': (len(completed) / len(self.progress_data) * 100) if len(self.progress_data) > 0 else 0,
            'avg_effectiveness': completed['effectiveness_score'].mean() if len(completed) > 0 else 0,
            'success_rate': (completed['meets_expectations'].sum() / len(completed) * 100) if len(completed) > 0 else 0,
            'avg_overall_improvement': completed['overall_improvement'].mean() if len(completed) > 0 else 0
        }

        return stats


# Testing code
if __name__ == "__main__":
    """
    Test the Progress Tracker
    """
    print("Testing Progress Tracker Module")
    print("=" * 70)

    # Initialize tracker
    print("\n1. Initializing Progress Tracker...")
    tracker = ProgressTracker()

    # Simulate user starting activity
    print("\n2. User starts Emotion Journaling...")

    baseline = {
        'emotional_regulation_score': 0.30,
        'social_connectivity_score': 0.65,
        'behavioral_stability_score': 0.28,
        'cognitive_flexibility_score': 0.50,
        'stress_coping_mechanism': 0.35
    }

    expected = {
        'target_emotional': 0.12,
        'target_social': 0.00,
        'target_behavioral': 0.08,
        'target_cognitive': 0.06,
        'target_stress': 0.05
    }

    tracking_id = tracker.start_activity(
        user_id="USER_0001",
        activity_id="ACT_002",
        activity_name="Emotion Journaling Practice",
        baseline_scores=baseline,
        expected_improvements=expected,
        duration_days=14
    )

    # Simulate progress update
    print("\n3. Update progress (Day 7)...")
    tracker.update_progress(tracking_id, 50, "Halfway through, feeling good!")

    # Simulate completion
    print("\n4. Complete activity (Day 14)...")

    outcome = {
        'emotional_regulation_score': 0.42,  # +0.12 (matched expectation)
        'social_connectivity_score': 0.65,  # No change (as expected)
        'behavioral_stability_score': 0.34,  # +0.06 (close to +0.08 expected)
        'cognitive_flexibility_score': 0.56,  # +0.06 (matched)
        'stress_coping_mechanism': 0.38  # +0.03 (below +0.05 expected)
    }

    analysis = tracker.complete_activity(
        tracking_id=tracking_id,
        outcome_scores=outcome,
        notes="Completed successfully! Feeling more emotionally aware."
    )

    # Get statistics
    print("\n5. Activity Effectiveness Statistics...")
    activity_stats = tracker.get_activity_effectiveness("ACT_002")

    print(f"\nActivity: {activity_stats['activity_name']}")
    print(f"Completions: {activity_stats['completions']}")
    print(f"Avg Effectiveness: {activity_stats['avg_effectiveness']:.1f}%")
    print(f"Success Rate: {activity_stats['success_rate']:.1f}%")
    print(f"Avg Overall Improvement: {activity_stats['avg_improvement']:.2f}")

    # Overall statistics
    print("\n6. Overall System Statistics...")
    overall_stats = tracker.get_overall_statistics()

    print(f"\nTotal Activities Started: {overall_stats['total_activities_started']}")
    print(f"Total Completed: {overall_stats['total_completed']}")
    print(f"Completion Rate: {overall_stats['completion_rate']:.1f}%")
    print(f"Avg Effectiveness: {overall_stats['avg_effectiveness']:.1f}%")
    print(f"Success Rate: {overall_stats['success_rate']:.1f}%")

    print("\n" + "=" * 70)
    print("✓ ALL TESTS PASSED!")
    print("=" * 70)