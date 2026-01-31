package com.recipenotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.recipenotes.ui.navigation.RecipeNotesNavGraph
import com.recipenotes.ui.theme.RecipeNotesTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * The single Activity for the entire app (single-activity architecture).
 *
 * @AndroidEntryPoint tells Hilt to inject dependencies into this Activity.
 * Without this, hiltViewModel() in composables would crash at runtime.
 *
 * Modern Android apps use a single Activity with Jetpack Compose for all screens.
 * Navigation between screens is handled by Navigation Compose (not by starting
 * new Activities). This is simpler, faster, and gives more control over transitions.
 *
 * enableEdgeToEdge() makes the app draw behind the system bars (status bar and
 * navigation bar) for a more immersive look. The system bars become transparent
 * and the app content extends underneath them. Scaffold handles the insets
 * automatically to prevent content from being hidden behind the bars.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecipeNotesTheme {
                RecipeNotesNavGraph()
            }
        }
    }
}
