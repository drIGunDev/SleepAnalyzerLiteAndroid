package de.igor.gun.sleep.analyzer.ui.screens.archive.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import de.igor.gun.sleep.analyzer.R
import de.igor.gun.sleep.analyzer.db.entities.Series
import de.igor.gun.sleep.analyzer.db.entities.satisfactionToEmoji
import de.igor.gun.sleep.analyzer.misc.formatToString
import de.igor.gun.sleep.analyzer.misc.millisToDurationCompact
import de.igor.gun.sleep.analyzer.misc.millisToDurationFull
import de.igor.gun.sleep.analyzer.ui.misc.formatDuration
import de.igor.gun.sleep.analyzer.ui.misc.formatHR
import de.igor.gun.sleep.analyzer.ui.misc.viewModel
import de.igor.gun.sleep.analyzer.ui.screens.archive.model.ArchiveListViewModel
import de.igor.gun.sleep.analyzer.ui.screens.archive.model.SeriesWrapper
import de.igor.gun.sleep.analyzer.ui.theme.AWAKEColor
import de.igor.gun.sleep.analyzer.ui.theme.DSLEEPColor
import de.igor.gun.sleep.analyzer.ui.theme.DialogBackground
import de.igor.gun.sleep.analyzer.ui.theme.LSLEEPColor
import de.igor.gun.sleep.analyzer.ui.theme.REMColor


@Composable
fun LazyItemScope.ShowSeriesItem(
    series: Series,
    navController: NavHostController,
    isFirst: Boolean,
    onDelete: (Series) -> Unit,
    onRescale: (Series, Boolean, Float?, Int, Int) -> Unit,
    onRescaleCompleted: () -> Unit,
) {
    val viewModel = viewModel<ArchiveListViewModel>()
    val shouldDisplayMenu = rememberSaveable { mutableStateOf(false) }
    val showDeleteSeriesAlert = rememberSaveable { mutableStateOf(false) }
    val shouldShowRescaleDialog = rememberSaveable { mutableStateOf(false) }
    val wrapperCache = viewModel.seriesWrapperCache.asStateMap()
    val rescalingSate = viewModel.rescalingSate

    LaunchedEffect(series, wrapperCache[series.id] == null) {
        if (wrapperCache[series.id] == null) {
            viewModel.updateSeriesCache(series)
        }
    }

    val item = wrapperCache[series.id]
    if (item == null) {
        ShowPlaceHolder()
        return
    }

    Surface(
        modifier = Modifier
            .animateItem()
            .padding(8.dp),
        color = DialogBackground,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 15.dp, end = 15.dp, top = 8.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ShowHeader(
                series = series,
                item = item,
                isFirst = isFirst,
                shouldDisplayMenu = shouldDisplayMenu,
                showDeleteSeriesAlert = showDeleteSeriesAlert,
                shouldShowRescaleDialog = shouldShowRescaleDialog,
                rescalingSate = rescalingSate,
                onDelete = onDelete,
                onRescale = onRescale,
                onRescaleCompleted = onRescaleCompleted
            )
            ShowSeries(
                item = item,
                navController = navController
            )
        }
    }
}

@Composable
private fun ShowSeries(
    item: SeriesWrapper,
    navController: NavHostController
) {
    val description = createDescription(item = item)

    when (item) {
        is SeriesWrapper.Cached -> ShowCacheContent(
            item = item,
            navController = navController,
            description = description
        )

        is SeriesWrapper.Measurements -> ShowMeasurements(
            item = item,
            navController = navController,
            description = description
        )
    }
}

@Composable
private fun ShowMeasurements(
    item: SeriesWrapper.Measurements,
    navController: NavHostController,
    description: String
) {
    ShowChart(
        navController = navController,
        item = item,
    )
    Text(
        text = description,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun ShowCacheContent(item: SeriesWrapper.Cached, navController: NavHostController, description: String) {
    ShowImage(
        navController = navController,
        item = item,
    )
    Text(
        text = description,
        style = MaterialTheme.typography.bodySmall
    )

    if (item.isValidHypnogram().not()) return

    ShowSleepPhases(item = item)
}

@Composable
private fun createDescription(item: SeriesWrapper): String {
    return when (item) {
        is SeriesWrapper.Cached -> {
            "${formatHR(R.string.min_hr, item.cache.minHRScaled)} " +
                    "${formatHR(R.string.max_hr, item.cache.maxHRScaled)} " +
                    "(${millisToDurationFull(item.cache.duration)})"
        }

        is SeriesWrapper.Measurements -> {
            val hr = item.measurements.filter { it.hr != 0 }.map { it.hr }
            if (hr.isNotEmpty()) {
                "${formatHR(R.string.min_hr, hr.min().toFloat())} " +
                        "${formatHR(R.string.max_hr, hr.max().toFloat())} " +
                        "(${millisToDurationFull(item.series.durationMillis)})"
            } else {
                formatDuration(R.string.duration, millisToDurationFull(item.series.durationMillis))
            }
        }
    }
}

@Composable
private fun ShowHeader(
    series: Series,
    item: SeriesWrapper,
    isFirst: Boolean,
    shouldDisplayMenu: MutableState<Boolean>,
    showDeleteSeriesAlert: MutableState<Boolean>,
    shouldShowRescaleDialog: MutableState<Boolean>,
    rescalingSate: MutableState<ArchiveListViewModel.RescalingState?>,
    onDelete: (Series) -> Unit,
    onRescale: (Series, Boolean, Float?, Int, Int) -> Unit,
    onRescaleCompleted: () -> Unit,

    ) {
    val viewModel = viewModel<ArchiveListViewModel>()

    ShowDialogs(
        item = item,
        shouldShowRescaleDialog = shouldShowRescaleDialog,
        showDeleteSeriesAlert = showDeleteSeriesAlert,
        rescalingSate = rescalingSate,
        onDelete = onDelete,
        onRescale = onRescale,
        onRescaleCompleted = onRescaleCompleted,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(10f),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShowItemTitle(item = item)
        }

        val hasMenu = !(isFirst && viewModel.isRecording.value && item is SeriesWrapper.Measurements)
        if (hasMenu) {
            Column(modifier = Modifier.weight(1f)) {
                ShowMenu(
                    series = series,
                    shouldDisplayMenu = shouldDisplayMenu,
                    showDeleteSeriesAlert = showDeleteSeriesAlert,
                    shouldShowRescaleDialog = shouldShowRescaleDialog
                )
            }
        }
    }
}

@Composable
private fun ShowItemTitle(item: SeriesWrapper) {
    Text(
        text = "${item.series.startDate.formatToString()} ${item.series.satisfactionToEmoji()}",
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
private fun ShowMenu(
    series: Series,
    shouldDisplayMenu: MutableState<Boolean>,
    showDeleteSeriesAlert: MutableState<Boolean>,
    shouldShowRescaleDialog: MutableState<Boolean>
) {
    IconButton(onClick = { shouldDisplayMenu.value = !shouldDisplayMenu.value }) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Menu(Delete, Preview, etc)"
        )
    }
    ShowDropdownMenu(
        series = series,
        shouldDisplayMenu = shouldDisplayMenu,
        onDelete = { showDeleteSeriesAlert.value = true },
        onRescale = { shouldShowRescaleDialog.value = true }
    )
}

@Composable
private fun ShowSleepPhases(item: SeriesWrapper.Cached) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 15.dp, bottom = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val awakeText = stringResource(R.string.sleep_phase_awake)
                Text(text = awakeText, style = MaterialTheme.typography.bodySmall, color = AWAKEColor)
                Text(text = millisToDurationCompact(item.cache.awake), style = MaterialTheme.typography.bodySmall, color = AWAKEColor)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val lowSleepText = stringResource(R.string.sleep_phase_l_sleep)
                Text(text = lowSleepText, style = MaterialTheme.typography.bodySmall, color = LSLEEPColor)
                Text(text = millisToDurationCompact(item.cache.lSleep), style = MaterialTheme.typography.bodySmall, color = LSLEEPColor)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val deepSleepText = stringResource(R.string.sleep_phase_d_sleep)
                Text(text = deepSleepText, style = MaterialTheme.typography.bodySmall, color = DSLEEPColor)
                Text(text = millisToDurationCompact(item.cache.dSleep), style = MaterialTheme.typography.bodySmall, color = DSLEEPColor)

            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val remText = stringResource(R.string.sleep_phase_rem)
                Text(text = remText, style = MaterialTheme.typography.bodySmall, color = REMColor)
                Text(text = millisToDurationCompact(item.cache.rem), style = MaterialTheme.typography.bodySmall, color = REMColor)
            }
        }
    }
}

@Composable
private fun ShowPlaceHolder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center,
    ) {
        val loadingText = stringResource(R.string.loading)
        Text(loadingText)
    }
}