# CTGAN ML Utility Assessment Report

This report compares models trained on real vs. CTGAN-generated data.

## Model Performance on Real Test Set

### Model Trained on REAL Data (Benchmark)

| Metric    | Score  |
|-----------|--------|
| Accuracy  | 0.7649 |
| Precision | 0.7502 |
| Recall    | 0.8104 |
| F1-Score  | 0.7791 |
| ROC-AUC   | 0.8423 |

### Model Trained on SYNTHETIC Data (CTGAN)

| Metric    | Score  |
|-----------|--------|
| Accuracy  | 0.5459 |
| Precision | 0.5302 |
| Recall    | 0.9879 |
| F1-Score  | 0.6900 |
| ROC-AUC   | 0.8191 |
