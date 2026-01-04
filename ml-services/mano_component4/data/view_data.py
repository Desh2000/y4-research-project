# data/view_data.py
# Quick script to view our data

import pandas as pd

print("="*60)
print("DATA OVERVIEW")
print("="*60)

# Load training data
print("\n1. GMM TRAINING DATA:")
print("-"*40)
train_df = pd.read_csv('data/gmm_training_data.csv')
print(f"Shape: {train_df.shape[0]} rows, {train_df.shape[1]} columns")
print(f"\nColumns: {list(train_df.columns)}")
print(f"\nFirst 3 rows:")
print(train_df.head(3))

# Load test data
print("\n\n2. SYNTHETIC TEST DATA:")
print("-"*40)
test_df = pd.read_csv('data/synthetic_test_data.csv')
print(f"Shape: {test_df.shape[0]} rows, {test_df.shape[1]} columns")
print(f"\nFirst 3 rows:")
print(test_df.head(3))

# Load individual test cases
print("\n\n3. INDIVIDUAL TEST CASES:")
print("-"*40)
cases_df = pd.read_csv('data/individual_test_cases.csv')
print(cases_df[['user_id', 'name', 'stress_score', 'sleep_score', 'social_score']])

print("\n" + "="*60)
print("DATA READY FOR GMM TRAINING!")
print("="*60)
