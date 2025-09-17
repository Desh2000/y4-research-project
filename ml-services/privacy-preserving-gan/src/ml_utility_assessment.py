"""
This script assesses the ML utility of the CTGAN-generated synthetic data.
"""
import pandas as pd
import numpy as np
import os
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, roc_auc_score
from data_loader import MentalHealthDataLoader  # To load the real DASS data

# --- Configuration --- #
current_script_dir = os.path.dirname(os.path.abspath(__file__))
project_root = os.path.abspath(os.path.join(
    current_script_dir, "..", "..", ".."))

DATA_PATH = os.path.join(project_root, "data")  # Path to your raw data
LOG_DIR = os.path.join(project_root, "gan_logs") # Path where data and reports are saved

# *** MODIFICATION 1: Point to the new CTGAN synthetic data file. ***
SYNTHETIC_DATA_PATH = os.path.join(
    LOG_DIR, "ctgan_synthetic_data_for_validation.csv")

# *** MODIFICATION 2: Create a new report path to avoid overwriting the baseline. ***
OUTPUT_REPORT_PATH = os.path.join(LOG_DIR, "ctgan_ml_utility_report.md")

# --- Helper Function for Preprocessing --- #
def preprocess_data_for_ml(df, target_column='Q1A'):
    """
    Preprocesses the DataFrame for ML training.
    - Selects numerical columns.
    - Fills missing values with the mean.
    - Creates a binary target variable from a specified column.
    """
    # WHY: We only want numerical features for this ML model.
    numerical_cols = df.select_dtypes(include=np.number).columns
    processed_df = df[numerical_cols].copy()

    # WHY: Fill missing values to prevent errors during training.
    processed_df = processed_df.fillna(processed_df.mean())

    # WHY: Create a binary classification task to test ML utility.
    if target_column in processed_df.columns:
        median_val = processed_df[target_column].median()
        processed_df['target'] = (processed_df[target_column] >= median_val).astype(int)

        # WHY: Drop the original column to prevent data leakage.
        # FIX for PerformanceWarning: .copy() creates a clean, non-fragmented DataFrame.
        X = processed_df.drop(columns=[target_column, 'target']).copy() 
        y = processed_df['target']
    else:
        # This case should not be hit if column names are handled correctly
        X = processed_df.copy()
        y = pd.Series(np.zeros(len(processed_df)), index=processed_df.index)

    return X, y

# --- Main Logic --- #
if __name__ == "__main__":
    print("\n--- Starting ML Utility Assessment for CTGAN Data ---")

    # 1. Load and Preprocess Real DASS Data
    data_loader = MentalHealthDataLoader(DATA_PATH)
    real_dass_data = data_loader.load_dass_dataset()
    if real_dass_data is None:
        print("Error: Could not load real DASS dataset. Exiting.")
        exit()

    # Get original numerical columns to assign to synthetic data later
    numerical_cols = real_dass_data.select_dtypes(include=np.number).columns
    X_real, y_real = preprocess_data_for_ml(real_dass_data, target_column='Q1A')
    print(f"Loaded and preprocessed real DASS data. Features shape: {X_real.shape}")

    # 2. Load and Preprocess Synthetic CTGAN Data
    try:
        synthetic_data = pd.read_csv(SYNTHETIC_DATA_PATH)
        # Assign correct column names, as the saved CSV has no headers.
        synthetic_data.columns = numerical_cols
        
        X_synth, y_synth = preprocess_data_for_ml(synthetic_data, target_column='Q1A')
        print(f"Loaded and preprocessed synthetic data. Features shape: {X_synth.shape}")
    except FileNotFoundError:
        print(f"Error: Synthetic data file not found at {SYNTHETIC_DATA_PATH}. Please generate it first.")
        exit()

    # 3. Split Real Data into Training and Test Sets
    X_real_train, X_real_test, y_real_train, y_real_test = train_test_split(
        X_real, y_real, test_size=0.3, random_state=42, stratify=y_real
    )
    print(f"Real data split: Train {X_real_train.shape}, Test {X_real_test.shape}")

    # 4. Train Model on Real Data
    # WHY: This is our benchmark model.
    # FIX for ConvergenceWarning: Increased max_iter from 1000 to 5000 to ensure the model fully learns.
    model_real = LogisticRegression(solver='liblinear', random_state=42, max_iter=5000)
    print("\nTraining model on REAL data...")
    model_real.fit(X_real_train, y_real_train)
    print("Model trained on REAL data.")

    # 5. Train Model on Synthetic Data
    # WHY: This tests the utility of the CTGAN data.
    model_synth = LogisticRegression(solver='liblinear', random_state=42, max_iter=5000)
    print("Training model on SYNTHETIC data (CTGAN)...")
    model_synth.fit(X_synth, y_synth)
    print("Model trained on SYNTHETIC data.")

    # 6. Evaluate Models on the REAL Test Set
    print("\nEvaluating models on REAL test set...")
    y_pred_real = model_real.predict(X_real_test)
    y_proba_real = model_real.predict_proba(X_real_test)[:, 1]

    y_pred_synth = model_synth.predict(X_real_test)
    y_proba_synth = model_synth.predict_proba(X_real_test)[:, 1]

    metrics = { "Accuracy": accuracy_score, "Precision": precision_score, "Recall": recall_score, "F1-Score": f1_score, "ROC-AUC": roc_auc_score }

    report_content = "# CTGAN ML Utility Assessment Report\n\n"
    report_content += "This report compares models trained on real vs. CTGAN-generated data.\n\n"
    report_content += "## Model Performance on Real Test Set\n\n"
    
    # Performance of model trained on REAL data
    report_content += "### Model Trained on REAL Data (Benchmark)\n\n| Metric    | Score  |\n|-----------|--------|\n"
    for name, func in metrics.items():
        score = func(y_real_test, y_pred_real) if name != "ROC-AUC" else func(y_real_test, y_proba_real)
        report_content += f"| {name:<10}| {score:.4f} |\n"
    
    # Performance of model trained on SYNTHETIC data
    report_content += "\n### Model Trained on SYNTHETIC Data (CTGAN)\n\n| Metric    | Score  |\n|-----------|--------|\n"
    for name, func in metrics.items():
        score = func(y_real_test, y_pred_synth) if name != "ROC-AUC" else func(y_real_test, y_proba_synth)
        report_content += f"| {name:<10}| {score:.4f} |\n"

    # 7. Save the Report
    with open(OUTPUT_REPORT_PATH, "w") as f:
        f.write(report_content)
        
    print(f"\nML Utility Assessment report saved to: {OUTPUT_REPORT_PATH}")
    print("ML Utility Assessment for CTGAN complete.")
