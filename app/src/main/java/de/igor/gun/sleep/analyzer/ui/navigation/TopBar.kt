package de.igor.gun.sleep.analyzer.ui.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import de.igor.gun.sleep.analyzer.R
import de.igor.gun.sleep.analyzer.ui.misc.viewModel
import de.igor.gun.sleep.analyzer.ui.screens.archive.model.ArchiveListViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navController: NavHostController,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val viewModel = navController.viewModel<ArchiveListViewModel>() ?: return

    TopAppBar(
        title = { Text(stringResource(R.string.brand_name)) },
        navigationIcon = {
            if (navBackStackEntry?.isDestinationInRootGraph() != true) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(painter = painterResource(id = R.drawable.ic_arrow_back), contentDescription = "back stack push")
                }
            }
        },
        actions = {
            ShowMenu(
                navController = navController,
                viewModel = viewModel
            )
        }
    )
}