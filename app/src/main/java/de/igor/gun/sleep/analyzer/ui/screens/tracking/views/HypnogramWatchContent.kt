package de.igor.gun.sleep.analyzer.ui.screens.tracking.views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import de.igor.gun.sleep.analyzer.R
import de.igor.gun.sleep.analyzer.ui.screens.tracking.model.SensorViewModel
import de.igor.gun.sleep.analyzer.ui.screens.tracking.model.ServiceViewModel
import de.igor.gun.sleep.analyzer.ui.screens.tracking.model.TrackingViewModel
import de.igor.gun.sleep.analyzer.ui.theme.Red
import de.igor.gun.sleep.analyzer.ui.tools.indicators.ppgviewer.PPGIndicator


@Composable
fun ShowTrackingContent(
    gathererViewModel: TrackingViewModel,
    serviceViewModel: ServiceViewModel,
    sensorViewModel: SensorViewModel,
    stopRecordingShowSatisfactionDialog: MutableState<Boolean>,
    startRecordingShowAlert: MutableState<Boolean>,
) {
    val ppgSource = gathererViewModel.ppgSource
    val context = LocalContext.current
    val hr = gathererViewModel.hrFlow.collectAsState(0)
    val isStreaming = sensorViewModel.connectionState.value == SensorViewModel.ConnectionState.STREAMING

    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val (button, text, measurements) = createRefs()
        PPGIndicator(
            ppgSource = ppgSource,
            modifier = Modifier
                .width(170.dp)
                .height(30.dp)
                .constrainAs(measurements) {
                    top.linkTo(parent.top, margin = 100.dp)
                    bottom.linkTo(text.top, margin = 50.dp)
                    centerHorizontallyTo(text)
                },
            xResolution = 2,
            showStream = isStreaming
        )
        Text(
            modifier = Modifier.constrainAs(text) {
                centerHorizontallyTo(parent)
                centerVerticallyTo(parent)
            },
            text = if (isStreaming) "${hr.value} BPM" else "-- BPM",
            style = MaterialTheme.typography.headlineLarge,
            color = if (isStreaming) Red else Color.Gray
        )
        PhantomButton(
            modifier = Modifier
                .constrainAs(button) {
                    top.linkTo(text.bottom, margin = 16.dp)
                    centerHorizontallyTo(text)
                },
            onClick = {
                if (!gathererViewModel.isRecording.value) {
                    serviceViewModel.startService(context)
                    gathererViewModel.startRecording {
                        gathererViewModel.startChartUpdating()
                        startRecordingShowAlert.value = true
                    }
                } else if (gathererViewModel.isRecording.value) {
                    stopRecordingShowSatisfactionDialog.value = true
                }
            },
            isOutlined = !gathererViewModel.isRecording.value,
            isEnabled = isStreaming
        ) {
            val startTrackingText = stringResource(R.string.tracking_start)
            val stopTrackingText = stringResource(R.string.tracking_stop)
            Text(if (gathererViewModel.isRecording.value) stopTrackingText else startTrackingText)
        }
    }
}

@Composable
private fun PhantomButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isOutlined: Boolean = false,
    isEnabled: Boolean = false,
    content: @Composable () -> Unit,
) {
    if (isOutlined) {
        OutlinedButton(
            modifier = modifier,
            onClick = onClick,
            enabled = isEnabled
        ) { content() }
    } else {
        Button(
            modifier = modifier,
            onClick = onClick,
            enabled = isEnabled
        ) { content() }
    }
}