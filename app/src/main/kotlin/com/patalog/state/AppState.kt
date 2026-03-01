package com.patalog.state

import com.patalog.backend.BackendWatchdog
import com.patalog.domain.models.Animal
import com.patalog.domain.models.Owner
import com.patalog.domain.models.Consultation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Estado global de la aplicacion.
 * Centraliza todo el estado reactivo para facilitar la gestion.
 */
class AppState {
    
    // --- Backend ---
    private val _backendState = MutableStateFlow(BackendWatchdog.WatchdogState.STOPPED)
    val backendState: StateFlow<BackendWatchdog.WatchdogState> = _backendState.asStateFlow()
    
    fun updateBackendState(state: BackendWatchdog.WatchdogState) {
        _backendState.value = state
    }
    
    // --- Grabacion ---
    private val _recordingState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val recordingState: StateFlow<UiState<String>> = _recordingState.asStateFlow()
    
    fun updateRecordingState(state: UiState<String>) {
        _recordingState.value = state
    }
    
    // --- Transcripcion ---
    private val _transcriptionState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val transcriptionState: StateFlow<UiState<String>> = _transcriptionState.asStateFlow()
    
    fun updateTranscriptionState(state: UiState<String>) {
        _transcriptionState.value = state
    }
    
    // --- Generacion de informe ---
    private val _reportState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val reportState: StateFlow<UiState<String>> = _reportState.asStateFlow()
    
    fun updateReportState(state: UiState<String>) {
        _reportState.value = state
    }
    
    // --- Exportacion PDF ---
    private val _exportState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val exportState: StateFlow<UiState<String>> = _exportState.asStateFlow()
    
    fun updateExportState(state: UiState<String>) {
        _exportState.value = state
    }
    
    // --- Seleccion actual ---
    private val _selectedAnimal = MutableStateFlow<Animal?>(null)
    val selectedAnimal: StateFlow<Animal?> = _selectedAnimal.asStateFlow()
    
    fun selectAnimal(animal: Animal?) {
        _selectedAnimal.value = animal
    }
    
    private val _selectedOwner = MutableStateFlow<Owner?>(null)
    val selectedOwner: StateFlow<Owner?> = _selectedOwner.asStateFlow()
    
    fun selectOwner(owner: Owner?) {
        _selectedOwner.value = owner
    }
    
    // --- Validaciones (Sprint 1) ---
    
    fun canStartRecording(): Boolean {
        return _selectedAnimal.value != null
    }
    
    fun canSaveReport(): Boolean {
        val report = _reportState.value.getOrNull()
        return _selectedAnimal.value != null && !report.isNullOrBlank()
    }
    
    data class ValidationError(val field: String, val message: String)
    
    fun validateForRecording(): ValidationError? {
        if (_selectedAnimal.value == null) {
            return ValidationError("animal", "Selecciona un animal antes de grabar")
        }
        return null
    }
    
    fun validateForSave(): ValidationError? {
        if (_selectedAnimal.value == null) {
            return ValidationError("animal", "Selecciona un animal")
        }
        if (_reportState.value.getOrNull().isNullOrBlank()) {
            return ValidationError("report", "El informe esta vacio")
        }
        return null
    }
}
