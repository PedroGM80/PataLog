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
import com.patalog.data.OwnerRepository
import com.patalog.domain.models.Owner
import com.patalog.state.AppState

/**
 * Pantalla de gestion de propietarios.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnersScreen(
    appState: AppState,
    ownerRepository: OwnerRepository,
    onOwnerSelected: (Owner) -> Unit
) {
    val selectedOwner by appState.selectedOwner.collectAsState()
    
    // Cargar datos de la base de datos
    var owners by remember { mutableStateOf(ownerRepository.getAll()) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var ownerToEdit by remember { mutableStateOf<Owner?>(null) }
    var ownerToDelete by remember { mutableStateOf<Owner?>(null) }
    
    // Funcion para recargar
    fun reload() {
        owners = if (searchQuery.isBlank()) {
            ownerRepository.getAll()
        } else {
            ownerRepository.search(searchQuery)
        }
    }
    
    // Filtrar por busqueda local
    val filteredOwners = if (searchQuery.isBlank()) {
        owners
    } else {
        owners.filter { owner ->
            owner.name.contains(searchQuery, ignoreCase = true) ||
            owner.phone.contains(searchQuery) ||
            owner.email.contains(searchQuery, ignoreCase = true)
        }
    }
    
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        // Cabecera
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Propietarios",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Button(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Nuevo propietario")
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Buscador
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar por nombre, telefono o email") },
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
        
        // Lista de propietarios
        if (filteredOwners.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isBlank()) "No hay propietarios registrados" else "Sin resultados",
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
                items(filteredOwners, key = { it.id }) { owner ->
                    OwnerCard(
                        owner = owner,
                        isSelected = owner.id == selectedOwner?.id,
                        onSelect = { 
                            appState.selectOwner(owner)
                            onOwnerSelected(owner)
                        },
                        onEdit = { ownerToEdit = owner },
                        onDelete = { ownerToDelete = owner }
                    )
                }
            }
        }
    }
    
    // Dialog: Añadir propietario
    if (showAddDialog) {
        OwnerFormDialog(
            owner = null,
            onDismiss = { showAddDialog = false },
            onSave = { newOwner ->
                val id = ownerRepository.insert(newOwner)
                if (id > 0) {
                    reload()
                }
                showAddDialog = false
            }
        )
    }
    
    // Dialog: Editar propietario
    ownerToEdit?.let { owner ->
        OwnerFormDialog(
            owner = owner,
            onDismiss = { ownerToEdit = null },
            onSave = { updated ->
                if (ownerRepository.update(updated)) {
                    reload()
                    if (selectedOwner?.id == updated.id) {
                        appState.selectOwner(updated)
                    }
                }
                ownerToEdit = null
            }
        )
    }
    
    // Dialog: Confirmar eliminacion
    ownerToDelete?.let { owner ->
        AlertDialog(
            onDismissRequest = { ownerToDelete = null },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Eliminar propietario") },
            text = { 
                Text("¿Seguro que quieres eliminar a ${owner.name}? Los animales asociados quedaran sin propietario.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (ownerRepository.delete(owner.id)) {
                            reload()
                            if (selectedOwner?.id == owner.id) {
                                appState.selectOwner(null)
                            }
                        }
                        ownerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { ownerToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun OwnerCard(
    owner: Owner,
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
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
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
                    text = owner.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (owner.phone.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Phone,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = owner.phone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (owner.email.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Email,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = owner.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
        }
    }
}

@Composable
private fun OwnerFormDialog(
    owner: Owner?,
    onDismiss: () -> Unit,
    onSave: (Owner) -> Unit
) {
    var name by remember { mutableStateOf(owner?.name ?: "") }
    var phone by remember { mutableStateOf(owner?.phone ?: "") }
    var email by remember { mutableStateOf(owner?.email ?: "") }
    
    var nameError by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (owner == null) "Nuevo propietario" else "Editar propietario") },
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
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefono") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = "El nombre es obligatorio"
                    } else {
                        onSave(
                            Owner(
                                id = owner?.id ?: 0,
                                name = name.trim(),
                                phone = phone.trim(),
                                email = email.trim()
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
