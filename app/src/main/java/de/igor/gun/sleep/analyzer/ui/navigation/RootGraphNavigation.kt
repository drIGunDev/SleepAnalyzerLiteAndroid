package de.igor.gun.sleep.analyzer.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import de.igor.gun.sleep.analyzer.ui.screens.archive.ArchivePreviewTab
import de.igor.gun.sleep.analyzer.ui.screens.archive.detail.ArchiveDetailScreen
import de.igor.gun.sleep.analyzer.ui.screens.archive.detail.Detail
import de.igor.gun.sleep.analyzer.ui.screens.report.ReportTab
import de.igor.gun.sleep.analyzer.ui.screens.settings.SettingsTab
import de.igor.gun.sleep.analyzer.ui.screens.tracking.TrackingTab
import de.igor.gun.sleep.analyzer.ui.screens.tracking.model.SensorViewModel
import de.igor.gun.sleep.analyzer.ui.screens.tracking.model.ServiceViewModel


@Composable
fun RootGraphNavigation(
    serviceViewModel: ServiceViewModel,
    sensorViewModel: SensorViewModel,
) {
    val navController = rememberNavController()

    Scaffold(
        topBar = { TopBar(navController = navController) },
        bottomBar = { NavBar(navController = navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = RootGraph.Tracking,
            modifier = Modifier.padding(padding)
        ) {
            composable<RootGraph.Tracking> {
                TrackingTab(
                    serviceViewModel = serviceViewModel,
                    sensorViewModel = sensorViewModel,
                )
            }
            composable<RootGraph.Report> { ReportTab(navController) }
            composable<RootGraph.Archive> { ArchivePreviewTab(navController) }
            composable<RootGraph.Settings> { SettingsTab(navController) }
            composable<Detail> {
                val detail = it.toRoute<Detail>()
                ArchiveDetailScreen(navController, detail.seriesId)
            }
        }
    }
}