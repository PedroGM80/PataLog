# PataLog - Planning de Desarrollo

## Vision
Aplicacion de escritorio para veterinarios que transcribe consultas por voz y genera informes clinicos usando IA local (Whisper + Ollama).

## Arquitectura
- **Frontend**: Kotlin + Compose Desktop
- **Backend**: Python (Whisper para transcripcion, Ollama para LLM)
- **Comunicacion**: JSON sobre stdin/stdout (proceso hijo)
- **Persistencia**: SQLite local

---

## Sprint 1 - Estabilidad Core [COMPLETADO]
| Tarea | Estado | Archivo(s) |
|-------|--------|------------|
| Watchdog: detectar si Python muere y relanzar | HECHO | `BackendWatchdog.kt` |
| Manejo de errores visible en UI | HECHO | `state/UiState.kt`, `components/StateComponents.kt` |
| Timeout en llamadas al backend (3 min max) | HECHO | `BackendClient.kt` |
| Validaciones: no grabar sin animal, no guardar informe vacio | HECHO | `state/AppState.kt`, `ConsultationScreen.kt` |

---

## Sprint 2 - Datos Completos [COMPLETADO]
| Tarea | Estado | Archivo(s) |
|-------|--------|------------|
| Persistencia SQLite | HECHO | `data/Database.kt`, `data/*Repository.kt` |
| Editar animal y dueno | HECHO | `AnimalsScreen.kt`, `OwnersScreen.kt` |
| Eliminar con confirmacion | HECHO | `AnimalsScreen.kt`, `OwnersScreen.kt` |
| Busqueda por fecha en historial | HECHO | `HistoryScreen.kt` |
| Numero de colegiado en PDF | HECHO | `pdf/exporter.py`, `ClinicConfig` |

---

## Sprint 3 - Configuracion y Audio [COMPLETADO]
| Tarea | Estado | Archivo(s) |
|-------|--------|------------|
| Grabacion de audio real | HECHO | `audio/AudioRecorder.kt` |
| Pantalla de ajustes completa | HECHO | `SettingsScreen.kt` |
| Datos clinica: nombre, direccion, telefono | HECHO | `SettingsScreen.kt` |
| Selector de modelo Ollama | HECHO | `SettingsScreen.kt` |
| Idioma de transcripcion | HECHO | `SettingsScreen.kt` |
| Carpeta destino PDFs | HECHO | `SettingsScreen.kt` |
| Estado Ollama en UI | HECHO | `SettingsScreen.kt` |

---

## Sprint 4 - Onboarding [COMPLETADO]
| Tarea | Estado | Archivo(s) |
|-------|--------|------------|
| Detectar Ollama instalado y corriendo | HECHO | `services/ollama.py`, `OnboardingScreen.kt` |
| Detectar modelo descargado | HECHO | `services/ollama.py`, `OnboardingScreen.kt` |
| Formulario datos clinica en primer inicio | HECHO | `OnboardingScreen.kt` |
| Pantalla bienvenida con estado | HECHO | `OnboardingScreen.kt`, `App.kt` |

---

## Sprint 5 - Instalador [PENDIENTE]
| Tarea | Estado | Archivo(s) |
|-------|--------|------------|
| Empaquetar Kotlin con jpackage | Preparado | `build.gradle.kts` |
| Empaquetar Python con PyInstaller | Pendiente | |
| Instalador final | Pendiente | |

---

## Sprint 6 - Pulido [EN PROGRESO]
| Tarea | Estado | Archivo(s) |
|-------|--------|------------|
| Modo oscuro / claro | HECHO | `Theme.kt`, `App.kt`, `SettingsScreen.kt` |
| Atajos de teclado | Pendiente | |
| Indicador estado Python en barra | HECHO | `BackendStatusIndicator.kt` |

---

## Changelog

### [0.4.0] - 2025-03-01

#### Sprint 4 Completado - Onboarding
- **Pantalla de bienvenida** en primer inicio
  - Deteccion automatica de Ollama (conectado/desconectado)
  - Lista de modelos disponibles con selector
  - Formulario datos clinica (nombre, direccion, telefono, colegiado)
  - Comprobacion periodica del estado cada 5 segundos
  - Instrucciones si Ollama no esta instalado
- **Logica de primer inicio**: detecta si necesita onboarding por nombre default + colegiado vacio

#### Sprint 6 Parcial - Pulido
- **Modo oscuro/claro** con paleta verde veterinario
  - Esquema de colores personalizado (Theme.kt)
  - Switch en pantalla de Ajustes
  - Persistencia en base de datos
  - Cambio en tiempo real sin reiniciar

### [0.3.0] - 2025-02-13

#### Sprint 3 Completado - Audio Real y Ajustes
- **Grabacion de audio real** con Java Sound API (16kHz, 16-bit, mono WAV)
  - Deteccion automatica de microfono
  - Indicador de duracion en tiempo real
  - Boton cancelar grabacion
  - Formato compatible con Whisper
- **Pantalla de ajustes completa**:
  - Datos de clinica: nombre, direccion, telefono, colegiado
  - Estado de conexion con Ollama
  - Selector de modelo de IA (lista dinamica desde Ollama)
  - Selector de idioma de transcripcion (ES, EN, CA, GL, EU)
  - Selector de carpeta para PDFs
  - Seccion "Acerca de"

### [0.2.0] - 2025-02-13
- Persistencia SQLite completa
- Pantalla de historial con filtros
- CRUD de animales y propietarios

### [0.1.0] - 2025-02-13
- Arquitectura base
- Pantallas principales
- Watchdog y manejo de errores

---

## Como probar

### Requisitos previos
1. **Python 3.10+** con dependencias:
   ```bash
   cd backend
   pip install -r requirements.txt
   ```

2. **Ollama** instalado y corriendo:
   ```bash
   # Instalar desde https://ollama.ai
   ollama serve
   ollama pull llama3
   ```

3. **JDK 17+** para Kotlin

### Ejecutar la app
```bash
cd app
./gradlew run
```

### Flujo de prueba
1. Ir a **Ajustes** y verificar que Ollama esta conectado
2. Crear un **Propietario** (opcional)
3. Crear un **Paciente** (animal)
4. Ir a **Consulta**, seleccionar el paciente
5. Pulsar el boton de microfono y hablar
6. Al detener, se transcribe automaticamente
7. Pulsar "Generar informe" para crear el informe con IA
8. Editar el informe si es necesario
9. Guardar consulta y/o exportar PDF
