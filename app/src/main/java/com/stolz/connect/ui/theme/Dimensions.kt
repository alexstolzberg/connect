package com.stolz.connect.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Design system dimensions. Use these instead of hardcoded dp values
 * so spacing, sizing, and radius stay consistent (Material-style 4dp grid).
 */
object Dimensions {

    // ─── Spacing (4dp base grid) ─────────────────────────────────────────
    val spacingNone = 0.dp
    val spacingTiny = 1.dp
    val xxxsmall = 2.dp
    val xxsmall = 4.dp
    val xsmall = 8.dp
    val small = 12.dp
    val medium = 16.dp
    val large = 24.dp
    val xlarge = 32.dp
    val xxlarge = 40.dp
    val xxxlarge = 48.dp

    // Semantic spacing aliases
    val cardPadding = medium
    val screenPaddingHorizontal = medium
    val screenPaddingVertical = medium
    val listItemSpacing = xsmall
    val sectionSpacing = large
    val inlineSpacing = xsmall
    /** Vertical gap between label and value inside a data row (e.g. "Phone" / "555-1234"). */
    val dataRowLabelToValue = xxsmall
    /** Vertical gap between consecutive data rows (Phone, Email, Birthday, Notes, Next/Last). */
    val dataRowSpacing = xsmall

    // ─── Icon sizes ───────────────────────────────────────────────────────
    val iconExtraSmall = 16.dp
    val iconSmall = 20.dp
    val iconMedium = 24.dp
    val iconLarge = 32.dp
    val iconExtraLarge = 40.dp

    // Touch targets (min 48dp for accessibility)
    val touchTargetMin = 48.dp
    val iconButtonSize = 48.dp
    val fabSize = 56.dp

    // ─── Component heights ─────────────────────────────────────────────────
    val buttonHeight = 40.dp
    val buttonHeightLarge = 48.dp
    val textFieldHeight = 56.dp
    val listItemMinHeight = 56.dp
    val listItemMinHeightLarge = 72.dp

    // ─── Corner radius (Material 3–style) ──────────────────────────────────
    val radiusExtraSmall = 4.dp
    val radiusSmall = 8.dp
    val radiusMedium = 12.dp
    val radiusLarge = 16.dp
    val radiusExtraLarge = 28.dp
    val radiusFull = 9999.dp

    // ─── Avatar / media ────────────────────────────────────────────────────
    val avatarSmall = 32.dp
    val avatarMedium = 40.dp
    val avatarLarge = 56.dp
    val avatarXLarge = 120.dp

    // ─── Border / stroke ───────────────────────────────────────────────────
    val borderThin = xxxsmall
    val borderMedium = xxsmall
    val borderThick = 3.dp

    // ─── Bottom bar / FAB offset ───────────────────────────────────────────
    val bottomBarHeight = 80.dp
    val snackbarBottomOffset = 72.dp
}
