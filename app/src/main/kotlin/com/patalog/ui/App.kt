package com.patalog.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import com.patalog.Repositories
import com.patalog.backend.BackendClient
import com.patalog.backend.BackendWatchdog
import com.patalog.state.AppState
import com.patalog.ui.components.BackendStatusIndicator
import com.patalog.ui.navigation.Screen
import com.patalog.ui.screens.*
import com.patalog.ui.theme.DarkColorScheme
import com.patalog.ui.theme.LightColorScheme

@Composable
fun App(appState: AppState, backendClient: BackendClient, repositories: Repositories) {
    val backendState by appState.backendState.collectAsState()
    
    // Detectar si es primer inicio
    val needsOnboarding = remember { repositories.clinicConfig.needsOnboarding() }
    var currentScreen by remember { 
        mutableStateOf(if (needsOnboarding) Screen.ONBOARDING else Screen.CONSULTATION) 
    }
    
    // Estado del tema
    var isDarkMode by remember { mutableStateOf(repositories.clinicConfig.get().darkMode) }
    val colorScheme = if (isDarkMode) DarkColorScheme else LightColorScheme
    
    // Focus para capturar atajos de teclado
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    MaterialTheme(colorScheme = colorScheme) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown && keyEvent.isCtrlPressed) {
                        when (keyEvent.key) {
                            Key.One, Key.NumPad1 -> {
                                if (currentScreen != Screen.ONBOARDING) {
                                    currentScreen = Screen.CONSULTATION
                                    true
                                } else false
                            }
                            Key.Two, Key.NumPad2 -> {
                                if (currentScreen != Screen.ONBOARDING) {
                                    currentScreen = Screen.ANIMALS
                                    true
                                } else false
                            }
                            Key.Three, Key.NumPad3 -> {
                                if (currentScreen != Screen.ONBOARDING) {
                                    currentScreen = Screen.OWNERS
                                    true
                                } else false
                            }
                            Key.Four, Key.NumPad4 -> {
                                if (currentScreen != Screen.ONBOARDING) {
                                    currentScreen = Screen.HISTORY
                                    true
                                } else false
                            }
                            Key.Comma -> {
                                if (currentScreen != Screen.ONBOARDING) {
                                    currentScreen = Screen.SETTINGS
                                    true
                                } else false
                            }
                            else -> false
                        }
                    } else false
                },
            color = MaterialTheme.colorScheme.background
        ) {
            // Onboarding sin barra lateral
            if (currentScreen == Screen.ONBOARDING) {
                OnboardingScreen(
                    clinicConfigRepository = repositories.clinicConfig,
                    backendClient = backendClient,
                    onComplete = { currentScreen = Screen.CONSULTATION }
                )
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Barra lateral de navegacion
                    NavigationRail(
                        backendState = backendState,
                        currentScreen = currentScreen,
                        onNavigate = { currentScreen = it }
                    )
                    
                    // Contenido principal
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (currentScreen) {
                            Screen.ONBOARDING -> { /* Handled above */ }
                            Screen.CONSULTATION -> ConsultationScreen(
                                appState = appState,
                                backendClient = backendClient,
                                consultationRepository = repositories.consultations,
                                clinicConfigRepository = repositories.clinicConfig,
                                onSelectAnimal = { currentScreen = Screen.ANIMALS }
                            )
                            Screen.ANIMALS -> AnimalsScreen(
                                appState = appState,
                                animalRepository = repositories.animals,
                                ownerRepository = repositories.owners,
                                onAnimalSelected = { currentScreen = Screen.CONSULTATION },
                                onBack = { currentScreen = Screen.CONSULTATION }
                            )
                            Screen.OWNERS -> OwnersScreen(
                                appState = appState,
                                ownerRepository = repositories.owners,
                                onOwnerSelected = { }
                            )
                            Screen.HISTORY -> HistoryScreen(
                                consultationRepository = repositories.consultations,
                                animalRepository = repositories.animals
                            )
                            Screen.SETTINGS -> SettingsScreen(
                                clinicConfigRepository = repositories.clinicConfig,
                                backendClient = backendClient,
                                isDarkMode = isDarkMode,
                                onThemeChange = { isDarkMode = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationRail(
    backendState: BackendWatchdog.WatchdogState,
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    NavigationRail(
        header = {
            Column(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "PataLog",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    ) {
        Spacer(Modifier.height(8.dp))
        
        NavigationRailItem(
            selected = currentScreen == Screen.CONSULTATION,
            onClick = { onNavigate(Screen.CONSULTATION) },
            icon = { Icon(Icons.Default.MedicalServices, "Consulta") },
            label = { Text("Consulta") }
        )
        
        NavigationRailItem(
            selected = currentScreen == Screen.ANIMALS,
            onClick = { onNavigate(Screen.ANIMALS) },
            icon = { Icon(Icons.Default.Pets, "Pacientes") },
            label = { Text("Pacientes") }
        )
        
        NavigationRailItem(
            selected = currentScreen == Screen.OWNERS,
            onClick = { onNavigate(Screen.OWNERS) },
            icon = { Icon(Icons.Default.People, "Propietarios") },
            label = { Text("Propietarios") }
        )
        
        NavigationRailItem(
            selected = currentScreen == Screen.HISTORY,
            onClick = { onNavigate(Screen.HISTORY) },
            icon = { Icon(Icons.Default.History, "Historial") },
            label = { Text("Historial") }
        )
        
        Spacer(Modifier.weight(1f))
        
        NavigationRailItem(
            selected = currentScreen == Screen.SETTINGS,
            onClick = { onNavigate(Screen.SETTINGS) },
            icon = { Icon(Icons.Default.Settings, "Ajustes") },
            label = { Text("Ajustes") }
        )
        
        // Estado del backend al fondo
        Spacer(Modifier.height(16.dp))
        BackendStatusIndicator(backendState)
        Spacer(Modifier.height(16.dp))
    }
}
