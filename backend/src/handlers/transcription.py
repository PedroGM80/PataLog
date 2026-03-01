"""
Handler para transcripcion de audio.
"""

import os
from ..protocol import Request, send_ok, send_error, log
from ..services import WhisperService


def handle_transcribe(request: Request, whisper: WhisperService) -> None:
    """Procesa una peticion de transcripcion."""
    audio_path = request.params.get("audio_path", "")
    language = request.params.get("language", "es")
    
    if not audio_path:
        send_error(request.id, "Falta el parametro 'audio_path'")
        return
    
    if not os.path.exists(audio_path):
        send_error(request.id, f"Archivo de audio no encontrado: {audio_path}")
        return
    
    log(f"Transcribiendo {audio_path}...")
    
    try:
        transcript = whisper.transcribe(audio_path, language)
        
        # Eliminar archivo temporal
        try:
            os.remove(audio_path)
        except OSError:
            pass
        
        log("Transcripcion completada.")
        send_ok(request.id, transcript=transcript)
        
    except Exception as e:
        send_error(request.id, f"Error en transcripcion: {e}")
