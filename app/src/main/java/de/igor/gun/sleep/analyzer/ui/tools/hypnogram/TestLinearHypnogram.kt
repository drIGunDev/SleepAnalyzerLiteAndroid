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
import de.igor.gun.sleep.analyzer.repositories.tools.SleepSegment
import de.igor.gun.sleep.analyzer.ui.theme.MainBackgroundColor
import timber.log.Timber


@Composable
@Preview(showBackground = true)
fun UseHypnogram() {
    val hour = 60 * 60L
    fun duration(durationHours: Long) = durationHours * hour
    fun time(timeHours: Long) = timeHours * hour * 1000
    val sleepDataPoints = listOf(
        SleepSegment(time(0), duration(1), HypnogramHolder.SleepState.AWAKE),
        SleepSegment(time(1), duration(1), HypnogramHolder.SleepState.LIGHT_SLEEP),
        SleepSegment(time(2), duration(2), HypnogramHolder.SleepState.DEEP_SLEEP),
        SleepSegment(time(4), duration(1), HypnogramHolder.SleepState.REM),
        SleepSegment(time(5), duration(1), HypnogramHolder.SleepState.AWAKE),
    )
    Timber.d("sleepDataPoints: $sleepDataPoints")
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
                builder = HypnogramHolder().apply {
                    setSleepSegments(sleepDataPoints)
                }
            )
        }
    }
}