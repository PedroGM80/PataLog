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
import androidx.compose.ui.unit.dp
import com.patalog.backend.BackendClient
import com.patalog.data.ClinicConfigRepository
import com.patalog.domain.models.ClinicConfig
import kotlinx.coroutines.launch
import javax.swing.JFileChooser

/**
 * Pantalla de configuracion de la clinica.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    clinicConfigRepository: ClinicConfigRepository,
    backendClient: BackendClient,
    isDarkMode: Boolean = false,
    onThemeChange: (Boolean) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    
    // Cargar configuracion actual
    var config by remember { mutableStateOf(clinicConfigRepository.get()) }
    var hasChanges by remember { mutableStateOf(false) }
    var showSaveSuccess by remember { mutableStateOf(false) }
    
    // Estado del backend
    var ollamaAvailable by remember { mutableStateOf<Boolean?>(null) }
    var availableModels by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentModel by remember { mutableStateOf<String?>(null) }
    var loadingModels by remember { mutableStateOf(true) }
    
    // Cargar estado de Ollama al iniciar
    LaunchedEffect(Unit) {
        try {
            val status = backendClient.getStatus()
            ollamaAvailable = status.ollamaReady
            currentModel = status.ollamaModel
            
            if (status.ollamaReady) {
                availableModels = backendClient.listModels()
            }
        } catch (e: Exception) {
            ollamaAvailable = false
        }
        loadingModels = false
    }
    
    // Campos editables
    var clinicName by remember { mutableStateOf(config.name) }
    var clinicAddress by remember { mutableStateOf(config.address) }
    var clinicPhone by remember { mutableStateOf(config.phone) }
    var vetLicense by remember { mutableStateOf(config.vetLicense) }
    var selectedModel by remember { mutableStateOf(config.ollamaModel) }
    var transcriptionLanguage by remember { mutableStateOf(config.transcriptionLanguage) }
    var pdfOutputFolder by remember { mutableStateOf(config.pdfOutputFolder) }
    var darkMode by remember { mutableStateOf(isDarkMode) }
    
    // Detectar cambios
    LaunchedEffect(clinicName, clinicAddress, clinicPhone, vetLicense, selectedModel, transcriptionLanguage, pdfOutputFolder, darkMode) {
        hasChanges = clinicName != config.name ||
                clinicAddress != config.address ||
                clinicPhone != config.phone ||
                vetLicense != config.vetLicense ||
                selectedModel != config.ollamaModel ||
                transcriptionLanguage != config.transcriptionLanguage ||
                pdfOutputFolder != config.pdfOutputFolder ||
                darkMode != config.darkMode
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Cabecera
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Configuracion",
                style = MaterialTheme.typography.headlineMedium
            )
            
            if (hasChanges) {
                Button(
                    onClick = {
                        val newConfig = ClinicConfig(
                            name = clinicName,
                            address = clinicAddress,
                            phone = clinicPhone,
                            logoPath = config.logoPath,
                            vetLicense = vetLicense,
                            ollamaModel = selectedModel,
                            transcriptionLanguage = transcriptionLanguage,
                            pdfOutputFolder = pdfOutputFolder,
                            darkMode = darkMode
                        )
                        
                        if (clinicConfigRepository.update(newConfig)) {
                            config = newConfig
                            hasChanges = false
                            showSaveSuccess = true
                            onThemeChange(darkMode)
                            
                            // Cambiar modelo en backend si es diferente
                            if (selectedModel != currentModel && ollamaAvailable == true) {
                                scope.launch {
                                    try {
                                        backendClient.setModel(selectedModel)
                                        currentModel = selectedModel
                                    } catch (e: Exception) {
                                        // Ignorar error
                                    }
                                }
                            }
                            
                            scope.launch {
                                kotlinx.coroutines.delay(2000)
                                showSaveSuccess = false
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar cambios")
                }
            }
        }
        
        if (showSaveSuccess) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Configuracion guardada",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        // --- Seccion: Datos de la clinica ---
        SectionHeader(
            icon = Icons.Default.LocalHospital,
            title = "Datos de la clinica"
        )
        
        Spacer(Modifier.height(12.dp))
        
        OutlinedTextField(
            value = clinicName,
            onValueChange = { clinicName = it },
            label = { Text("Nombre de la clinica") },
            leadingIcon = { Icon(Icons.Default.Business, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(Modifier.height(12.dp))
        
        OutlinedTextField(
            value = clinicAddress,
            onValueChange = { clinicAddress = it },
            label = { Text("Direccion") },
            leadingIcon = { Icon(Icons.Default.LocationOn, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(Modifier.height(12.dp))
        
        OutlinedTextField(
            value = clinicPhone,
            onValueChange = { clinicPhone = it },
            label = { Text("Telefono") },
            leadingIcon = { Icon(Icons.Default.Phone, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(Modifier.height(12.dp))
        
        OutlinedTextField(
            value = vetLicense,
            onValueChange = { vetLicense = it },
            label = { Text("Numero de colegiado") },
            leadingIcon = { Icon(Icons.Default.Badge, null) },
            supportingText = { Text("Aparecera en los informes PDF") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(Modifier.height(32.dp))
        
        // --- Seccion: IA y transcripcion ---
        SectionHeader(
            icon = Icons.Default.Psychology,
            title = "IA y transcripcion"
        )
        
        Spacer(Modifier.height(12.dp))
        
        // Estado de Ollama
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (ollamaAvailable == true)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (ollamaAvailable == true) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (ollamaAvailable == true)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (ollamaAvailable == true) "Ollama conectado" else "Ollama no disponible",
                        style = MaterialTheme.typography.titleSmall
                    )
                    if (ollamaAvailable == true && currentModel != null) {
                        Text(
                            text = "Modelo activo: $currentModel",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else if (ollamaAvailable != true) {
                        Text(
                            text = "Instala Ollama desde ollama.ai",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        // Selector de modelo
        var modelDropdownExpanded by remember { mutableStateOf(false) }
        
        ExposedDropdownMenuBox(
            expanded = modelDropdownExpanded && availableModels.isNotEmpty(),
            onExpandedChange = { 
                if (availableModels.isNotEmpty()) {
                    modelDropdownExpanded = it 
                }
            }
        ) {
            OutlinedTextField(
                value = selectedModel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Modelo de IA") },
                leadingIcon = { Icon(Icons.Default.SmartToy, null) },
                trailingIcon = { 
                    if (loadingModels) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelDropdownExpanded)
                    }
                },
                supportingText = { 
                    Text(
                        if (availableModels.isEmpty() && !loadingModels) 
                            "No hay modelos. Ejecuta: ollama pull llama3" 
                        else 
                            "Modelo para generar informes"
                    ) 
                },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                enabled = availableModels.isNotEmpty()
            )
            
            ExposedDropdownMenu(
                expanded = modelDropdownExpanded,
                onDismissRequest = { modelDropdownExpanded = false }
            ) {
                availableModels.forEach { model ->
                    DropdownMenuItem(
                        text = { Text(model) },
                        onClick = {
                            selectedModel = model
                            modelDropdownExpanded = false
                        },
                        leadingIcon = {
                            if (model == currentModel) {
                                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                }
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        // Selector de idioma
        var languageDropdownExpanded by remember { mutableStateOf(false) }
        val languages = listOf(
            "es" to "Espanol",
            "en" to "English",
            "ca" to "Catala",
            "gl" to "Galego",
            "eu" to "Euskara"
        )
        val selectedLanguageName = languages.find { it.first == transcriptionLanguage }?.second ?: transcriptionLanguage
        
        ExposedDropdownMenuBox(
            expanded = languageDropdownExpanded,
            onExpandedChange = { languageDropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedLanguageName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Idioma de transcripcion") },
                leadingIcon = { Icon(Icons.Default.Translate, null) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageDropdownExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = languageDropdownExpanded,
                onDismissRequest = { languageDropdownExpanded = false }
            ) {
                languages.forEach { (code, name) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            transcriptionLanguage = code
                            languageDropdownExpanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(Modifier.height(32.dp))
        
        // --- Seccion: Archivos ---
        SectionHeader(
            icon = Icons.Default.Folder,
            title = "Archivos"
        )
        
        Spacer(Modifier.height(12.dp))
        
        // Carpeta de PDFs
        OutlinedTextField(
            value = pdfOutputFolder.ifBlank { "Carpeta de usuario (por defecto)" },
            onValueChange = {},
            readOnly = true,
            label = { Text("Carpeta para PDFs") },
            leadingIcon = { Icon(Icons.Default.FolderOpen, null) },
            trailingIcon = {
                Row {
                    if (pdfOutputFolder.isNotBlank()) {
                        IconButton(onClick = { pdfOutputFolder = "" }) {
                            Icon(Icons.Default.Clear, "Usar por defecto")
                        }
                    }
                    IconButton(
                        onClick = {
                            val chooser = JFileChooser().apply {
                                fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                                dialogTitle = "Seleccionar carpeta para PDFs"
                            }
                            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                pdfOutputFolder = chooser.selectedFile.absolutePath
                            }
                        }
                    ) {
                        Icon(Icons.Default.FolderOpen, "Seleccionar carpeta")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(Modifier.height(32.dp))
        
        // --- Seccion: Apariencia ---
        SectionHeader(
            icon = Icons.Default.Palette,
            title = "Apariencia"
        )
        
        Spacer(Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (darkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Modo oscuro",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = if (darkMode) "Activado" else "Desactivado",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = darkMode,
                    onCheckedChange = { darkMode = it }
                )
            }
        }
        
        Spacer(Modifier.height(32.dp))
        
        // --- Seccion: Atajos de teclado ---
        SectionHeader(
            icon = Icons.Default.Keyboard,
            title = "Atajos de teclado"
        )
        
        Spacer(Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Navegacion",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                KeyboardShortcutRow("Ctrl+1", "Consulta")
                KeyboardShortcutRow("Ctrl+2", "Pacientes")
                KeyboardShortcutRow("Ctrl+3", "Propietarios")
                KeyboardShortcutRow("Ctrl+4", "Historial")
                KeyboardShortcutRow("Ctrl+,", "Ajustes")
                
                Spacer(Modifier.height(12.dp))
                
                Text(
                    text = "Consulta",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                KeyboardShortcutRow("Ctrl+R / F5", "Grabar / Detener")
                KeyboardShortcutRow("Ctrl+G", "Generar informe")
                KeyboardShortcutRow("Ctrl+S", "Guardar consulta")
                KeyboardShortcutRow("Ctrl+E", "Exportar PDF")
                KeyboardShortcutRow("Ctrl+L", "Limpiar")
                KeyboardShortcutRow("Escape", "Cancelar grabacion")
                
                Spacer(Modifier.height(12.dp))
                
                Text(
                    text = "Listas",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                KeyboardShortcutRow("Ctrl+N", "Nuevo elemento")
                KeyboardShortcutRow("Ctrl+F", "Buscar")
                KeyboardShortcutRow("Escape", "Cerrar dialogo")
            }
        }
        
        Spacer(Modifier.height(32.dp))
        
        // --- Seccion: Acerca de ---
        SectionHeader(
            icon = Icons.Default.Info,
            title = "Acerca de"
        )
        
        Spacer(Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "PataLog",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Version 0.5.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Asistente de transcripcion para consultas veterinarias con IA local.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Whisper (OpenAI) + Ollama",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun KeyboardShortcutRow(
    shortcut: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = shortcut,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
    Divider(modifier = Modifier.padding(top = 8.dp))
}
