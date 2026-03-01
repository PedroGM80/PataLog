package com.patalog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patalog.backend.BackendClient
import com.patalog.data.ClinicConfigRepository
import com.patalog.domain.models.ClinicConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla de bienvenida y configuracion inicial.
 * Se muestra en el primer inicio de la aplicacion.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    clinicConfigRepository: ClinicConfigRepository,
    backendClient: BackendClient,
    onComplete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // Estado de Ollama
    var ollamaConnected by remember { mutableStateOf(false) }
    var availableModels by remember { mutableStateOf<List<String>>(emptyList()) }
    var checkingOllama by remember { mutableStateOf(true) }
    
    // Campos del formulario
    var clinicName by remember { mutableStateOf("") }
    var clinicAddress by remember { mutableStateOf("") }
    var clinicPhone by remember { mutableStateOf("") }
    var vetLicense by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf("") }
    var modelExpanded by remember { mutableStateOf(false) }
    
    // Validacion
    val isFormValid = clinicName.isNotBlank() && vetLicense.isNotBlank()
    
    // Comprobar Ollama al iniciar y periodicamente
    LaunchedEffect(Unit) {
        while (true) {
            checkingOllama = true
            try {
                val result = backendClient.checkOllama()
                ollamaConnected = result.available
                availableModels = result.models
                if (selectedModel.isBlank() && result.models.isNotEmpty()) {
                    selectedModel = result.models.first()
                }
            } catch (e: Exception) {
                ollamaConnected = false
                availableModels = emptyList()
            }
            checkingOllama = false
            delay(5000) // Recomprobar cada 5 segundos
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            
            // Logo y titulo
            Icon(
                imageVector = Icons.Default.LocalHospital,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = "Bienvenido a PataLog",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Configura tu clinica para empezar",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(48.dp))
            
            // Card de estado del sistema
            Card(
                modifier = Modifier.widthIn(max = 500.dp).fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Estado del sistema",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Estado de Ollama
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (checkingOllama) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (ollamaConnected) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (ollamaConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Spacer(Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = "Ollama (IA local)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = when {
                                    checkingOllama -> "Comprobando conexion..."
                                    ollamaConnected -> "Conectado - ${availableModels.size} modelo(s) disponible(s)"
                                    else -> "No conectado - Instala Ollama desde ollama.ai"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    if (!ollamaConnected && !checkingOllama) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedCard(
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "PataLog necesita Ollama para generar informes. Descargalo desde ollama.ai, ejecuta 'ollama serve' y luego 'ollama pull llama3'.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Card de datos de la clinica
            Card(
                modifier = Modifier.widthIn(max = 500.dp).fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Datos de la clinica",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = clinicName,
                        onValueChange = { clinicName = it },
                        label = { Text("Nombre de la clinica *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = clinicName.isBlank()
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = vetLicense,
                        onValueChange = { vetLicense = it },
                        label = { Text("Numero de colegiado *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = vetLicense.isBlank()
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = clinicAddress,
                        onValueChange = { clinicAddress = it },
                        label = { Text("Direccion") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = clinicPhone,
                        onValueChange = { clinicPhone = it },
                        label = { Text("Telefono") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (ollamaConnected && availableModels.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        
                        ExposedDropdownMenuBox(
                            expanded = modelExpanded,
                            onExpandedChange = { modelExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedModel,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Modelo de IA") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = modelExpanded,
                                onDismissRequest = { modelExpanded = false }
                            ) {
                                availableModels.forEach { model ->
                                    DropdownMenuItem(
                                        text = { Text(model) },
                                        onClick = {
                                            selectedModel = model
                                            modelExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        text = "* Campos obligatorios",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Boton de continuar
            Button(
                onClick = {
                    scope.launch {
                        val config = ClinicConfig(
                            name = clinicName,
                            address = clinicAddress,
                            phone = clinicPhone,
                            vetLicense = vetLicense,
                            ollamaModel = selectedModel.ifBlank { "llama3" },
                            transcriptionLanguage = "es",
                            pdfOutputFolder = ""
                        )
                        clinicConfigRepository.update(config)
                        onComplete()
                    }
                },
                enabled = isFormValid,
                modifier = Modifier.widthIn(max = 500.dp).fillMaxWidth().height(50.dp)
            ) {
                Text("Empezar a usar PataLog")
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}
