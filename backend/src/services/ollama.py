"""
Servicio de generacion de texto con Ollama.
"""

import requests
from ..protocol import log


class OllamaService:
    """Cliente para la API de Ollama."""
    
    def __init__(self, base_url: str = "http://localhost:11434", model: str = "llama3"):
        self._base_url = base_url
        self._model = model
    
    @property
    def model(self) -> str:
        return self._model
    
    @model.setter
    def model(self, value: str) -> None:
        self._model = value
    
    def is_available(self) -> bool:
        """Comprueba si Ollama esta corriendo."""
        try:
            response = requests.get(f"{self._base_url}/api/tags", timeout=5)
            return response.status_code == 200
        except requests.RequestException:
            return False
    
    def list_models(self) -> list[str]:
        """Lista los modelos disponibles en Ollama."""
        try:
            response = requests.get(f"{self._base_url}/api/tags", timeout=5)
            if response.status_code == 200:
                data = response.json()
                return [m["name"] for m in data.get("models", [])]
        except requests.RequestException:
            pass
        return []
    
    def generate(self, prompt: str, system: str = "") -> str:
        """
        Genera texto usando el modelo configurado.
        
        Args:
            prompt: Texto de entrada
            system: Prompt de sistema opcional
            
        Returns:
            Texto generado
        """
        payload = {
            "model": self._model,
            "prompt": prompt,
            "stream": False,
        }
        if system:
            payload["system"] = system
        
        response = requests.post(
            f"{self._base_url}/api/generate",
            json=payload,
            timeout=300  # 5 minutos max para generacion
        )
        response.raise_for_status()
        return response.json().get("response", "").strip()
    
    def generate_report(self, transcript: str) -> str:
        """Genera un informe clinico a partir de una transcripcion."""
        system = """Eres un asistente veterinario. A partir de la transcripcion de una consulta, 
genera un informe clinico estructurado con las siguientes secciones:
- MOTIVO DE CONSULTA
- ANAMNESIS
- EXPLORACION FISICA
- DIAGNOSTICO PRESUNTIVO
- TRATAMIENTO
- RECOMENDACIONES

Usa lenguaje tecnico profesional. Se conciso y preciso."""
        
        prompt = f"Transcripcion de la consulta:\n\n{transcript}\n\nGenera el informe clinico:"
        return self.generate(prompt, system)
    
    def generate_summary(self, history: str) -> str:
        """Genera un resumen del historial clinico."""
        system = """Eres un asistente veterinario. Resume el historial clinico del paciente 
de forma concisa, destacando:
- Problemas cronicos o recurrentes
- Tratamientos anteriores
- Alergias o contraindicaciones conocidas
- Ultima visita y motivo"""
        
        prompt = f"Historial clinico:\n\n{history}\n\nResumen:"
        return self.generate(prompt, system)
