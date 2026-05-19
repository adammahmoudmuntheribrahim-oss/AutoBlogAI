package com.autoblog.ai.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// الألوان الأساسية - أخضر زمردي وأزرق
private val PrimaryGreen = Color(0xFF10B981)      // أخضر زمردي
private val PrimaryGreenDark = Color(0xFF059669)   // أخضر زمردي داكن
private val SecondaryBlue = Color(0xFF3B82F6)     // أزرق
private val SecondaryBlueDark = Color(0xFF1E40AF) // أزرق داكن

// الخلفيات
private val DarkBg = Color(0xFF0F0F0F)            // أسود عميق
private val DarkBgSecondary = Color(0xFF1A1A1A)   // رمادي داكن جداً
private val DarkCard = Color(0xFF1E1E2E)          // رمادي داكن مع لمسة زرقاء

// النصوص
private val TextLight = Color(0xFFFFFFFF)         // أبيض
private val TextMuted = Color(0xFF9CA3AF)         // رمادي فاتح

private val LightColors = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF064E3B),
    
    secondary = SecondaryBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDBE4FF),
    onSecondaryContainer = Color(0xFF0D1B6C),
    
    surface = Color.White,
    onSurface = Color(0xFF1F2937),
    surfaceVariant = Color(0xFFF3F4F6),
    
    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
)

private val DarkColors = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = Color(0xFFD1FAE5),
    
    secondary = SecondaryBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0D1B6C),
    onSecondaryContainer = Color(0xFFDBE4FF),
    
    surface = DarkBg,
    onSurface = TextLight,
    surfaceVariant = DarkBgSecondary,
    
    background = DarkBg,
    onBackground = TextLight,
    
    error = Color(0xFFF87171),
    onError = Color(0xFF7F1D1D),
    errorContainer = Color(0xFF7F1D1D),
)

@Composable
fun AutoBlogAITheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}
