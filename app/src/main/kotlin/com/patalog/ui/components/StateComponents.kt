package com.patalog.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patalog.state.UiState

/**
 * Componente que muestra el estado de una operacion asincrona.
 * 
 * @param state Estado actual de la operacion
 * @param onRetry Callback para reintentar en caso de error (opcional)
 * @param content Contenido a mostrar cuando hay exito
 */
@Composable
fun <T> StateContainer(
    state: UiState<T>,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    idleContent: @Composable () -> Unit = {},
    content: @Composable (T) -> Unit
) {
    Box(modifier = modifier) {
        when (state) {
            is UiState.Idle -> {
                idleContent()
            }
            is UiState.Loading -> {
                LoadingIndicator(state.message)
            }
            is UiState.Error -> {
                ErrorMessage(state.message, onRetry)
            }
            is UiState.Success -> {
                content(state.data)
            }
        }
    }
}

@Composable
fun LoadingIndicator(message: String = "") {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        CircularProgressIndicator()
        if (message.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onRetry: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        if (onRetry != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
fun SuccessMessage(message: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Exito",
            tint = Color(0xFF4CAF50)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF4CAF50)
        )
    }
}
