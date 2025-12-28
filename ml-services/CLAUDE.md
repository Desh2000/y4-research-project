# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is **Component 4** of a research project: a GMM (Gaussian Mixture Model) based clustering system for analyzing user resilience indicators. The system processes user behavioral data across five resilience dimensions and groups users into clusters for community-based mental health support.

## Development Commands

```bash
# Activate virtual environment (Windows)
.venv\Scripts\activate

# Install dependencies
pip install -r Component4_GMM/requirements.txt

# Generate sample test data
python Component4_GMM/generate_sample_data.py

# Run main application
python Component4_GMM/main.py
```

## Architecture

### Directory Structure

```
Component4_GMM/
├── src/
│   ├── clustering/       # GMM clustering implementation
│   ├── community/        # Community grouping logic
│   ├── data_processing/  # Data ingestion and preprocessing
│   ├── utils/            # Shared utilities
│   └── visualization/    # Plotting and visualization
├── data/
│   ├── raw/              # Original input data (gitignored)
│   ├── processed/        # Cleaned/transformed data
│   └── sample/           # Sample data for testing
├── models/               # Saved GMM model files (.pkl)
├── results/
│   ├── clusters/         # Cluster assignment outputs
│   ├── reports/          # Analysis reports
│   └── visualizations/   # Generated plots
├── config/               # Configuration files
├── notebooks/            # Jupyter notebooks for exploration
└── tests/                # Unit tests
```

### Key Data Dimensions

The system clusters users based on five resilience indicators (0-1 scale):
- `emotional_regulation_score`
- `social_connectivity_score`
- `behavioral_stability_score`
- `cognitive_flexibility_score`
- `stress_coping_mechanism`

## Data Flow

1. Raw user data → `data/raw/`
2. Preprocessing pipeline → `data/processed/`
3. GMM clustering → `models/` (saved models)
4. Cluster assignments → `results/clusters/`
5. Visualizations → `results/visualizations/`
