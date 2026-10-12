package com.example.hiddenarmy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hiddenarmy.model.AppTheme
import com.example.hiddenarmy.model.CpuDifficulty
import com.example.hiddenarmy.ui.viewmodel.HiddenArmyViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: HiddenArmyViewModel,
    modifier: Modifier = Modifier
) {
    val prefs = viewModel.preferences
    val theme by prefs.theme.collectAsState()
    val dynamicColor by prefs.dynamicColor.collectAsState()
    val soundEnabled by prefs.soundEnabled.collectAsState()
    val hapticsEnabled by prefs.hapticsEnabled.collectAsState()
    val cpuDifficulty by prefs.cpuDifficulty.collectAsState()
    val battleAnimation by prefs.battleAnimationEnabled.collectAsState()
    val confirmRestart by prefs.confirmRestart.collectAsState()
    val showRules by prefs.showRules.collectAsState()

    var showResetStatsDialog by remember { mutableStateOf(false) }
    var showResetSettingsDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SETTINGS",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Appearance Card
        SettingsSectionHeader(title = "APPEARANCE")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "App Theme",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    AppTheme.entries.forEachIndexed { index, appTheme ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = AppTheme.entries.size),
                            onClick = { prefs.setTheme(appTheme) },
                            selected = appTheme == theme
                        ) {
                            Text(text = appTheme.label, fontSize = 12.sp)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                SwitchSettingRow(
                    label = "Dynamic Material Color",
                    description = "Follow Android system color accent (Android 12+)",
                    checked = dynamicColor,
                    onCheckedChange = { prefs.setDynamicColor(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Gameplay Card
        SettingsSectionHeader(title = "GAMEPLAY & AUDIO")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Default CPU Difficulty",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    CpuDifficulty.entries.forEachIndexed { index, diff ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = CpuDifficulty.entries.size),
                            onClick = { prefs.setCpuDifficulty(diff) },
                            selected = diff == cpuDifficulty
                        ) {
                            Text(text = diff.label, fontSize = 12.sp)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                SwitchSettingRow(
                    label = "Battle Animations",
                    description = "Animate reveal and combat strength comparisons",
                    checked = battleAnimation,
                    onCheckedChange = { prefs.setBattleAnimationEnabled(it) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                SwitchSettingRow(
                    label = "Sound Effects",
                    description = "Audio cues for moves, reveals, and captures",
                    checked = soundEnabled,
                    onCheckedChange = { prefs.setSoundEnabled(it) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                SwitchSettingRow(
                    label = "Haptic Feedback",
                    description = "Vibration for taps and battle impacts",
                    checked = hapticsEnabled,
                    onCheckedChange = { prefs.setHapticsEnabled(it) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                SwitchSettingRow(
                    label = "Confirm Restart",
                    description = "Ask before restarting an active match",
                    checked = confirmRestart,
                    onCheckedChange = { prefs.setConfirmRestart(it) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                SwitchSettingRow(
                    label = "Show Formula & Rules",
                    description = "Display rule reference throughout the game",
                    checked = showRules,
                    onCheckedChange = { prefs.setShowRules(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Data Management
        SettingsSectionHeader(title = "DATA MANAGEMENT")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showResetStatsDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "RESET STATISTICS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                OutlinedButton(
                    onClick = { showResetSettingsDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "RESET SETTINGS TO DEFAULT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
    }

    if (showResetStatsDialog) {
        AlertDialog(
            onDismissRequest = { showResetStatsDialog = false },
            title = { Text("Reset Statistics") },
            text = { Text("Are you sure you want to clear your games played, wins, streak, and best scores? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        prefs.resetStatistics()
                        showResetStatsDialog = false
                    }
                ) {
                    Text("RESET", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetStatsDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    if (showResetSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showResetSettingsDialog = false },
            title = { Text("Reset Settings") },
            text = { Text("Reset all theme, difficulty, and audio preferences back to their factory defaults?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        prefs.resetSettings()
                        showResetSettingsDialog = false
                    }
                ) {
                    Text("RESET", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetSettingsDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
private fun SwitchSettingRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
