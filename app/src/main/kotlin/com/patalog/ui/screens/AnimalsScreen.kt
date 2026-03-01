package com.patalog.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patalog.data.AnimalRepository
import com.patalog.data.OwnerRepository
import com.patalog.domain.models.Animal
import com.patalog.domain.models.Owner
import com.patalog.state.AppState

/**
 * Pantalla de gestion de animales.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalsScreen(
    appState: AppState,
    animalRepository: AnimalRepository,
    ownerRepository: OwnerRepository,
    onAnimalSelected: (Animal) -> Unit,
    onBack: () -> Unit
) {
    val selectedAnimal by appState.selectedAnimal.collectAsState()
    
    // Cargar datos de la base de datos
    var animals by remember { mutableStateOf(animalRepository.getAll()) }
    var owners by remember { mutableStateOf(ownerRepository.getAll()) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var animalToEdit by remember { mutableStateOf<Animal?>(null) }
    var animalToDelete by remember { mutableStateOf<Animal?>(null) }
    
    // Funcion para recargar
    fun reload() {
        animals = if (searchQuery.isBlank()) {
            animalRepository.getAll()
        } else {
            animalRepository.search(searchQuery)
        }
    }
    
    // Filtrar por busqueda local
    val filteredAnimals = if (searchQuery.isBlank()) {
        animals
    } else {
        animals.filter { animal ->
            animal.name.contains(searchQuery, ignoreCase = true) ||
            animal.species.contains(searchQuery, ignoreCase = true) ||
            animal.breed.contains(searchQuery, ignoreCase = true)
        }
    }
    
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        // Cabecera
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Volver")
                }
                Text(
                    text = "Pacientes",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            
            Button(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Nuevo paciente")
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Buscador
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar por nombre, especie o raza") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, "Limpiar")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(Modifier.height(16.dp))
        
        // Lista de animales
        if (filteredAnimals.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Pets,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isBlank()) "No hay pacientes registrados" else "Sin resultados",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (searchQuery.isBlank()) {
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { showAddDialog = true }) {
                            Text("Añadir el primero")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredAnimals, key = { it.id }) { animal ->
                    val owner = owners.find { it.id == animal.ownerId }
                    AnimalCard(
                        animal = animal,
                        owner = owner,
                        isSelected = animal.id == selectedAnimal?.id,
                        onSelect = { 
                            appState.selectAnimal(animal)
                            onAnimalSelected(animal)
                        },
                        onEdit = { animalToEdit = animal },
                        onDelete = { animalToDelete = animal }
                    )
                }
            }
        }
    }
    
    // Dialog: Añadir animal
    if (showAddDialog) {
        AnimalFormDialog(
            animal = null,
            owners = owners,
            onDismiss = { showAddDialog = false },
            onSave = { newAnimal ->
                val id = animalRepository.insert(newAnimal)
                if (id > 0) {
                    reload()
                }
                showAddDialog = false
            }
        )
    }
    
    // Dialog: Editar animal
    animalToEdit?.let { animal ->
        AnimalFormDialog(
            animal = animal,
            owners = owners,
            onDismiss = { animalToEdit = null },
            onSave = { updated ->
                if (animalRepository.update(updated)) {
                    reload()
                    // Actualizar seleccion si es el animal actual
                    if (selectedAnimal?.id == updated.id) {
                        appState.selectAnimal(updated)
                    }
                }
                animalToEdit = null
            }
        )
    }
    
    // Dialog: Confirmar eliminacion
    animalToDelete?.let { animal ->
        AlertDialog(
            onDismissRequest = { animalToDelete = null },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Eliminar paciente") },
            text = { 
                Text("¿Seguro que quieres eliminar a ${animal.name}? Se eliminaran tambien todas sus consultas. Esta accion no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (animalRepository.delete(animal.id)) {
                            reload()
                            if (selectedAnimal?.id == animal.id) {
                                appState.selectAnimal(null)
                            }
                        }
                        animalToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { animalToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun AnimalCard(
    animal: Animal,
    owner: Owner?,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) 
            CardDefaults.outlinedCardBorder() 
        else 
            null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = animal.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${animal.species}${if (animal.breed.isNotBlank()) " - ${animal.breed}" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (owner != null) {
                    Text(
                        text = "Propietario: ${owner.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Editar")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
            
            if (isSelected) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Seleccionado",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimalFormDialog(
    animal: Animal?,
    owners: List<Owner>,
    onDismiss: () -> Unit,
    onSave: (Animal) -> Unit
) {
    var name by remember { mutableStateOf(animal?.name ?: "") }
    var species by remember { mutableStateOf(animal?.species ?: "") }
    var breed by remember { mutableStateOf(animal?.breed ?: "") }
    var selectedOwnerId by remember { mutableStateOf(animal?.ownerId ?: 0L) }
    var ownerDropdownExpanded by remember { mutableStateOf(false) }
    
    var nameError by remember { mutableStateOf<String?>(null) }
    var speciesError by remember { mutableStateOf<String?>(null) }
    
    val selectedOwner = owners.find { it.id == selectedOwnerId }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (animal == null) "Nuevo paciente" else "Editar paciente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        nameError = null
                    },
                    label = { Text("Nombre *") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = species,
                    onValueChange = { 
                        species = it
                        speciesError = null
                    },
                    label = { Text("Especie *") },
                    placeholder = { Text("Perro, Gato, Ave...") },
                    isError = speciesError != null,
                    supportingText = speciesError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Raza") },
                    placeholder = { Text("Opcional") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Selector de propietario
                ExposedDropdownMenuBox(
                    expanded = ownerDropdownExpanded,
                    onExpandedChange = { ownerDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedOwner?.name ?: "Sin propietario",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Propietario") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ownerDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = ownerDropdownExpanded,
                        onDismissRequest = { ownerDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin propietario") },
                            onClick = {
                                selectedOwnerId = 0
                                ownerDropdownExpanded = false
                            }
                        )
                        owners.forEach { owner ->
                            DropdownMenuItem(
                                text = { Text(owner.name) },
                                onClick = {
                                    selectedOwnerId = owner.id
                                    ownerDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validar
                    var valid = true
                    if (name.isBlank()) {
                        nameError = "El nombre es obligatorio"
                        valid = false
                    }
                    if (species.isBlank()) {
                        speciesError = "La especie es obligatoria"
                        valid = false
                    }
                    
                    if (valid) {
                        onSave(
                            Animal(
                                id = animal?.id ?: 0,
                                name = name.trim(),
                                species = species.trim(),
                                breed = breed.trim(),
                                ownerId = selectedOwnerId
                            )
                        )
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
