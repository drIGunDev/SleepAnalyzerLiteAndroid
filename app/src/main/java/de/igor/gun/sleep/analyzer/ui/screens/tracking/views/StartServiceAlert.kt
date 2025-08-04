package de.igor.gun.sleep.analyzer.ui.screens.tracking.views

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.igor.gun.sleep.analyzer.R


@Composable
@Preview(showBackground = true)
fun ShowStartServiceAlert(
    shouldShowDialog: MutableState<Boolean> = rememberSaveable { mutableStateOf(true) },
    onDismissDialog: () -> Unit = { shouldShowDialog.value = false }
) {
    if (!shouldShowDialog.value) return

    AlertDialog(
        onDismissRequest = { onDismissDialog() },
        title = { Text(stringResource(R.string.service_started_alert_title)) },
        text = { Text(stringResource(R.string.service_started_alert_text)) },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_warning_24),
                tint = Color.Red,
                contentDescription = null
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onDismissDialog()
            }) {
                Text(stringResource(R.string.ok))
            }
        },
    )
}