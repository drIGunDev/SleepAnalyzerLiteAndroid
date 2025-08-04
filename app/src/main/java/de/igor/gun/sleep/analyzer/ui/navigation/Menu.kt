package de.igor.gun.sleep.analyzer.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import de.igor.gun.sleep.analyzer.R
import de.igor.gun.sleep.analyzer.misc.durationToMillis
import de.igor.gun.sleep.analyzer.ui.misc.isRouteInDestination
import de.igor.gun.sleep.analyzer.ui.screens.archive.model.ArchiveListViewModel
import de.igor.gun.sleep.analyzer.ui.screens.archive.views.ShowHRScaleDialog
import de.igor.gun.sleep.analyzer.ui.screens.archive.views.ShowHRScaleProgressDialog


@Composable
fun RowScope.ShowMenu(
    navController: NavHostController,
    viewModel: ArchiveListViewModel,
) {
    val shouldShowMainMenu = rememberSaveable { mutableStateOf(false) }
    if (!navController.isRouteInDestination<RootGraph.Archive>()) return

    IconButton(onClick = { shouldShowMainMenu.value = !shouldShowMainMenu.value }) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Main Menu"
        )
    }

    val shouldShowRescaleDialog = rememberSaveable { mutableStateOf(false) }
    ShowMainMenu(
        shouldDisplayMenu = shouldShowMainMenu,
        onRescale = {
            shouldShowRescaleDialog.value = true
        }
    )

    val shouldShowRescaleProgress = rememberSaveable { mutableStateOf(false) }
    ShowHRScaleDialog(
        shouldShowDialog = shouldShowRescaleDialog,
        onRescale = { autoScale, duration, minHR, maxHR ->
            viewModel.startSeriesHRRescaling(
                if (autoScale) null else minHR.toFloat(),
                if (autoScale) null else maxHR.toFloat(),
                maxXScaled = if (autoScale) null else duration?.durationToMillis(6f)
            )
            shouldShowRescaleProgress.value = true
        },
    )

    ShowHRScaleProgressDialog(
        shouldShowDialog = shouldShowRescaleProgress,
        rescaleState = viewModel.rescalingSate,
        onRescaleCompleted = {
            viewModel.updatePreviewList()
        },
        onCanceled = {
            viewModel.stopHRRescaling()
            viewModel.updatePreviewList()
        }
    )
}

@Composable
private fun ShowMainMenu(
    shouldDisplayMenu: MutableState<Boolean>,
    onDismiss: () -> Unit = { shouldDisplayMenu.value = false },
    onRescale: () -> Unit,
) {
    if (shouldDisplayMenu.value) {
        Box {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { onDismiss() }) {
                DropdownMenuItem(
                    text = {
                        val rescaleAllText = stringResource(R.string.menu_rescale_all)
                        Text(rescaleAllText)
                    },
                    onClick = {
                        onDismiss()
                        onRescale()
                    }
                )
            }
        }
    }
}