package com.stolz.connect.ui.design

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import com.stolz.connect.ui.theme.Dimensions

/**
 * Icon with design-system size and optional tint.
 * Use Dimensions.iconSmall, iconMedium, iconLarge for consistency.
 */
@Composable
fun ConnectIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = Dimensions.iconMedium,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier.then(Modifier.size(size)),
        tint = tint
    )
}
