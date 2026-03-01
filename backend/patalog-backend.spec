# -*- mode: python ; coding: utf-8 -*-
# PataLog Backend - PyInstaller spec file

import sys
from pathlib import Path

block_cipher = None

# Ruta base del proyecto
backend_dir = Path(SPECPATH)

a = Analysis(
    ['src/main.py'],
    pathex=[str(backend_dir)],
    binaries=[],
    datas=[
        # Incluir cualquier archivo de datos necesario
    ],
    hiddenimports=[
        'whisper',
        'torch',
        'numpy',
        'requests',
        'reportlab',
        'reportlab.lib',
        'reportlab.lib.pagesizes',
        'reportlab.lib.styles',
        'reportlab.lib.units',
        'reportlab.platypus',
        'reportlab.pdfgen',
    ],
    hookspath=[],
    hooksconfig={},
    runtime_hooks=[],
    excludes=[
        'tkinter',
        'matplotlib',
        'PIL',
        'PyQt5',
        'PyQt6',
    ],
    win_no_prefer_redirects=False,
    win_private_assemblies=False,
    cipher=block_cipher,
    noarchive=False,
)

pyz = PYZ(a.pure, a.zipped_data, cipher=block_cipher)

exe = EXE(
    pyz,
    a.scripts,
    [],
    exclude_binaries=True,
    name='patalog-backend',
    debug=False,
    bootloader_ignore_signals=False,
    strip=False,
    upx=True,
    console=True,  # Necesario para stdin/stdout
    disable_windowed_traceback=False,
    argv_emulation=False,
    target_arch=None,
    codesign_identity=None,
    entitlements_file=None,
)

coll = COLLECT(
    exe,
    a.binaries,
    a.zipfiles,
    a.datas,
    strip=False,
    upx=True,
    upx_exclude=[],
    name='patalog-backend',
)
