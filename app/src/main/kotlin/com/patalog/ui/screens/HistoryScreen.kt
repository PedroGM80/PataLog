package com.patalog.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patalog.data.AnimalRepository
import com.patalog.data.ConsultationRepository
import com.patalog.domain.models.Animal
import com.patalog.domain.models.Consultation
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Pantalla de historial de consultas con busqueda por fecha y texto.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    consultationRepository: ConsultationRepository,
    animalRepository: AnimalRepository
) {
    // Estado
    var consultations by remember { mutableStateOf(consultationRepository.getAll()) }
    var animals by remember { mutableStateOf(animalRepository.getAll()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedConsultation by remember { mutableStateOf<Consultation?>(null) }
    
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    
    // Funcion para recargar con filtros
    fun reload() {
        consultations = when {
            selectedDate != null -> {
                consultationRepository.getByDate(selectedDate!!.atStartOfDay())
            }
            searchQuery.isNotBlank() -> {
                consultationRepository.search(searchQuery)
            }
            else -> {
                consultationRepository.getAll()
            }
        }
    }
    
    // Obtener nombre del animal
    fun getAnimalName(animalId: Long): String {
        return animals.find { it.id == animalId }?.name ?: "Desconocido"
    }
    
    Row(modifier = Modifier.fillMaxSize()) {
        // Panel izquierdo: Lista de consultas
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(24.dp)
        ) {
            Text(
                text = "Historial de consultas",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Filtros
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Busqueda por texto
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it
                        selectedDate = null
                        reload()
                    },
                    label = { Text("Buscar en informes") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { 
                                searchQuery = ""
                                reload()
                            }) {
                                Icon(Icons.Default.Clear, "Limpiar")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                // Filtro por fecha
                OutlinedButton(
                    onClick = { showDatePicker = true }
                ) {
                    Icon(Icons.Default.CalendarMonth, null)
                    Spacer(Modifier.width(8.dp))
                    Text(selectedDate?.format(dateFormatter) ?: "Fecha")
                }
                
                if (selectedDate != null) {
                    IconButton(onClick = {
                        selectedDate = null
                        reload()
                    }) {
                        Icon(Icons.Default.Clear, "Quitar filtro de fecha")
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Lista de consultas
            if (consultations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = when {
                                selectedDate != null -> "No hay consultas en esta fecha"
                                searchQuery.isNotBlank() -> "Sin resultados para \"$searchQuery\""
                                else -> "No hay consultas registradas"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(consultations, key = { it.id }) { consultation ->
                        ConsultationListItem(
                            consultation = consultation,
                            animalName = getAnimalName(consultation.animalId),
                            isSelected = selectedConsultation?.id == consultation.id,
                            dateTimeFormatter = dateTimeFormatter,
                            onClick = { selectedConsultation = consultation }
                        )
                    }
                }
            }
        }
        
        // Panel derecho: Detalle de consulta
        if (selectedConsultation != null) {
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            )
            ConsultationDetailPanel(
                consultation = selectedConsultation!!,
                animalName = getAnimalName(selectedConsultation!!.animalId),
                dateTimeFormatter = dateTimeFormatter,
                onClose = { selectedConsultation = null }
            )
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (selectedDate ?: LocalDate.now())
                .atStartOfDay()
                .toInstant(java.time.ZoneOffset.UTC)
                .toEpochMilli()
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneOffset.UTC)
                            .toLocalDate()
                        searchQuery = ""
                        reload()
                    }
                    showDatePicker = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun ConsultationListItem(
    consultation: Consultation,
    animalName: String,
    isSelected: Boolean,
    dateTimeFormatter: DateTimeFormatter,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = animalName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = consultation.date.format(dateTimeFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (consultation.report.isNotBlank()) {
                    Text(
                        text = consultation.report.take(100) + if (consultation.report.length > 100) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
            
            if (isSelected) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ConsultationDetailPanel(
    consultation: Consultation,
    animalName: String,
    dateTimeFormatter: DateTimeFormatter,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(400.dp)
            .fillMaxHeight()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Detalle",
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, "Cerrar")
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Info basica
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Pets, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(animalName, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        consultation.date.format(dateTimeFormatter),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Contenido scrollable
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Transcripcion
            if (consultation.transcript.isNotBlank()) {
                Text(
                    text = "Transcripcion",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = consultation.transcript,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(16.dp))
            }
            
            // Informe
            if (consultation.report.isNotBlank()) {
                Text(
                    text = "Informe clinico",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = consultation.report,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(16.dp))
            }
            
            // Notas
            if (consultation.notes.isNotBlank()) {
                Text(
                    text = "Notas",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = consultation.notes,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
