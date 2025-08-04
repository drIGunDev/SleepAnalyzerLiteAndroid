package de.igor.gun.sleep.analyzer.ui.tools.hypnogram

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.igor.gun.sleep.analyzer.repositories.tools.HypnogramHolder
import de.igor.gun.sleep.analyzer.repositories.tools.HypnogramHolder.SleepDataPoint
import de.igor.gun.sleep.analyzer.ui.theme.MainBackgroundColor


@Composable
@Preview(showBackground = true)
fun UseHypnogram() {
    val hour = 1000 * 60 * 60L
    val sleepDataPoints = listOf(
        SleepDataPoint(0, HypnogramHolder.SleepState.AWAKE),
        SleepDataPoint(1 * hour, HypnogramHolder.SleepState.AWAKE),
        SleepDataPoint(1 * hour, HypnogramHolder.SleepState.AWAKE),
        SleepDataPoint(1 * hour, HypnogramHolder.SleepState.LIGHT_SLEEP),
        SleepDataPoint(2 * hour, HypnogramHolder.SleepState.LIGHT_SLEEP),
        SleepDataPoint(2 * hour, HypnogramHolder.SleepState.LIGHT_SLEEP),
        SleepDataPoint(2 * hour, HypnogramHolder.SleepState.DEEP_SLEEP),
        SleepDataPoint(2 * hour, HypnogramHolder.SleepState.DEEP_SLEEP),
        SleepDataPoint(3 * hour, HypnogramHolder.SleepState.DEEP_SLEEP),
        SleepDataPoint(3 * hour, HypnogramHolder.SleepState.DEEP_SLEEP),
        SleepDataPoint(3 * hour, HypnogramHolder.SleepState.REM),
        SleepDataPoint(4 * hour, HypnogramHolder.SleepState.REM),
        SleepDataPoint(4 * hour, HypnogramHolder.SleepState.REM),
    )
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MainBackgroundColor),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            LinearHypnogram(
                modifier = Modifier
                    .padding(start = 30.dp, end = 30.dp)
                    .fillMaxWidth()
                    .height(200.dp),
                builder = HypnogramHolder().apply { setUniformSleepDataPoints(sleepDataPoints) }
            )
        }
    }
}