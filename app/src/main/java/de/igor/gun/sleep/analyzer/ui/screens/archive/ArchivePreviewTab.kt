package de.igor.gun.sleep.analyzer.ui.screens.archive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import de.igor.gun.sleep.analyzer.ui.screens.archive.views.ShowSeriesList


@Composable
fun ArchivePreviewTab(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ShowSeriesList(navController = navController)
    }
}