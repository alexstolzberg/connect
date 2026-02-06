package com.stolz.connect.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Design system shapes. All corners use Dimensions for consistency.
 */
val ConnectShapes = Shapes(
    extraSmall = RoundedCornerShape(Dimensions.radiusExtraSmall),
    small = RoundedCornerShape(Dimensions.radiusSmall),
    medium = RoundedCornerShape(Dimensions.radiusMedium),
    large = RoundedCornerShape(Dimensions.radiusLarge),
    extraLarge = RoundedCornerShape(Dimensions.radiusExtraLarge)
)
