package de.igor.gun.sleep.analyzer.ui.screens.archive.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.igor.gun.sleep.analyzer.R


@Composable
@Preview(showBackground = true)
fun ShowDeleteItemAlert(
    shouldShowDeleteSeriesAlert: MutableState<Boolean> = mutableStateOf(true),
    onDismissDialog: () -> Unit = { shouldShowDeleteSeriesAlert.value = false },
    confirmAction: () -> Unit = {},
    cancelAction: () -> Unit = {},
) {
    if (shouldShowDeleteSeriesAlert.value) {
        AlertDialog(
            onDismissRequest = { onDismissDialog() },
            title = {
                val text = stringResource(R.string.delete_series_title)
                Text(text)
            },
            text = {
                val text = stringResource(R.string.delete_series_text)
                Text(text)
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    tint = Color.Red,
                    contentDescription = null
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDismissDialog()
                    confirmAction()
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDismissDialog()
                    cancelAction()
                }) {
                    Text(stringResource(R.string.cancel))
                }
            })
    }
}