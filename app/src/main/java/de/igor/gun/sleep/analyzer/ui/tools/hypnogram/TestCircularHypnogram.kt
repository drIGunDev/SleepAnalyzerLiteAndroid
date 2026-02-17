package de.igor.gun.sleep.analyzer.ui.tools.hypnogram

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import de.igor.gun.sleep.analyzer.misc.parseLocalDateTimeToSeconds
import de.igor.gun.sleep.analyzer.repositories.tools.HypnogramHolder
import de.igor.gun.sleep.analyzer.repositories.tools.SleepPhase
import de.igor.gun.sleep.analyzer.repositories.tools.sleepPhases2Segments
import de.igor.gun.sleep.analyzer.ui.theme.MainBackgroundColor
import de.igor.gun.sleep.analyzer.ui.theme.MainWhiteColor
import de.igor.gun.sleep.analyzer.ui.tools.indicators.heartindicator.HeartIndicator
import timber.log.Timber


@Composable
@Preview(showBackground = true)
fun UseSleepPhasesChart() {
    val startTime = "2026-02-12T12:00:00".parseLocalDateTimeToSeconds()
    val hour = 60 * 60L
    fun duration(durationSeconds: Double) = durationSeconds * hour
    val data = listOf(
        SleepPhase(HypnogramHolder.SleepState.AWAKE, duration(1.0)),
        SleepPhase(HypnogramHolder.SleepState.LIGHT_SLEEP, duration(1.0)),
        SleepPhase(HypnogramHolder.SleepState.DEEP_SLEEP, duration(2.0)),
        SleepPhase(HypnogramHolder.SleepState.REM, duration(1.0)),
        SleepPhase(HypnogramHolder.SleepState.AWAKE, duration(1.0))
    )

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MainBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            CircularHypnogram(
                holder = HypnogramHolder().apply {
                    val segments = data.sleepPhases2Segments(startTime)
                    Timber.d("segments: $segments")
                    setSleepSegments(segments)
                },
                showSleepStates = true,
                modifier = Modifier.padding(16.dp)
            ) {
                ConstraintLayout(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val (button, text, hart) = createRefs()
                    HeartIndicator(
                        modifier = Modifier
                            .size(50.dp, 50.dp)
                            .constrainAs(hart) {
                                bottom.linkTo(text.top, margin = 16.dp)
                                centerHorizontallyTo(text)
                            },
                        pulse = 0
                    )
                    Text(
                        modifier = Modifier.constrainAs(text) {
                            centerHorizontallyTo(parent)
                            centerVerticallyTo(parent)
                        },
                        text = "60 BRM",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MainWhiteColor
                    )
                    Button(
                        modifier = Modifier
                            .constrainAs(button) {
                                top.linkTo(text.bottom, margin = 16.dp)
                                centerHorizontallyTo(text)
                            },
                        onClick = {},
                        enabled = true
                    ) {
                        Text("Start")
                    }
                }
            }
        }
    }
}