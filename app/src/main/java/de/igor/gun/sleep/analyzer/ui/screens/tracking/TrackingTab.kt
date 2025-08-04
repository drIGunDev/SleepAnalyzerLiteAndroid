package de.igor.gun.sleep.analyzer.ui.screens.tracking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import de.igor.gun.sleep.analyzer.db.entities.toSatisfaction
import de.igor.gun.sleep.analyzer.misc.toLocalDateTime
import de.igor.gun.sleep.analyzer.repositories.tools.SleepPhasesHolder
import de.igor.gun.sleep.analyzer.ui.screens.tracking.model.SensorViewModel
import de.igor.gun.sleep.analyzer.ui.screens.tracking.model.ServiceViewModel
import de.igor.gun.sleep.analyzer.ui.screens.tracking.model.TrackingViewModel
import de.igor.gun.sleep.analyzer.ui.screens.tracking.views.ShowNoPermissionsGrantedAlert
import de.igor.gun.sleep.analyzer.ui.screens.tracking.views.ShowProgressRepairDialog
import de.igor.gun.sleep.analyzer.ui.screens.tracking.views.ShowRepairDialog
import de.igor.gun.sleep.analyzer.ui.screens.tracking.views.ShowSatisfactionDialog
import de.igor.gun.sleep.analyzer.ui.screens.tracking.views.ShowSensorInfoPanel
import de.igor.gun.sleep.analyzer.ui.screens.tracking.views.ShowStartServiceAlert
import de.igor.gun.sleep.analyzer.ui.screens.tracking.views.ShowTrackingContent
import de.igor.gun.sleep.analyzer.ui.screens.tracking.views.cancelSatisfactionDialog
import de.igor.gun.sleep.analyzer.ui.tools.hypnogram.CircularHypnogram
import java.time.LocalDateTime


@Composable
fun TrackingTab(
    serviceViewModel: ServiceViewModel,
    sensorViewModel: SensorViewModel,
) {
    val gathererViewModel = hiltViewModel<TrackingViewModel>()
    val stopRecordingShowSatisfactionDialog = rememberSaveable { mutableStateOf(false) }
    val startRecordingShowAlert = rememberSaveable { mutableStateOf(false) }
    val showRecoveryDialog = gathererViewModel.isDataInconsistent
    val showRecoveryProgress = rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    ShowNoPermissionsGrantedAlert(noPermissionsGranted = serviceViewModel.notGrantedPermissions)

    if (serviceViewModel.notGrantedPermissions.value != null) return

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (!gathererViewModel.isRecording.value) {
            gathererViewModel.checkSeriesToRepair()
        }
    }

    if (!gathererViewModel.isRecording.value) {
        ShowRepairDialog(
            shouldShowDialog = showRecoveryDialog,
            onStarted = {
                gathererViewModel.repairSeries()
                showRecoveryProgress.value = true
            },
        )

        ShowProgressRepairDialog(
            shouldShowDialog = showRecoveryProgress,
            repairState = gathererViewModel.repairState,
            onCanceled = { gathererViewModel.cancelRepairSeries() }
        )
    } else {
        ShowStartServiceAlert(shouldShowDialog = startRecordingShowAlert)
    }

    ShowSatisfactionDialog(shouldShowDialog = stopRecordingShowSatisfactionDialog) { selectedSatisfaction ->
        if (selectedSatisfaction != cancelSatisfactionDialog) {
            serviceViewModel.stopService(context)
            gathererViewModel.stopRecording(satisfaction = selectedSatisfaction.toSatisfaction())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // show sensor info & HR graph
            ShowSensorInfoPanel(
                sensorViewModel = sensorViewModel,
                gathererViewModel = gathererViewModel
            )
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // show running watch and sleep phases chart
                CircularHypnogram(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    holder = SleepPhasesHolder().apply {
                        setSleepDataPoints(
                            sleepDataPoints = gathererViewModel.sleepPhasesListState.value,
                            startTime = gathererViewModel.startTime.toLocalDateTime(),
                            endTime = LocalDateTime.now()
                        )
                    },
                    showSleepStates = true,
                    showRunner = gathererViewModel.isRecording.value,
                    invalidateRunner = gathererViewModel.hypnogramUpdateState
                ) {
                    // show PPG, HR and tracking control
                    ShowTrackingContent(
                        gathererViewModel = gathererViewModel,
                        serviceViewModel = serviceViewModel,
                        sensorViewModel = sensorViewModel,
                        stopRecordingShowSatisfactionDialog = stopRecordingShowSatisfactionDialog,
                        startRecordingShowAlert = startRecordingShowAlert,
                    )
                }
            }
        }
    }
}