package de.igor.gun.sleep.analyzer.ui.tools.indicators.batteryindicator

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


data class BatteryIndicatorParams(
    val backgroundColor: Color = Color.White,
    val chargingColor: Color = Color.Green,
    val lowMarkColor: Color = Color.Red
)

@Composable
fun BatteryIndicator(
    modifier: Modifier = Modifier,
    state: Int = 20,
    params: BatteryIndicatorParams = BatteryIndicatorParams()
) {
    Box(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
            .drawWithCache {
                onDrawBehind {
                    drawRect(
                        color = params.backgroundColor,
                        size = size
                    )
                    val levelPoint = size.height * (1f - state / 100f)
                    val color = if (state <= 20) params.lowMarkColor else params.chargingColor
                    drawRect(
                        color = color,
                        topLeft = Offset(x = 0f, y = levelPoint),
                        size = Size(width = size.width, height = size.height - levelPoint)
                    )
                }
            }
    )
}

@Composable
@Preview(showBackground = true)
fun UseBatteryIndicator() {
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BatteryIndicator(
                modifier = Modifier.size(15.dp, 40.dp).border(width = 0.dp, color = Color.Black),
                state = 20
            )
        }
    }
}