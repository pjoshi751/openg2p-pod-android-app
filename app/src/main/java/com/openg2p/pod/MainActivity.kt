package com.openg2p.pod.ui.theme
package com.openg2p.pod

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels // Required for viewModels delegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.openg2p.pod.ui.PodFormScreen
import com.openg2p.pod.ui.theme.OpenG2PPodAppTheme // Assuming a theme file exists or create one
import com.openg2p.pod.viewmodel.PodViewModel

class MainActivity : ComponentActivity() {

    // Use the by viewModels() delegate for ViewModel creation
    private val podViewModel: PodViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Apply your theme here (You might need to create this Theme file)
            OpenG2PPodAppTheme { // Replace with your actual theme if different
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pass the ViewModel instance to the screen
                    PodFormScreen(viewModel = podViewModel)
                }
            }
        }
    }
}

// Define a basic theme if you don't have one yet
// Create a new file: app/src/main/java/com/openg2p/pod/ui/theme/Theme.kt


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Define a simple light color scheme
private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    // ... add other colors as needed or use defaults
     background = md_theme_light_background,
     surface = md_theme_light_surface,
     onBackground = md_theme_light_onBackground,
     onSurface = md_theme_light_onSurface,
)

// Define color values (replace with your actual desired colors)
// Example Material 3 color values (generate from https://m3.material.io/theme-builder)
val md_theme_light_primary = Color(0xFF6750A4)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFEADDFF)
val md_theme_light_onPrimaryContainer = Color(0xFF21005D)
val md_theme_light_background = Color(0xFFFFFBFE)
val md_theme_light_surface = Color(0xFFFFFBFE)
val md_theme_light_onBackground = Color(0xFF1C1B1F)
val md_theme_light_onSurface = Color(0xFF1C1B1F)
// Add other color definitions (secondary, tertiary, error, etc.) if needed

@Composable
fun OpenG2PPodAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme, // Use the defined light scheme
        typography = Typography, // Assuming Typography.kt exists
        content = content
    )
}

// --- You'll also need Typography.kt ---
// Create: app/src/main/java/com/openg2p/pod/ui/theme/Typography.kt
package com.openg2p.pod.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Default Material 3 Typography (or customize as needed)
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    // Define other text styles (titleLarge, labelSmall, etc.) if required
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

// --- And Color.kt ---
// Create: app/src/main/java/com/openg2p/pod/ui/theme/Color.kt
package com.openg2p.pod.ui.theme

import androidx.compose.ui.graphics.Color

// Define your theme colors here (can be the same as in Theme.kt)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

