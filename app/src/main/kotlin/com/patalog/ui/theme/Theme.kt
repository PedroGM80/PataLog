package com.patalog.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Colores del tema de PataLog.
 */

// Colores primarios - Verde veterinario
private val PrimaryLight = Color(0xFF2E7D32)
private val OnPrimaryLight = Color.White
private val PrimaryContainerLight = Color(0xFFA5D6A7)
private val OnPrimaryContainerLight = Color(0xFF1B5E20)

private val PrimaryDark = Color(0xFF81C784)
private val OnPrimaryDark = Color(0xFF1B5E20)
private val PrimaryContainerDark = Color(0xFF2E7D32)
private val OnPrimaryContainerDark = Color(0xFFC8E6C9)

// Colores secundarios
private val SecondaryLight = Color(0xFF558B2F)
private val OnSecondaryLight = Color.White
private val SecondaryContainerLight = Color(0xFFDCEDC8)
private val OnSecondaryContainerLight = Color(0xFF33691E)

private val SecondaryDark = Color(0xFFAED581)
private val OnSecondaryDark = Color(0xFF33691E)
private val SecondaryContainerDark = Color(0xFF558B2F)
private val OnSecondaryContainerDark = Color(0xFFDCEDC8)

// Colores de error
private val ErrorLight = Color(0xFFB00020)
private val OnErrorLight = Color.White
private val ErrorContainerLight = Color(0xFFFFDAD4)
private val OnErrorContainerLight = Color(0xFF410002)

private val ErrorDark = Color(0xFFCF6679)
private val OnErrorDark = Color(0xFF410002)
private val ErrorContainerDark = Color(0xFF93000A)
private val OnErrorContainerDark = Color(0xFFFFDAD4)

// Fondos y superficies - Modo claro
private val BackgroundLight = Color(0xFFFAFAFA)
private val OnBackgroundLight = Color(0xFF1C1B1F)
private val SurfaceLight = Color.White
private val OnSurfaceLight = Color(0xFF1C1B1F)
private val SurfaceVariantLight = Color(0xFFE7E0EC)
private val OnSurfaceVariantLight = Color(0xFF49454F)

// Fondos y superficies - Modo oscuro
private val BackgroundDark = Color(0xFF121212)
private val OnBackgroundDark = Color(0xFFE6E1E5)
private val SurfaceDark = Color(0xFF1E1E1E)
private val OnSurfaceDark = Color(0xFFE6E1E5)
private val SurfaceVariantDark = Color(0xFF2D2D2D)
private val OnSurfaceVariantDark = Color(0xFFCAC4D0)

// Outline
private val OutlineLight = Color(0xFF79747E)
private val OutlineDark = Color(0xFF938F99)

/**
 * Esquema de colores claro.
 */
val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight
)

/**
 * Esquema de colores oscuro.
 */
val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)
