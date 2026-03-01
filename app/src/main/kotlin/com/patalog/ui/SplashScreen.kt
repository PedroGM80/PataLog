package com.patalog.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patalog.ui.theme.LightColorScheme
import kotlinx.coroutines.delay

/**
 * Pantalla de splash con logo y animacion de carga.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Animacion de fade in
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )
    
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500) // Mostrar splash por 2.5 segundos
        onSplashFinished()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alphaAnim.value)
        ) {
            // Logo
            Image(
                painter = painterResource("TeckelSoftLogo.jpg"),
                contentDescription = "TeckelSoft Logo",
                modifier = Modifier.size(200.dp)
            )
            
            Spacer(Modifier.height(24.dp))
            
            // Nombre de la app
            Text(
                text = "PataLog",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = LightColorScheme.primary
            )
            
            Text(
                text = "Asistente Veterinario",
                fontSize = 16.sp,
                color = Color.Gray
            )
            
            Spacer(Modifier.height(32.dp))
            
            // Indicador de carga
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = LightColorScheme.primary,
                strokeWidth = 3.dp
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = "Iniciando...",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        
        // Powered by
        Text(
            text = "Powered by TeckelSoft",
            fontSize = 12.sp,
            color = Color.LightGray,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .alpha(alphaAnim.value)
        )
    }
}
