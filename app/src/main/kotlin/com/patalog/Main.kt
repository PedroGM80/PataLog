package com.patalog

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp
import com.patalog.data.*
import com.patalog.ui.App
import com.patalog.ui.SplashScreen
import com.patalog.backend.BackendClient
import com.patalog.backend.BackendWatchdog
import com.patalog.state.AppState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

fun main() = application {
    // Estado del splash
    var showSplash by remember { mutableStateOf(true) }
    
    // Inicializar base de datos
    Database.init()
    
    // Crear repositorios
    val repositories = Repositories(
        animals = AnimalRepository(),
        owners = OwnerRepository(),
        consultations = ConsultationRepository(),
        clinicConfig = ClinicConfigRepository()
    )
    
    val windowState = rememberWindowState(width = 1200.dp, height = 800.dp)
    
    // Scope de la aplicacion
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Estado global
    val appState = AppState()
    
    // Backend con watchdog
    val backendClient = BackendClient(
        pythonPath = "python",
        scriptPath = "../backend/src/main.py"
    )
    val watchdog = BackendWatchdog(backendClient)
    
    // Conectar watchdog con estado
    watchdog.onStateChange = { state -> 
        appState.updateBackendState(state)
    }
    
    // Iniciar watchdog
    watchdog.start(appScope)
    
    if (showSplash) {
        // Ventana de splash sin decoracion
        Window(
            onCloseRequest = { exitApplication() },
            title = "PataLog",
            state = rememberWindowState(width = 500.dp, height = 400.dp),
            undecorated = true,
            resizable = false,
            transparent = false
        ) {
            SplashScreen(
                onSplashFinished = { showSplash = false }
            )
        }
    } else {
        // Ventana principal
        Window(
            onCloseRequest = {
                watchdog.stop()
                Database.close()
                exitApplication()
            },
            title = "PataLog - Asistente Veterinario",
            state = windowState
        ) {
            App(appState, backendClient, repositories)
        }
    }
}

/**
 * Contenedor de todos los repositorios.
 */
data class Repositories(
    val animals: AnimalRepository,
    val owners: OwnerRepository,
    val consultations: ConsultationRepository,
    val clinicConfig: ClinicConfigRepository
)
