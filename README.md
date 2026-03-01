# PataLog

Aplicacion de escritorio para veterinarios. Transcribe consultas por voz y genera informes clinicos usando IA local.

## Requisitos

- Python 3.11+
- JDK 17+
- Ollama instalado y corriendo

## Estructura

```
PataLog/
├── backend/          # Proceso Python (Whisper + Ollama)
├── app/              # Aplicacion Kotlin (Compose Desktop)
└── PLANNING.md       # Roadmap y tracking de sprints
```

## Desarrollo

### Backend
```bash
cd backend
pip install -r requirements.txt
python src/main.py
```

### App
```bash
cd app
./gradlew run
```

## Licencia

Pendiente
