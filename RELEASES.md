# Descargas y Releases de PataLog

## Descargar Instaladores

### Windows
Descarga la última versión de:
- **PataLog-x.x.x.msi** - Instalador estándar (recomendado)
- **PataLog-x.x.x.exe** - Ejecutable portátil

### Linux
Descarga la última versión de:
- **patalog-x.x.x.deb** - Para Ubuntu/Debian

### macOS
Descarga la última versión de:
- **PataLog-x.x.x.dmg** - Instalador estándar

## Ubicación de Descargas

Todas las versiones están disponibles en:
👉 [GitHub Releases](https://github.com/PedroGM80/PataLog/releases)

## Crear una Nueva Release

### Método 1: Automático (Recomendado)

1. **Actualiza la versión** en `app/build.gradle.kts`:
   ```kotlin
   version = "0.6.0"
   ```

2. **Crear un tag** en Git:
   ```bash
   git tag v0.6.0
   git push origin v0.6.0
   ```

3. **GitHub Actions compilará automáticamente** y creará la release con los instaladores.

### Método 2: Manual

1. **Crea la tag**:
   ```bash
   git tag v0.6.0
   git push origin v0.6.0
   ```

2. **Espera a que GitHub Actions termine** (puedes ver el progreso en Actions).

3. **Verifica en Releases** que los instaladores están disponibles.

## Requisitos del Sistema

### Windows
- Windows 10 o superior (64-bit)
- Java Runtime Environment (JRE) 17+ (opcional, incluido en algunos instaladores)
- Mínimo 4GB RAM
- 500MB disco disponible

### Linux
- Ubuntu 20.04 LTS o superior / Debian 11+
- Java Runtime Environment (JRE) 17+
- Mínimo 4GB RAM
- 500MB disco disponible

### Servicios Locales (Todos los Sistemas Operativos)
- **Ollama** - Para generación de reportes con IA local
  - Descarga en: https://ollama.ai
  - Modelo recomendado: `ollama pull llama2`

## Instalación

### Windows
1. Descarga el `.msi`
2. Ejecuta el instalador
3. Sigue las instrucciones
4. Ejecuta PataLog desde el menú Inicio

### Linux
```bash
sudo dpkg -i patalog-x.x.x.deb
patalog
```

### macOS
1. Descarga el `.dmg`
2. Arrastra PataLog a Applications
3. Ejecuta desde Aplicaciones

## Soporte

Si encuentras problemas:
1. Revisa [Issues](https://github.com/PedroGM80/PataLog/issues)
2. Crea un nuevo issue con detalles del error
3. Incluye tu sistema operativo y versión de PataLog

## Build Manual

Si necesitas compilar desde fuente:

### Windows
```bash
cd PataLog
build-windows.bat
```

### Linux/macOS
```bash
cd PataLog
chmod +x build.sh
./build.sh
```

Los instaladores se guardarán en `app/build/compose/binaries/main/`
