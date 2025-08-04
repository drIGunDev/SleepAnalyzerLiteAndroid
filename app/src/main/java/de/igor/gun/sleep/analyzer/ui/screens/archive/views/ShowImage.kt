package de.igor.gun.sleep.analyzer.ui.screens.archive.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import de.igor.gun.sleep.analyzer.ui.screens.archive.detail.Detail
import de.igor.gun.sleep.analyzer.ui.screens.archive.model.SeriesWrapper


@Composable
fun ShowImage(
    navController: NavHostController,
    item: SeriesWrapper.Cached,
) {
    Image(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp, bottom = 5.dp)
            .clickable { navController.navigate(Detail(item.series.id)) },
        bitmap = item.cache.chartImage.asImageBitmap(),
        contentScale = ContentScale.FillBounds,
        contentDescription = "Series snapshot"
    )
}