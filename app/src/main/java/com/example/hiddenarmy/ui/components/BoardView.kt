package com.example.hiddenarmy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.hiddenarmy.logic.HiddenArmyGameEngine
import com.example.hiddenarmy.model.Base
import com.example.hiddenarmy.model.BaseOwner
import com.example.hiddenarmy.model.GameState
import com.example.hiddenarmy.model.Position
import com.example.hiddenarmy.model.Side
import com.example.hiddenarmy.ui.theme.AttackHighlightColor
import com.example.hiddenarmy.ui.theme.BoardCellAltDark
import com.example.hiddenarmy.ui.theme.BoardCellAltLight
import com.example.hiddenarmy.ui.theme.BoardCellDark
import com.example.hiddenarmy.ui.theme.BoardCellLight
import com.example.hiddenarmy.ui.theme.MoveHighlightColor
import com.example.hiddenarmy.ui.theme.SelectedHighlightColor

@Composable
fun BoardView(
    state: GameState,
    onCellClicked: (Position) -> kotlin.Unit,
    modifier: Modifier = Modifier
) {
    val boardSize = HiddenArmyGameEngine.BOARD_SIZE

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
        ) {
            for (r in 0 until boardSize) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    for (c in 0 until boardSize) {
                        val pos = Position(r, c)
                        val unit = state.units.find { it.position == pos }
                        val base = state.bases.find { it.position == pos }
                        val isSelected = unit != null && unit.id == state.selectedUnitId
                        val isLegalMove = pos in state.legalMoves
                        val isAttackTarget = pos in state.attackTargets

                        BoardCell(
                            position = pos,
                            unit = unit,
                            base = base,
                            isSelected = isSelected,
                            isLegalMove = isLegalMove,
                            isAttackTarget = isAttackTarget,
                            onClick = { onCellClicked(pos) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .padding(2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BoardCell(
    position: Position,
    unit: com.example.hiddenarmy.model.Unit?,
    base: Base?,
    isSelected: Boolean,
    isLegalMove: Boolean,
    isAttackTarget: Boolean,
    onClick: () -> kotlin.Unit,
    modifier: Modifier = Modifier
) {
    val isLight = MaterialTheme.colorScheme.background.luminance() > 0.5f
    val isAlt = (position.row + position.column) % 2 == 1

    val defaultBg = if (isLight) {
        if (isAlt) BoardCellAltLight else BoardCellLight
    } else {
        if (isAlt) BoardCellAltDark else BoardCellDark
    }

    val cellDesc = when {
        isSelected -> "Selected unit"
        isAttackTarget -> "Attack target"
        isLegalMove -> "Available move"
        unit != null && unit.side == Side.PLAYER -> "Player unit ${unit.type.displayName}"
        unit != null && unit.playerKnowsType -> "Revealed CPU unit ${unit.type.displayName}"
        unit != null -> "Hidden enemy unit"
        base != null -> when (base.owner) {
            BaseOwner.NEUTRAL -> "Neutral base"
            BaseOwner.PLAYER -> "Player controlled base"
            BaseOwner.CPU -> "CPU controlled base"
        }
        else -> "Empty square row ${position.row + 1} column ${position.column + 1}"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(defaultBg)
            .then(
                when {
                    isSelected -> Modifier.border(2.dp, SelectedHighlightColor, RoundedCornerShape(8.dp))
                    isAttackTarget -> Modifier.border(2.dp, AttackHighlightColor, RoundedCornerShape(8.dp))
                    isLegalMove -> Modifier.border(1.5.dp, MoveHighlightColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    else -> Modifier.border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .semantics { contentDescription = cellDesc },
        contentAlignment = Alignment.Center
    ) {
        // Base token background if this cell contains a base
        if (base != null) {
            BaseToken(
                base = base,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Legal move indicator (small dot)
        if (isLegalMove && unit == null) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MoveHighlightColor.copy(alpha = 0.85f))
            )
        }

        // Attack target indicator
        if (isAttackTarget) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AttackHighlightColor.copy(alpha = 0.25f))
            )
        }

        // Unit token if present
        if (unit != null) {
            UnitToken(
                unit = unit,
                isSelected = isSelected
            )
        }
    }
}
