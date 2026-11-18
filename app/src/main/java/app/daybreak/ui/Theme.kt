package app.daybreak.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Палитра: небо перед рассветом и одна тёплая линия горизонта. */
object Dawn {
    val Midnight = Color(0xFF131929)
    val Dusk = Color(0xFF1D2640)
    val Rule = Color(0xFF2E3859)
    val Edge = Color(0xFF59648C)
    val Paper = Color(0xFFECE7DC)
    val Haze = Color(0xFF9CA5BF)
    val FirstLight = Color(0xFFF2B36B)
}

private val Scheme = darkColorScheme(
    primary = Dawn.FirstLight,
    onPrimary = Dawn.Midnight,
    background = Dawn.Midnight,
    onBackground = Dawn.Paper,
    surface = Dawn.Midnight,
    onSurface = Dawn.Paper,
    surfaceVariant = Dawn.Dusk,
    onSurfaceVariant = Dawn.Haze,
    outline = Dawn.Edge,
    outlineVariant = Dawn.Rule
)

private val Serif = FontFamily.Serif
private val Sans = FontFamily.SansSerif

private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Serif, fontWeight = FontWeight.Bold,
        fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Serif, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 35.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Serif, fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, lineHeight = 27.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(fontFamily = Sans, fontSize = 16.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontFamily = Sans, fontSize = 14.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp
    )
)

@Composable
fun DaybreakTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = AppTypography, content = content)
}

/** Цвета неба над шапкой: четыре остановки, нижняя — линия горизонта. */
fun horizonColors(hour: Int): List<Color> = when (hour) {
    in 5..8 -> listOf(Color(0xFF131929), Color(0xFF3B2F5E), Color(0xFFC9566B), Color(0xFFF2B36B))
    in 9..16 -> listOf(Color(0xFF131929), Color(0xFF26407A), Color(0xFF5C86C4), Color(0xFFA9C6EA))
    in 17..20 -> listOf(Color(0xFF131929), Color(0xFF3A2C5A), Color(0xFFB8486A), Color(0xFFE8895A))
    else -> listOf(Color(0xFF131929), Color(0xFF161E38), Color(0xFF1B2547), Color(0xFF243059))
}
