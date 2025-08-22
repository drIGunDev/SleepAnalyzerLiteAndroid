package de.igor.gun.sleep.analyzer.ui.screens.archive.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import de.igor.gun.sleep.analyzer.repositories.tools.ChartBuilder
import de.igor.gun.sleep.analyzer.ui.misc.viewModel
import de.igor.gun.sleep.analyzer.ui.screens.archive.detail.Detail
import de.igor.gun.sleep.analyzer.ui.screens.archive.model.ArchiveListViewModel
import de.igor.gun.sleep.analyzer.ui.screens.archive.model.SeriesWrapper
import de.igor.gun.sleep.analyzer.ui.tools.chart.Chart


@Composable
fun ShowChart(
    navController: NavHostController,
    item: SeriesWrapper,
) {
    val viewModel = viewModel<ArchiveListViewModel>()
    val chartBuilderState = remember { mutableStateOf<ChartBuilder?>(null) }
    LaunchedEffect(chartBuilderState) {
        when (item) {
            is SeriesWrapper.Measurements -> chartBuilderState.value = viewModel.requestChartBuilder(item.measurements)
            else -> Unit
        }
    }

    chartBuilderState.value?.let { chartBuilder ->
        Chart(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(top = 5.dp, bottom = 5.dp, start = 16.dp, end = 16.dp)
                .onSizeChanged {
                    chartBuilder.resetXScale()
                }
                .clickable { navController.navigate(Detail(item.series.id)) },
            builder = chartBuilder,
            drawOffscreen = true
        )
    }
}