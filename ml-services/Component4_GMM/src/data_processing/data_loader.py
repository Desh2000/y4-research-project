"""
Data Loader Module

This module handles loading data from CSV files and basic validation.
It provides a clean interface for reading resilience indicator data.

Author: [Your Name]
Date: [Current Date]
"""

import pandas as pd
import os
from typing import Tuple, List


class DataLoader:
    """
    Handles loading and basic validation of resilience indicator data

    Attributes:
        data_dir (str): Path to the data directory
        required_columns (list): List of column names that must exist
    """

    def __init__(self, data_dir: str = "data"):
        """
        Initialize the DataLoader

        Parameters:
            data_dir (str): Path to the data directory (default: "data")
        """
        self.data_dir = data_dir

        # Define required columns for resilience indicators
        self.required_columns = [
            'user_id',
            'emotional_regulation_score',
            'social_connectivity_score',
            'behavioral_stability_score',
            'cognitive_flexibility_score',
            'stress_coping_mechanism'
        ]

        print(f"✓ DataLoader initialized")
        print(f"  Data directory: {self.data_dir}")
        print(f"  Required columns: {len(self.required_columns)}")

    def load_csv(self, filepath: str) -> pd.DataFrame:
        """
        Load data from a CSV file

        Parameters:
            filepath (str): Path to the CSV file (relative to project root)

        Returns:
            pd.DataFrame: Loaded data

        Raises:
            FileNotFoundError: If file doesn't exist
            ValueError: If file is empty
        """
        print(f"\n--- Loading Data ---")
        print(f"File: {filepath}")

        # Check if file exists
        if not os.path.exists(filepath):
            raise FileNotFoundError(f"File not found: {filepath}")

        # Load CSV
        try:
            df = pd.read_csv(filepath)
            print(f"✓ Loaded {len(df)} rows, {len(df.columns)} columns")
        except Exception as e:
            raise ValueError(f"Error reading CSV: {str(e)}")

        # Check if empty
        if len(df) == 0:
            raise ValueError(f"File is empty: {filepath}")

        print(f"✓ Data loaded successfully")
        return df

    def validate_columns(self, df: pd.DataFrame) -> Tuple[bool, List[str]]:
        """
        Validate that required columns exist in the dataframe

        Parameters:
            df (pd.DataFrame): Data to validate

        Returns:
            Tuple[bool, List[str]]: (is_valid, list_of_missing_columns)
        """
        print(f"\n--- Validating Columns ---")

        # Get actual columns
        actual_columns = set(df.columns)
        required_columns = set(self.required_columns)

        # Find missing columns
        missing_columns = required_columns - actual_columns

        # Check if valid
        is_valid = len(missing_columns) == 0

        if is_valid:
            print(f"✓ All required columns present")
        else:
            print(f"✗ Missing columns: {missing_columns}")

        return is_valid, list(missing_columns)

    def get_feature_columns(self) -> List[str]:
        """
        Get list of feature columns (excluding user_id)

        Returns:
            List[str]: List of feature column names
        """
        # Return all columns except user_id
        return [col for col in self.required_columns if col != 'user_id']

    def load_and_validate(self, filepath: str) -> pd.DataFrame:
        """
        Load data and validate it in one step

        Parameters:
            filepath (str): Path to CSV file

        Returns:
            pd.DataFrame: Loaded and validated data

        Raises:
            ValueError: If validation fails
        """
        # Load data
        df = self.load_csv(filepath)

        # Validate columns
        is_valid, missing_cols = self.validate_columns(df)

        if not is_valid:
            raise ValueError(f"Validation failed. Missing columns: {missing_cols}")

        print(f"\n✓ Data loaded and validated successfully")
        print(f"  Shape: {df.shape}")
        print(f"  Columns: {df.columns.tolist()}")

        return df

    def get_data_summary(self, df: pd.DataFrame) -> dict:
        """
        Get summary statistics about the loaded data

        Parameters:
            df (pd.DataFrame): Data to summarize

        Returns:
            dict: Summary statistics
        """
        feature_cols = self.get_feature_columns()

        summary = {
            'n_users': len(df),
            'n_features': len(feature_cols),
            'feature_names': feature_cols,
            'missing_values': df[feature_cols].isnull().sum().to_dict(),
            'value_ranges': {
                col: {
                    'min': df[col].min(),
                    'max': df[col].max(),
                    'mean': df[col].mean()
                }
                for col in feature_cols
            }
        }

        return summary

    def print_summary(self, df: pd.DataFrame):
        """
        Print a formatted summary of the data

        Parameters:
            df (pd.DataFrame): Data to summarize
        """
        summary = self.get_data_summary(df)

        print(f"\n{'=' * 60}")
        print(f"DATA SUMMARY")
        print(f"{'=' * 60}")
        print(f"Number of users: {summary['n_users']}")
        print(f"Number of features: {summary['n_features']}")
        print(f"\nFeatures: {', '.join(summary['feature_names'])}")

        print(f"\nMissing Values:")
        for col, count in summary['missing_values'].items():
            status = "✓ None" if count == 0 else f"✗ {count} missing"
            print(f"  {col}: {status}")

        print(f"\nValue Ranges:")
        for col, ranges in summary['value_ranges'].items():
            print(f"  {col}:")
            print(f"    Min: {ranges['min']:.2f}")
            print(f"    Max: {ranges['max']:.2f}")
            print(f"    Mean: {ranges['mean']:.2f}")

        print(f"{'=' * 60}\n")


# Testing code
if __name__ == "__main__":
    """
    Test the DataLoader module independently
    """
    print("Testing DataLoader Module")
    print("=" * 60)

    # Initialize loader
    loader = DataLoader()

    # Load sample data
    # Get the path relative to this script's location
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.join(script_dir, "..", "..")
    sample_data_path = os.path.join(project_root, "data", "sample", "sample_users.csv")

    try:
        df = loader.load_and_validate(sample_data_path)

        # Print summary
        loader.print_summary(df)

        # Show first few rows
        print("First 5 rows:")
        print(df.head())

        print("\n✓ All tests passed!")

    except Exception as e:
        print(f"\n✗ Error: {str(e)}")