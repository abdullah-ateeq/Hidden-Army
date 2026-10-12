package com.example.hiddenarmy.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hiddenarmy.model.ActiveBattle
import com.example.hiddenarmy.model.Side
import com.example.hiddenarmy.model.Unit
import com.example.hiddenarmy.model.UnitType

@Composable
fun BattleOverlay(
    activeBattle: ActiveBattle?,
    onSkip: () -> kotlin.Unit,
    modifier: Modifier = Modifier
) {
    if (activeBattle == null) return

    val attacker = activeBattle.attacker
    val defender = activeBattle.defender
    val result = activeBattle.result

    // Tap to skip
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSkip
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "BATTLE ENGAGED",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Combat cards row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Attacker Card
                    CombatantCard(
                        title = if (attacker.side == Side.PLAYER) "ATTACKER (YOU)" else "ATTACKER (CPU)",
                        unitType = result.revealedAttacker,
                        power = result.revealedAttacker.power,
                        isWinner = result.attackerSurvives,
                        isLoser = !result.attackerSurvives,
                        side = attacker.side
                    )

                    Text(
                        text = "VS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.outline
                    )

                    // Defender Card
                    CombatantCard(
                        title = if (defender.side == Side.PLAYER) "DEFENDER (YOU)" else "DEFENDER (CPU)",
                        unitType = result.revealedDefender,
                        power = result.revealedDefender.power,
                        isWinner = result.defenderSurvives,
                        isLoser = !result.defenderSurvives,
                        side = defender.side
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Outcome description
                val outcomeText = when {
                    result.attackerSurvives -> "${result.revealedAttacker.displayName.uppercase()} WINS"
                    result.defenderSurvives -> "${result.revealedDefender.displayName.uppercase()} WINS"
                    else -> "EQUAL STRENGTH - BOTH DESTROYED"
                }

                val subText = when {
                    result.attackerSurvives -> "${result.revealedDefender.displayName} eliminated."
                    result.defenderSurvives -> "${result.revealedAttacker.displayName} eliminated."
                    else -> "Both units eliminated from the board."
                }

                Text(
                    text = outcomeText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = when (result.winner) {
                        Side.PLAYER -> MaterialTheme.colorScheme.primary
                        Side.CPU -> MaterialTheme.colorScheme.secondary
                        null -> MaterialTheme.colorScheme.onSurface
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subText,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Tap to continue",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun CombatantCard(
    title: String,
    unitType: UnitType,
    power: Int,
    isWinner: Boolean,
    isLoser: Boolean,
    side: Side
) {
    val isPlayer = side == Side.PLAYER
    val accentColor = if (isPlayer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier.size(76.dp),
            shape = RoundedCornerShape(14.dp),
            color = if (isPlayer) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
            border = androidx.compose.foundation.BorderStroke(
                width = if (isWinner) 2.5.dp else 1.dp,
                color = if (isWinner) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            shadowElevation = if (isWinner) 6.dp else 1.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getUnitIcon(unitType),
                    contentDescription = null,
                    tint = if (isPlayer) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(34.dp)
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = power.toString(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = unitType.displayName,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Strength $power",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
