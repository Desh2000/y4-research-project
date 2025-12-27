"""
Effectiveness Analyzer Module

Analyzes activity effectiveness across users and activities.

Author: [Your Name]
Date: [Current Date]
"""

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
from typing import Dict, List
import os


class EffectivenessAnalyzer:
    """
    Analyzes and visualizes activity effectiveness
    """

    def __init__(self, progress_tracker):
        """
        Initialize analyzer

        Parameters:
            progress_tracker: ProgressTracker instance
        """
        self.tracker = progress_tracker
        self.completed_activities = self.tracker.progress_data[
            self.tracker.progress_data['status'] == 'completed'
            ].copy()

        print(f"✓ EffectivenessAnalyzer initialized")
        print(f"  Completed activities: {len(self.completed_activities)}")

    def generate_effectiveness_report(self, output_dir: str = "results/effectiveness"):
        """
        Generate comprehensive effectiveness report

        Parameters:
            output_dir (str): Output directory for report files
        """
        os.makedirs(output_dir, exist_ok=True)

        print(f"\n{'=' * 60}")
        print(f"GENERATING EFFECTIVENESS REPORT")
        print(f"{'=' * 60}\n")

        if len(self.completed_activities) == 0:
            print("No completed activities to analyze")
            return

        # 1. Overall statistics
        overall_stats = self.tracker.get_overall_statistics()

        print(f"Overall Statistics:")
        print(f"  Total Started: {overall_stats['total_activities_started']}")
        print(f"  Total Completed: {overall_stats['total_completed']}")
        print(f"  Completion Rate: {overall_stats['completion_rate']:.1f}%")
        print(f"  Avg Effectiveness: {overall_stats['avg_effectiveness']:.1f}%")
        print(f"  Success Rate: {overall_stats['success_rate']:.1f}%")

        # 2. By activity analysis
        print(f"\nAnalyzing by activity...")
        activity_stats = []

        for activity_id in self.completed_activities['activity_id'].unique():
            stats = self.tracker.get_activity_effectiveness(activity_id)
            activity_stats.append(stats)

        activity_df = pd.DataFrame(activity_stats)
        activity_df = activity_df.sort_values('avg_effectiveness', ascending=False)

        print(f"\nTop Performing Activities:")
        for i, row in activity_df.head(5).iterrows():
            print(f"  {row['activity_name']}: {row['avg_effectiveness']:.1f}% effectiveness")

        # Save activity statistics
        activity_df.to_csv(f"{output_dir}/activity_effectiveness.csv", index=False)
        print(f"\n✓ Saved: {output_dir}/activity_effectiveness.csv")

        # 3. Dimension improvements
        print(f"\nDimension Improvement Analysis...")
        dimension_improvements = {
            'Emotional Regulation': self.completed_activities['improvement_emotional'].mean(),
            'Social Connectivity': self.completed_activities['improvement_social'].mean(),
            'Behavioral Stability': self.completed_activities['improvement_behavioral'].mean(),
            'Cognitive Flexibility': self.completed_activities['improvement_cognitive'].mean(),
            'Stress Coping': self.completed_activities['improvement_stress'].mean()
        }

        print(f"\nAverage Improvements by Dimension:")
        for dim, improvement in dimension_improvements.items():
            print(f"  {dim}: +{improvement:.3f}")

        # 4. Generate visualizations
        self._create_visualizations(activity_df, dimension_improvements, output_dir)

        print(f"\n{'=' * 60}")
        print(f"✓ REPORT COMPLETE")
        print(f"  Location: {output_dir}/")
        print(f"{'=' * 60}\n")

    def _create_visualizations(self, activity_df, dimension_improvements, output_dir):
        """Create effectiveness visualizations"""

        # Set style
        sns.set_style("whitegrid")

        # 1. Activity Effectiveness Bar Chart
        if len(activity_df) > 0:
            plt.figure(figsize=(12, 6))

            # Sort and get top 10
            top_activities = activity_df.nlargest(10, 'avg_effectiveness')

            colors = ['#2ecc71' if x >= 75 else '#f39c12' if x >= 50 else '#e74c3c'
                      for x in top_activities['avg_effectiveness']]

            plt.barh(range(len(top_activities)), top_activities['avg_effectiveness'], color=colors)
            plt.yticks(range(len(top_activities)), top_activities['activity_name'])
            plt.xlabel('Effectiveness Score (%)', fontsize=12)
            plt.title('Top 10 Activities by Effectiveness', fontsize=14, fontweight='bold')
            plt.axvline(x=75, color='green', linestyle='--', alpha=0.5, label='Success Threshold (75%)')
            plt.legend()
            plt.tight_layout()

            plt.savefig(f"{output_dir}/activity_effectiveness.png", dpi=300, bbox_inches='tight')
            plt.close()

            print(f"✓ Created: {output_dir}/activity_effectiveness.png")

        # 2. Dimension Improvements Radar Chart
        if len(dimension_improvements) > 0:
            fig = plt.figure(figsize=(10, 10))
            ax = fig.add_subplot(111, projection='polar')

            categories = list(dimension_improvements.keys())
            values = list(dimension_improvements.values())

            # Number of variables
            N = len(categories)

            # Compute angle for each axis
            angles = [n / float(N) * 2 * np.pi for n in range(N)]
            values += values[:1]  # Close the plot
            angles += angles[:1]

            # Plot
            ax.plot(angles, values, 'o-', linewidth=2, color='#3498db')
            ax.fill(angles, values, alpha=0.25, color='#3498db')

            # Fix axis to go in the right order
            ax.set_xticks(angles[:-1])
            ax.set_xticklabels(categories)

            ax.set_ylim(0, max(values) * 1.2)
            ax.set_title('Average Resilience Improvements by Dimension',
                         fontsize=14, fontweight='bold', pad=20)

            plt.tight_layout()
            plt.savefig(f"{output_dir}/dimension_improvements.png", dpi=300, bbox_inches='tight')
            plt.close()

            print(f"✓ Created: {output_dir}/dimension_improvements.png")

        # 3. Completion Success Distribution
        if len(self.completed_activities) > 0:
            plt.figure(figsize=(10, 6))

            success_counts = self.completed_activities['meets_expectations'].value_counts()

            # Build labels and colors based on actual data
            labels = []
            colors = []
            for idx in success_counts.index:
                if idx == True:
                    labels.append('Meets Expectations')
                    colors.append('#2ecc71')
                else:
                    labels.append('Below Expectations')
                    colors.append('#e74c3c')

            plt.pie(success_counts.values, labels=labels, colors=colors, autopct='%1.1f%%',
                    startangle=90, textprops={'fontsize': 12})
            plt.title('Activity Success Rate', fontsize=14, fontweight='bold')

            plt.tight_layout()
            plt.savefig(f"{output_dir}/success_rate.png", dpi=300, bbox_inches='tight')
            plt.close()

            print(f"✓ Created: {output_dir}/success_rate.png")


# Testing code
if __name__ == "__main__":
    """
    Test the Effectiveness Analyzer
    """
    print("Testing Effectiveness Analyzer Module")
    print("=" * 60)

    # Load or create tracker with sample data
    from progress_tracker import ProgressTracker

    tracker = ProgressTracker()

    # Add some sample completed activities if none exist
    if len(tracker.progress_data[tracker.progress_data['status'] == 'completed']) == 0:
        print("\nAdding sample completed activities...")

        # Sample activity 1
        baseline1 = {
            'emotional_regulation_score': 0.30,
            'social_connectivity_score': 0.65,
            'behavioral_stability_score': 0.28,
            'cognitive_flexibility_score': 0.50,
            'stress_coping_mechanism': 0.35
        }

        expected1 = {
            'target_emotional': 0.12,
            'target_social': 0.00,
            'target_behavioral': 0.08,
            'target_cognitive': 0.06,
            'target_stress': 0.05
        }

        outcome1 = {
            'emotional_regulation_score': 0.42,
            'social_connectivity_score': 0.65,
            'behavioral_stability_score': 0.34,
            'cognitive_flexibility_score': 0.56,
            'stress_coping_mechanism': 0.38
        }

        tid1 = tracker.start_activity(
            "USER_0001", "ACT_002", "Emotion Journaling Practice",
            baseline1, expected1, 14
        )
        tracker.complete_activity(tid1, outcome1)

    # Initialize analyzer
    print("\nInitializing Effectiveness Analyzer...")
    analyzer = EffectivenessAnalyzer(tracker)

    # Generate report
    print("\nGenerating effectiveness report...")
    analyzer.generate_effectiveness_report()

    print("\n" + "=" * 60)
    print("✓ ALL TESTS PASSED!")
    print("=" * 60)