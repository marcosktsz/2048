package com.example.game2048

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val preferences = getSharedPreferences("2048", MODE_PRIVATE)

        setContent {
            val context = LocalContext.current
            val systemDarkTheme = isSystemInDarkTheme()
            var themeMode by remember {
                mutableStateOf(AppThemeMode.fromPreference(preferences.getString("theme_mode", null)))
            }
            var hapticsEnabled by remember { mutableStateOf(preferences.getBoolean("haptics_enabled", true)) }
            var undoEnabled by remember { mutableStateOf(preferences.getBoolean("undo_enabled", true)) }
            var playSoundEnabled by remember { mutableStateOf(preferences.getBoolean("play_sound_enabled", true)) }
            val darkTheme = when (themeMode) {
                AppThemeMode.SYSTEM -> systemDarkTheme
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }
            val colorScheme = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> dynamicDarkColorScheme(context)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
                darkTheme -> darkColorScheme()
                else -> lightColorScheme()
            }
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }

            MaterialTheme(colorScheme = colorScheme, motionScheme = MotionScheme.expressive()) {
                SettingsScreen(
                    themeMode = themeMode,
                    hapticsEnabled = hapticsEnabled,
                    undoEnabled = undoEnabled,
                    playSoundEnabled = playSoundEnabled,
                    onThemeModeChange = { mode ->
                        themeMode = mode
                        preferences.edit().putString("theme_mode", mode.preferenceValue).apply()
                    },
                    onHapticsChange = { enabled ->
                        hapticsEnabled = enabled
                        preferences.edit().putBoolean("haptics_enabled", enabled).apply()
                    },
                    onUndoEnabledChange = { enabled ->
                        undoEnabled = enabled
                        preferences.edit().putBoolean("undo_enabled", enabled).apply()
                    },
                    onPlaySoundChange = { enabled ->
                        playSoundEnabled = enabled
                        preferences.edit().putBoolean("play_sound_enabled", enabled).apply()
                    },
                    onBack = { finish() },
                )
            }
        }
    }
}
