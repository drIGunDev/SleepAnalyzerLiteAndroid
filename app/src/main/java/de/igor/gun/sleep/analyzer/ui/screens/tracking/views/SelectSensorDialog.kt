package de.igor.gun.sleep.analyzer.ui.screens.tracking.views


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import de.igor.gun.sleep.analyzer.services.sensors.SensorAPI
import de.igor.gun.sleep.analyzer.ui.theme.DialogBackground
import de.igor.gun.sleep.analyzer.ui.tools.dialog.ActionScope
import de.igor.gun.sleep.analyzer.ui.tools.dialog.DialogActionButtons
import de.igor.gun.sleep.analyzer.ui.tools.dialog.DialogTitle


@Composable
fun ShowSelectSensorDialogIfRequested(
    shouldShowDialog: MutableState<Boolean> = rememberSaveable { mutableStateOf(true) },
    sensorList: List<SensorAPI.SensorInfo> = mutableListOf(),
    onDismissDialog: () -> Unit = { shouldShowDialog.value = false },
    onSensorSelected: (SensorAPI.SensorInfo) -> Unit = {},
) {
    if (!shouldShowDialog.value) return

    Dialog(onDismissRequest = { onDismissDialog() }) {
        Surface(
            color = DialogBackground,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val title = stringResource(R.string.select_sensor_title)
                DialogTitle(title)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, top = 20.dp)
                ) {
                    val text = stringResource(R.string.select_sensor_text)
                    Text(text, style = MaterialTheme.typography.labelMedium)
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    items(sensorList) { item ->
                        TextButton(
                            onClick = {
                                onSensorSelected(item)
                                onDismissDialog()
                            }
                        ) {
                            Text(text = item.name)
                        }
                    }
                }
                LinearProgressIndicator(modifier = Modifier.padding(top = 50.dp, bottom = 10.dp))
                DialogActionButtons(
                    showPositiveButton = remember { mutableStateOf(false) },
                    negativeButtonAction = ActionScope(stringResource(R.string.cancel)) {
                        onDismissDialog()
                    }
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun TestSelectSensorDialog() {
    val sensorList = mutableListOf<SensorAPI.SensorInfo>()
    sensorList.add(SensorAPI.SensorInfo("Sensor 0", "", 0, "Sensor 0", true))
    sensorList.add(SensorAPI.SensorInfo("Sensor 1", "", 0, "Sensor 0", true))
    sensorList.add(SensorAPI.SensorInfo("Sensor 2", "", 0, "Sensor 0", true))
    ShowSelectSensorDialogIfRequested(sensorList = sensorList)
}