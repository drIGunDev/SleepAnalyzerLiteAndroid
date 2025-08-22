package de.igor.gun.sleep.analyzer.ui.misc

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import de.igor.gun.sleep.analyzer.ui.navigation.RootGraph
import timber.log.Timber


@Composable
internal inline fun <reified T : RootGraph> NavHostController.isRouteInDestination(): Boolean {
    val navBackStackEntry by this.currentBackStackEntryAsState()
    return navBackStackEntry?.destinationSimpleName().toString() == T::class.java.simpleName
}

@Composable
inline fun <reified T : ViewModel> viewModel(): T {
    return hiltViewModel<T>(key = T::class.java.name)
        .also { Timber.w("${T::class.java.simpleName} --> $it") }
}

@Composable
fun formatHR(@StringRes resourceId: Int, value: Float) = String.format(stringResource(resourceId), value)

@Composable
fun formatDuration(@StringRes resourceId: Int, value: String) = String.format(stringResource(resourceId), value)

@Composable
fun ShowProgressBar() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LinearProgressIndicator(modifier = Modifier.padding(top = 50.dp, bottom = 10.dp))
    }
}

fun NavBackStackEntry.destinationSimpleName(): String? = destination.route?.split(".")?.last()

