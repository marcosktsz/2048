package com.example.game2048

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ToggleButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SettingsScreen(
    themeMode: AppThemeMode,
    hapticsEnabled: Boolean,
    undoEnabled: Boolean,
    playSoundEnabled: Boolean,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onUndoEnabledChange: (Boolean) -> Unit,
    onPlaySoundChange: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val hapticFeedback = LocalHapticFeedback.current
    val tap: () -> Unit = {
        if (hapticsEnabled) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
        }
    }
    Surface(color = colors.surfaceContainerHigh, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .widthIn(max = 540.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceContainerHighest),
                    )
                    IconButton(onClick = { tap(); onBack() }, modifier = Modifier.size(48.dp)) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                Text(
                    "Settings",
                    color = colors.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Surface(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = colors.surface,
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                ) {
                    val compact = maxHeight < 760.dp
                    Column(
                        modifier = Modifier
                            .widthIn(max = 540.dp)
                            .fillMaxWidth()
                            .fillMaxHeight(),
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp)
                                .padding(
                                    top = if (compact) 14.dp else 18.dp,
                                    bottom = 4.dp,
                                ),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(
                                topStart = 24.dp,
                                topEnd = 24.dp,
                                bottomStart = 8.dp,
                                bottomEnd = 8.dp,
                            ),
                            color = colors.surfaceContainerLow,
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(Icons.Rounded.Palette, contentDescription = null, tint = colors.onSurfaceVariant)
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("Theme", color = colors.onSurface, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            themeMode.label,
                                            color = colors.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(start = 52.dp, end = 12.dp, bottom = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                                ) {
                                    AppThemeMode.entries.forEachIndexed { index, option ->
                                        ToggleButton(
                                            checked = themeMode == option,
                                            onCheckedChange = { tap(); onThemeModeChange(option) },
                                            modifier = Modifier.weight(1f).semantics { role = Role.RadioButton },
                                            shapes =
                                                when (index) {
                                                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                                    AppThemeMode.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                                },
                                        ) {
                                            Text(option.label)
                                        }
                                    }
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = colors.surfaceContainerLow,
                        ) {
                            SettingsSwitchRow(
                                icon = Icons.AutoMirrored.Rounded.Undo,
                                title = "Undo button",
                                description = if (undoEnabled) "On - applies next match" else "Off - applies next match",
                                checked = undoEnabled,
                                onCheckedChange = { tap(); onUndoEnabledChange(it) },
                            )
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = colors.surfaceContainerLow,
                        ) {
                            SettingsSwitchRow(
                                icon = Icons.Rounded.Vibration,
                                title = "Haptic feedback",
                                description = if (hapticsEnabled) "On" else "Off",
                                checked = hapticsEnabled,
                                onCheckedChange = { tap(); onHapticsChange(it) },
                            )
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = colors.surfaceContainerLow,
                        ) {
                            SettingsSwitchRow(
                                icon = Icons.AutoMirrored.Rounded.VolumeUp,
                                title = "Play sound",
                                description = if (playSoundEnabled) "On" else "Off",
                                checked = playSoundEnabled,
                                onCheckedChange = { tap(); onPlaySoundChange(it) },
                            )
                        }

                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 8.dp, bottom = 2.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "built with",
                                color = colors.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                            )
                            Spacer(Modifier.size(4.dp))
                            Icon(
                                Icons.Filled.Favorite,
                                contentDescription = null,
                                tint = colors.error,
                                modifier = Modifier.size(12.dp),
                            )
                            Spacer(Modifier.size(4.dp))
                            Text(
                                "for",
                                color = colors.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                            )
                            Spacer(Modifier.size(6.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.ic_android),
                                contentDescription = "Android",
                                tint = Color.Unspecified,
                                modifier = Modifier.offset(y = (-3).dp).size(24.dp),
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                "v${BuildConfig.VERSION_NAME}  ·  ☕",
                                color = colors.onSurfaceVariant.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = colors.onSurfaceVariant)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = colors.onSurface, style = MaterialTheme.typography.titleMedium)
            Text(description, color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.semantics { contentDescription = "$title. $description" },
            thumbContent = {
                Icon(
                    imageVector = if (checked) Icons.Filled.Check else Icons.Filled.Close,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            },
            colors = SwitchDefaults.colors(),
        )
    }
}
