"""
Servicio de transcripcion con Whisper.
"""

import whisper
from ..protocol import log


class WhisperService:
    """Wrapper sobre el modelo Whisper."""
    
    def __init__(self, model_name: str = "base"):
        self._model_name = model_name
        self._model = None
    
    @property
    def is_loaded(self) -> bool:
        return self._model is not None
    
    def load(self) -> None:
        """Carga el modelo en memoria."""
        if self._model is None:
            log(f"Cargando modelo Whisper '{self._model_name}'...")
            self._model = whisper.load_model(self._model_name)
            log("Modelo Whisper cargado.")
    
    def transcribe(self, audio_path: str, language: str = "es") -> str:
        """
        Transcribe un archivo de audio.
        
        Args:
            audio_path: Ruta al archivo de audio
            language: Codigo de idioma (es, en, etc.)
            
        Returns:
            Texto transcrito
        """
        if self._model is None:
            self.load()
        
        result = self._model.transcribe(
            audio_path,
            language=language,
            fp16=False  # Compatibilidad con CPUs sin soporte FP16
        )
        return result["text"].strip()
