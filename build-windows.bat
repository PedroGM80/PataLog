@echo off
REM ===========================================
REM PataLog - Script de Build para Windows
REM ===========================================

echo.
echo ========================================
echo   PataLog Build Script
echo ========================================
echo.

REM Verificar Python
python --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Python no encontrado. Instala Python 3.11+
    exit /b 1
)

REM Verificar Java
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java no encontrado. Instala JDK 17+
    exit /b 1
)

echo [1/4] Instalando dependencias de Python...
cd backend
pip install -r requirements.txt
pip install pyinstaller

echo.
echo [2/4] Empaquetando backend con PyInstaller...
pyinstaller --clean patalog-backend.spec
if errorlevel 1 (
    echo [ERROR] Fallo al empaquetar backend
    exit /b 1
)

echo.
echo [3/4] Copiando backend a dist...
cd ..
if not exist "dist" mkdir dist
xcopy /E /I /Y backend\dist\patalog-backend dist\patalog-backend

echo.
echo [4/4] Empaquetando aplicacion con Gradle...
cd app
call gradlew.bat packageMsi
if errorlevel 1 (
    echo [ERROR] Fallo al empaquetar aplicacion
    exit /b 1
)

echo.
echo ========================================
echo   Build completado!
echo ========================================
echo.
echo Instalador en: app\build\compose\binaries\main\msi\
echo.

cd ..
pause
