package com.patalog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patalog.backend.BackendWatchdog.WatchdogState

/**
 * Indicador visual del estado del backend Python.
 */
@Composable
fun BackendStatusIndicator(state: WatchdogState) {
    val (color, text, icon) = when (state) {
        WatchdogState.STOPPED -> Triple(Color.Gray, "Detenido", Icons.Default.Error)
        WatchdogState.STARTING -> Triple(Color.Yellow, "Iniciando...", Icons.Default.Refresh)
        WatchdogState.RUNNING -> Triple(Color.Green, "Activo", Icons.Default.CheckCircle)
        WatchdogState.RESTARTING -> Triple(Color.Yellow, "Reiniciando...", Icons.Default.Refresh)
        WatchdogState.FAILED -> Triple(Color.Red, "Error", Icons.Default.Error)
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
