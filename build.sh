#!/bin/bash
# ===========================================
# PataLog - Script de Build para Linux/Mac
# ===========================================

echo ""
echo "========================================"
echo "  PataLog Build Script"
echo "========================================"
echo ""

# Verificar Python
if ! command -v python3 &> /dev/null; then
    echo "[ERROR] Python no encontrado. Instala Python 3.11+"
    exit 1
fi

# Verificar Java
if ! command -v java &> /dev/null; then
    echo "[ERROR] Java no encontrado. Instala JDK 17+"
    exit 1
fi

echo "[1/4] Instalando dependencias de Python..."
cd backend
pip3 install -r requirements.txt
pip3 install pyinstaller

echo ""
echo "[2/4] Empaquetando backend con PyInstaller..."
pyinstaller --clean patalog-backend.spec
if [ $? -ne 0 ]; then
    echo "[ERROR] Fallo al empaquetar backend"
    exit 1
fi

echo ""
echo "[3/4] Copiando backend a dist..."
cd ..
mkdir -p dist
cp -r backend/dist/patalog-backend dist/

echo ""
echo "[4/4] Empaquetando aplicacion con Gradle..."
cd app

if [[ "$OSTYPE" == "darwin"* ]]; then
    ./gradlew packageDmg
else
    ./gradlew packageDeb
fi

if [ $? -ne 0 ]; then
    echo "[ERROR] Fallo al empaquetar aplicacion"
    exit 1
fi

echo ""
echo "========================================"
echo "  Build completado!"
echo "========================================"
echo ""
echo "Instalador en: app/build/compose/binaries/main/"
echo ""
