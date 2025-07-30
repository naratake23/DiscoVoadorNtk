package com.liberty.discovoadorntk.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font

import com.liberty.discovoadorntk.R


//@Composable
//fun MyTheme(
//    content: @Composable () -> Unit
//) {
//
//    MaterialTheme(
//colorScheme = MyLightColorScheme,
//content = content
//    )
//}

//val provider = GoogleFont.Provider(
//    providerAuthority = "com.google.android.gms.fonts",
//    providerPackage = "com.google.android.gms",
//    certificates = R.array.com_google_android_gms_fonts_certs
//)
//
//val shareTechMono = FontFamily(
//    Font(
//        googleFont = GoogleFont("Share Tech Mono"),
//        fontProvider = provider,
//    )
//)
//
//val shareTechMono = FontFamily(
//    Font(
//        googleFont = GoogleFont("Share Tech Mono"),
//        fontProvider = provider,
//    )
//)
//val AppTypography = Typography(
//    displayLarge = baseline.displayLarge.copy(fontFamily = shareTechMono),
//    displayMedium = baseline.displayMedium.copy(fontFamily = shareTechMono),
//    displaySmall = baseline.displaySmall.copy(fontFamily = shareTechMono),
//    headlineLarge = baseline.headlineLarge.copy(fontFamily = shareTechMono),
//    headlineMedium = baseline.headlineMedium.copy(fontFamily = shareTechMono),
//    headlineSmall = baseline.headlineSmall.copy(fontFamily = shareTechMono),
//    titleLarge = baseline.titleLarge.copy(fontFamily = shareTechMono),
//    titleMedium = baseline.titleMedium.copy(fontFamily = shareTechMono),
//    titleSmall = baseline.titleSmall.copy(fontFamily = shareTechMono),
//    bodyLarge = baseline.bodyLarge.copy(fontFamily = shareTechMono),
//    bodyMedium = baseline.bodyMedium.copy(fontFamily = shareTechMono),
//    bodySmall = baseline.bodySmall.copy(fontFamily = shareTechMono),
//    labelLarge = baseline.labelLarge.copy(fontFamily = shareTechMono),
//    labelMedium = baseline.labelMedium.copy(fontFamily = shareTechMono),
//    labelSmall = baseline.labelSmall.copy(fontFamily = shareTechMono),
//)


// Default Material 3 typography values
val baseline = Typography()

val shareTechMono = FontFamily(
    Font(R.font.share_tech_mono_regular, weight = FontWeight.Normal)
)

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = shareTechMono),
    displayMedium = baseline.displayMedium.copy(fontFamily = shareTechMono),
    displaySmall = baseline.displaySmall.copy(fontFamily = shareTechMono),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = shareTechMono),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = shareTechMono),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = shareTechMono),
    titleLarge = baseline.titleLarge.copy(fontFamily = shareTechMono),
    titleMedium = baseline.titleMedium.copy(fontFamily = shareTechMono),
    titleSmall = baseline.titleSmall.copy(fontFamily = shareTechMono),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = shareTechMono),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = shareTechMono),
    bodySmall = baseline.bodySmall.copy(fontFamily = shareTechMono),
    labelLarge = baseline.labelLarge.copy(fontFamily = shareTechMono),
    labelMedium = baseline.labelMedium.copy(fontFamily = shareTechMono),
    labelSmall = baseline.labelSmall.copy(fontFamily = shareTechMono),
)

val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
//    surfaceDim = surfaceDimLight,
//    surfaceBright = surfaceBrightLight,
//    surfaceContainerLowest = surfaceContainerLowestLight,
//    surfaceContainerLow = surfaceContainerLowLight,
//    surfaceContainer = surfaceContainerLight,
//    surfaceContainerHigh = surfaceContainerHighLight,
//    surfaceContainerHighest = surfaceContainerHighestLight,
)

val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,

)

@Composable
fun MyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable() () -> Unit
) {
    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }

        darkTheme -> darkScheme
        else -> lightScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }


    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}