"""
Handler para generacion de informes y resumenes.
"""

from ..protocol import Request, send_ok, send_error, log
from ..services import OllamaService


def handle_report(request: Request, ollama: OllamaService) -> None:
    """Genera un informe clinico a partir de una transcripcion."""
    transcript = request.params.get("transcript", "")
    
    if not transcript.strip():
        send_error(request.id, "La transcripcion esta vacia")
        return
    
    log("Generando informe...")
    
    try:
        report = ollama.generate_report(transcript)
        log("Informe generado.")
        send_ok(request.id, report=report)
        
    except Exception as e:
        send_error(request.id, f"Error generando informe: {e}")


def handle_summary(request: Request, ollama: OllamaService) -> None:
    """Genera un resumen del historial clinico."""
    history = request.params.get("history", "")
    
    if not history.strip():
        send_error(request.id, "El historial esta vacio")
        return
    
    log("Generando resumen...")
    
    try:
        summary = ollama.generate_summary(history)
        log("Resumen generado.")
        send_ok(request.id, summary=summary)
        
    except Exception as e:
        send_error(request.id, f"Error generando resumen: {e}")
