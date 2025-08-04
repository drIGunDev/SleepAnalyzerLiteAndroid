package de.igor.gun.sleep.analyzer.ui.screens.archive.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.igor.gun.sleep.analyzer.R
import de.igor.gun.sleep.analyzer.misc.toInt
import de.igor.gun.sleep.analyzer.ui.screens.archive.model.ArchiveListViewModel
import de.igor.gun.sleep.analyzer.ui.theme.DialogBackground
import de.igor.gun.sleep.analyzer.ui.tools.dialog.ActionScope
import de.igor.gun.sleep.analyzer.ui.tools.dialog.DialogActionButtons
import de.igor.gun.sleep.analyzer.ui.tools.dialog.DialogTitle
import de.igor.gun.sleep.analyzer.ui.tools.dialog.ProgressBar


private const val DURATION = 6f
private const val MIN_HR = 30
private const val MAX_HR = 100
private val FIELDS_WIDTH = 200.dp
private val SPACE_HEIGHT = 15.dp

@Composable
@Preview(showBackground = true)
fun ShowHRScaleDialog(
    shouldShowDialog: MutableState<Boolean> = rememberSaveable { mutableStateOf(true) },
    onDismissDialog: () -> Unit = { shouldShowDialog.value = false },
    onRescale: (Boolean, Float?, Int, Int) -> Unit = { _, _, _, _ -> },
) {
    if (!shouldShowDialog.value) return

    val autoScale = remember { mutableStateOf(false) }
    val minHR = remember { mutableStateOf(MIN_HR.toString()) }
    val maxHR = remember { mutableStateOf(MAX_HR.toString()) }

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = { onDismissDialog() },
    ) {
        Surface(
            modifier = Modifier.verticalScroll(state = scrollState),
            color = DialogBackground,
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val dialogTitle = stringResource(R.string.rescale_title)
                DialogTitle(title = dialogTitle)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val autoscale = stringResource(R.string.rescale_autoscale)
                    val textFieldStyle = MaterialTheme.typography.bodyLarge
                    Text(autoscale, style = textFieldStyle)
                    Checkbox(
                        checked = autoScale.value,
                        onCheckedChange = { autoScale.value = it }
                    )
                }
                if (!autoScale.value) {
                    val minHRText = stringResource(R.string.min_hr_text)
                    val maxHRText = stringResource(R.string.max_hr_text)
                    OutlinedTextField(
                        modifier = Modifier.width(FIELDS_WIDTH),
                        singleLine = true,
                        label = { Text(minHRText) },
                        value = minHR.value,
                        onValueChange = { minHR.value = it }
                    )
                    Spacer(modifier = Modifier.height(SPACE_HEIGHT))
                    OutlinedTextField(
                        modifier = Modifier.width(FIELDS_WIDTH),
                        singleLine = true,
                        label = { Text(maxHRText) },
                        value = maxHR.value,
                        onValueChange = { maxHR.value = it }
                    )
                }
                val cancelRes = stringResource(R.string.cancel)
                DialogActionButtons(
                    positiveButtonAction = ActionScope { button ->
                        onRescale(
                            autoScale.value,
                            null,
                            minHR.value.toInt(default = MIN_HR),
                            maxHR.value.toInt(default = MAX_HR)
                        )
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
@Preview(showBackground = true)
fun ShowHRScaleProgressDialog(
    shouldShowDialog: MutableState<Boolean> = rememberSaveable { mutableStateOf(true) },
    rescaleState: MutableState<ArchiveListViewModel.RescalingState?> = rememberSaveable { mutableStateOf(null) },
    onDismissDialog: () -> Unit = { shouldShowDialog.value = false },
    onCanceled: () -> Unit = {},
    onRescaleCompleted: () -> Unit = {},
) {
    val progressText = remember { mutableStateOf(" ") }
    val progressValue = remember { mutableFloatStateOf(0f) }

    rescaleState.value?.let {
        when (it) {
            is ArchiveListViewModel.RescalingState.Progress -> {
                progressText.value = it.text
                progressValue.floatValue = it.progress
            }

            is ArchiveListViewModel.RescalingState.Completed -> {
                onRescaleCompleted()
                rescaleState.value = null
                onDismissDialog()
            }
        }
    }

    if (!shouldShowDialog.value) return

    Dialog(
        onDismissRequest = { }
    ) {
        Surface(
            color = DialogBackground,
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val dialogTitle = stringResource(R.string.rescaling_title)
                DialogTitle(title = dialogTitle)
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