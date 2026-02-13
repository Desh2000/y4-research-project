"""
Test script for Behavioral Assessment Questionnaire
"""

import requests
import json

API_URL = "http://localhost:8000"

def test_questionnaire():
    """Test the questionnaire submission endpoint"""
    print("\n=== Testing Behavioral Assessment Questionnaire ===\n")
    
    # Create a session first
    print("1. Creating session...")
    response = requests.post(f"{API_URL}/session/create", json={})
    if response.status_code == 200:
        session_id = response.json()['session_id']
        print(f"✓ Session created: {session_id}\n")
    else:
        print("❌ Failed to create session")
        return
    
    # Test questionnaire with sample answers
    print("2. Submitting questionnaire...")
    test_answers = {
        "session_id": session_id,
        "answers": {
            "work_environment": "Balanced routine",
            "stress_management": 7,
            "selfcare_frequency": "A few times a week",
            "support_interest": "Long-term strategies",
            "energy_level": 6
        }
    }
    
    response = requests.post(
        f"{API_URL}/questionnaire/submit",
        json=test_answers
    )
    
    if response.status_code == 200:
        result = response.json()
        print("✓ Questionnaire submitted successfully!\n")
        print("=" * 60)
        print("BEHAVIORAL SCORE RESULTS")
        print("=" * 60)
        print(f"Session ID: {result['session_id']}")
        print(f"Timestamp: {result['timestamp']}")
        print(f"\nTotal Score: {result['total_score']:.1f}/31")
        print(f"Category: {result['category']}")
        print(f"\nInterpretation:")
        print(f"  {result['interpretation']}")
        print(f"\nIndividual Scores:")
        for key, value in result['individual_scores'].items():
            print(f"  - {key.replace('_', ' ').title()}: {value}")
        print(f"\n📄 Results saved to:")
        print(f"  JSON: {result['file_path']}")
        print(f"  CSV: {result['file_path'].replace('.json', '.csv')}")
        print("=" * 60)
    else:
        print(f"❌ Failed to submit questionnaire: {response.status_code}")
        print(response.text)
    
    # Test with different scenarios
    print("\n3. Testing different behavioral profiles...\n")
    
    scenarios = [
        {
            "name": "High-Stress Profile",
            "answers": {
                "work_environment": "High-pressure deadlines",
                "stress_management": 3,
                "selfcare_frequency": "Never",
                "support_interest": "Professional advice",
                "energy_level": 2
            }
        },
        {
            "name": "Excellent Well-being Profile",
            "answers": {
                "work_environment": "Balanced routine",
                "stress_management": 9,
                "selfcare_frequency": "Daily",
                "support_interest": "Quick tips",
                "energy_level": 9
            }
        },
        {
            "name": "Moderate Profile",
            "answers": {
                "work_environment": "Collaborative team",
                "stress_management": 6,
                "selfcare_frequency": "A few times a week",
                "support_interest": "Long-term strategies",
                "energy_level": 5
            }
        }
    ]
    
    for scenario in scenarios:
        # Create new session for each scenario
        response = requests.post(f"{API_URL}/session/create", json={})
        scenario_session_id = response.json()['session_id']
        
        response = requests.post(
            f"{API_URL}/questionnaire/submit",
            json={
                "session_id": scenario_session_id,
                "answers": scenario['answers']
            }
        )
        
        if response.status_code == 200:
            result = response.json()
            print(f"✓ {scenario['name']}:")
            print(f"  Score: {result['total_score']:.1f}/31 | Category: {result['category']}")
        else:
            print(f"❌ {scenario['name']}: Failed")
    
    print("\n✅ All questionnaire tests completed!")
    print("\n📂 Check the 'questionnaire_results/' directory for saved files.")


if __name__ == "__main__":
    try:
        # Check if API is running
        response = requests.get(f"{API_URL}/health")
        if response.status_code != 200:
            print("❌ API is not running. Please start the backend first.")
            print("   python backend/api.py")
            exit(1)
        
        print("✓ API is running")
        test_questionnaire()
        
    except requests.exceptions.ConnectionError:
        print("❌ Cannot connect to API. Please start the backend:")
        print("   python backend/api.py")
    except Exception as e:
        print(f"❌ Error: {e}")
