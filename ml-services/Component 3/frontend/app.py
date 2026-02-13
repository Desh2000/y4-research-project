"""
Frontend Application for Empathetic Conversational Support System
Streamlit-based user interface
"""

import streamlit as st
import requests
import json
from datetime import datetime
import sys
import os

# Add parent directory to path
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

# Configuration
API_URL = "http://localhost:8000"

# Page configuration
st.set_page_config(
    page_title="Manō - Mental Health Support",
    page_icon="💙",
    layout="wide",
    initial_sidebar_state="expanded"
)

# Custom CSS
st.markdown("""
<style>
    .main-header {
        font-size: 3rem;
        color: #1565C0;
        text-align: center;
        margin-bottom: 1rem;
        font-weight: bold;
    }
    .sub-header {
        text-align: center;
        color: #424242;
        margin-bottom: 2rem;
        font-size: 1.2rem;
    }
    .persona-card {
        padding: 1rem;
        border-radius: 10px;
        margin: 1rem 0;
        border: 2px solid #ddd;
    }
    .persona-card h4 {
        color: #1a1a1a;
        font-weight: bold;
        margin-bottom: 0.5rem;
    }
    .persona-card p {
        color: #2c2c2c;
        font-size: 0.95rem;
        margin-bottom: 0.5rem;
    }
    .persona-card ul {
        color: #333333;
    }
    .persona-card li {
        color: #404040;
        margin: 0.3rem 0;
    }
    .friend-card {
        background-color: #FFF8E1;
        border-color: #FFA726;
    }
    .counselor-card {
        background-color: #E8F5E9;
        border-color: #4CAF50;
    }
    .doctor-card {
        background-color: #E1F5FE;
        border-color: #29B6F6;
    }
    .chat-message-user {
        background-color: #BBDEFB;
        padding: 1rem;
        border-radius: 10px;
        margin: 0.5rem 0;
        border-left: 4px solid #1976D2;
    }
    .chat-message-user strong {
        color: #0D47A1;
        font-size: 1rem;
    }
    .chat-message-user {
        color: #1a1a1a;
    }
    .chat-message-bot {
        background-color: #F5F5F5;
        padding: 1rem;
        border-radius: 10px;
        margin: 0.5rem 0;
        border-left: 4px solid #757575;
    }
    .chat-message-bot strong {
        color: #212121;
        font-size: 1rem;
    }
    .chat-message-bot {
        color: #1a1a1a;
    }
    .crisis-alert {
        background-color: #FFCDD2;
        border: 3px solid #C62828;
        padding: 1rem;
        border-radius: 10px;
        margin: 1rem 0;
    }
    .crisis-alert strong {
        color: #B71C1C;
        font-size: 1.1rem;
    }
    .crisis-alert {
        color: #1a1a1a;
    }
    .crisis-alert ul {
        color: #2c2c2c;
        font-weight: 500;
    }
    .crisis-alert li {
        color: #1a1a1a;
        font-weight: 500;
        margin: 0.3rem 0;
    }
    .input-container {
        display: flex;
        gap: 10px;
        align-items: flex-start;
        margin-top: 1rem;
    }
    .input-container .stTextArea {
        flex: 1;
    }
    div[data-testid="column"] {
        padding: 0 !important;
    }
</style>
""", unsafe_allow_html=True)


def initialize_session():
    """Initialize session state"""
    if 'session_id' not in st.session_state:
        st.session_state.session_id = None
    if 'messages' not in st.session_state:
        st.session_state.messages = []
    if 'current_persona' not in st.session_state:
        st.session_state.current_persona = 'friend'
    if 'input_counter' not in st.session_state:
        st.session_state.input_counter = 0
    if 'questionnaire_completed' not in st.session_state:
        st.session_state.questionnaire_completed = False
    if 'behavior_score' not in st.session_state:
        st.session_state.behavior_score = None


def create_session():
    """Create a new chat session"""
    try:
        response = requests.post(f"{API_URL}/session/create", json={})
        if response.status_code == 200:
            return response.json()['session_id']
    except Exception as e:
        st.error(f"Error creating session: {e}")
    return None


def send_message(session_id, message, persona):
    """Send a message to the chatbot"""
    try:
        response = requests.post(
            f"{API_URL}/chat",
            json={
                "session_id": session_id,
                "message": message,
                "persona": persona
            }
        )
        if response.status_code == 200:
            return response.json()
        else:
            st.error(f"Error: {response.status_code}")
    except Exception as e:
        st.error(f"Error sending message: {e}")
    return None


def submit_questionnaire(session_id, answers):
    """Submit questionnaire answers to the backend"""
    try:
        response = requests.post(
            f"{API_URL}/questionnaire/submit",
            json={
                "session_id": session_id,
                "answers": answers
            }
        )
        if response.status_code == 200:
            return response.json()
        else:
            st.error(f"Error: {response.status_code}")
    except Exception as e:
        st.error(f"Error submitting questionnaire: {e}")
    return None


def display_questionnaire():
    """Display behavioral assessment questionnaire"""
    st.sidebar.markdown("---")
    st.sidebar.title("📋 Behavioral Assessment")
    
    with st.sidebar.expander("📊 Questionnaire", expanded=False):
        st.markdown("""
        <div style='background-color: #E3F2FD; padding: 1rem; border-radius: 8px; margin-bottom: 1rem;'>
            <p style='color: #1565C0; font-weight: 600; margin: 0;'>
                Complete this brief assessment to help us better understand your well-being and provide personalized support.
            </p>
        </div>
        """, unsafe_allow_html=True)
        
        # Question 1: Work Environment
        q1 = st.selectbox(
            "1. Which best describes your typical work environment?",
            options=[
                "High-pressure deadlines",
                "Collaborative team",
                "Independent focus",
                "Balanced routine"
            ],
            key="q1_work_env"
        )
        
        # Question 2: Stress Management (Slider)
        st.markdown("**2. How manageable do you find your daily stress?**")
        q2 = st.slider(
            "Stress level",
            min_value=1,
            max_value=10,
            value=5,
            help="1 = Frequently overwhelming, 10 = Generally easy to handle",
            key="q2_stress",
            label_visibility="collapsed"
        )
        st.caption("1 = Frequently overwhelming | 10 = Generally easy to handle")
        
        # Question 3: Self-care Frequency
        q3 = st.selectbox(
            "3. How often do you engage in self-care activities like exercise or relaxation?",
            options=[
                "Daily",
                "A few times a week",
                "Rarely",
                "Never"
            ],
            key="q3_selfcare"
        )
        
        # Question 4: Support Type Interest
        q4 = st.selectbox(
            "4. What type of support are you most interested in exploring?",
            options=[
                "Quick tips",
                "Long-term strategies",
                "Professional advice",
                "None right now"
            ],
            key="q4_support"
        )
        
        # Question 5: Energy Level (Slider)
        st.markdown("**5. Rate your energy level at the end of a typical day:**")
        q5 = st.slider(
            "",
            min_value=1,
            max_value=10,
            value=5,
            help="1 = Completely drained, 10 = Energized and ready for more",
            key="q5_energy"
        )
        st.caption("1 = Completely drained | 10 = Energized and ready for more")
        
        # Submit Button
        if st.button("📊 Submit Assessment", key="submit_questionnaire", type="primary"):
            # Ensure session exists
            if st.session_state.session_id is None:
                st.session_state.session_id = create_session()
                if st.session_state.session_id is None:
                    st.error("❌ Failed to create session. Please check backend connection.")
                    return
            
            # Prepare answers
            answers = {
                "work_environment": q1,
                "stress_management": q2,
                "selfcare_frequency": q3,
                "support_interest": q4,
                "energy_level": q5
            }
            
            # Submit to backend
            result = submit_questionnaire(st.session_state.session_id, answers)
            
            if result:
                st.session_state.questionnaire_completed = True
                st.session_state.behavior_score = result
                st.success("✅ Assessment completed!")
                st.markdown(f"""
                <div style='background-color: #E8F5E9; padding: 1rem; border-radius: 8px; margin-top: 1rem;'>
                    <h4 style='color: #2E7D32; margin-top: 0;'>Your Behavioral Score</h4>
                    <p style='font-size: 2rem; font-weight: bold; color: #1B5E20; margin: 0.5rem 0;'>
                        {result['total_score']:.1f}/31
                    </p>
                    <p style='color: #424242; margin: 0;'><strong>Category:</strong> {result['category']}</p>
                    <p style='color: #424242; font-size: 0.9rem; margin-top: 0.5rem;'>{result['interpretation']}</p>
                    <hr style='margin: 1rem 0;'>
                    <p style='font-size: 0.85rem; color: #616161; margin: 0;'>
                        📄 Results saved to: <code>{result['file_path']}</code>
                    </p>
                </div>
                """, unsafe_allow_html=True)
                st.rerun()
        
        # Show previous score if completed
        if st.session_state.questionnaire_completed and st.session_state.behavior_score:
            st.markdown("---")
            st.markdown(f"""
            <div style='background-color: #FFF3E0; padding: 0.75rem; border-radius: 8px;'>
                <p style='color: #E65100; font-weight: 600; margin: 0; font-size: 0.9rem;'>
                    ✓ Last Score: {st.session_state.behavior_score['total_score']:.1f}/31 
                    ({st.session_state.behavior_score['category']})
                </p>
            </div>
            """, unsafe_allow_html=True)


def display_persona_selector():
    """Display persona selection"""
    st.sidebar.markdown("---")
    st.sidebar.title("🎭 Choose Your Support Persona")
    
    # Friend Persona
    with st.sidebar.expander("👥 Friend", expanded=(st.session_state.current_persona == 'friend')):
        st.markdown("""
        <div class='persona-card friend-card'>
            <h4>Friendly & Supportive</h4>
            <p>A warm, casual friend who listens and provides emotional comfort.</p>
            <ul>
                <li>Casual conversation</li>
                <li>Emotional support</li>
                <li>Active listening</li>
                <li>Encouragement</li>
            </ul>
        </div>
        """, unsafe_allow_html=True)
        if st.button("Select Friend", key="select_friend"):
            st.session_state.current_persona = 'friend'
            st.rerun()
    
    # Counselor Persona
    with st.sidebar.expander("🧑‍⚕️ Counselor", expanded=(st.session_state.current_persona == 'counselor')):
        st.markdown("""
        <div class='persona-card counselor-card'>
            <h4>Professional & Therapeutic</h4>
            <p>A professional counselor providing therapeutic support and coping strategies.</p>
            <ul>
                <li>CBT techniques</li>
                <li>Coping strategies</li>
                <li>Video resources</li>
                <li>Solution-focused</li>
            </ul>
        </div>
        """, unsafe_allow_html=True)
        if st.button("Select Counselor", key="select_counselor"):
            st.session_state.current_persona = 'counselor'
            st.rerun()
    
    # Medical Officer Persona
    with st.sidebar.expander("👨‍⚕️ Medical Officer", expanded=(st.session_state.current_persona == 'medical_officer')):
        st.markdown("""
        <div class='persona-card doctor-card'>
            <h4>Clinical & Informational</h4>
            <p>A medical professional providing clinical information and guidance.</p>
            <ul>
                <li>Medical information</li>
                <li>Symptoms & diagnosis</li>
                <li>Treatment options</li>
                <li>Clinical perspective</li>
            </ul>
        </div>
        """, unsafe_allow_html=True)
        if st.button("Select Medical Officer", key="select_medical_officer"):
            st.session_state.current_persona = 'medical_officer'
            st.rerun()


def display_chat():
    """Display chat interface"""
    st.markdown("<h1 class='main-header'>💙 Manō</h1>", unsafe_allow_html=True)
    st.markdown("<p class='sub-header'>Empathetic Mental Health Support for STEM Professionals</p>", unsafe_allow_html=True)
    
    # Display current persona
    persona_names = {
        'friend': '👥 Friend',
        'counselor': '🧑‍⚕️ Counselor',
        'medical_officer': '👨‍⚕️ Medical Officer'
    }
    st.info(f"**Currently chatting with:** {persona_names[st.session_state.current_persona]}")
    
    # Display chat history
    chat_container = st.container()
    with chat_container:
        for msg in st.session_state.messages:
            if msg['role'] == 'user':
                st.markdown(f"""
                <div class='chat-message-user'>
                    <strong>You:</strong><br>
                    {msg['content']}
                </div>
                """, unsafe_allow_html=True)
            else:
                persona_icon = {
                    'friend': '👥',
                    'counselor': '🧑‍⚕️',
                    'medical_officer': '👨‍⚕️'
                }.get(msg.get('persona', 'friend'), '🤖')
                
                st.markdown(f"""
                <div class='chat-message-bot'>
                    <strong>{persona_icon} {msg.get('persona', 'Bot').capitalize()}:</strong><br>
                    {msg['content']}
                </div>
                """, unsafe_allow_html=True)
                
                # Show crisis alert if detected
                if msg.get('crisis_detected'):
                    st.markdown("""
                    <div class='crisis-alert'>
                        <strong>⚠️ Crisis Detected</strong><br>
                        If you're experiencing thoughts of self-harm, please contact:
                        <ul>
                            <li>National Suicide Prevention Lifeline: 988</li>
                            <li>Crisis Text Line: Text HELLO to 741741</li>
                            <li>Emergency Services: 911</li>
                        </ul>
                    </div>
                    """, unsafe_allow_html=True)
    
    # Input area
    st.markdown("---")
    
    # Create columns for inline layout
    col_input, col_send = st.columns([5, 1])
    
    with col_input:
        user_input = st.text_area(
            "Type your message here...",
            key=f"user_input_{st.session_state.input_counter}",
            height=100,
            placeholder=f"Chat with {persona_names[st.session_state.current_persona]}...",
            label_visibility="collapsed"
        )
    
    with col_send:
        st.markdown("<br>", unsafe_allow_html=True)  # Spacing
        send_button = st.button("📤", use_container_width=True, help="Send message")
        st.markdown("<br>", unsafe_allow_html=True)  # Spacing
        clear_button = st.button("🗑️", use_container_width=True, help="Clear chat")
    
    # Handle send button
    if send_button and user_input.strip():
        if not st.session_state.session_id:
            st.session_state.session_id = create_session()
        
        if st.session_state.session_id:
            # Store the message before clearing
            message_to_send = user_input.strip()
            
            # Add user message
            st.session_state.messages.append({
                'role': 'user',
                'content': message_to_send,
                'timestamp': datetime.now().isoformat()
            })
            
            # Get bot response
            with st.spinner("Thinking..."):
                response = send_message(
                    st.session_state.session_id,
                    message_to_send,
                    st.session_state.current_persona
                )
            
            if response:
                # Add bot message
                st.session_state.messages.append({
                    'role': 'bot',
                    'content': response['bot_response'],
                    'persona': response['persona'],
                    'intent': response.get('intent'),
                    'confidence': response.get('confidence'),
                    'crisis_detected': response.get('crisis_detected', False),
                    'timestamp': datetime.now().isoformat()
                })
            
            # Increment counter to clear the input by creating a new widget
            st.session_state.input_counter += 1
            
            st.rerun()
    
    # Handle clear button
    if clear_button:
        st.session_state.messages = []
        st.session_state.session_id = None
        st.rerun()


def display_info():
    """Display information sidebar"""
    st.sidebar.markdown("---")
    st.sidebar.title("ℹ️ About Manō")
    
    with st.sidebar.expander("What is Manō?"):
        st.write("""
        Manō is an empathetic conversational support system designed specifically 
        for STEM professionals dealing with stress, burnout, and mental health challenges.
        
        **Key Features:**
        - 🔒 Privacy-preserving (differential privacy)
        - 🎭 Three personas (Friend, Counselor, Medical Officer)
        - 💡 Evidence-based support
        - 📚 Resource recommendations
        """)
    
    with st.sidebar.expander("Privacy & Security"):
        st.write("""
        Your privacy matters:
        - All conversations are anonymized
        - Differential privacy protocols applied
        - No personal data stored
        - Session-based (temporary storage)
        """)
    
    with st.sidebar.expander("Crisis Resources"):
        st.write("""
        **Immediate Help:**
        - 🆘 National Suicide Prevention Lifeline: **988**
        - 💬 Crisis Text Line: Text **HELLO** to **741741**
        - 🚨 Emergency: **911**
        
        **This is not a substitute for professional help.**
        """)
    
    with st.sidebar.expander("Disclaimer"):
        st.write("""
        Manō is an AI-powered support tool and **NOT** a replacement for 
        professional mental health care. If you're experiencing a mental 
        health crisis or need clinical treatment, please contact a 
        licensed mental health professional.
        """)


def main():
    """Main application"""
    # Initialize session
    initialize_session()
    
    # Check if API is running
    try:
        response = requests.get(f"{API_URL}/health")
        if response.status_code != 200:
            st.error("⚠️ Backend API is not responding. Please start the API server.")
            st.code("python backend/api.py")
            return
    except Exception as e:
        st.error(f"⚠️ Cannot connect to backend API at {API_URL}")
        st.info("Please start the backend server: `python backend/api.py`")
        return
    
    # Display questionnaire
    display_questionnaire()
    
    # Display persona selector
    display_persona_selector()
    
    # Display info
    display_info()
    
    # Display chat
    display_chat()


if __name__ == "__main__":
    main()
