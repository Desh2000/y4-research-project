"""
Data Preprocessor Module

Handles cleaning, validating, and normalizing resilience indicator data
for machine learning.

Author: [Your Name]
Date: [Current Date]
"""

import pandas as pd
import numpy as np
from sklearn.preprocessing import MinMaxScaler
from typing import Tuple, List
import os


class DataPreprocessor:
    """
    Handles data preprocessing including cleaning, validation, and normalization

    Attributes:
        feature_columns (list): Names of feature columns to process
        scaler (MinMaxScaler): Scikit-learn scaler object
        is_fitted (bool): Whether the scaler has been fitted to data
    """

    def __init__(self, feature_columns: List[str]):
        """
        Initialize the preprocessor

        Parameters:
            feature_columns (List[str]): List of feature column names
        """
        self.feature_columns = feature_columns
        self.scaler = MinMaxScaler(feature_range=(0, 1))
        self.is_fitted = False

        print(f"✓ DataPreprocessor initialized")
        print(f"  Features to process: {len(self.feature_columns)}")

    def handle_missing_values(self, df: pd.DataFrame, strategy: str = 'mean') -> pd.DataFrame:
        """
        Handle missing values in the dataframe

        Parameters:
            df (pd.DataFrame): Input data
            strategy (str): How to fill missing values
                - 'mean': Fill with column mean (default)
                - 'median': Fill with column median
                - 'drop': Drop rows with missing values

        Returns:
            pd.DataFrame: Data with missing values handled
        """
        print(f"\n--- Handling Missing Values ---")
        print(f"Strategy: {strategy}")

        # Make a copy to avoid modifying original
        df_clean = df.copy()

        # Count missing values before
        missing_before = df_clean[self.feature_columns].isnull().sum().sum()
        print(f"Missing values before: {missing_before}")

        if missing_before == 0:
            print(f"✓ No missing values found")
            return df_clean

        # Handle based on strategy
        if strategy == 'mean':
            for col in self.feature_columns:
                if df_clean[col].isnull().any():
                    mean_val = df_clean[col].mean()
                    df_clean[col].fillna(mean_val, inplace=True)
                    print(f"  Filled {col} with mean: {mean_val:.2f}")

        elif strategy == 'median':
            for col in self.feature_columns:
                if df_clean[col].isnull().any():
                    median_val = df_clean[col].median()
                    df_clean[col].fillna(median_val, inplace=True)
                    print(f"  Filled {col} with median: {median_val:.2f}")

        elif strategy == 'drop':
            rows_before = len(df_clean)
            df_clean = df_clean.dropna(subset=self.feature_columns)
            rows_after = len(df_clean)
            print(f"  Dropped {rows_before - rows_after} rows")

        else:
            raise ValueError(f"Unknown strategy: {strategy}")

        # Count missing values after
        missing_after = df_clean[self.feature_columns].isnull().sum().sum()
        print(f"Missing values after: {missing_after}")
        print(f"✓ Missing values handled")

        return df_clean

    def remove_duplicates(self, df: pd.DataFrame) -> pd.DataFrame:
        """
        Remove duplicate rows based on user_id

        Parameters:
            df (pd.DataFrame): Input data

        Returns:
            pd.DataFrame: Data without duplicates
        """
        print(f"\n--- Removing Duplicates ---")

        rows_before = len(df)
        df_clean = df.drop_duplicates(subset=['user_id'], keep='first')
        rows_after = len(df_clean)

        duplicates_removed = rows_before - rows_after

        if duplicates_removed > 0:
            print(f"  Removed {duplicates_removed} duplicate rows")
        else:
            print(f"✓ No duplicates found")

        return df_clean

    def validate_value_ranges(self, df: pd.DataFrame) -> Tuple[bool, dict]:
        """
        Validate that all values are in expected ranges (0-1 for resilience scores)

        Parameters:
            df (pd.DataFrame): Data to validate

        Returns:
            Tuple[bool, dict]: (is_valid, dict_of_violations)
        """
        print(f"\n--- Validating Value Ranges ---")

        violations = {}

        for col in self.feature_columns:
            # Check for negative values
            negative_count = (df[col] < 0).sum()

            # Check for values > 1
            over_one_count = (df[col] > 1).sum()

            # Check for NaN or infinity
            invalid_count = (~np.isfinite(df[col])).sum()

            if negative_count > 0 or over_one_count > 0 or invalid_count > 0:
                violations[col] = {
                    'negative': negative_count,
                    'over_one': over_one_count,
                    'invalid': invalid_count
                }

        is_valid = len(violations) == 0

        if is_valid:
            print(f"✓ All values in valid ranges [0, 1]")
        else:
            print(f"✗ Found range violations:")
            for col, viol in violations.items():
                print(f"  {col}: {viol}")

        return is_valid, violations

    def clip_values(self, df: pd.DataFrame, min_val: float = 0.0, max_val: float = 1.0) -> pd.DataFrame:
        """
        Clip values to be within specified range

        Parameters:
            df (pd.DataFrame): Input data
            min_val (float): Minimum allowed value
            max_val (float): Maximum allowed value

        Returns:
            pd.DataFrame: Data with clipped values
        """
        print(f"\n--- Clipping Values ---")
        print(f"Range: [{min_val}, {max_val}]")

        df_clipped = df.copy()

        for col in self.feature_columns:
            clipped_count = ((df[col] < min_val) | (df[col] > max_val)).sum()

            if clipped_count > 0:
                df_clipped[col] = df_clipped[col].clip(min_val, max_val)
                print(f"  Clipped {clipped_count} values in {col}")

        print(f"✓ Values clipped to range")

        return df_clipped

    def fit_scaler(self, df: pd.DataFrame):
        """
        Fit the scaler to the data

        This learns the min and max values for each feature.
        Must be called before transform().

        Parameters:
            df (pd.DataFrame): Data to fit on
        """
        print(f"\n--- Fitting Scaler ---")

        X = df[self.feature_columns].values
        self.scaler.fit(X)
        self.is_fitted = True

        print(f"✓ Scaler fitted to {len(df)} samples")
        print(f"  Feature mins: {self.scaler.data_min_}")
        print(f"  Feature maxs: {self.scaler.data_max_}")

    def transform(self, df: pd.DataFrame) -> pd.DataFrame:
        """
        Transform data using the fitted scaler

        Parameters:
            df (pd.DataFrame): Data to transform

        Returns:
            pd.DataFrame: Normalized data

        Raises:
            ValueError: If scaler hasn't been fitted
        """
        if not self.is_fitted:
            raise ValueError("Scaler not fitted. Call fit_scaler() first.")

        print(f"\n--- Transforming Data ---")

        df_normalized = df.copy()

        # Get feature values
        X = df[self.feature_columns].values

        # Transform
        X_normalized = self.scaler.transform(X)

        # Replace in dataframe
        df_normalized[self.feature_columns] = X_normalized

        print(f"✓ Data normalized")
        print(f"  Shape: {X_normalized.shape}")

        return df_normalized

    def fit_transform(self, df: pd.DataFrame) -> pd.DataFrame:
        """
        Fit scaler and transform data in one step

        Parameters:
            df (pd.DataFrame): Data to fit and transform

        Returns:
            pd.DataFrame: Normalized data
        """
        self.fit_scaler(df)
        return self.transform(df)

    def preprocess_pipeline(self,
                            df: pd.DataFrame,
                            handle_missing: str = 'mean',
                            clip_outliers: bool = True) -> pd.DataFrame:
        """
        Complete preprocessing pipeline

        Steps:
        1. Remove duplicates
        2. Handle missing values
        3. Clip outliers (optional)
        4. Validate ranges
        5. Normalize features

        Parameters:
            df (pd.DataFrame): Raw data
            handle_missing (str): Strategy for missing values
            clip_outliers (bool): Whether to clip values to [0, 1]

        Returns:
            pd.DataFrame: Fully preprocessed data
        """
        print(f"\n{'=' * 60}")
        print(f"PREPROCESSING PIPELINE")
        print(f"{'=' * 60}")

        # Step 1: Remove duplicates
        df_clean = self.remove_duplicates(df)

        # Step 2: Handle missing values
        df_clean = self.handle_missing_values(df_clean, strategy=handle_missing)

        # Step 3: Clip outliers (optional)
        if clip_outliers:
            df_clean = self.clip_values(df_clean)

        # Step 4: Validate ranges
        is_valid, violations = self.validate_value_ranges(df_clean)
        if not is_valid:
            print(f"\n⚠ Warning: Range violations found but continuing...")

        # Step 5: Normalize
        df_normalized = self.fit_transform(df_clean)

        print(f"\n{'=' * 60}")
        print(f"✓ PREPROCESSING COMPLETE")
        print(f"{'=' * 60}")
        print(f"Input rows: {len(df)}")
        print(f"Output rows: {len(df_normalized)}")
        print(f"Features normalized: {len(self.feature_columns)}")

        return df_normalized

    def save_processed_data(self, df: pd.DataFrame, filepath: str):
        """
        Save processed data to CSV

        Parameters:
            df (pd.DataFrame): Processed data
            filepath (str): Where to save
        """
        print(f"\n--- Saving Processed Data ---")

        # Create directory if it doesn't exist
        os.makedirs(os.path.dirname(filepath), exist_ok=True)

        # Save
        df.to_csv(filepath, index=False)

        print(f"✓ Saved to: {filepath}")
        print(f"  Rows: {len(df)}")
        print(f"  Columns: {len(df.columns)}")


# Testing code
if __name__ == "__main__":
    """
    Test the DataPreprocessor module
    """
    print("Testing DataPreprocessor Module")
    print("=" * 60)

    # First, load data using DataLoader
    import sys
    import os

    sys.path.append('src/data_processing')
    from data_loader import DataLoader

    # Get the path relative to this script's location
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.join(script_dir, "..", "..")
    sample_data_path = os.path.join(project_root, "data", "sample", "sample_users.csv")

    # Load data
    loader = DataLoader()
    df = loader.load_and_validate(sample_data_path)

    # Get feature columns
    feature_cols = loader.get_feature_columns()

    # Initialize preprocessor
    preprocessor = DataPreprocessor(feature_cols)

    # Run preprocessing pipeline
    df_processed = preprocessor.preprocess_pipeline(
        df,
        handle_missing='mean',
        clip_outliers=True
    )

    # Show results
    print("\nBEFORE preprocessing:")
    print(df[feature_cols].head())
    print("\nAFTER preprocessing:")
    print(df_processed[feature_cols].head())

    # Save processed data
    processed_data_path = os.path.join(project_root, "data", "processed", "resilience_indicators_normalized.csv")
    preprocessor.save_processed_data(
        df_processed,
        processed_data_path
    )

    print("\n✓ All tests passed!")