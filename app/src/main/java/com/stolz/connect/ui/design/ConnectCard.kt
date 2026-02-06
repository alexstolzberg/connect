package com.stolz.connect.ui.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.stolz.connect.ui.theme.Dimensions

/**
 * Standard card with design-system padding and shape.
 * Use for list items, detail blocks, and elevated content.
 *
 * @param leadingBarColor Optional accent color for a vertical status bar on the start edge
 *                        (e.g. green/yellow/red for connection status). Improves scannability.
 * @param leadingBarWidth Width of the leading bar when [leadingBarColor] is set.
 */
@Composable
fun ConnectCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = PaddingValues(Dimensions.cardPadding),
    leadingBarColor: Color? = null,
    leadingBarWidth: Dp = Dimensions.radiusExtraSmall,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick ?: {},
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = MaterialTheme.shapes.medium,
        border = border,
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimensions.xxxsmall,
            pressedElevation = Dimensions.xxsmall,
            focusedElevation = Dimensions.xxsmall,
            hoveredElevation = Dimensions.xxsmall
        ),
        content = {
            Row(
                modifier = Modifier.padding(contentPadding),
                verticalAlignment = Alignment.Top
            ) {
                if (leadingBarColor != null) {
                    Box(
                        modifier = Modifier
                            .width(leadingBarWidth)
                            .fillMaxHeight()
                            .padding(end = Dimensions.xxsmall)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(color = leadingBarColor)
                        }
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    content()
                }
            }
        }
    )
}
