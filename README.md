# 🐾 PataLog

<p align="center">
  <img src="app/src/main/resources/TeckelSoftLogo.jpg" alt="TeckelSoft Logo" width="150">
</p>

<p align="center">
  <strong>Asistente de transcripción para consultas veterinarias con IA local</strong>
</p>

<p align="center">
  <a href="#características">Características</a> •
  <a href="#requisitos">Requisitos</a> •
  <a href="#instalación">Instalación</a> •
  <a href="#uso">Uso</a> •
  <a href="#desarrollo">Desarrollo</a> •
  <a href="#atajos-de-teclado">Atajos</a>
</p>

---

## Características

- 🎙️ **Grabación de audio** de consultas veterinarias
- 📝 **Transcripción automática** con Whisper (IA local)
- 🤖 **Generación de informes clínicos** con Ollama (LLM local)
- 📄 **Exportación a PDF** con datos de la clínica
- 🗃️ **Historial de consultas** con búsqueda por fecha
- 👥 **Gestión de pacientes y propietarios**
- 🌙 **Modo oscuro/claro**
- ⌨️ **Atajos de teclado** completos
- 🔒 **100% local** - Tus datos nunca salen de tu ordenador

## Requisitos

### Sistema
- Windows 10/11, macOS 10.15+, o Linux
- 8 GB RAM mínimo (16 GB recomendado)
- 10 GB espacio en disco

### Software
| Software | Versión | Descarga |
|----------|---------|----------|
| Python | 3.11+ | [python.org](https://python.org) |
| JDK | 17+ | [adoptium.net](https://adoptium.net) |
| Ollama | Última | [ollama.ai](https://ollama.ai) |

## Instalación

### 1. Instalar Ollama

```bash
# Windows/Mac: Descarga desde https://ollama.ai

# Linux:
curl -fsSL https://ollama.ai/install.sh | sh
```

Después de instalar, descarga un modelo:

```bash
ollama pull llama3
```

### 2. Clonar el repositorio

```bash
git clone https://github.com/PedroGM80/PataLog.git
cd PataLog
```

### 3. Instalar dependencias de Python

```bash
cd backend
pip install -r requirements.txt
```

### 4. Ejecutar la aplicación

```bash
cd app
./gradlew run        # Linux/Mac
gradlew.bat run      # Windows
```

## Uso

### Flujo de trabajo típico

1. **Configuración inicial** (primer inicio)
   - Introduce los datos de tu clínica
   - Verifica que Ollama esté conectado
   - Selecciona el modelo de IA

2. **Nueva consulta**
   - Selecciona o crea un paciente
   - Pulsa el botón de grabar (o `Ctrl+R`)
   - Habla durante la consulta
   - Detén la grabación

3. **Generar informe**
   - La transcripción aparece automáticamente
   - Pulsa "Generar informe" (o `Ctrl+G`)
   - Edita el informe si es necesario

4. **Guardar y exportar**
   - Guarda la consulta (`Ctrl+S`)
   - Exporta a PDF (`Ctrl+E`)

### Pantallas

| Pantalla | Descripción |
|----------|-------------|
| Consulta | Grabación, transcripción y generación de informes |
| Pacientes | Gestión de animales (CRUD) |
| Propietarios | Gestión de dueños (CRUD) |
| Historial | Consultas anteriores con filtros |
| Ajustes | Configuración de clínica, modelo IA, tema |

## Atajos de teclado

### Navegación global
| Atajo | Acción |
|-------|--------|
| `Ctrl+1` | Ir a Consulta |
| `Ctrl+2` | Ir a Pacientes |
| `Ctrl+3` | Ir a Propietarios |
| `Ctrl+4` | Ir a Historial |
| `Ctrl+,` | Ir a Ajustes |

### Pantalla de consulta
| Atajo | Acción |
|-------|--------|
| `Ctrl+R` / `F5` | Grabar / Detener |
| `Ctrl+G` | Generar informe |
| `Ctrl+S` | Guardar consulta |
| `Ctrl+E` | Exportar PDF |
| `Ctrl+L` | Limpiar formulario |
| `Escape` | Cancelar grabación |

### Listas (Pacientes/Propietarios)
| Atajo | Acción |
|-------|--------|
| `Ctrl+N` | Nuevo elemento |
| `Ctrl+F` | Buscar |
| `Escape` | Cerrar diálogo |

## Desarrollo

### Estructura del proyecto

```
PataLog/
├── app/                    # Frontend (Kotlin + Compose Desktop)
│   ├── src/main/kotlin/
│   │   └── com/patalog/
│   │       ├── ui/         # Pantallas y componentes
│   │       ├── data/       # Repositorios SQLite
│   │       ├── domain/     # Modelos de dominio
│   │       ├── backend/    # Cliente del backend Python
│   │       ├── audio/      # Grabación de audio
│   │       └── state/      # Estado de la aplicación
│   └── build.gradle.kts
├── backend/                # Backend (Python)
│   ├── src/
│   │   ├── handlers/       # Manejadores de peticiones
│   │   ├── services/       # Whisper y Ollama
│   │   └── pdf/           # Generación de PDF
│   └── requirements.txt
├── build-windows.bat       # Script de build para Windows
├── build.sh               # Script de build para Linux/Mac
└── PLANNING.md            # Roadmap y sprints
```

### Arquitectura

```
┌─────────────────┐     JSON/stdin      ┌─────────────────┐
│                 │◄──────────────────►│                 │
│  Kotlin/Compose │                     │  Python Backend │
│    (Frontend)   │                     │                 │
└────────┬────────┘                     └────────┬────────┘
         │                                       │
         ▼                                       ▼
   ┌──────────┐                          ┌─────────────┐
   │  SQLite  │                          │   Whisper   │
   │   (DB)   │                          │   Ollama    │
   └──────────┘                          └─────────────┘
```

### Comandos de desarrollo

```bash
# Ejecutar en modo desarrollo
cd app && ./gradlew run

# Ejecutar tests
cd app && ./gradlew test

# Generar instalador (Windows)
./build-windows.bat

# Generar instalador (Linux/Mac)
chmod +x build.sh && ./build.sh
```

### Contribuir

1. Fork el repositorio
2. Crea una rama (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -m 'feat: nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abre un Pull Request

## Tecnologías

- **Frontend**: Kotlin, Compose Desktop, Material 3
- **Backend**: Python 3.11
- **Transcripción**: OpenAI Whisper (local)
- **LLM**: Ollama (local)
- **Base de datos**: SQLite
- **PDF**: ReportLab

## Changelog

Ver [PLANNING.md](PLANNING.md) para el historial completo de cambios.

## Licencia

Este proyecto está bajo la Licencia MIT. Ver [LICENSE](LICENSE) para más detalles.

---

<p align="center">
  Desarrollado con ❤️ por <a href="https://github.com/PedroGM80">TeckelSoft</a>
</p>
