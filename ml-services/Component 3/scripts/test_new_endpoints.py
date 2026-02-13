"""
Test script for new API endpoints (Chat History & Model Management)
"""

import requests
import json

API_URL = "http://localhost:8000"

def test_model_endpoints():
    """Test model prediction endpoints"""
    print("\n=== Testing Model Endpoints ===\n")
    
    # 1. Test single prediction
    print("1. Testing /model/predict")
    response = requests.post(
        f"{API_URL}/model/predict",
        json={
            "text": "I'm feeling really stressed about work deadlines",
            "return_confidence": True
        }
    )
    print(f"Status: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}\n")
    
    # 2. Test batch prediction
    print("2. Testing /model/predict/batch")
    response = requests.post(
        f"{API_URL}/model/predict/batch",
        json={
            "texts": [
                "I'm anxious about my presentation",
                "Feeling burnt out from long hours",
                "Need help managing work-life balance"
            ],
            "return_confidence": True
        }
    )
    print(f"Status: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}\n")
    
    # 3. Test model info
    print("3. Testing /model/info")
    response = requests.get(f"{API_URL}/model/info")
    print(f"Status: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}\n")


def test_chat_history_endpoints():
    """Test chat history endpoints"""
    print("\n=== Testing Chat History Endpoints ===\n")
    
    # 1. Create a session
    print("1. Creating session")
    response = requests.post(f"{API_URL}/session/create", json={})
    session_id = response.json()['session_id']
    print(f"Session ID: {session_id}\n")
    
    # 2. Send some messages
    print("2. Sending test messages")
    messages = [
        "Hello, I'm feeling stressed",
        "Can you help me with anxiety?",
        "Thanks for your support"
    ]
    
    for msg in messages:
        response = requests.post(
            f"{API_URL}/chat",
            json={
                "session_id": session_id,
                "message": msg,
                "persona": "counselor"
            }
        )
        print(f"  - Sent: {msg}")
        print(f"    Bot: {response.json()['bot_response'][:50]}...\n")
    
    # 3. Get chat history
    print("3. Testing GET /history/{session_id}")
    response = requests.get(f"{API_URL}/history/{session_id}")
    print(f"Status: {response.status_code}")
    if response.status_code == 200:
        data = response.json()
        print(f"Session has {len(data['history']['messages'])} messages\n")
    
    # 4. List all histories
    print("4. Testing GET /history (list all)")
    response = requests.get(f"{API_URL}/history")
    print(f"Status: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}\n")
    
    # 5. Manual save
    print("5. Testing POST /history/{session_id}/save")
    response = requests.post(f"{API_URL}/history/{session_id}/save")
    print(f"Status: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}\n")


def test_medical_officer_persona():
    """Test Medical Officer persona renaming"""
    print("\n=== Testing Medical Officer Persona ===\n")
    
    # 1. List personas
    print("1. Testing GET /personas")
    response = requests.get(f"{API_URL}/personas")
    print(f"Status: {response.status_code}")
    personas = response.json()['personas']
    print("Available personas:")
    for persona in personas:
        print(f"  - {persona['name']}: {persona['description'][:50]}...")
    print()
    
    # 2. Test chat with Medical Officer
    print("2. Testing chat with medical_officer")
    response = requests.post(f"{API_URL}/session/create", json={})
    session_id = response.json()['session_id']
    
    response = requests.post(
        f"{API_URL}/chat",
        json={
            "session_id": session_id,
            "message": "What are the symptoms of burnout?",
            "persona": "medical_officer"
        }
    )
    print(f"Status: {response.status_code}")
    if response.status_code == 200:
        print(f"Response: {response.json()['bot_response']}\n")


if __name__ == "__main__":
    try:
        # Check if API is running
        response = requests.get(f"{API_URL}/health")
        if response.status_code != 200:
            print("❌ API is not running. Please start the backend first.")
            exit(1)
        
        print("✓ API is running\n")
        
        # Run tests
        test_model_endpoints()
        test_chat_history_endpoints()
        test_medical_officer_persona()
        
        print("\n✅ All tests completed!")
        
    except requests.exceptions.ConnectionError:
        print("❌ Cannot connect to API. Please start the backend with: python backend/api.py")
    except Exception as e:
        print(f"❌ Error: {e}")
