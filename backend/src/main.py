"""
Punto de entrada del backend PataLog.

Protocolo de comunicacion con la app Kotlin:
- Lee mensajes JSON de stdin, una linea por mensaje.
- Escribe respuestas JSON en stdout, una linea por respuesta.
- stderr se usa para logs internos.
"""

import sys
import os

# Asegurar que el directorio src esta en el path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from protocol import log, read_request, send_ok, send_error
from services import WhisperService, OllamaService
from handlers import handle_transcribe, handle_report, handle_summary, handle_export_pdf


def main():
    log("Iniciando backend PataLog...")
    
    # Inicializar servicios
    whisper = WhisperService(model_name="base")
    ollama = OllamaService()
    
    # Precargar Whisper
    log("Cargando modelo Whisper...")
    whisper.load()
    log("Whisper listo.")
    
    # Bucle principal
    while True:
        request = read_request()
        
        if request is None:
            # stdin cerrado
            break
        
        if not request.action:
            # Request vacio (error de parseo ya enviado)
            continue
        
        try:
            if request.action == "status":
                send_ok(
                    request.id,
                    whisper_ready=whisper.is_loaded,
                    ollama_ready=ollama.is_available(),
                    ollama_model=ollama.model,
                )
            
            elif request.action == "transcribe":
                handle_transcribe(request, whisper)
            
            elif request.action == "report":
                handle_report(request, ollama)
            
            elif request.action == "summary":
                handle_summary(request, ollama)
            
            elif request.action == "export_pdf":
                handle_export_pdf(request)
            
            elif request.action == "list_models":
                models = ollama.list_models()
                send_ok(request.id, models=models)
            
            elif request.action == "set_model":
                model = request.params.get("model", "")
                if model:
                    ollama.model = model
                    send_ok(request.id, model=model)
                else:
                    send_error(request.id, "Falta el parametro 'model'")
            
            elif request.action == "shutdown":
                log("Apagando backend.")
                send_ok(request.id, message="shutdown")
                break
            
            else:
                send_error(request.id, f"Accion desconocida: {request.action}")
        
        except Exception as e:
            log(f"Error procesando '{request.action}': {e}")
            send_error(request.id, str(e))
    
    log("Backend terminado.")


if __name__ == "__main__":
    main()
