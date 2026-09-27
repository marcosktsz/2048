package com.example.game2048

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

internal enum class AppThemeMode(val preferenceValue: String, val label: String) {
    SYSTEM("system", "System"),
    LIGHT("light", "Light"),
    DARK("dark", "Dark");

    companion object {
        fun fromPreference(value: String?): AppThemeMode =
            entries.firstOrNull { it.preferenceValue == value } ?: SYSTEM
    }
}

@Composable
internal fun SettingsScreen(
    themeMode: AppThemeMode,
    hapticsEnabled: Boolean,
    animationsEnabled: Boolean,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onAnimationsChange: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(color = colors.surface, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceContainerHigh)
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
                Text("Settings", color = colors.onSurface, style = MaterialTheme.typography.titleLarge)
            }
            Surface(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = colors.surface,
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 540.dp)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 22.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = colors.surfaceContainerLow,
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text("APPEARANCE", color = colors.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                Text("Theme", color = colors.onSurface, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Choose how 2048 follows your device.",
                                    color = colors.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AppThemeMode.entries.forEach { option ->
                                        FilterChip(
                                            selected = themeMode == option,
                                            onClick = { onThemeModeChange(option) },
                                            label = { Text(option.label) },
                                        )
                                    }
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = colors.surfaceContainerLow,
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                                Text(
                                    "FEEDBACK",
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = colors.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                )
                                SettingsSwitchRow(
                                    title = "Haptic feedback",
                                    description = "A small pulse on tile merges and game over.",
                                    checked = hapticsEnabled,
                                    onCheckedChange = onHapticsChange,
                                )
                                Spacer(Modifier.fillMaxWidth().height(1.dp).background(colors.outlineVariant.copy(alpha = 0.35f)))
                                SettingsSwitchRow(
                                    title = "Tile animations",
                                    description = "Springy slides, merges, and score changes.",
                                    checked = animationsEnabled,
                                    onCheckedChange = onAnimationsChange,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = colors.onSurface, style = MaterialTheme.typography.titleMedium)
            Text(description, color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.semantics { contentDescription = "$title. $description" },
            thumbContent = {
                Icon(
                    if (checked) Icons.Rounded.Check else Icons.Rounded.Close,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            },
            colors = SwitchDefaults.colors(),
        )
    }
}
