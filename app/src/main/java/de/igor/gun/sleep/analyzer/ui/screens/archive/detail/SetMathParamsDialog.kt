package de.igor.gun.sleep.analyzer.ui.screens.archive.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.igor.gun.sleep.analyzer.R
import de.igor.gun.sleep.analyzer.ui.theme.DialogBackground
import de.igor.gun.sleep.analyzer.ui.tools.dialog.ActionScope
import de.igor.gun.sleep.analyzer.ui.tools.dialog.DialogActionButtons
import de.igor.gun.sleep.analyzer.ui.tools.dialog.DialogTitle


private val FIELDS_WIDTH = 200.dp
private val SPACE_HEIGHT = 15.dp

@Composable
@Preview(showBackground = true)
fun ShowMathParamsDialog(
    hrFrameCurrent: Int = 20,
    accFrameCurrent: Int = 20,
    quantizationHRCurrent: Float = 0.8f,
    quantizationACCCurrent: Float = 0.89f,
    shouldShowDialog: MutableState<Boolean> = remember { mutableStateOf(true) },
    onDismiss: () -> Unit = { shouldShowDialog.value = false },
    onConfirm: (Int, Int, Float, Float) -> Unit = { _, _, _, _ -> }
) {
    if (!shouldShowDialog.value) return

    var hrFrame by rememberSaveable { mutableStateOf(hrFrameCurrent.toString()) }
    var accFrame by rememberSaveable { mutableStateOf(accFrameCurrent.toString()) }
    var quantizationHR by rememberSaveable { mutableStateOf(quantizationHRCurrent.toString()) }
    var quantizationACC by rememberSaveable { mutableStateOf(quantizationACCCurrent.toString()) }
    val validationError = remember { mutableStateListOf(false, false, false, false) }

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = { onDismiss() }
    ) {
        Surface(
            modifier = Modifier.verticalScroll(state = scrollState),
            color = DialogBackground,
            shape = MaterialTheme.shapes.medium,
        )
        {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                DialogTitle(title = "Math Params")
                OutlinedTextField(
                    modifier = Modifier.width(FIELDS_WIDTH),
                    singleLine = true,
                    label = { TextWithValidMessage("Frame size HR:", validationError[0], "Int = 20") },
                    value = hrFrame,
                    onValueChange = { hrFrame = it }
                )
                Spacer(modifier = Modifier.height(SPACE_HEIGHT))
                OutlinedTextField(
                    modifier = Modifier.width(FIELDS_WIDTH),
                    singleLine = true,
                    label = { TextWithValidMessage("Frame size ACC:", validationError[1], "Int = 20") },
                    value = accFrame,
                    onValueChange = { accFrame = it }
                )
                Spacer(modifier = Modifier.height(SPACE_HEIGHT))
                OutlinedTextField(
                    modifier = Modifier.width(FIELDS_WIDTH),
                    singleLine = true,
                    label = { TextWithValidMessage("Quantization HR:", validationError[2], "Float = 0.8") },
                    value = quantizationHR,
                    onValueChange = { quantizationHR = it }
                )
                Spacer(modifier = Modifier.height(SPACE_HEIGHT))
                OutlinedTextField(
                    modifier = Modifier.width(FIELDS_WIDTH),
                    singleLine = true,
                    label = { TextWithValidMessage("Quantization ACC:", validationError[3], "Float = 0.89") },
                    value = quantizationACC,
                    onValueChange = { quantizationACC = it }
                )
                Spacer(modifier = Modifier.height(SPACE_HEIGHT))
                val cancelRes = stringResource(R.string.cancel)
                DialogActionButtons(
                    positiveButtonAction = ActionScope { button ->
                        val hrFrame = hrFrame.toIntOrNull()
                        validationError[0] = hrFrame == null

                        val accFrame = accFrame.toIntOrNull()
                        validationError[1] = accFrame == null

                        val quantizationHR = quantizationHR.toFloatOrNull()
                        validationError[2] = quantizationHR == null

                        val quantizationACC = quantizationACC.toFloatOrNull()
                        validationError[3] = quantizationACC == null

                        if (validationError.contains(true)) {
                            return@ActionScope
                        }

                        onConfirm(hrFrame!!, accFrame!!, quantizationHR!!, quantizationACC!!)
                        onDismiss()
                    },
                    negativeButtonAction = ActionScope(cancelRes) {
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
fun TextWithValidMessage(
    text: String,
    validationError: Boolean,
    defaultValue: String = ""
) {
    val textExt = if (validationError) "$text[default :$defaultValue]" else text
    Text(
        text = textExt,
        color = if (validationError) Color.Red else MaterialTheme.colorScheme.onSurface
    )
}