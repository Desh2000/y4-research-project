# ML Utility Assessment Report

## Objective

This report compares the performance of a Logistic Regression classifier trained on real DASS data versus synthetic DASS data, evaluated on a held-out real test set.

## Data Used

- Real DASS Data (Training): 27842 samples
- Real DASS Data (Test): 11933 samples
- Synthetic DASS Data (Training): 10000 samples
- Target Variable: Binary classification based on 'Q1A' (median split)

## Model Performance on Real Test Set

### Model Trained on REAL Data

| Metric    | Score  |
|-----------|--------|
| Accuracy  | 0.7016 |
| Precision | 0.6590 |
| Recall    | 0.8639 |
| F1-Score  | 0.7476 |
| ROC-AUC   | 0.7977 |

### Model Trained on SYNTHETIC Data

| Metric    | Score  |
|-----------|--------|
| Accuracy  | 0.5166 |
| Precision | 0.5163 |
| Recall    | 0.8791 |
| F1-Score  | 0.6505 |
| ROC-AUC   | 0.5082 |
