"""
Voice Support Module
Implements speech-to-text and text-to-speech functionality
"""

import os
from typing import Optional


class VoiceSupport:
    """
    Voice support for the conversational system
    Provides speech-to-text and text-to-speech capabilities
    """
    
    def __init__(self):
        self.stt_engine = None
        self.tts_engine = None
        self._initialize_engines()
    
    def _initialize_engines(self):
        """Initialize speech engines"""
        # Initialize TTS
        try:
            import pyttsx3
            self.tts_engine = pyttsx3.init()
            self.tts_engine.setProperty('rate', 150)  # Speed
            self.tts_engine.setProperty('volume', 0.9)  # Volume
            print("✓ Text-to-speech engine initialized")
        except Exception as e:
            print(f"Warning: Could not initialize TTS engine: {e}")
        
        # Initialize STT
        try:
            import speech_recognition as sr
            self.stt_engine = sr.Recognizer()
            print("✓ Speech-to-text engine initialized")
        except Exception as e:
            print(f"Warning: Could not initialize STT engine: {e}")
    
    def text_to_speech(self, text: str, save_to_file: Optional[str] = None) -> bool:
        """
        Convert text to speech
        
        Args:
            text: Text to convert
            save_to_file: Optional path to save audio file
        
        Returns:
            Success status
        """
        if not self.tts_engine:
            print("TTS engine not available")
            return False
        
        try:
            if save_to_file:
                self.tts_engine.save_to_file(text, save_to_file)
                self.tts_engine.runAndWait()
            else:
                self.tts_engine.say(text)
                self.tts_engine.runAndWait()
            return True
        except Exception as e:
            print(f"Error in TTS: {e}")
            return False
    
    def speech_to_text(self, audio_file: Optional[str] = None, 
                       use_microphone: bool = False) -> Optional[str]:
        """
        Convert speech to text
        
        Args:
            audio_file: Path to audio file (WAV format)
            use_microphone: Use microphone for live recording
        
        Returns:
            Transcribed text or None
        """
        if not self.stt_engine:
            print("STT engine not available")
            return None
        
        import speech_recognition as sr
        
        try:
            if use_microphone:
                # Record from microphone
                with sr.Microphone() as source:
                    print("Listening... Speak now.")
                    self.stt_engine.adjust_for_ambient_noise(source, duration=1)
                    audio = self.stt_engine.listen(source, timeout=10)
            
            elif audio_file:
                # Load from file
                with sr.AudioFile(audio_file) as source:
                    audio = self.stt_engine.record(source)
            else:
                print("No audio source specified")
                return None
            
            # Recognize speech using Google Speech Recognition
            text = self.stt_engine.recognize_google(audio)
            return text
            
        except sr.UnknownValueError:
            print("Could not understand audio")
            return None
        except sr.RequestError as e:
            print(f"Could not request results: {e}")
            return None
        except Exception as e:
            print(f"Error in STT: {e}")
            return None
    
    def set_voice_properties(self, rate: int = 150, volume: float = 0.9, 
                            voice_id: Optional[int] = None):
        """
        Set voice properties for TTS
        
        Args:
            rate: Speech rate (words per minute)
            volume: Volume level (0.0 to 1.0)
            voice_id: Voice ID (platform dependent)
        """
        if not self.tts_engine:
            return
        
        try:
            self.tts_engine.setProperty('rate', rate)
            self.tts_engine.setProperty('volume', volume)
            
            if voice_id is not None:
                voices = self.tts_engine.getProperty('voices')
                if voice_id < len(voices):
                    self.tts_engine.setProperty('voice', voices[voice_id].id)
        except Exception as e:
            print(f"Error setting voice properties: {e}")
    
    def list_available_voices(self):
        """List available TTS voices"""
        if not self.tts_engine:
            print("TTS engine not available")
            return []
        
        try:
            voices = self.tts_engine.getProperty('voices')
            print(f"\nAvailable voices ({len(voices)}):")
            for i, voice in enumerate(voices):
                print(f"{i}: {voice.name} ({voice.languages})")
            return voices
        except Exception as e:
            print(f"Error listing voices: {e}")
            return []


class AudioRecorder:
    """
    Record audio from microphone for voice messages
    """
    
    def __init__(self, output_dir: str = "audio_recordings"):
        self.output_dir = output_dir
        os.makedirs(output_dir, exist_ok=True)
    
    def record_audio(self, duration: int = 10, filename: Optional[str] = None) -> str:
        """
        Record audio from microphone
        
        Args:
            duration: Recording duration in seconds
            filename: Output filename (auto-generated if None)
        
        Returns:
            Path to recorded audio file
        """
        import pyaudio
        import wave
        from datetime import datetime
        
        if not filename:
            filename = f"recording_{datetime.now().strftime('%Y%m%d_%H%M%S')}.wav"
        
        filepath = os.path.join(self.output_dir, filename)
        
        # Recording parameters
        CHUNK = 1024
        FORMAT = pyaudio.paInt16
        CHANNELS = 1
        RATE = 16000
        
        p = pyaudio.PyAudio()
        
        print(f"Recording for {duration} seconds...")
        
        stream = p.open(
            format=FORMAT,
            channels=CHANNELS,
            rate=RATE,
            input=True,
            frames_per_buffer=CHUNK
        )
        
        frames = []
        for _ in range(0, int(RATE / CHUNK * duration)):
            data = stream.read(CHUNK)
            frames.append(data)
        
        print("Recording finished")
        
        stream.stop_stream()
        stream.close()
        p.terminate()
        
        # Save to file
        wf = wave.open(filepath, 'wb')
        wf.setnchannels(CHANNELS)
        wf.setsampwidth(p.get_sample_size(FORMAT))
        wf.setframerate(RATE)
        wf.writeframes(b''.join(frames))
        wf.close()
        
        print(f"Audio saved to: {filepath}")
        return filepath


# Integration with chatbot
class VoiceChatbot:
    """
    Voice-enabled chatbot interface
    """
    
    def __init__(self, chat_function):
        """
        Initialize voice chatbot
        
        Args:
            chat_function: Function to call for text-based chat
                          Should accept (message: str) and return response: str
        """
        self.chat_function = chat_function
        self.voice_support = VoiceSupport()
    
    def voice_chat(self, use_microphone: bool = True, 
                   audio_file: Optional[str] = None) -> tuple:
        """
        Conduct a voice-based chat interaction
        
        Args:
            use_microphone: Use microphone for input
            audio_file: Path to audio file (if not using microphone)
        
        Returns:
            (user_text, bot_response_text)
        """
        # Convert speech to text
        print("\n🎤 Listening...")
        user_text = self.voice_support.speech_to_text(
            audio_file=audio_file,
            use_microphone=use_microphone
        )
        
        if not user_text:
            print("Could not understand audio")
            return None, None
        
        print(f"You said: {user_text}")
        
        # Get chatbot response
        bot_response = self.chat_function(user_text)
        
        print(f"Bot: {bot_response}")
        
        # Convert response to speech
        print("🔊 Speaking...")
        self.voice_support.text_to_speech(bot_response)
        
        return user_text, bot_response


if __name__ == "__main__":
    # Test voice support
    print("Testing Voice Support Module\n")
    
    voice = VoiceSupport()
    
    # Test TTS
    print("\n1. Testing Text-to-Speech")
    test_text = "Hello, I am your empathetic mental health support assistant. How are you feeling today?"
    voice.text_to_speech(test_text)
    
    # List voices
    print("\n2. Available Voices")
    voice.list_available_voices()
    
    print("\n3. Speech-to-Text")
    print("Note: STT requires microphone access or audio file")
    print("To test: voice.speech_to_text(use_microphone=True)")
