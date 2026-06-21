package gr.posokanei4real.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppBlue = Color(0xFF1565C0)
private val AppBlueDark = Color(0xFF003C8F)
private val AppBlueLight = Color(0xFF5E92F3)
private val AppGreen = Color(0xFF2E7D32)
private val AppGreenContainer = Color(0xFFC8E6C9)

private val LightColorScheme = lightColorScheme(
    primary = AppBlue,
    onPrimary = Color.White,
    primaryContainer = AppBlueLight.copy(alpha = 0.2f),
    onPrimaryContainer = AppBlueDark,
    secondary = AppGreen,
    onSecondary = Color.White,
    secondaryContainer = AppGreenContainer,
    onSecondaryContainer = AppGreen,
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

@Composable
fun PosoKanei4RealTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content,
    )
}
