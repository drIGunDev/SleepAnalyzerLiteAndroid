package de.igor.gun.sleep.analyzer.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import de.igor.gun.sleep.analyzer.ui.misc.destinationSimpleName


@Composable
fun NavBar(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destinationSimpleName()
    NavigationBar(modifier = modifier) {
        bottomTabs.forEach { tab ->
            NavigationBarItem(
                label = { Text(text = tab.title()) },
                icon = { Icon(painter = painterResource(id = tab.icon!!), contentDescription = tab.title()) },
                selected = currentRoute == tab::class.java.simpleName,
                onClick = { navController.navigate(tab) }
            )
        }
    }
}