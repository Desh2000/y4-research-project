"""
Quick Start Script
Automated setup and launch for the Empathetic Conversational Support System
"""

import os
import sys
import subprocess
import time
from pathlib import Path


def print_header(text):
    """Print formatted header"""
    print("\n" + "="*70)
    print(f" {text}")
    print("="*70 + "\n")


def print_step(step_num, total_steps, description):
    """Print step information"""
    print(f"[{step_num}/{total_steps}] {description}")


def check_python_version():
    """Check if Python version is compatible"""
    version = sys.version_info
    if version.major < 3 or (version.major == 3 and version.minor < 8):
        print("❌ Error: Python 3.8 or higher is required")
        print(f"Current version: {sys.version}")
        return False
    print(f"✓ Python {version.major}.{version.minor}.{version.micro}")
    return True


def check_virtual_environment():
    """Check if running in virtual environment"""
    in_venv = hasattr(sys, 'real_prefix') or (
        hasattr(sys, 'base_prefix') and sys.base_prefix != sys.prefix
    )
    if not in_venv:
        print("⚠️  Warning: Not running in a virtual environment")
        response = input("Continue anyway? (y/n): ")
        if response.lower() != 'y':
            return False
    else:
        print("✓ Running in virtual environment")
    return True


def check_datasets():
    """Check if datasets exist"""
    data_dir = Path("data")
    required_files = [
        "Mental_Health_FAQ.csv",
        "intents.json",
        "train.csv"
    ]
    
    missing_files = []
    for file in required_files:
        if not (data_dir / file).exists():
            missing_files.append(file)
    
    if missing_files:
        print(f"❌ Missing dataset files: {', '.join(missing_files)}")
        print("\nPlease copy the following files to the 'data/' directory:")
        print("  - Mental_Health_FAQ.csv")
        print("  - intents.json")
        print("  - train.csv")
        return False
    
    print("✓ All datasets found")
    return True


def install_dependencies():
    """Install required packages"""
    print("Installing dependencies...")
    try:
        subprocess.run(
            [sys.executable, "-m", "pip", "install", "-r", "requirements.txt"],
            check=True,
            capture_output=True
        )
        print("✓ Dependencies installed")
        return True
    except subprocess.CalledProcessError as e:
        print(f"❌ Error installing dependencies: {e}")
        return False


def download_nltk_data():
    """Download required NLTK data"""
    print("Downloading NLTK data...")
    try:
        import nltk
        nltk.download('punkt', quiet=True)
        nltk.download('stopwords', quiet=True)
        nltk.download('wordnet', quiet=True)
        print("✓ NLTK data downloaded")
        return True
    except Exception as e:
        print(f"❌ Error downloading NLTK data: {e}")
        return False


def check_model_trained():
    """Check if model is already trained"""
    model_dir = Path("models/trained_intent_classifier")
    if model_dir.exists() and (model_dir / "intent_model.pt").exists():
        print("✓ Trained model found")
        return True
    print("⚠️  Model not trained yet")
    return False


def train_model():
    """Train the intent classification model"""
    print("\n" + "-"*70)
    print("Training model (this may take 10-30 minutes)...")
    print("-"*70 + "\n")
    
    try:
        subprocess.run([sys.executable, "train_model.py"], check=True)
        return True
    except subprocess.CalledProcessError as e:
        print(f"❌ Error training model: {e}")
        return False


def start_backend():
    """Start the backend API"""
    print("\nStarting backend API...")
    print("API will be available at: http://localhost:8000")
    print("Press Ctrl+C to stop\n")
    
    try:
        subprocess.run([sys.executable, "backend/api.py"])
    except KeyboardInterrupt:
        print("\n\n✓ Backend stopped")


def start_frontend():
    """Start the frontend application"""
    print("\nStarting frontend application...")
    print("Web interface will open automatically")
    print("Press Ctrl+C to stop\n")
    
    try:
        subprocess.run(["streamlit", "run", "frontend/app.py"])
    except KeyboardInterrupt:
        print("\n\n✓ Frontend stopped")
    except FileNotFoundError:
        print("❌ Streamlit not found. Install with: pip install streamlit")


def main():
    """Main setup and launch function"""
    print_header("EMPATHETIC CONVERSATIONAL SUPPORT SYSTEM - QUICK START")
    
    # Check Python version
    print_step(1, 8, "Checking Python version...")
    if not check_python_version():
        return
    
    # Check virtual environment
    print_step(2, 8, "Checking environment...")
    if not check_virtual_environment():
        return
    
    # Check datasets
    print_step(3, 8, "Checking datasets...")
    if not check_datasets():
        return
    
    # Install dependencies
    print_step(4, 8, "Installing dependencies...")
    if not install_dependencies():
        print("⚠️  You may need to install dependencies manually:")
        print("   pip install -r requirements.txt")
    
    # Download NLTK data
    print_step(5, 8, "Downloading NLTK data...")
    download_nltk_data()
    
    # Check if model is trained
    print_step(6, 8, "Checking model status...")
    model_trained = check_model_trained()
    
    if not model_trained:
        print("\nModel needs to be trained before use.")
        response = input("Train model now? (y/n): ")
        if response.lower() == 'y':
            print_step(7, 8, "Training model...")
            if not train_model():
                print("❌ Model training failed. Please run manually:")
                print("   python train_model.py")
                return
        else:
            print("\nPlease train the model manually before starting:")
            print("   python train_model.py")
            return
    else:
        print_step(7, 8, "Model already trained ✓")
    
    # Launch options
    print_step(8, 8, "Ready to launch!")
    print("\nChoose an option:")
    print("  1. Start Backend API only")
    print("  2. Start Frontend only (requires backend running)")
    print("  3. Full system (Backend + Frontend in separate terminals)")
    print("  4. Exit")
    
    choice = input("\nEnter choice (1-4): ")
    
    if choice == "1":
        start_backend()
    elif choice == "2":
        start_frontend()
    elif choice == "3":
        print("\n" + "="*70)
        print("FULL SYSTEM LAUNCH")
        print("="*70)
        print("\nPlease run the following commands in SEPARATE terminals:")
        print("\n  Terminal 1 (Backend):")
        print("    .\\venv\\Scripts\\activate")
        print("    python backend/api.py")
        print("\n  Terminal 2 (Frontend):")
        print("    .\\venv\\Scripts\\activate")
        print("    streamlit run frontend/app.py")
        print("\n" + "="*70 + "\n")
    else:
        print("\nExiting...")
    
    print_header("SETUP COMPLETE")
    print("For detailed documentation, see: SETUP_GUIDE.md")
    print("For API documentation: http://localhost:8000/docs (when backend running)")
    print("\nThank you for using Manō! 💙\n")


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\nSetup interrupted by user")
    except Exception as e:
        print(f"\n❌ Unexpected error: {e}")
        print("\nPlease refer to SETUP_GUIDE.md for manual setup instructions")
