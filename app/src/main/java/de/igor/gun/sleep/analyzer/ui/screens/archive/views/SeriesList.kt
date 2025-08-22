package de.igor.gun.sleep.analyzer.ui.screens.archive.views

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavHostController
import de.igor.gun.sleep.analyzer.misc.durationToMillis
import de.igor.gun.sleep.analyzer.ui.misc.ShowProgressBar
import de.igor.gun.sleep.analyzer.ui.misc.viewModel
import de.igor.gun.sleep.analyzer.ui.screens.archive.model.ArchiveListViewModel


@Composable
fun ShowSeriesList(navController: NavHostController) {
    val viewModel = viewModel<ArchiveListViewModel>()
    val listState = rememberLazyListState()

    val seriesList = viewModel.seriesList

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (seriesList.value == null) {
            viewModel.updateSeriesList()
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.cleanPreviewList()
    }

    if (seriesList.value == null) {
        ShowProgressBar()
        return
    }

    seriesList.value?.let { list ->
        val firstItem = list.firstOrNull()
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            state = listState
        ) {
            items(
                list,
                key = { it.id }
            ) { item ->
                ShowSeriesItem(
                    series = item,
                    navController = navController,
                    isFirst = item == firstItem,
                    onDelete = { series -> viewModel.deleteSeries(series) },
                    onRescale = { series, autoScale, duration, minHR, maxHR ->
                        viewModel.startSingleHRRescaling(
                            series,
                            if (autoScale) null else minHR.toFloat(),
                            if (autoScale) null else maxHR.toFloat(),
                            maxXScaled = if (autoScale) null else duration?.durationToMillis(6f),
                        )
                    },
                    onRescaleCompleted = { viewModel.updateSeriesList() }
                )
            }
        }
    }
}