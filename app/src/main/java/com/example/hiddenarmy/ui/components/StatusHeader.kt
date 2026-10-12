package com.example.hiddenarmy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hiddenarmy.model.GameStatus
import com.example.hiddenarmy.model.Side

@Composable
fun StatusHeader(
    turn: Side,
    status: GameStatus,
    turnCount: Int,
    onBackClicked: () -> Unit,
    onRestartClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "HIDDEN ARMY",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val (turnText, turnColor) = when {
                    status == GameStatus.PLAYER_WON -> "VICTORY" to MaterialTheme.colorScheme.primary
                    status == GameStatus.CPU_WON -> "DEFEAT" to MaterialTheme.colorScheme.secondary
                    status == GameStatus.DRAW -> "DRAW" to MaterialTheme.colorScheme.outline
                    status == GameStatus.BATTLE -> "BATTLE" to MaterialTheme.colorScheme.secondary
                    turn == Side.PLAYER -> "YOUR TURN" to MaterialTheme.colorScheme.primary
                    else -> "CPU TURN" to MaterialTheme.colorScheme.secondary
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(turnColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$turnText • TURN $turnCount",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = turnColor
                    )
                }
            }

            IconButton(onClick = onRestartClicked) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Restart",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
