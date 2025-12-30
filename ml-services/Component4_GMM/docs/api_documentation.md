# Component 4 API Documentation

## Base URL
```
http://localhost:5000
```

## Authentication
Currently no authentication required (development only)

## Endpoints

### 1. Root Endpoint

**GET /**

Returns API information and available endpoints.

**Response:**
```json
{
  "name": "Manō Component 4 API",
  "version": "1.0.0",
  "status": "running",
  "endpoints": {...}
}
```

---

### 2. Health Check

**GET /api/health**

Check if API is running and models are loaded.

**Response:**
```json
{
  "status": "healthy",
  "models_loaded": true,
  "timestamp": "2025-12-27T10:30:00"
}
```

---

### 3. Predict Cluster

**POST /api/predict**

Predict which cluster a new user belongs to.

**Request Body:**
```json
{
  "emotional_regulation_score": 0.75,
  "social_connectivity_score": 0.68,
  "behavioral_stability_score": 0.72,
  "cognitive_flexibility_score": 0.70,
  "stress_coping_mechanism": 0.65
}
```

**Response:**
```json
{
  "predicted_cluster": 1,
  "cluster_probabilities": [0.05, 0.85, 0.08, 0.02, 0.00],
  "primary_probability": 0.85,
  "timestamp": "2025-12-27T10:31:00"
}
```

**Error Response (400):**
```json
{
  "error": "Missing required field: emotional_regulation_score"
}
```

---

### 4. Get All Communities

**GET /api/communities**

Retrieve list of all communities.

**Response:**
```json
{
  "communities": [
    {
      "community_id": "COMM_000",
      "name": "Building Resilience Together - Group A",
      "cluster_id": 0,
      "size": 9,
      "avg_emotional_regulation": 0.31,
      ...
    },
    ...
  ],
  "count": 11
}
```

---

### 5. Get User Community

**GET /api/community/<user_id>**

Get community information for a specific user.

**Parameters:**
- `user_id` (path): User identifier

**Response:**
```json
{
  "user_id": "USER_0001",
  "community": {
    "community_id": "COMM_002",
    "name": "Resilience Champions - Group A",
    "cluster_id": 1,
    "members": ["USER_0001", "USER_0005", ...],
    "size": 11,
    "profile": {...},
    "recommended_activities": [...]
  }
}
```

**Error Response (404):**
```json
{
  "error": "User not found"
}
```

---

### 6. Get Statistics

**GET /api/stats**

Get system-wide statistics.

**Response:**
```json
{
  "total_users": 100,
  "total_clusters": 5,
  "total_communities": 11,
  "avg_community_size": 9.1,
  "cluster_distribution": {
    "0": 18,
    "1": 22,
    "2": 25,
    "3": 20,
    "4": 15
  }
}
```

---

## Error Codes

- **200** - Success
- **400** - Bad Request (missing/invalid parameters)
- **404** - Not Found (user/community doesn't exist)
- **500** - Internal Server Error

---

## Rate Limiting

Currently no rate limiting (development only).

For production, implement rate limiting:
- 100 requests/minute per IP
- 1000 requests/hour per IP

---

## Examples

### Python
```python
import requests

# Predict cluster
response = requests.post(
    'http://localhost:5000/api/predict',
    json={
        'emotional_regulation_score': 0.75,
        'social_connectivity_score': 0.68,
        'behavioral_stability_score': 0.72,
        'cognitive_flexibility_score': 0.70,
        'stress_coping_mechanism': 0.65
    }
)

data = response.json()
print(f"Cluster: {data['predicted_cluster']}")
```

### cURL
```bash
curl -X POST http://localhost:5000/api/predict \
  -H "Content-Type: application/json" \
  -d '{
    "emotional_regulation_score": 0.75,
    "social_connectivity_score": 0.68,
    "behavioral_stability_score": 0.72,
    "cognitive_flexibility_score": 0.70,
    "stress_coping_mechanism": 0.65
  }'
```

### JavaScript
```javascript
fetch('http://localhost:5000/api/predict', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    emotional_regulation_score: 0.75,
    social_connectivity_score: 0.68,
    behavioral_stability_score: 0.72,
    cognitive_flexibility_score: 0.70,
    stress_coping_mechanism: 0.65
  })
})
.then(response => response.json())
.then(data => console.log(data));
```