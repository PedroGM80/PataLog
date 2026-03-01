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
import com.patalog.audio.AudioRecorder
import com.patalog.backend.BackendClient
import com.patalog.data.ClinicConfigRepository
import com.patalog.data.ConsultationRepository
import com.patalog.domain.models.Animal
import com.patalog.domain.models.Consultation
import com.patalog.state.AppState
import com.patalog.state.UiState
import com.patalog.ui.components.ErrorMessage
import com.patalog.ui.components.LoadingIndicator
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Pantalla principal de consulta.
 * Flujo: Seleccionar animal -> Grabar -> Transcribir -> Generar informe -> Guardar/Exportar
 */
@Composable
fun ConsultationScreen(
    appState: AppState,
    backendClient: BackendClient,
    consultationRepository: ConsultationRepository,
    clinicConfigRepository: ClinicConfigRepository,
    onSelectAnimal: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    val selectedAnimal by appState.selectedAnimal.collectAsState()
    val transcriptionState by appState.transcriptionState.collectAsState()
    val reportState by appState.reportState.collectAsState()
    val exportState by appState.exportState.collectAsState()
    
    // Cargar configuracion de clinica
    val clinicConfig = remember { clinicConfigRepository.get() }
    
    // Grabador de audio
    val audioRecorder = remember { AudioRecorder() }
    val recordingState by audioRecorder.state.collectAsState()
    val recordingDuration by audioRecorder.duration.collectAsState()
    
    // Estado local
    var audioFile by remember { mutableStateOf<File?>(null) }
    var notes by remember { mutableStateOf("") }
    var showValidationError by remember { mutableStateOf<String?>(null) }
    var showSaveSuccess by remember { mutableStateOf(false) }
    var microphoneError by remember { mutableStateOf<String?>(null) }
    
    // Verificar microfono al iniciar
    LaunchedEffect(Unit) {
        if (!audioRecorder.isMicrophoneAvailable()) {
            microphoneError = "No se detecta microfono. Verifica que esta conectado y tiene permisos."
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // --- Seccion: Animal seleccionado ---
        AnimalSelectionCard(
            animal = selectedAnimal,
            onSelectClick = onSelectAnimal,
            validationError = showValidationError?.takeIf { it.contains("animal") }
        )
        
        Spacer(Modifier.height(16.dp))
        
        // --- Seccion: Grabacion ---
        RecordingCard(
            recordingState = recordingState,
            recordingDuration = recordingDuration,
            canRecord = selectedAnimal != null && microphoneError == null,
            transcriptionState = transcriptionState,
            microphoneError = microphoneError,
            formatDuration = { audioRecorder.formatDuration(it) },
            onStartRecording = {
                val validation = appState.validateForRecording()
                if (validation != null) {
                    showValidationError = validation.message
                } else {
                    showValidationError = null
                    microphoneError = null
                    
                    if (!audioRecorder.start(scope)) {
                        microphoneError = "Error al iniciar grabacion. Verifica el microfono."
                    }
                }
            },
            onStopRecording = {
                scope.launch {
                    val file = audioRecorder.stop()
                    if (file != null && file.exists()) {
                        audioFile = file
                        
                        // Transcribir automaticamente
                        appState.updateTranscriptionState(UiState.Loading("Transcribiendo audio..."))
                        try {
                            val transcript = backendClient.transcribe(
                                file.absolutePath,
                                clinicConfig.transcriptionLanguage
                            )
                            appState.updateTranscriptionState(UiState.Success(transcript))
                        } catch (e: Exception) {
                            appState.updateTranscriptionState(
                                UiState.Error(e.message ?: "Error de transcripcion")
                            )
                        }
                    } else {
                        appState.updateTranscriptionState(
                            UiState.Error("No se pudo guardar la grabacion")
                        )
                    }
                }
            },
            onCancelRecording = {
                audioRecorder.cancel()
            }
        )
        
        Spacer(Modifier.height(16.dp))
        
        // --- Seccion: Transcripcion y generacion de informe ---
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Columna izquierda: Transcripcion
            TranscriptionCard(
                state = transcriptionState,
                onGenerateReport = { transcript ->
                    scope.launch {
                        appState.updateReportState(UiState.Loading("Generando informe..."))
                        try {
                            val report = backendClient.generateReport(transcript)
                            appState.updateReportState(UiState.Success(report))
                        } catch (e: Exception) {
                            appState.updateReportState(UiState.Error(e.message ?: "Error generando informe"))
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            )
            
            // Columna derecha: Informe
            ReportCard(
                state = reportState,
                onReportChange = { newReport ->
                    appState.updateReportState(UiState.Success(newReport))
                },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        // --- Seccion: Notas adicionales ---
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notas adicionales") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )
        
        Spacer(Modifier.height(16.dp))
        
        // --- Seccion: Acciones ---
        ActionsRow(
            canSave = appState.canSaveReport(),
            exportState = exportState,
            validationError = showValidationError,
            showSaveSuccess = showSaveSuccess,
            onSave = {
                val validation = appState.validateForSave()
                if (validation != null) {
                    showValidationError = validation.message
                } else {
                    showValidationError = null
                    
                    val animal = selectedAnimal!!
                    val transcript = transcriptionState.getOrNull() ?: ""
                    val report = reportState.getOrNull() ?: ""
                    
                    val consultation = Consultation(
                        animalId = animal.id,
                        date = LocalDateTime.now(),
                        transcript = transcript,
                        report = report,
                        notes = notes
                    )
                    
                    val id = consultationRepository.insert(consultation)
                    if (id > 0) {
                        showSaveSuccess = true
                        scope.launch {
                            kotlinx.coroutines.delay(2000)
                            showSaveSuccess = false
                        }
                    }
                }
            },
            onExportPdf = {
                val validation = appState.validateForSave()
                if (validation != null) {
                    showValidationError = validation.message
                    return@ActionsRow
                }
                showValidationError = null
                
                scope.launch {
                    appState.updateExportState(UiState.Loading("Generando PDF..."))
                    try {
                        val animal = selectedAnimal!!
                        val report = reportState.getOrNull() ?: ""
                        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                        
                        val outputDir = if (clinicConfig.pdfOutputFolder.isNotBlank()) {
                            clinicConfig.pdfOutputFolder
                        } else {
                            System.getProperty("user.home")
                        }
                        val outputPath = "$outputDir/PataLog_${animal.name}_$timestamp.pdf"
                        
                        val pdfPath = backendClient.exportPdf(
                            outputPath = outputPath,
                            animalName = animal.name,
                            animalSpecies = animal.species,
                            animalBreed = animal.breed,
                            report = report,
                            notes = notes,
                            consultationDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                            clinicName = clinicConfig.name,
                            vetLicense = clinicConfig.vetLicense
                        )
                        appState.updateExportState(UiState.Success(pdfPath))
                    } catch (e: Exception) {
                        appState.updateExportState(UiState.Error(e.message ?: "Error exportando PDF"))
                    }
                }
            },
            onClear = {
                audioRecorder.cancel()
                appState.updateTranscriptionState(UiState.Idle)
                appState.updateReportState(UiState.Idle)
                appState.updateExportState(UiState.Idle)
                notes = ""
                audioFile = null
                showValidationError = null
                showSaveSuccess = false
            }
        )
    }
}

@Composable
private fun AnimalSelectionCard(
    animal: Animal?,
    onSelectClick: () -> Unit,
    validationError: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (validationError != null) 
                MaterialTheme.colorScheme.errorContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (animal != null) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                if (animal != null) {
                    Text(
                        text = animal.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${animal.species}${if (animal.breed.isNotBlank()) " - ${animal.breed}" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Ningun animal seleccionado",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (validationError != null) {
                        Text(
                            text = validationError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Button(onClick = onSelectClick) {
                Text(if (animal != null) "Cambiar" else "Seleccionar")
            }
        }
    }
}

@Composable
private fun RecordingCard(
    recordingState: AudioRecorder.RecordingState,
    recordingDuration: Long,
    canRecord: Boolean,
    transcriptionState: UiState<String>,
    microphoneError: String?,
    formatDuration: (Long) -> String,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCancelRecording: () -> Unit
) {
    val isRecording = recordingState == AudioRecorder.RecordingState.RECORDING
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Error de microfono
            if (microphoneError != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        Icons.Default.MicOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = microphoneError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Boton de grabacion
                FilledIconButton(
                    onClick = { 
                        if (isRecording) onStopRecording() else onStartRecording() 
                    },
                    enabled = canRecord || isRecording,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isRecording) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecording) "Detener" else "Grabar",
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when {
                            isRecording -> "Grabando..."
                            transcriptionState.isLoading -> "Procesando audio..."
                            transcriptionState.isSuccess -> "Transcripcion lista"
                            transcriptionState.isError -> "Error en transcripcion"
                            recordingState == AudioRecorder.RecordingState.ERROR -> "Error de grabacion"
                            else -> "Pulsa para grabar la consulta"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    if (isRecording) {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Indicador de grabacion parpadeante
                            Icon(
                                Icons.Default.FiberManualRecord,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = formatDuration(recordingDuration),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                // Boton cancelar grabacion
                if (isRecording) {
                    OutlinedButton(onClick = onCancelRecording) {
                        Text("Cancelar")
                    }
                }
            }
            
            // Barra de progreso durante transcripcion
            if (transcriptionState.isLoading) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun TranscriptionCard(
    state: UiState<String>,
    onGenerateReport: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transcripcion",
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (state.isSuccess) {
                    Button(
                        onClick = { state.getOrNull()?.let { onGenerateReport(it) } },
                        enabled = !state.getOrNull().isNullOrBlank()
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Generar informe")
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (state) {
                    is UiState.Idle -> {
                        Text(
                            text = "La transcripcion aparecera aqui",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is UiState.Loading -> {
                        LoadingIndicator(state.message)
                    }
                    is UiState.Error -> {
                        ErrorMessage(state.message)
                    }
                    is UiState.Success -> {
                        Text(
                            text = state.data,
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportCard(
    state: UiState<String>,
    onReportChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize()
        ) {
            Text(
                text = "Informe clinico",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(Modifier.height(12.dp))
            
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (state) {
                    is UiState.Idle -> {
                        Text(
                            text = "El informe aparecera aqui",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is UiState.Loading -> {
                        LoadingIndicator(state.message)
                    }
                    is UiState.Error -> {
                        ErrorMessage(state.message)
                    }
                    is UiState.Success -> {
                        OutlinedTextField(
                            value = state.data,
                            onValueChange = onReportChange,
                            modifier = Modifier.fillMaxSize(),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionsRow(
    canSave: Boolean,
    exportState: UiState<String>,
    validationError: String?,
    showSaveSuccess: Boolean,
    onSave: () -> Unit,
    onExportPdf: () -> Unit,
    onClear: () -> Unit
) {
    Column {
        // Mostrar error de validacion
        if (validationError != null) {
            Text(
                text = validationError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // Mostrar exito al guardar
        if (showSaveSuccess) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Consulta guardada correctamente",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        // Mostrar estado de exportacion
        when (exportState) {
            is UiState.Loading -> {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
            }
            is UiState.Success -> {
                Text(
                    text = "PDF guardado: ${exportState.data}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            is UiState.Error -> {
                Text(
                    text = "Error: ${exportState.message}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            else -> {}
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
        ) {
            OutlinedButton(onClick = onClear) {
                Icon(Icons.Default.Clear, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Limpiar")
            }
            
            OutlinedButton(
                onClick = onExportPdf,
                enabled = canSave && !exportState.isLoading
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Exportar PDF")
            }
            
            Button(
                onClick = onSave,
                enabled = canSave
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Guardar consulta")
            }
        }
    }
}
