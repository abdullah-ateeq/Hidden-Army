package com.example.hiddenarmy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hiddenarmy.model.Base
import com.example.hiddenarmy.model.BaseOwner

@Composable
fun BaseToken(
    base: Base,
    modifier: Modifier = Modifier
) {
    val description = when (base.owner) {
        BaseOwner.NEUTRAL -> "Neutral base"
        BaseOwner.PLAYER -> "Player controlled base"
        BaseOwner.CPU -> "CPU controlled base"
    }

    val baseColor = when (base.owner) {
        BaseOwner.NEUTRAL -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
        BaseOwner.PLAYER -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        BaseOwner.CPU -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
    }

    Box(
        modifier = modifier
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        // Rotated square to create diamond symbol ◆
        Box(
            modifier = Modifier
                .size(16.dp)
                .rotate(45f)
                .clip(RoundedCornerShape(3.dp))
                .background(baseColor)
        )
    }
}
