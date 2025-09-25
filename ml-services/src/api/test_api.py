"""
Test the Clustering API
"""

import requests
import json

API_BASE = "http://localhost:5000"


def test_health_check():
    """Test health check endpoint"""
    print("🏥 Testing health check...")

    response = requests.get(f"{API_BASE}/health")
    print(f"Status: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")
    print()


def test_single_prediction():
    """Test single user prediction"""
    print("🧑 Testing single user prediction...")

    user_data = {
        "stress_score": 0.8,
        "depression_score": 0.3,
        "anxiety_score": 0.6
    }

    response = requests.post(f"{API_BASE}/predict", json=user_data)
    print(f"Status: {response.status_code}")

    if response.status_code == 200:
        result = response.json()
        print("Prediction Result:")
        print(f"  Cluster: {result['cluster_assignment']['cluster_identifier']}")
        print(f"  Support Level: {result['recommendations']['support_level']}")
        print(f"  High Risk: {result['risk_assessment']['is_high_risk']}")
        print(f"  Overall Risk: {result['user_scores']['overall_risk']:.3f}")
    else:
        print(f"Error: {response.json()}")
    print()


def test_batch_prediction():
    """Test batch prediction"""
    print("👥 Testing batch prediction...")

    batch_data = {
        "users": [
            {"stress_score": 0.2, "depression_score": 0.1, "anxiety_score": 0.3},  # Low risk
            {"stress_score": 0.9, "depression_score": 0.8, "anxiety_score": 0.7},  # High risk
            {"stress_score": 0.5, "depression_score": 0.4, "anxiety_score": 0.6}  # Medium risk
        ]
    }

    response = requests.post(f"{API_BASE}/predict/batch", json=batch_data)
    print(f"Status: {response.status_code}")

    if response.status_code == 200:
        result = response.json()
        print(f"Batch Results ({result['total_users']} users):")
        for pred in result['predictions']:
            print(f"  User {pred['user_index']}: {pred['cluster_assignment']['cluster_identifier']} "
                  f"({pred['recommendations']['support_level']})")
    else:
        print(f"Error: {response.json()}")
    print()


def test_cluster_info():
    """Test cluster information endpoint"""
    print("📊 Testing cluster info...")

    response = requests.get(f"{API_BASE}/clusters/info")
    print(f"Status: {response.status_code}")

    if response.status_code == 200:
        result = response.json()
        print(f"Model Version: {result['model_info']['version']}")
        print(f"Number of Clusters: {result['model_info']['n_clusters']}")
        print(f"Silhouette Score: {result['performance_metrics']['silhouette_score']:.3f}")
    else:
        print(f"Error: {response.json()}")
    print()


if __name__ == "__main__":
    print("🧠 Testing Mental Health Clustering API")
    print("=" * 50)

    try:
        test_health_check()
        test_single_prediction()
        test_batch_prediction()
        test_cluster_info()

        print("✅ All API tests completed!")

    except requests.exceptions.ConnectionError:
        print("❌ Could not connect to API. Make sure the server is running:")
        print("   python src/api/clustering_api.py")
    except Exception as e:
        print(f"❌ Test error: {e}")
