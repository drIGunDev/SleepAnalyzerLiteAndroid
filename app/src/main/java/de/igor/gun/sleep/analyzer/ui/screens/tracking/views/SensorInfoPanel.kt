package de.igor.gun.sleep.analyzer.ui.screens.tracking.views

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.igor.gun.sleep.analyzer.R
import de.igor.gun.sleep.analyzer.misc.AppSettings
import de.igor.gun.sleep.analyzer.misc.formatToString
import de.igor.gun.sleep.analyzer.services.sensors.SensorAPI
import de.igor.gun.sleep.analyzer.ui.screens.tracking.model.SensorViewModel
import de.igor.gun.sleep.analyzer.ui.screens.tracking.model.TrackingViewModel
import de.igor.gun.sleep.analyzer.ui.theme.MainWhiteColor
import de.igor.gun.sleep.analyzer.ui.tools.indicators.batteryindicator.BatteryIndicator
import de.igor.gun.sleep.analyzer.ui.tools.indicators.batteryindicator.BatteryIndicatorParams
import de.igor.gun.sleep.analyzer.ui.tools.indicators.rssiindicator.RSSIIndicator
import kotlinx.coroutines.delay
import java.time.LocalDateTime


@SuppressLint("DefaultLocale")
@Composable
fun ShowSensorInfoPanel(
    sensorViewModel: SensorViewModel,
    gathererViewModel: TrackingViewModel,
) {
    val rssiFlow = gathererViewModel.rssiFlow.collectAsState(-200)
    val batteryFlow = gathererViewModel.batteryFlow.collectAsState(0)

    val sensorState = sensorViewModel.sensorStateFlow.collectAsState()
    val currentSensorId = sensorViewModel.sensorConnectFlow.collectAsState()
    val connectionState = sensorViewModel.connectionState
    val showSelectSensorDialog = rememberSaveable { mutableStateOf(false) }
    val availableSensors = sensorViewModel.availableSensorsFlow.collectAsState(emptyList())
    val context = LocalContext.current

    when (sensorState.value) {
        is SensorAPI.SensorState.Disconnected -> gathererViewModel.resetFlows()

        else -> {}
    }

    LaunchedEffect(showSelectSensorDialog.value) {
        if (!showSelectSensorDialog.value) return@LaunchedEffect

        sensorViewModel.resetAvailableSensors()
        delay(500)
        sensorViewModel.startScanSensors()
    }

    // show select sensor dialog
    ShowSelectSensorDialogIfRequested(
        showSelectSensorDialog,
        availableSensors.value,
        onSensorSelected = {
            AppSettings(context).deviceId = it.deviceId
            sensorViewModel.bindSensor(it.deviceId)
        }
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        AnimatedVisibility(
            sensorViewModel.connectionState.value != SensorViewModel.ConnectionState.UNBOUND &&
                    sensorViewModel.connectionState.value != SensorViewModel.ConnectionState.BINDING
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // show left side info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.End
                ) {
                    Column(
                        modifier = Modifier.padding(start = 10.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        if (gathererViewModel.isRecording.value) {
                            // show start date
                            gathererViewModel.recordedSeries?.let {
                                Text(text = it.startDate.formatToString(end = LocalDateTime.now()), style = MaterialTheme.typography.labelSmall)
                            }
                            // show HR Graph
                            MeasurementsPanel(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(70.dp),
                                gathererModel = gathererViewModel
                            )
                        }
                    }
                    // show duration
                    if (gathererViewModel.isRecording.value) {
                        val text = stringResource(R.string.tracking_duration)
                        val value = gathererViewModel.recordingDuration.value
                        Text(
                            text = if (value == null) " " else "$text: $value",
                            color = MainWhiteColor,
                            fontSize = 9.sp
                        )
                    }
                }
                // show right side info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.End
                ) {
                    val spaceHeight = 5.dp
                    // show Sensor ID
                    Text(text = "Sensor ID: ${currentSensorId.value ?: ""}", style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(spaceHeight))
                    // show Battery state
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(text = "Battery ${batteryFlow.value}%: ", style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.width(5.dp))
                        BatteryIndicator(
                            modifier = Modifier
                                .size(12.dp, 20.dp)
                                .border(width = 0.dp, color = Color.Black),
                            state = batteryFlow.value,
                            params = BatteryIndicatorParams().copy(backgroundColor = MaterialTheme.colorScheme.primary, chargingColor = Color.Green)
                        )
                    }
                    Spacer(Modifier.height(spaceHeight))
                    // show RSSI
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(text = "RSSI ${rssiFlow.value} dB: ", style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.width(5.dp))
                        RSSIIndicator(
                            modifier = Modifier.size(12.dp, 12.dp),
                            rssiLevel = rssiFlow.value.toFloat()
                        )
                    }
                    Spacer(Modifier.height(spaceHeight))
                    // show Sensor state
                    Text(text = connectionState.value.name, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Spacer(Modifier.height(15.dp))
        TextButton(
            onClick = { showSelectSensorDialog.value = true },
            enabled = !gathererViewModel.isRecording.value
        ) {
            val selectSensorText = stringResource(R.string.select_sensor_title)
            Text(text = selectSensorText)
        }
    }
}