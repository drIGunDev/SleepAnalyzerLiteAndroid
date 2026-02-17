package de.igor.gun.sleep.analyzer.ui.screens.archive.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import de.igor.gun.sleep.analyzer.db.entities.Measurement
import de.igor.gun.sleep.analyzer.misc.formatToString
import de.igor.gun.sleep.analyzer.ui.misc.ShowProgressBar
import de.igor.gun.sleep.analyzer.ui.misc.viewModel
import de.igor.gun.sleep.analyzer.ui.screens.archive.model.ArchiveListViewModel
import de.igor.gun.sleep.analyzer.ui.tools.chart.Chart
import de.igor.gun.sleep.analyzer.ui.tools.hypnogram.LinearHypnogram
import kotlinx.serialization.Serializable


@Serializable
data class Detail(val seriesId: Long)

@Composable
fun ArchiveDetailScreen(
    seriesId: Long,
) {
    val viewModel = viewModel<ArchiveListViewModel>()

    val textFieldStyle = MaterialTheme.typography.labelSmall
    val appSettings = viewModel.appParametersLive
    val isShowHR = rememberSaveable { mutableStateOf(false) }
    val isShowACC = rememberSaveable { mutableStateOf(false) }
    val showProgress = rememberSaveable { mutableStateOf(true) }
    val showMathParamsDialog = rememberSaveable { mutableStateOf(false) }

    fun setChangeModelParameters() {
        viewModel.updateAppParameters(appSettings.value)
    }

    fun requestChart() {
        val typesRmse = mutableListOf<Measurement.Id>().apply {
            if (viewModel.isDebugVersion) {
                if (isShowHR.value) this.add(Measurement.Id.HR)
                if (isShowACC.value) this.add(Measurement.Id.ACC)
            }
        }
        setChangeModelParameters()
        val types = if (viewModel.isDebugVersion)
            listOf(
                Measurement.Id.HR,
                Measurement.Id.ACC,
                Measurement.Id.GYRO
            ) else
            listOf(Measurement.Id.HR)
        viewModel.requestChart(
            seriesId,
            types = types,
            typesRmse,
            showRmse = viewModel.isDebugVersion,
        )
    }

    val chartBuilder = viewModel.chartBuilderState
    val hypnogramBuilder = viewModel.hypnogramBuilderState
    val series = viewModel.seriesState.value

    LifecycleStartEffect(Unit) {
        viewModel.requestAppParameters()
        if (hypnogramBuilder.value.isEmpty()) {
            requestChart()
        }
        onStopOrDispose {
            viewModel.cleanChart()
            showProgress.value = true
        }
    }

    if (hypnogramBuilder.value.isEmpty()) {
        ShowProgressBar()
        return
    }

    ShowMathParamsDialog(
        hrFrameCurrent = appSettings.value.frameSizeHR,
        accFrameCurrent = appSettings.value.frameSizeACC,
        quantizationHRCurrent = appSettings.value.quantizationHR,
        quantizationACCCurrent = appSettings.value.quantizationACC,
        shouldShowDialog = showMathParamsDialog,
        onConfirm = { hrFrameSize, accFrameSize, quantizationHR, quantizationACC ->
            viewModel.updateAppParameters(hrFrameSize, accFrameSize, quantizationHR, quantizationACC)
            requestChart()
        }
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (series != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = series.startDate.formatToString(end = series.endDate),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
        LinearHypnogram(
            modifier = Modifier
                .fillMaxSize()
                .height(200.dp)
                .padding(25.dp),
            builder = hypnogramBuilder.value
        )
        Spacer(modifier = Modifier.height(20.dp))
        Chart(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(25.dp),
            builder = chartBuilder.value
        )

        if (!viewModel.isDebugVersion) return

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Show rmse hr:", style = textFieldStyle)
            Checkbox(
                checked = isShowHR.value,
                onCheckedChange = { isShowHR.value = it; requestChart() }
            )
            Text("acc:", style = textFieldStyle)
            Checkbox(
                checked = isShowACC.value,
                onCheckedChange = { isShowACC.value = it; requestChart() }
            )
        }
        Text(text = "Frame size HR: ${appSettings.value.frameSizeHR.toInt()}", style = textFieldStyle)
        Slider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 50.dp, end = 50.dp),
            value = appSettings.value.frameSizeHR.toFloat(),
            valueRange = 10f..1000f,
            onValueChange = { appSettings.value.frameSizeHR = it.toInt() },
            onValueChangeFinished = { requestChart() }
        )
        Text(text = "Quantization HR: ${appSettings.value.quantizationHR}", style = textFieldStyle)
        Slider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 50.dp, end = 50.dp),
            value = appSettings.value.quantizationHR,
            valueRange = 0f..1f,
            onValueChange = { appSettings.value.quantizationHR = it },
            onValueChangeFinished = { requestChart() }
        )
        Text(text = "Frame size ACC: ${appSettings.value.frameSizeACC}", style = textFieldStyle)
        Slider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 50.dp, end = 50.dp),
            value = appSettings.value.frameSizeACC.toFloat(),
            valueRange = 10f..1000f,
            onValueChange = { appSettings.value.frameSizeACC = it.toInt() },
            onValueChangeFinished = { requestChart() }
        )
        Text(text = "Quantization ACC: ${appSettings.value.quantizationACC}", style = textFieldStyle)
        Slider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 50.dp, end = 50.dp),
            value = appSettings.value.quantizationACC,
            valueRange = 0f..1f,
            onValueChange = { appSettings.value.quantizationACC = it },
            onValueChangeFinished = { requestChart() }
        )
        TextButton(onClick = { showMathParamsDialog.value = true }) {
            Text("Set params", style = textFieldStyle)
        }
    }
}
