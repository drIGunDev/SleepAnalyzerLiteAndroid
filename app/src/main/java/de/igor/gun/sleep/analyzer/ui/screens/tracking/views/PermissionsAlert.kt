package de.igor.gun.sleep.analyzer.ui.screens.tracking.views

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.igor.gun.sleep.analyzer.R


@Composable
@Preview(showBackground = true)
fun ShowNoPermissionsGrantedAlert(
    noPermissionsGranted: MutableState<String?> = mutableStateOf("\nCONNECTION,\nBLUETOOTH_CONNECT"),
    confirmAction: () -> Unit = {},
    cancelAction: () -> Unit = {}
) {
    if (noPermissionsGranted.value != null &&
        noPermissionsGranted.value!!.isNotEmpty()
    ) {
        val format = stringResource(R.string.permissions_not_granted_text)
        val text = String.format(format, noPermissionsGranted.value)
        AlertDialog(
            onDismissRequest = { noPermissionsGranted.value = null },
            title = { Text(stringResource(R.string.permissions_not_granted_title)) },
            text = { Text(text) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_warning_24),
                    tint = Color.Red,
                    contentDescription = null
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    noPermissionsGranted.value = null
                    confirmAction()
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
        )
    }
}