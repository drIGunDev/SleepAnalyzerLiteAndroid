package de.igor.gun.sleep.analyzer.ui.screens.tracking.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.igor.gun.sleep.analyzer.R
import de.igor.gun.sleep.analyzer.ui.screens.tracking.model.TrackingViewModel
import de.igor.gun.sleep.analyzer.ui.theme.DialogBackground
import de.igor.gun.sleep.analyzer.ui.tools.dialog.ActionScope
import de.igor.gun.sleep.analyzer.ui.tools.dialog.DialogActionButtons
import de.igor.gun.sleep.analyzer.ui.tools.dialog.DialogTitle
import de.igor.gun.sleep.analyzer.ui.tools.dialog.ProgressBar


@Composable
fun ShowRepairDialog(
    modifier: Modifier = Modifier,
    shouldShowDialog: MutableState<Boolean>,
    onDismissDialog: () -> Unit = { shouldShowDialog.value = false },
    onStarted: () -> Unit = {},
) {
    if (!shouldShowDialog.value) return

    val shouldShowProgressBar = rememberSaveable { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { shouldShowDialog.value = shouldShowProgressBar.value },
    ) {
        Surface(
            color = DialogBackground,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .then(modifier)
                    .padding(start = 20.dp, end = 20.dp)
            ) {
                val titleRes = stringResource(R.string.repair_title)
                DialogTitle(titleRes)
                if (!shouldShowProgressBar.value) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp, top = 20.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.repair_text),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                val rescalingRes = stringResource(R.string.repairing_title)
                val cancelRes = stringResource(R.string.cancel)
                DialogActionButtons(
                    positiveButtonAction = ActionScope(rescalingRes) { button ->
                        onStarted()
                        onDismissDialog()
                    },
                    negativeButtonAction = ActionScope(cancelRes) {
                        onDismissDialog()
                    }
                )
            }
        }
    }
}

@Composable
fun ShowProgressRepairDialog(
    modifier: Modifier = Modifier,
    shouldShowDialog: MutableState<Boolean>,
    repairState: MutableState<TrackingViewModel.RepairState?>,
    onDismissDialog: () -> Unit = { shouldShowDialog.value = false },
    onCanceled: () -> Unit = {},
    onCompleted: () -> Unit = {},
) {
    val progressText = remember { mutableStateOf(" ") }
    val progressValue = remember { mutableFloatStateOf(0f) }
    repairState.value?.let {
        when (it) {
            is TrackingViewModel.RepairState.Progress -> {
                progressText.value = it.text
                progressValue.floatValue = it.progress
            }

            is TrackingViewModel.RepairState.Completed -> {
                repairState.value = null
                onDismissDialog()
                onCompleted()
            }
        }
    }

    if (!shouldShowDialog.value) return

    Dialog(
        onDismissRequest = { },
    ) {
        Surface(
            color = DialogBackground,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .then(modifier)
                    .padding(start = 20.dp, end = 20.dp)
            ) {
                val titleRes = stringResource(R.string.repairing_title)
                DialogTitle(titleRes)
                ProgressBar(
                    showProgressBar = shouldShowDialog,
                    progressText = progressText,
                    progress = progressValue,
                )
                val cancelRes = stringResource(R.string.cancel)
                DialogActionButtons(
                    showPositiveButton = remember { mutableStateOf(false) },
                    negativeButtonAction = ActionScope(cancelRes) {
                        onCanceled()
                        onDismissDialog()
                    }
                )
            }
        }
    }
}