package com.example.grocerymanager.core.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.R
import com.example.grocerymanager.core.designsystem.theme.AppShapes
import com.example.grocerymanager.core.designsystem.theme.AppTheme

/**
 * Premium date picker dialog.
 *
 * Wraps M3 [DatePickerDialog] with the app's elevated-card surface, the
 * standard [AppShapes.Card] shape, and the design-system [PrimaryButton] /
 * [TextLink] button pair (instead of the M3 default small TextButtons)
 * so the date picker reads as a first-class part of the app.
 *
 * Note: the underlying M3 [DatePickerDialog] in this Compose version
 * only supports [AppShapes.Card] for the surface and uses a fixed M3
 * surface color; the dialog reads cleanly against the premium theme
 * thanks to the matching shape and our [PrimaryButton] / [TextLink]
 * actions.
 *
 * @param onConfirm Called with the selected epoch millis (or null if the
 *   user picked nothing). The dialog is dismissed internally by the
 *   caller (typically `viewModel.showDatePicker(false)` after handling
 *   the result).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumDatePickerDialog(
    state: DatePickerState,
    onDismiss: () -> Unit,
    onConfirm: (Long?) -> Unit,
) {
    val okLabel = stringResource(R.string.action_ok)
    val cancelLabel = stringResource(R.string.action_cancel)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        shape = AppShapes.Card,
        confirmButton = {
            PrimaryButton(
                text = okLabel,
                onClick = { onConfirm(state.selectedDateMillis) },
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 12.dp),
            )
        },
        dismissButton = {
            TextLink(
                text = cancelLabel,
                onClick = onDismiss,
            )
        },
    ) {
        DatePicker(state = state)
    }
}
