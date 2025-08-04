package de.igor.gun.sleep.analyzer.ui.navigation

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import de.igor.gun.sleep.analyzer.R
import de.igor.gun.sleep.analyzer.ui.misc.destinationSimpleName
import kotlinx.serialization.Serializable


@Serializable
internal sealed class RootGraph(
    val route: String,
    val title: @Composable () -> String,
    @param:DrawableRes val icon: Int? = null,
) {
    @Serializable
    data object Tracking : RootGraph("tracking-tab", { stringResource(R.string.menu_tracking) }, R.drawable.ic_sleep_tracking)

    @Serializable
    data object Report : RootGraph("archive-debug-preview-tab", { stringResource(R.string.menu_report) }, R.drawable.ic_report)

    @Serializable
    data object Archive : RootGraph("archive-debug-preview-tab", { stringResource(R.string.menu_archive) }, R.drawable.ic_archive)

    @Serializable
    data object Settings : RootGraph("settings-tab", { stringResource(R.string.menu_settings) }, R.drawable.ic_settings_24)
}

internal val bottomTabs = listOf(RootGraph.Tracking, RootGraph.Report, RootGraph.Archive/*, RootGraph.Settings*/)

internal fun NavBackStackEntry.isDestinationInRootGraph(): Boolean = destinationSimpleName().toString() in bottomTabs.map { it::class.java.simpleName }
