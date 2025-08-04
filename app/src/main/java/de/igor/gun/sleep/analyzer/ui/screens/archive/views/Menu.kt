package de.igor.gun.sleep.analyzer.ui.screens.archive.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.igor.gun.sleep.analyzer.R
import de.igor.gun.sleep.analyzer.db.entities.Series
import de.igor.gun.sleep.analyzer.ui.screens.archive.model.ArchiveListViewModel
import de.igor.gun.sleep.analyzer.ui.screens.archive.model.SeriesWrapper


@Composable
fun ShowDropdownMenu(
    series: Series,
    shouldDisplayMenu: MutableState<Boolean>,
    onDismiss: () -> Unit = { shouldDisplayMenu.value = false },
    onDelete: (Series) -> Unit,
    onRescale: (Series) -> Unit,
) {
    if (shouldDisplayMenu.value) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
            DropdownMenu(expanded = true, onDismissRequest = { onDismiss() }) {
                DropdownMenuItem(
                    text = {
                        val deleteText = stringResource(R.string.menu_delete)
                        Text(deleteText)
                    },
                    onClick = {
                        onDismiss()
                        onDelete(series)
                    })
                DropdownMenuItem(
                    text = {
                        val rescaleText = stringResource(R.string.menu_rescale)
                        Text(rescaleText)
                    },
                    onClick = {
                        onDismiss()
                        onRescale(series)
                    })
            }
        }
    }
}

@Composable
fun ShowDialogs(
    item: SeriesWrapper,
    shouldShowRescaleDialog: MutableState<Boolean>,
    showDeleteSeriesAlert: MutableState<Boolean>,
    rescalingSate: MutableState<ArchiveListViewModel.RescalingState?>,
    onDelete: (Series) -> Unit,
    onRescale: (Series, Boolean, Float?, Int, Int) -> Unit,
    onRescaleCompleted: () -> Unit,
) {
    val shouldShowRescaleProgress = remember { mutableStateOf(false) }

    ShowHRScaleDialog(
        shouldShowDialog = shouldShowRescaleDialog,
        onRescale = { autoScale, duration, minHR, maxHR ->
            onRescale(item.series, autoScale, duration, minHR, maxHR)
            shouldShowRescaleProgress.value = true
        },
    )

    ShowHRScaleProgressDialog(
        shouldShowDialog = shouldShowRescaleProgress,
        rescaleState = rescalingSate,
        onRescaleCompleted = onRescaleCompleted,
    )

    ShowDeleteItemAlert(
        shouldShowDeleteSeriesAlert = showDeleteSeriesAlert,
        confirmAction = { onDelete(item.series) }
    )
}