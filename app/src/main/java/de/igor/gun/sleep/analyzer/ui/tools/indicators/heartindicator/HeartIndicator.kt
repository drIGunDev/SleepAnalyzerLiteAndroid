package de.igor.gun.sleep.analyzer.ui.tools.indicators.heartindicator

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.igor.gun.sleep.analyzer.R


@Composable
fun HeartIndicator(
    modifier: Modifier = Modifier,
    pulse: Int = 0,
    amplitude: Float = 0.2f,
) {
    val scale by animateFloatAsState(targetValue = if (pulse % 2 == 0) 1f else 1f - amplitude, label = "heart scale")
    Box(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_heart),
            contentDescription = "heart indicator",
            modifier = Modifier.scale(scale)
        )
    }
}

@Composable
@Preview(showBackground = true)
fun UseHeartIndicator() {
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeartIndicator(
                modifier = Modifier.size(24.dp, 24.dp),
                pulse = 0
            )
        }
    }
}