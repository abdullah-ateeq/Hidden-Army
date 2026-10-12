package com.example.hiddenarmy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hiddenarmy.model.Side
import com.example.hiddenarmy.model.Unit
import com.example.hiddenarmy.model.UnitType

@Composable
fun UnitToken(
    unit: Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val isPlayer = unit.side == Side.PLAYER
    val isRevealedToPlayer = isPlayer || unit.playerKnowsType

    val accessibilityDesc = when {
        isPlayer -> "${unit.type.displayName}, Strength ${unit.type.power}"
        unit.playerKnowsType -> "Revealed Enemy ${unit.type.displayName}, Strength ${unit.type.power}"
        else -> "Hidden enemy unit"
    }

    val backgroundColor = when {
        isPlayer -> MaterialTheme.colorScheme.primaryContainer
        !isRevealedToPlayer -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    val contentColor = when {
        isPlayer -> MaterialTheme.colorScheme.onPrimaryContainer
        !isRevealedToPlayer -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isPlayer -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        !isRevealedToPlayer -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(2.dp)
            .semantics { contentDescription = accessibilityDesc },
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor,
        shadowElevation = if (isSelected) 4.dp else 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (!isRevealedToPlayer) {
                // Unknown CPU Piece: completely identical appearance for all hidden CPU units!
                Text(
                    text = "?",
                    color = contentColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                // Known unit: show abstract icon & strength badge
                val icon = getUnitIcon(unit.type)

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )

                // Strength badge in top-right corner
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (isPlayer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = unit.type.power.toString(),
                        color = if (isPlayer) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun getUnitIcon(type: UnitType): ImageVector = when (type) {
    UnitType.SCOUT -> Icons.Filled.ArrowUpward
    UnitType.SOLDIER -> Icons.Filled.Shield
    UnitType.CAPTAIN -> Icons.Filled.Star
    UnitType.COMMANDER -> Icons.Filled.MilitaryTech
}
