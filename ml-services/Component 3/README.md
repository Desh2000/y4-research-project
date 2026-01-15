# Empathetic Conversational Support System (Component 3)

## 💙 Manō - Mental Health Support for STEM Professionals

A complete AI-powered mental health chatbot system with three distinct personas, built using BERT for intent classification and privacy-preserving mechanisms.

---

## 🎯 Overview

This system provides empathetic conversational support through three AI personas:
- **👥 Friend** - Casual, warm, and emotionally supportive
- **🧑‍⚕️ Counselor** - Professional therapeutic guidance with CBT techniques and video recommendations
- **👨‍⚕️ Doctor** - Clinical mental health information and medical guidance

**Key Features:**
- ✅ BERT-based intent classification (84+ categories, 290+ patterns)
- ✅ Three unique AI personas with distinct communication styles
- ✅ Context-aware response generation
- ✅ Privacy protection (Differential Privacy + PII anonymization)
- ✅ Crisis detection and intervention
- ✅ Video recommendations for mental health topics
- ✅ Real-time web interface
- ✅ RESTful API with 10+ endpoints
- ✅ Voice support capabilities (TTS/STT)

---

## 🚀 Quick Start

### 1. Run the System

```powershell
# Start both backend and frontend
.\start_system.ps1
```

This will:
- Start backend API on http://localhost:8000
- Start frontend UI on http://localhost:8501
- Open your browser automatically

### 2. Use the Application

1. Open http://localhost:8501 in your browser
2. Select a persona (Friend, Counselor, or Doctor)
3. Start chatting!

**Try these examples:**
- "I'm feeling stressed about work"
- "I can't sleep well lately"
- "What are the symptoms of anxiety?"
- "I need someone to talk to"

---

## 📁 Project Structure

```
empathetic_support_system/
├── backend/
│   ├── api.py                    # FastAPI REST API (10+ endpoints)
│   └── integration.py            # Component integration interfaces
├── frontend/
│   └── app.py                    # Streamlit web interface
├── models/
│   ├── intent_classifier.py     # BERT-based intent classification
│   ├── response_generator.py    # Hybrid response generation
│   └── trained_intent_classifier/  # Trained model files
├── personas/
│   ├── base_persona.py          # Friend persona + base class
│   ├── counselor_persona.py     # Counselor with CBT + videos
│   └── doctor_persona.py        # Clinical information
├── privacy/
│   └── privacy_manager.py       # Privacy mechanisms
├── utils/
│   ├── data_loader.py           # Dataset management
│   ├── text_preprocessor.py    # NLP preprocessing
│   └── voice_support.py         # TTS/STT capabilities
├── data/
│   ├── Mental_Health_FAQ.csv    # FAQ dataset
│   ├── intents.json             # Intent patterns
│   ├── train.csv                # Training conversations
│   └── additional_intents.json  # Extended patterns
├── venv/                        # Python virtual environment
├── train_model.py               # Model training script
├── test_system.py               # System verification
├── quick_start.py               # Setup wizard
├── start_system.ps1             # Quick launch script
├── config.py                    # Configuration management
├── requirements.txt             # Dependencies
└── README.md                    # This file
```

---

## 🛠️ Technical Stack

**Machine Learning:**
- transformers 4.57.3 (BERT)
- torch 2.9.1 (PyTorch)
- scikit-learn 1.7.2
- nltk 3.9.2

**Backend:**
- FastAPI 0.123.9
- uvicorn 0.38.0
- pydantic 2.12.5

**Frontend:**
- Streamlit 1.52.0

**Data Processing:**
- pandas 2.3.3
- numpy 2.3.5

---

## 📊 Model Performance

**Training Configuration:**
- **Model:** BERT base uncased (110M parameters)
- **Epochs:** 15
- **Batch Size:** 8
- **Learning Rate:** 3e-5 with ReduceLROnPlateau scheduler
- **Max Length:** 256 tokens
- **Regularization:** Weight decay (0.01) + Gradient clipping (1.0)
- **Dataset:** 290 patterns across 84 intent categories
- **Split:** 232 training / 58 validation samples

**Expected Accuracy:** 70%+ on validation set

**Optimizations:**
- Learning rate scheduler for adaptive learning
- Gradient clipping to prevent exploding gradients
- Weight decay for regularization
- Best model tracking
- Extended context window (256 tokens)

---

## 🎭 Persona Details

### 👥 Friend Persona
**Style:** Casual, warm, emotionally supportive
**Best For:** 
- Everyday emotional support
- Active listening
- Casual conversation
- Encouragement

**Sample Response:**
> "I hear you - that sounds really overwhelming. I'm here for you. Can you tell me more about what's stressing you out? 💙"

**Features:**
- Context-aware responses
- Conversation history tracking
- Emoji usage for warmth
- Detects availability questions, stress, uncertainty

---

### 🧑‍⚕️ Counselor Persona
**Style:** Professional, therapeutic, solution-focused
**Best For:**
- CBT techniques
- Coping strategies
- Structured guidance
- Video resources

**CBT Techniques:**
- Cognitive Restructuring
- Behavioral Activation
- Mindfulness Exercises
- Thought Challenging

**Video Resources (8+ curated):**
- Anxiety management
- Depression support
- Stress reduction
- Sleep improvement
- Meditation guides
- Breathing exercises

**Sample Response:**
> "It sounds like you're experiencing significant anxiety. Let me share some CBT techniques that can help. [Suggests relevant video resources]"

---

### 👨‍⚕️ Doctor Persona
**Style:** Clinical, informational, evidence-based
**Best For:**
- Mental health conditions
- Symptoms and diagnosis
- Treatment options
- Medical information

**Knowledge Base:**
- Depression (symptoms, treatments)
- Anxiety disorders (types, interventions)
- Stress management (techniques)
- Burnout (signs, recovery)
- Sleep disorders
- Therapy types (CBT, DBT, IPT)
- Medication information

**Sample Response:**
> "Depression is a mental health condition characterized by persistent feelings of sadness, loss of interest, and other symptoms. Common treatments include psychotherapy and medication."

---

## 🔒 Privacy Features

**1. Differential Privacy**
- Laplace/Gaussian noise addition
- Epsilon (ε) = 1.0, Delta (δ) = 1e-5
- Statistical queries protection

**2. Data Anonymization**
- PII detection (email, phone, SSN, credit card)
- Automatic redaction
- Pattern-based filtering

**3. Session Management**
- Temporary session storage
- Automatic cleanup
- No persistent personal data
- Aggregated statistics only

**4. Privacy Audit Logging**
- Track privacy operations
- Monitor data access
- Compliance ready

---

## 🌐 API Endpoints

**Base URL:** http://localhost:8000

### Session Management
- `POST /session/create` - Create new chat session
- `GET /session/{session_id}` - Get session details
- `DELETE /session/{session_id}` - Delete session

### Chat
- `POST /chat` - Send message and get response
  ```json
  {
    "session_id": "string",
    "message": "string",
    "persona": "friend|counselor|doctor"
  }
  ```

### Information
- `GET /personas` - List available personas
- `GET /statistics` - Get system statistics (with DP)
- `GET /health` - Health check

### Documentation
- `GET /docs` - Interactive API documentation (Swagger UI)
- `GET /redoc` - Alternative documentation (ReDoc)

---

## 🔧 Configuration

Edit `config.py` or create `.env` file:

```python
# API Settings
API_HOST = "localhost"
API_PORT = 8000

# Model Settings
MODEL_NAME = "bert-base-uncased"
MAX_LENGTH = 256

# Privacy Settings
EPSILON = 1.0
DELTA = 1e-5

# Integration URLs
COMPONENT1_URL = "http://localhost:8001"
COMPONENT2_URL = "http://localhost:8002"
COMPONENT4_URL = "http://localhost:8004"

# Features
ENABLE_VOICE = True
ENABLE_INTEGRATION = False
```

---

## 🧪 Testing

```powershell
# Test system components
python test_system.py

# Run unit tests
pytest

# Test API endpoints
# Visit http://localhost:8000/docs
```

---

## 🔄 Retraining the Model

If you want to retrain with different parameters:

```powershell
# Edit train_model.py to adjust:
# - epochs (default: 15)
# - batch_size (default: 8)
# - learning_rate (default: 3e-5)

# Then train
python train_model.py
```

**Training takes:** 30-45 minutes on CPU, 5-10 minutes on GPU

---

## 🐛 Troubleshooting

### Model Not Found
```powershell
python train_model.py
```

### Port Already in Use
Edit `config.py` to change ports:
```python
API_PORT = 8001  # Change from 8000
```

### Import Errors
```powershell
pip install -r requirements.txt
```

### Backend Won't Start
```powershell
# Check if model is trained
dir models\trained_intent_classifier

# If not found, train the model
python train_model.py
```

### Frontend Shows Connection Error
- Ensure backend is running on port 8000
- Check: http://localhost:8000/health
- Restart backend if needed

---

## 📚 Research Context

**Project:** SLIIT Research - Mental Health Support for STEM Professionals
**Component:** 3 (Empathetic Conversational Support System)
**Integration:** Works with Components 1, 2, and 4 of the Manō platform

**Research Contributions:**
- Multi-persona conversational AI for mental health
- Privacy-preserving conversation analysis
- Context-aware therapeutic recommendations
- Crisis detection and intervention protocols
- Evaluation metrics for empathetic AI

---

## 📈 System Statistics

- **Total Files:** 28+
- **Lines of Code:** 5,000+
- **Intent Categories:** 84
- **Training Patterns:** 290+
- **Response Templates:** 100+
- **Video Resources:** 8+
- **API Endpoints:** 10+
- **Personas:** 3
- **Privacy Mechanisms:** 4

---

## 🎓 Usage Tips

**For Best Results:**

1. **Be specific** - The more details you provide, the better the response
2. **Try different personas** - Each has unique strengths
3. **Use the Counselor for resources** - Get video recommendations
4. **Friend for emotional support** - When you need someone to listen
5. **Doctor for information** - Learn about mental health conditions

**Example Conversations:**

**With Friend:**
```
You: "I'm feeling really overwhelmed with work"
Friend: "That sounds really tough. I'm here for you. What's been weighing on you the most? 💙"
```

**With Counselor:**
```
You: "I can't stop worrying about everything"
Counselor: "Constant worrying can be exhausting. Let me share some CBT techniques..."
[Provides cognitive restructuring strategies + anxiety management videos]
```

**With Doctor:**
```
You: "What is burnout?"
Doctor: "Burnout is a state of emotional, physical, and mental exhaustion..."
[Provides clinical definition, symptoms, and treatment options]
```

---

## 🤝 Support

For issues or questions:
1. Check this README
2. See PROJECT_STEPS.md for setup details
3. Run `python test_system.py` to diagnose issues
4. Check logs in `logs/` directory

---

## 📄 License

Built for educational and research purposes.
SLIIT Research Project - 2025

---

## 🙏 Acknowledgments

- BERT model by Google Research
- Transformers library by Hugging Face
- FastAPI framework
- Streamlit framework
- Mental health datasets community

---

**Built with ❤️ for mental health support in STEM communities**

**Version:** 1.0
**Last Updated:** December 5, 2025
