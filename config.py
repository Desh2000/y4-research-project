"""
Configuration Module
Loads environment variables and provides configuration
"""

import os
from pathlib import Path
from typing import Optional
from dotenv import load_dotenv

# Load .env file if it exists
env_path = Path(".") / ".env"
if env_path.exists():
    load_dotenv(env_path)


class Config:
    """Configuration class for the application"""
    
    # API Configuration
    API_HOST: str = os.getenv("API_HOST", "0.0.0.0")
    API_PORT: int = int(os.getenv("API_PORT", "8000"))
    API_DEBUG: bool = os.getenv("API_DEBUG", "False").lower() == "true"
    
    # Model Configuration
    MODEL_NAME: str = os.getenv("MODEL_NAME", "bert-base-uncased")
    MAX_LENGTH: int = int(os.getenv("MAX_LENGTH", "128"))
    BATCH_SIZE: int = int(os.getenv("BATCH_SIZE", "16"))
    LEARNING_RATE: float = float(os.getenv("LEARNING_RATE", "2e-5"))
    EPOCHS: int = int(os.getenv("EPOCHS", "5"))
    
    # Privacy Configuration
    EPSILON: float = float(os.getenv("EPSILON", "1.0"))
    DELTA: float = float(os.getenv("DELTA", "1e-5"))
    
    # Session Configuration
    SESSION_TIMEOUT: int = int(os.getenv("SESSION_TIMEOUT", "3600"))
    MAX_SESSIONS: int = int(os.getenv("MAX_SESSIONS", "1000"))
    
    # Integration URLs
    COMPONENT1_URL: str = os.getenv("COMPONENT1_URL", "http://localhost:8001")
    COMPONENT2_URL: str = os.getenv("COMPONENT2_URL", "http://localhost:8002")
    COMPONENT4_URL: str = os.getenv("COMPONENT4_URL", "http://localhost:8004")
    
    # Voice Configuration
    TTS_RATE: int = int(os.getenv("TTS_RATE", "150"))
    TTS_VOLUME: float = float(os.getenv("TTS_VOLUME", "0.9"))
    STT_TIMEOUT: int = int(os.getenv("STT_TIMEOUT", "10"))
    
    # Database Configuration
    DATABASE_URL: str = os.getenv("DATABASE_URL", "sqlite:///./conversations.db")
    
    # Logging Configuration
    LOG_LEVEL: str = os.getenv("LOG_LEVEL", "INFO")
    LOG_FILE: str = os.getenv("LOG_FILE", "app.log")
    
    # CORS Configuration
    CORS_ORIGINS: str = os.getenv("CORS_ORIGINS", "*")
    
    # Feature Flags
    ENABLE_VOICE: bool = os.getenv("ENABLE_VOICE", "True").lower() == "true"
    ENABLE_INTEGRATION: bool = os.getenv("ENABLE_INTEGRATION", "True").lower() == "true"
    ENABLE_ANALYTICS: bool = os.getenv("ENABLE_ANALYTICS", "True").lower() == "true"
    ENABLE_CRISIS_ALERTS: bool = os.getenv("ENABLE_CRISIS_ALERTS", "True").lower() == "true"
    
    # Crisis Resources
    CRISIS_PHONE: str = os.getenv("CRISIS_PHONE", "988")
    CRISIS_TEXT: str = os.getenv("CRISIS_TEXT", "741741")
    EMERGENCY_NUMBER: str = os.getenv("EMERGENCY_NUMBER", "911")
    
    # Paths
    BASE_DIR: Path = Path(__file__).parent
    DATA_DIR: Path = BASE_DIR / "data"
    MODELS_DIR: Path = BASE_DIR / "models"
    LOGS_DIR: Path = BASE_DIR / "logs"
    
    @classmethod
    def ensure_directories(cls):
        """Ensure required directories exist"""
        for dir_path in [cls.DATA_DIR, cls.MODELS_DIR, cls.LOGS_DIR]:
            dir_path.mkdir(parents=True, exist_ok=True)
    
    @classmethod
    def get_crisis_resources(cls) -> dict:
        """Get crisis resource information"""
        return {
            "phone": cls.CRISIS_PHONE,
            "text": cls.CRISIS_TEXT,
            "emergency": cls.EMERGENCY_NUMBER,
            "message": (
                f"If you're experiencing a mental health crisis, please contact:\n"
                f"- National Suicide Prevention Lifeline: {cls.CRISIS_PHONE}\n"
                f"- Crisis Text Line: Text HELLO to {cls.CRISIS_TEXT}\n"
                f"- Emergency Services: {cls.EMERGENCY_NUMBER}"
            )
        }
    
    @classmethod
    def print_config(cls):
        """Print current configuration"""
        print("\n" + "="*70)
        print(" SYSTEM CONFIGURATION")
        print("="*70)
        print(f"API: {cls.API_HOST}:{cls.API_PORT}")
        print(f"Model: {cls.MODEL_NAME}")
        print(f"Privacy: ε={cls.EPSILON}, δ={cls.DELTA}")
        print(f"Features: Voice={cls.ENABLE_VOICE}, Integration={cls.ENABLE_INTEGRATION}")
        print("="*70 + "\n")


# Create global config instance
config = Config()

# Ensure directories exist
config.ensure_directories()


if __name__ == "__main__":
    # Print configuration
    config.print_config()
    
    # Print crisis resources
    print("Crisis Resources:")
    resources = config.get_crisis_resources()
    print(resources["message"])
