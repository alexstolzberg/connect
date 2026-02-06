package com.stolz.connect.ui.design

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.stolz.connect.ui.theme.Dimensions

/** Primary filled button. Use for main actions (Save, Add, Confirm). */
@Composable
fun ConnectPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = MaterialTheme.shapes.medium,
        contentPadding = ButtonDefaults.ContentPadding,
        content = {
            if (leadingIcon != null) {
                ConnectIcon(
                    imageVector = leadingIcon,
                    size = Dimensions.iconSmall,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(Dimensions.inlineSpacing))
            }
            content()
        }
    )
}

/** Outlined secondary button. Use for Pick from Contacts, Set Birthday, etc. */
@Composable
fun ConnectOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        ),
        shape = MaterialTheme.shapes.medium,
        contentPadding = ButtonDefaults.ContentPadding,
        content = {
            if (leadingIcon != null) {
                ConnectIcon(
                    imageVector = leadingIcon,
                    size = Dimensions.iconSmall,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(Dimensions.inlineSpacing))
            }
            content()
        }
    )
}

/** Text-only button. Use for Cancel, Edit, Clear, secondary actions. */
@Composable
fun ConnectTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        content = content
    )
}
