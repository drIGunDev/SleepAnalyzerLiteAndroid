package de.igor.gun.sleep.analyzer.ui.tools.indicators.rssiindicator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.igor.gun.sleep.analyzer.R


@Composable
fun RSSIIndicator(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    rssiLevel: Float = -30f,
) {
    Box(
        modifier = Modifier
            .then(modifier),
        contentAlignment = Alignment.BottomCenter
    ) {
        val imageId = when (rssiLevel) {
            in -65f..0f -> R.drawable.ic_signal_strength_40
            in -75f..<-65f -> R.drawable.ic_signal_strength_70
            in -90f..<-75f -> R.drawable.ic_signal_strength_90
            else -> R.drawable.ic_signal_strength_off
        }
        Icon(
            painter = painterResource(id = imageId),
            contentDescription = "rssi indicator",
            tint = tint,
        )
    }
}

@Composable
@Preview(showBackground = true)
fun RSSIIndicatorPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RSSIIndicator(
                modifier = Modifier.size(24.dp, 24.dp),
                rssiLevel = -70f,
            )
        }
    }
}