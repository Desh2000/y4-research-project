"""
Privacy-Preserving Mechanisms
Implements differential privacy and data anonymization
"""

import hashlib
import uuid
from typing import Dict, List, Any, Optional
from datetime import datetime
import json
import re


class DifferentialPrivacy:
    """
    Implements differential privacy mechanisms for sensitive data
    """
    
    def __init__(self, epsilon: float = 1.0, delta: float = 1e-5):
        """
        Initialize differential privacy parameters
        
        Args:
            epsilon: Privacy budget (smaller = more privacy)
            delta: Probability of privacy violation
        """
        self.epsilon = epsilon
        self.delta = delta
    
    def add_laplace_noise(self, value: float, sensitivity: float) -> float:
        """
        Add Laplace noise to a numeric value
        
        Args:
            value: Original value
            sensitivity: Sensitivity of the function
        
        Returns:
            Noisy value
        """
        import numpy as np
        scale = sensitivity / self.epsilon
        noise = np.random.laplace(0, scale)
        return value + noise
    
    def add_gaussian_noise(self, value: float, sensitivity: float) -> float:
        """
        Add Gaussian noise for (ε, δ)-differential privacy
        
        Args:
            value: Original value
            sensitivity: Sensitivity of the function
        
        Returns:
            Noisy value
        """
        import numpy as np
        sigma = (sensitivity * np.sqrt(2 * np.log(1.25 / self.delta))) / self.epsilon
        noise = np.random.normal(0, sigma)
        return value + noise
    
    def apply_noise_to_stats(self, statistics: Dict[str, float], 
                            sensitivity: float = 1.0) -> Dict[str, float]:
        """
        Apply differential privacy to statistical aggregates
        
        Args:
            statistics: Dictionary of statistics
            sensitivity: Sensitivity value
        
        Returns:
            Noisy statistics
        """
        noisy_stats = {}
        for key, value in statistics.items():
            if isinstance(value, (int, float)):
                noisy_stats[key] = self.add_laplace_noise(float(value), sensitivity)
            else:
                noisy_stats[key] = value
        
        return noisy_stats


class DataAnonymizer:
    """
    Anonymizes personally identifiable information (PII) in conversations
    """
    
    def __init__(self):
        # PII patterns
        self.patterns = {
            'email': r'\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b',
            'phone': r'\b\d{3}[-.]?\d{3}[-.]?\d{4}\b',
            'ssn': r'\b\d{3}-\d{2}-\d{4}\b',
            'url': r'http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\\(\\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+',
            'ip_address': r'\b(?:\d{1,3}\.){3}\d{1,3}\b',
            'credit_card': r'\b(?:\d{4}[-\s]?){3}\d{4}\b'
        }
        
        # Common names to redact (simplified list)
        self.common_names = ['john', 'jane', 'mary', 'michael', 'david', 'sarah', 
                           'james', 'robert', 'jennifer', 'linda']
    
    def anonymize_text(self, text: str, preserve_structure: bool = True) -> str:
        """
        Remove PII from text
        
        Args:
            text: Original text
            preserve_structure: Keep format (replace with similar length strings)
        
        Returns:
            Anonymized text
        """
        anonymized = text
        
        # Replace email addresses
        anonymized = re.sub(self.patterns['email'], '[EMAIL]', anonymized)
        
        # Replace phone numbers
        anonymized = re.sub(self.patterns['phone'], '[PHONE]', anonymized)
        
        # Replace SSN
        anonymized = re.sub(self.patterns['ssn'], '[SSN]', anonymized)
        
        # Replace URLs
        anonymized = re.sub(self.patterns['url'], '[URL]', anonymized)
        
        # Replace IP addresses
        anonymized = re.sub(self.patterns['ip_address'], '[IP]', anonymized)
        
        # Replace credit card numbers
        anonymized = re.sub(self.patterns['credit_card'], '[CREDIT_CARD]', anonymized)
        
        return anonymized
    
    def detect_pii(self, text: str) -> Dict[str, List[str]]:
        """
        Detect PII in text without removing it
        
        Returns:
            Dictionary of detected PII types and values
        """
        detected = {}
        
        for pii_type, pattern in self.patterns.items():
            matches = re.findall(pattern, text)
            if matches:
                detected[pii_type] = matches
        
        return detected
    
    def hash_identifier(self, identifier: str, salt: str = "") -> str:
        """
        Create a one-way hash of an identifier
        
        Args:
            identifier: Original identifier
            salt: Salt for hashing
        
        Returns:
            Hashed identifier
        """
        salted = f"{identifier}{salt}".encode('utf-8')
        return hashlib.sha256(salted).hexdigest()


class SessionManager:
    """
    Manages user sessions with privacy preservation
    """
    
    def __init__(self, use_persistent_ids: bool = False):
        self.use_persistent_ids = use_persistent_ids
        self.sessions = {}
        self.anonymizer = DataAnonymizer()
    
    def create_session(self, user_id: Optional[str] = None) -> str:
        """
        Create a new session
        
        Args:
            user_id: Optional user identifier
        
        Returns:
            Session ID
        """
        session_id = str(uuid.uuid4())
        
        # Hash user_id if provided
        hashed_user_id = None
        if user_id:
            hashed_user_id = self.anonymizer.hash_identifier(user_id)
        
        self.sessions[session_id] = {
            'session_id': session_id,
            'hashed_user_id': hashed_user_id,
            'created_at': datetime.now().isoformat(),
            'conversation_history': [],
            'metadata': {}
        }
        
        return session_id
    
    def add_message(self, session_id: str, user_message: str, 
                   bot_response: str, metadata: Optional[Dict] = None):
        """
        Add a message to session history (with anonymization)
        
        Args:
            session_id: Session identifier
            user_message: User's message
            bot_response: Bot's response
            metadata: Additional metadata
        """
        if session_id not in self.sessions:
            raise ValueError(f"Session {session_id} not found")
        
        # Anonymize messages
        anonymized_user_message = self.anonymizer.anonymize_text(user_message)
        
        message_entry = {
            'timestamp': datetime.now().isoformat(),
            'user_message': anonymized_user_message,
            'bot_response': bot_response,
            'metadata': metadata or {}
        }
        
        self.sessions[session_id]['conversation_history'].append(message_entry)
    
    def get_session(self, session_id: str) -> Optional[Dict]:
        """Get session data"""
        return self.sessions.get(session_id)
    
    def delete_session(self, session_id: str):
        """Delete session data"""
        if session_id in self.sessions:
            del self.sessions[session_id]
    
    def export_aggregated_data(self, dp_mechanism: DifferentialPrivacy) -> Dict:
        """
        Export aggregated statistics with differential privacy
        
        Args:
            dp_mechanism: Differential privacy mechanism
        
        Returns:
            Anonymized aggregated data
        """
        total_sessions = len(self.sessions)
        total_messages = sum(len(s['conversation_history']) for s in self.sessions.values())
        
        avg_messages_per_session = total_messages / total_sessions if total_sessions > 0 else 0
        
        statistics = {
            'total_sessions': total_sessions,
            'total_messages': total_messages,
            'avg_messages_per_session': avg_messages_per_session
        }
        
        # Apply differential privacy
        noisy_stats = dp_mechanism.apply_noise_to_stats(statistics, sensitivity=1.0)
        
        return noisy_stats


class PrivacyAudit:
    """
    Audit and log privacy-related actions
    """
    
    def __init__(self, log_file: str = "privacy_audit.log"):
        self.log_file = log_file
        self.logs = []
    
    def log_action(self, action: str, details: Dict[str, Any]):
        """
        Log a privacy-related action
        
        Args:
            action: Action type (data_access, data_anonymization, etc.)
            details: Additional details
        """
        log_entry = {
            'timestamp': datetime.now().isoformat(),
            'action': action,
            'details': details
        }
        
        self.logs.append(log_entry)
        
        # Write to file
        with open(self.log_file, 'a') as f:
            f.write(json.dumps(log_entry) + '\n')
    
    def get_logs(self, action_type: Optional[str] = None) -> List[Dict]:
        """
        Get audit logs
        
        Args:
            action_type: Filter by action type
        
        Returns:
            List of log entries
        """
        if action_type:
            return [log for log in self.logs if log['action'] == action_type]
        return self.logs


if __name__ == "__main__":
    # Test privacy mechanisms
    print("Testing Privacy Mechanisms\n")
    
    # Test differential privacy
    print("1. Differential Privacy")
    dp = DifferentialPrivacy(epsilon=1.0)
    original_value = 100.0
    noisy_value = dp.add_laplace_noise(original_value, sensitivity=1.0)
    print(f"Original: {original_value}, Noisy: {noisy_value:.2f}\n")
    
    # Test anonymization
    print("2. Data Anonymization")
    anonymizer = DataAnonymizer()
    text = "Contact me at john.doe@email.com or call 555-123-4567"
    anonymized = anonymizer.anonymize_text(text)
    print(f"Original: {text}")
    print(f"Anonymized: {anonymized}\n")
    
    # Test session management
    print("3. Session Management")
    session_mgr = SessionManager()
    session_id = session_mgr.create_session(user_id="user123")
    print(f"Created session: {session_id}")
    
    session_mgr.add_message(
        session_id, 
        "I'm feeling anxious", 
        "I understand. Tell me more."
    )
    print(f"Added message to session\n")
    
    # Export aggregated data
    aggregated = session_mgr.export_aggregated_data(dp)
    print(f"Aggregated stats: {aggregated}")
