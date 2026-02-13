# Empathetic Conversational Support System

## 💙 Manō - Mental Health Support for STEM Professionals

A complete AI-powered mental health chatbot system with three distinct personas, behavioral assessment questionnaire, and privacy-preserving mechanisms. Built using BERT for intent classification.

---

## 🎯 Overview

This system provides empathetic conversational support through three AI personas:
- **👥 Friend** - Casual, warm, and emotionally supportive
- **🧑‍⚕️ Counselor** - Professional therapeutic guidance with CBT techniques and video recommendations
- **👨‍⚕️ Medical Officer** - Clinical mental health information and medical guidance

**Key Features:**
- ✅ BERT-based intent classification (23 categories, 290+ patterns)
- ✅ Three unique AI personas with distinct communication styles
- ✅ **Behavioral Assessment Questionnaire** with automated scoring
- ✅ Context-aware response generation
- ✅ Privacy protection (Differential Privacy + PII anonymization)
- ✅ **Auto-save chat history** (JSON format)
- ✅ Crisis detection and intervention
- ✅ Video recommendations for mental health topics
- ✅ Real-time web interface
- ✅ RESTful API with 15+ endpoints
- ✅ Voice support capabilities (TTS/STT)
- ✅ **Model prediction endpoints** for integration

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
2. **(Optional) Complete Behavioral Assessment** - Fill out the 5-question questionnaire for personalized insights
3. Select a persona (Friend, Counselor, or Medical Officer)
4. Start chatting!

**Try these examples:**
- "I'm feeling stressed about work"
- "I can't sleep well lately"
- "What are the symptoms of anxiety?"
- "I need someone to talk to"

**Access API Documentation:**
- Swagger UI: http://localhost:8000/docs
- Alternative: http://localhost:8000/redoc

---

## 📁 Project Structure

```
research_c/
├── backend/
│   ├── api.py                    # FastAPI REST API (15+ endpoints)
│   └── integration.py            # Component integration interfaces
├── frontend/
│   └── app.py                    # Streamlit web interface
├── models/
│   ├── intent_classifier.py     # BERT-based intent classification
│   ├── response_generator.py    # Hybrid response generation
│   ├── trained_intent_classifier/     # Trained model files (82.93% accuracy)
│   └── finetuned_intent_classifier_v2/ # Fine-tuned model
├── personas/
│   ├── base_persona.py          # Friend persona + base class
│   ├── counselor_persona.py     # Counselor with CBT + videos
│   └── doctor_persona.py        # Medical Officer (clinical info)
├── privacy/
│   └── privacy_manager.py       # Privacy mechanisms
├── utils/
│   ├── data_loader.py           # Dataset management
│   ├── text_preprocessor.py    # NLP preprocessing
│   └── voice_support.py         # TTS/STT capabilities
├── data/
│   ├── intents.json             # Intent patterns (20 categories)
│   └── synthetic_mental_health_data_v1.csv  # Training data
├── scripts/                     # Python scripts for training & testing
│   ├── train_model.py           # Train BERT model
│   ├── finetune_model_enhanced.py  # Fine-tune with enhanced data
│   ├── finetune_model_improved.py  # Improved fine-tuning
│   ├── finetune_model.py        # Basic fine-tuning
│   ├── evaluate_model.py        # Model evaluation
│   ├── generate_model_output.py # Generate output files
│   ├── test_system.py           # System verification
│   ├── test_questionnaire.py    # Test behavioral assessment
│   ├── test_new_endpoints.py    # Test new API endpoints
│   ├── test_finetuned_model.py  # Test fine-tuned models
│   ├── test_422_fix.py          # Test 422 error fixes
│   └── quick_start.py           # Setup wizard
├── chat_history/                # Auto-saved chat sessions (JSON)
├── questionnaire_results/       # Behavioral assessment results (JSON + CSV)
├── venv/                        # Python virtual environment
├── config.py                    # Configuration management
├── requirements.txt             # Dependencies
├── start_system.ps1             # Quick launch script
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

## 📋 Behavioral Assessment Questionnaire

**NEW FEATURE:** Standardized questionnaire to assess user well-being and stress levels.

### Questions (5 Total)

1. **Work Environment** (Multiple choice)
   - High-pressure deadlines (1 pt)
   - Collaborative team (3 pts)
   - Independent focus (2 pts)
   - Balanced routine (4 pts)

2. **Stress Management** (Slider: 1-10)
   - Direct score: 1 = Frequently overwhelming, 10 = Easy to handle

3. **Self-Care Frequency** (Dropdown)
   - Daily (4 pts) | Few times/week (3 pts) | Rarely (2 pts) | Never (1 pt)

4. **Support Interest** (Multiple choice)
   - Quick tips (2 pts) | Long-term strategies (3 pts) | Professional advice (3 pts) | None (1 pt)

5. **Energy Level** (Slider: 1-10)
   - Direct score: 1 = Completely drained, 10 = Energized

### Scoring Categories

- **25-31 points:** Excellent Well-being ✅
- **20-24 points:** Good Well-being 👍
- **15-19 points:** Moderate Concern ⚠️
- **0-14 points:** Needs Attention 🚨

### Output Files

Results automatically saved in **two formats**:

**JSON Format** (`questionnaire_results/{session_id}_{timestamp}.json`):
```json
{
  "session_id": "...",
  "timestamp": "2026-02-13T12:51:27",
  "answers": {...},
  "individual_scores": {...},
  "total_score": 23.0,
  "category": "Good Well-being",
  "interpretation": "You're managing well overall..."
}
```

**CSV Format** (`questionnaire_results/{session_id}_{timestamp}.csv`):
- Excel-compatible for analysis
- Contains all metrics and scores
- Timestamp for tracking

### API Endpoint

```bash
POST /questionnaire/submit
Content-Type: application/json

{
  "session_id": "uuid",
  "answers": {
    "work_environment": "Balanced routine",
    "stress_management": 7,
    "selfcare_frequency": "A few times a week",
    "support_interest": "Long-term strategies",
    "energy_level": 6
  }
}
```

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

### 👨‍⚕️ Medical Officer Persona
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
- `GET /session/{session_id}/history` - Get conversation history
- `DELETE /session/{session_id}` - Delete session

### Chat
- `POST /chat` - Send message and get response
  ```json
  {
    "session_id": "string",
    "message": "string",
    "persona": "friend|counselor|medical_officer"
  }
  ```

### Chat History (Auto-Save)
- `GET /history/{session_id}` - Retrieve saved chat history
- `POST /history/{session_id}/save` - Manually save history
- `GET /history` - List all saved histories

**Note:** Chat history automatically saves after each message to `chat_history/{session_id}.json`

### Behavioral Assessment
- `POST /questionnaire/submit` - Submit questionnaire and get score
  - Returns: total_score, category, interpretation, individual_scores
  - Auto-saves to: `questionnaire_results/{session_id}_{timestamp}.json` and `.csv`

### Model Prediction
- `POST /model/predict` - Predict intent for single text
  ```json
  {
    "text": "I'm feeling stressed",
    "return_confidence": true
  }
  ```
  
- `POST /model/predict/batch` - Batch intent prediction
  ```json
  {
    "texts": ["text1", "text2", "text3"],
    "return_confidence": true
  }
  ```
  
- `GET /model/info` - Get model metadata
  - Returns: model_name, device, num_classes, intents list

### Information
- `GET /personas` - List available personas (Friend, Counselor, Medical Officer)
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
python scripts/test_system.py

# Test behavioral questionnaire
python scripts/test_questionnaire.py

# Test new API endpoints
python scripts/test_new_endpoints.py

# Test API endpoints interactively
# Visit http://localhost:8000/docs
```

---

## 🔄 Training & Fine-tuning

### Train the Model

```powershell
# Train BERT model from scratch
python scripts/train_model.py
```

### Fine-tune the Model

```powershell
# Enhanced fine-tuning with augmented data
python scripts/finetune_model_enhanced.py

# Improved fine-tuning
python scripts/finetune_model_improved.py

# Basic fine-tuning
python scripts/finetune_model.py
```

### Evaluate Model

```powershell
# Evaluate model accuracy
python scripts/evaluate_model.py
```

**Training takes:** 30-45 minutes on CPU, 5-10 minutes on GPU

---

## 🐛 Troubleshooting

### Model Not Found
```powershell
# Train the model
python scripts/train_model.py
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
python scripts/train_model.py
```

### Frontend Shows Connection Error
- Ensure backend is running on port 8000
- Check: http://localhost:8000/health
- Restart backend if needed

### Questionnaire Not Working
```powershell
# Test the questionnaire endpoint
python scripts/test_questionnaire.py
```

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

- **Total Files:** 35+
- **Lines of Code:** 6,500+
- **Intent Categories:** 23
- **Training Patterns:** 290+
- **Response Templates:** 150+
- **Video Resources:** 8+
- **API Endpoints:** 15+
- **Personas:** 3 (Friend, Counselor, Medical Officer)
- **Privacy Mechanisms:** 4
- **Questionnaire Questions:** 5
- **Model Accuracy:** 82.93%
- **Model Size:** 438MB
- **Output Formats:** JSON + CSV

---

## 🤝 Support

For issues or questions:
1. Check this README
2. Run `python scripts/test_system.py` to diagnose issues
3. Run `python scripts/test_questionnaire.py` to test behavioral assessment
4. Visit API docs: http://localhost:8000/docs

---

## 📄 License

Built for educational and research purposes.
SLIIT Research Project - 2026

---

## 🙏 Acknowledgments

- BERT model by Google Research
- Transformers library by Hugging Face
- FastAPI framework
- Streamlit framework
- Mental health datasets community

---

**Built with ❤️ for mental health support in STEM communities**

**Version:** 1.2.0
**Last Updated:** February 13, 2026

**Recent Updates (v1.2.0):**
- ✅ Added Behavioral Assessment Questionnaire (5 questions, automated scoring)
- ✅ Auto-save chat history to JSON files
- ✅ Model prediction API endpoints (single & batch)
- ✅ Renamed Doctor persona to Medical Officer
- ✅ Enhanced privacy features
- ✅ Improved model accuracy (82.93%)
- ✅ CSV + JSON output formats for analysis
