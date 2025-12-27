import pandas as pd
import numpy as np


def generate_sample_data(n_users=100):
    """
    Generate sample resilience indicator data for testing

    Parameters:
    n_users (int): Number of sample users to generate
    """

    np.random.seed(42)  # For reproducibility

    # Generate user IDs
    user_ids = [f"USER_{str(i).zfill(4)}" for i in range(1, n_users + 1)]

    # Generate resilience indicators (0-1 scale)
    data = {
        'user_id': user_ids,
        'emotional_regulation_score': np.random.beta(2, 2, n_users),  # Most users mid-range
        'social_connectivity_score': np.random.beta(2, 2, n_users),
        'behavioral_stability_score': np.random.beta(2, 2, n_users),
        'cognitive_flexibility_score': np.random.beta(2, 2, n_users),
        'stress_coping_mechanism': np.random.beta(2, 2, n_users)
    }

    # Create DataFrame
    df = pd.DataFrame(data)

    # Round to 2 decimal places
    for col in df.columns[1:]:
        df[col] = df[col].round(2)

    # Add some metadata
    df['timestamp'] = pd.Timestamp.now()
    df['data_source'] = 'sample_generation'

    # Save to CSV
    output_path = 'data/sample/sample_users.csv'
    df.to_csv(output_path, index=False)

    print(f"✓ Generated {n_users} sample users")
    print(f"✓ Saved to: {output_path}")
    print(f"\nFirst 5 rows:")
    print(df.head())
    print(f"\nData shape: {df.shape}")
    print(f"\nColumns: {df.columns.tolist()}")

    return df


if __name__ == "__main__":
    generate_sample_data(100)