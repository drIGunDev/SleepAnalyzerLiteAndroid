package de.igor.gun.sleep.analyzer.ui.screens.tracking.views

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import de.igor.gun.sleep.analyzer.ui.screens.tracking.model.TrackingViewModel
import de.igor.gun.sleep.analyzer.ui.tools.chart.Chart


@Composable
fun MeasurementsPanel(
    modifier: Modifier = Modifier,
    gathererModel: TrackingViewModel,
) {
    val chartBuilder = gathererModel.chartBuilder
    val invalidate = gathererModel.chartUpdateState

    Chart(
        modifier = Modifier
            .then(modifier)
            .padding(top = 5.dp, bottom = 5.dp)
            .onSizeChanged {
                chartBuilder.resetXScale()
            },
        builder = chartBuilder,
        invalidate = invalidate,
        drawOffscreen = true
    )
}
