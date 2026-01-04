import pandas as pd
import os
import numpy as np

# --- Configuration ---

# Define file paths
# Uses os.path.join for cross-platform compatibility (Windows, macOS, Linux)
RAW_DATA_DIR = 'data/raw'
PROCESSED_DATA_DIR = 'data/processed'

RAW_FILENAME = 'Mental Health in Tech Survey.csv'
PROCESSED_FILENAME = 'mental_health_tech_survey_PROCESSED.csv'

RAW_FILE_PATH = os.path.join(RAW_DATA_DIR, RAW_FILENAME)
PROCESSED_FILE_PATH = os.path.join(PROCESSED_DATA_DIR, PROCESSED_FILENAME)

# Define the columns we want to keep for the Manō project
# These are the most relevant features for modeling
RELEVANT_COLUMNS = [
    'Age',
    'Gender',
    'Country',
    'self_employed',
    'family_history',
    'treatment',  # This is a key target variable
    'work_interfere',
    'no_employees',  # Company size
    'remote_work',
    'tech_company',
    'benefits',
    'care_options',
    'wellness_program',
    'seek_help',
    'anonymity',
    'leave',
    'mental_health_consequence',
    'phys_health_consequence',
    'coworkers',
    'supervisor'
]

# --- Helper Functions for Cleaning ---


def clean_gender(df):
    """
    Standardizes the 'Gender' column into 'Male', 'Female', or 'Other'.
    This dataset has many non-standard free-text entries.
    """
    print("Cleaning 'Gender' column...")
    # Use .str.lower() to make matching case-insensitive
    gender_col = df['Gender'].str.lower()

    # Define mappings
    # This list is built from manually inspecting the unique values in the column
    male_map = ['m', 'male', 'make', 'cis male',
                'mal', 'male (cis)', 'msle', 'guy (-r) cis, male', 'male.']
    female_map = ['f', 'female', 'woman', 'cis female',
                  'femake', 'female (cis)', 'femail']

    # Apply mappings
    # 1. Map all standard male entries to 'Male'
    df['Gender'] = np.where(gender_col.isin(male_map), 'Male', df['Gender'])
    # 2. Map all standard female entries to 'Female'
    df['Gender'] = np.where(gender_col.isin(
        female_map), 'Female', df['Gender'])
    # 3. Map everything else that isn't 'Male' or 'Female' to 'Other'
    df['Gender'] = np.where(~df['Gender'].isin(
        ['Male', 'Female']), 'Other', df['Gender'])

    print(
        f"Gender cleaning complete. Value counts:\n{df['Gender'].value_counts()}")
    return df


def clean_age(df):
    """
    Cleans the 'Age' column by removing invalid entries and clipping outliers.
    The dataset contains ages like -1 and 999.
    """
    print("Cleaning 'Age' column...")
    # Force column to numeric, invalid entries (like text) become NaN
    df['Age'] = pd.to_numeric(df['Age'], errors='coerce')

    # Remove biologically impossible or nonsensical ages
    # We set them to NaN so we can fill them with the median later
    df.loc[(df['Age'] < 18) | (df['Age'] > 75), 'Age'] = np.nan

    # Fill any missing (NaN) ages with the median age of the dataset
    median_age = df['Age'].median()
    df['Age'].fillna(median_age, inplace=True)

    # Convert to integer for the GAN
    df['Age'] = df['Age'].astype(int)

    print(
        f"Age cleaning complete. Min: {df['Age'].min()}, Max: {df['Age'].max()}, Median: {median_age}")
    return df


def fill_missing_values(df):
    """
    Fills remaining missing values (NaN) for categorical columns.
    CTGAN can handle NaNs, but it's often better to provide a specific
    'Unknown' or 'NA' category.
    """
    print("Filling missing values for categorical columns...")
    # Iterate through all selected columns
    for col in RELEVANT_COLUMNS:
        # We already handled 'Age'
        if col == 'Age':
            continue

        # Check if the column is of object type (i.e., categorical string)
        if df[col].dtype == 'object':
            # Fill NaN with a specific 'Unknown' category
            # This explicitly tells the GAN that this is a valid state
            df[col].fillna('Unknown', inplace=True)

    print("Missing value fill complete.")
    return df

# --- Main Execution ---


def main():
    """
    Main function to load, process, and save the dataset.
    """
    print(f"Starting data unification process...")

    # Ensure the processed data directory exists
    os.makedirs(PROCESSED_DATA_DIR, exist_ok=True)

    # --- 1. Load Data ---
    try:
        print(f"Loading raw data from: {RAW_FILE_PATH}")
        df = pd.read_csv(RAW_FILE_PATH)
        print(f"Successfully loaded {len(df)} rows.")
    except FileNotFoundError:
        print(f"ERROR: Raw data file not found at {RAW_FILE_PATH}")
        print("Please ensure the 'Mental Health in Tech Survey.csv' file is in the 'data/raw' directory.")
        return
    except Exception as e:
        print(f"An error occurred while loading the data: {e}")
        return

    # --- 2. Feature Selection ---
    print(f"Selecting {len(RELEVANT_COLUMNS)} relevant columns...")
    # Keep only the columns we defined
    try:
        df_processed = df[RELEVANT_COLUMNS].copy()
    except KeyError as e:
        print(f"ERROR: A column was not found in the raw data: {e}")
        print("Please check the RELEVANT_COLUMNS list against the CSV file header.")
        return

    # --- 3. Data Cleaning ---
    print("Starting data cleaning...")
    df_processed = clean_age(df_processed)
    df_processed = clean_gender(df_processed)
    df_processed = fill_missing_values(df_processed)
    print("Data cleaning finished.")

    # --- 4. Save Processed Data ---
    try:
        print(f"Saving processed data to: {PROCESSED_FILE_PATH}")
        df_processed.to_csv(PROCESSED_FILE_PATH, index=False)
        print("Processed data saved successfully.")
        print(f"\n--- Process Summary ---")
        print(f"Total rows processed: {len(df_processed)}")
        print(f"Total columns processed: {len(df_processed.columns)}")
        print("\nFinal Data Info:")
        df_processed.info()
        print("\n--- Unification Complete ---")

    except Exception as e:
        print(f"An error occurred while saving the processed data: {e}")


if __name__ == "__main__":
    main()
