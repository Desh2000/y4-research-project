"""
MANO Component 1: Biological Rhythm Generator - Phase 2 Starter
Part 1: Wearable Data Exploration

This script loads the Sleep Health dataset, analyzes its structure,
and identifies signals suitable for TimeGAN sequence generation.
"""

import pandas as pd
import numpy as np
import os
import sys
import json
from pathlib import Path
from datetime import datetime

# --- 1. SYSTEM SETUP & CONFIG IMPORT ---
# We need to find the central 'config.py' file to get our file paths.
# This makes the code robust: if you move the project, it still works.
current_dir = os.getcwd()
config_path = os.path.join(current_dir, 'ml-services',
                           'privacy-preserving-gan', 'config')
sys.path.append(config_path)

try:
    import config
    print("✅ Successfully imported system configuration")
except ImportError as e:
    # If this fails, the script cannot find the data files.
    print(f"❌ Critical Error: Could not import 'config.py'. Error: {e}")
    sys.exit(1)


class WearableDataExplorer:
    """
    A tool to explore and understand the wearable/sleep health data.
    It prepares the 'ground truth' understanding needed before we can
    simulate time-series data.
    """

    def __init__(self):
        """
        Initialize the explorer.
        We don't pass paths as arguments; we grab them directly from
        the centralized config to ensure we are always looking at the right file.
        """
        self.data = None
        # Get the specific path for the 'SLEEP_HEALTH' dataset defined in config.py
        self.dataset_path = config.DATASETS['SLEEP_HEALTH']['file_path']
        # We will store our findings in this dictionary to save as a JSON report later
        self.report = {}

    def load_data(self):
        """
        Logic: safely load the CSV file into a Pandas DataFrame.
        Returns: True if successful, False if failed.
        """
        print("\n" + "="*80)
        print("STEP 1: LOAD DATA")
        print("="*80)

        try:
            # check if the file actually exists on the disk
            if not self.dataset_path.exists():
                print(f"❌ ERROR: File not found at path: {self.dataset_path}")
                return False

            # Read the CSV
            self.data = pd.read_csv(self.dataset_path)

            print(f"\n✅ Successfully loaded: {self.dataset_path}")
            # .shape gives us (rows, columns). Important to know dataset size.
            print(
                f"   Shape: {self.data.shape[0]} rows × {self.data.shape[1]} columns")
            return True

        except Exception as e:
            # Catch unexpected errors (like corrupted files or permission issues)
            print(f"\n❌ ERROR loading data: {e}")
            return False

    def explore_structure(self):
        """
        Logic: Inspect what kind of data we have (Integers? Strings? Floats?).
        Why: TimeGAN works best with Numerical data (Floats/Ints). 
        Categorical data (Strings) needs complex encoding.
        """
        print("\n" + "="*80)
        print("STEP 2: DATA STRUCTURE")
        print("="*80)

        if self.data is None:
            return

        print(f"\n📊 COLUMNS ({len(self.data.columns)}):")
        # Loop through every column and print its Name and Data Type (dtype)
        for i, col in enumerate(self.data.columns, 1):
            print(f"   {i:2d}. {col:30s} - {self.data[col].dtype}")

        print(f"\n📈 FIRST 5 ROWS:")
        # .head() shows the first 5 rows so we can visually sanity-check the data
        print(self.data.head())

        # Save these stats to our report dictionary
        self.report['shape'] = self.data.shape
        self.report['columns'] = list(self.data.columns)

    def analyze_missing_values(self):
        """
        Logic: Check for 'NaN' (Not a Number) or empty cells.
        Why: Neural Networks (including GANs) cannot handle missing math. 
        If we feed a NaN into a matrix multiplication, the whole model breaks.
        """
        print("\n" + "="*80)
        print("STEP 3: MISSING DATA ANALYSIS")
        print("="*80)

        # .isnull().sum() counts how many empty cells are in each column
        missing = self.data.isnull().sum()

        print(f"\n❌ MISSING VALUES:")
        if missing.sum() == 0:
            print("   ✅ No missing values! Dataset is complete.")
            self.report['missing_values'] = "None"
        else:
            # If there are missing values, print specifically which columns have them
            print(missing[missing > 0])
            self.report['missing_values'] = missing[missing > 0].to_dict()

    def identify_wearable_signals(self):
        """
        Logic: Filter the dataset to find 'Signals'.

        What is a Signal? 
        A signal is a numerical value that fluctuates over time (e.g., Heart Rate).
        ID numbers (static) or Gender (categorical) are NOT signals for TimeGAN.

        This function identifies columns suitable for sequence generation.
        """
        print("\n" + "="*80)
        print("STEP 6: IDENTIFY WEARABLE SIGNALS FOR TIME-SERIES")
        print("="*80)

        # Select only numerical columns (int64, float64)
        numeric_cols = self.data.select_dtypes(include=[np.number]).columns

        print(f"\n🎯 POTENTIAL TIME-SERIES SIGNALS (for TimeGAN):")
        signal_candidates = []

        for col in numeric_cols:
            # Heuristic: Skip columns that look like IDs (Person ID, User ID)
            # because IDs don't "fluctuate" over time.
            if 'ID' in col:
                continue

            # Print min/max stats to help us decide if it's a good signal
            # e.g., Sleep Duration (min 4, max 10) is a great signal.
            print(
                f"   ✓ {col:35s} | min: {self.data[col].min():8.2f} | max: {self.data[col].max():8.2f}")
            signal_candidates.append(col)

        self.report['wearable_signals'] = signal_candidates
        return signal_candidates

    def save_report(self):
        """
        Logic: Save the 'self.report' dictionary to a JSON file.
        Why: This creates an audit trail. We can prove exactly what the data 
        looked like before we started manipulating it.
        """
        print("\n" + "="*80)
        print("STEP 8: SAVE EXPLORATION REPORT")
        print("="*80)

        # Create a unique filename with the current time
        timestamp = datetime.now().strftime("%Y_%m_%d_%H%M%S")
        report_file = config.REPORTS_DIR / \
            f"wearable_data_exploration_{timestamp}.json"

        self.report['timestamp'] = timestamp

        # Write to JSON
        with open(report_file, 'w') as f:
            json.dump(self.report, f, indent=2, default=str)

        print(f"\n✅ Report saved: {report_file}")

    def run_full_exploration(self):
        """
        Logic: The 'Conductor' function. It runs all the other functions in the correct order.
        """
        print("\n" + "█"*80)
        print("█ MANO PHASE 2: WEARABLE DATA EXPLORATION")
        print("█"*80)

        # Only proceed to steps 2, 3, etc., if Step 1 (Load) succeeds.
        if self.load_data():
            self.explore_structure()
            self.analyze_missing_values()
            self.identify_wearable_signals()
            self.save_report()

        print("\n" + "█"*80)
        print("█ EXPLORATION COMPLETE ✅")
        print("█"*80)


# This block ensures the script runs only when executed directly,
# not when imported as a module by another script.
if __name__ == "__main__":
    explorer = WearableDataExplorer()
    explorer.run_full_exploration()
