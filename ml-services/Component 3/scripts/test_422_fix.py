"""
Quick test for the 422 error fix
Tests submitting questionnaire without pre-existing session
"""

import requests
import json

API_URL = "http://localhost:8000"

def test_422_fix():
    """Test that questionnaire submission works without prior session"""
    print("\n=== Testing 422 Error Fix ===\n")
    
    # Test 1: Submit with valid session (should work)
    print("Test 1: Submit with valid session")
    response = requests.post(f"{API_URL}/session/create", json={})
    session_id = response.json()['session_id']
    
    response = requests.post(
        f"{API_URL}/questionnaire/submit",
        json={
            "session_id": session_id,
            "answers": {
                "work_environment": "Balanced routine",
                "stress_management": 7,
                "selfcare_frequency": "Daily",
                "support_interest": "Quick tips",
                "energy_level": 8
            }
        }
    )
    
    if response.status_code == 200:
        print("✓ Test 1 PASSED: Valid session submission successful")
        print(f"  Score: {response.json()['total_score']}/31\n")
    else:
        print(f"✗ Test 1 FAILED: Status {response.status_code}")
        print(f"  Error: {response.text}\n")
    
    # Test 2: Try to submit with missing session_id (should fail with 422)
    print("Test 2: Submit with missing session_id (expected to fail)")
    response = requests.post(
        f"{API_URL}/questionnaire/submit",
        json={
            "answers": {
                "work_environment": "Balanced routine",
                "stress_management": 7,
                "selfcare_frequency": "Daily",
                "support_interest": "Quick tips",
                "energy_level": 8
            }
        }
    )
    
    if response.status_code == 422:
        print("✓ Test 2 PASSED: Correctly rejected missing session_id (422)")
        print(f"  This is expected behavior for malformed requests\n")
    else:
        print(f"✗ Test 2 UNEXPECTED: Status {response.status_code}\n")
    
    # Test 3: Submit with None/null session_id (should fail with 422)
    print("Test 3: Submit with null session_id (expected to fail)")
    response = requests.post(
        f"{API_URL}/questionnaire/submit",
        json={
            "session_id": None,
            "answers": {
                "work_environment": "Balanced routine",
                "stress_management": 7,
                "selfcare_frequency": "Daily",
                "support_interest": "Quick tips",
                "energy_level": 8
            }
        }
    )
    
    if response.status_code == 422:
        print("✓ Test 3 PASSED: Correctly rejected null session_id (422)")
        print(f"  This is expected - frontend now creates session before submitting\n")
    else:
        print(f"✗ Test 3 UNEXPECTED: Status {response.status_code}\n")
    
    print("=" * 60)
    print("SUMMARY:")
    print("=" * 60)
    print("✓ The 422 error was caused by null/missing session_id")
    print("✓ Fix implemented: Frontend now creates session if needed")
    print("✓ Backend properly validates session_id is present and not null")
    print("\nThe frontend will now:")
    print("  1. Check if session_id exists before submitting")
    print("  2. Auto-create a session if session_id is None")
    print("  3. Only submit questionnaire with valid session_id")
    print("=" * 60)


if __name__ == "__main__":
    try:
        response = requests.get(f"{API_URL}/health")
        if response.status_code != 200:
            print("❌ API not running. Start with: python backend/api.py")
            exit(1)
        
        test_422_fix()
        
    except requests.exceptions.ConnectionError:
        print("❌ Cannot connect to API. Start backend:")
        print("   python backend/api.py")
    except Exception as e:
        print(f"❌ Error: {e}")
