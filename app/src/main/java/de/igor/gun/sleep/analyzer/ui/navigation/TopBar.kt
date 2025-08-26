package de.igor.gun.sleep.analyzer.ui.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import de.igor.gun.sleep.analyzer.R
import de.igor.gun.sleep.analyzer.misc.AppParameters
import de.igor.gun.sleep.analyzer.misc.MemoryRetrieveMethod
import de.igor.gun.sleep.analyzer.misc.ShowUsedMemory


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navController: NavHostController,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    TopAppBar(
        title = {
            Row {
                Text(stringResource(R.string.brand_name))
                if (AppParameters(navController.context).isDebugVersion) {
                    Spacer(Modifier.width(10.dp))
                    ShowUsedMemory(retrieveMethod = MemoryRetrieveMethod.DEBUG_RSS)
                }
            }
        },
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
            )
        }
    )
}