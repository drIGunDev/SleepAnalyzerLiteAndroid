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
import de.igor.gun.sleep.analyzer.misc.parseLocalDateTimeToMillis
import de.igor.gun.sleep.analyzer.misc.toLocalDateTime
import de.igor.gun.sleep.analyzer.misc.toMillis
import de.igor.gun.sleep.analyzer.repositories.tools.HypnogramHolder
import de.igor.gun.sleep.analyzer.repositories.tools.SleepPhasesHolder
import de.igor.gun.sleep.analyzer.ui.theme.MainBackgroundColor
import de.igor.gun.sleep.analyzer.ui.theme.MainWhiteColor
import de.igor.gun.sleep.analyzer.ui.tools.indicators.heartindicator.HeartIndicator


@Composable
@Preview(showBackground = true)
fun UseSleepPhasesChart() {
    val startTime = "2024-02-06T01:00:00".parseLocalDateTimeToMillis().toLocalDateTime()
    val data = listOf(
        HypnogramHolder.SleepDataPoint(
            "2024-02-06T01:00:00".parseLocalDateTimeToMillis() - startTime.toMillis(),
            HypnogramHolder.SleepState.AWAKE,
        ),
        HypnogramHolder.SleepDataPoint(
            "2024-02-06T02:00:00".parseLocalDateTimeToMillis() - startTime.toMillis(),
            HypnogramHolder.SleepState.LIGHT_SLEEP,
        ),
        HypnogramHolder.SleepDataPoint(
            "2024-02-06T02:30:00".parseLocalDateTimeToMillis() - startTime.toMillis(),
            HypnogramHolder.SleepState.DEEP_SLEEP,
        ),
        HypnogramHolder.SleepDataPoint(
            "2024-02-06T03:00:00".parseLocalDateTimeToMillis() - startTime.toMillis(),
            HypnogramHolder.SleepState.DEEP_SLEEP,
        ),
        HypnogramHolder.SleepDataPoint(
            "2024-02-06T04:10:00".parseLocalDateTimeToMillis() - startTime.toMillis(),
            HypnogramHolder.SleepState.REM,
        ),
        HypnogramHolder.SleepDataPoint(
            "2024-02-06T05:10:00".parseLocalDateTimeToMillis() - startTime.toMillis(),
            HypnogramHolder.SleepState.AWAKE,
        ),
        HypnogramHolder.SleepDataPoint(
            "2024-02-06T06:10:00".parseLocalDateTimeToMillis() - startTime.toMillis(),
            HypnogramHolder.SleepState.LIGHT_SLEEP,
        ),
        HypnogramHolder.SleepDataPoint(
            "2024-02-06T07:10:00".parseLocalDateTimeToMillis() - startTime.toMillis(),
            HypnogramHolder.SleepState.DEEP_SLEEP,
        ),
        HypnogramHolder.SleepDataPoint(
            "2024-02-06T08:20:00".parseLocalDateTimeToMillis() - startTime.toMillis(),
            HypnogramHolder.SleepState.REM,
        ),
        HypnogramHolder.SleepDataPoint(
            "2024-02-06T09:00:00".parseLocalDateTimeToMillis() - startTime.toMillis(),
            HypnogramHolder.SleepState.AWAKE,
        ),
        HypnogramHolder.SleepDataPoint(
            "2024-02-06T09:20:20".parseLocalDateTimeToMillis() - startTime.toMillis(),
            HypnogramHolder.SleepState.LIGHT_SLEEP,
        ),
        HypnogramHolder.SleepDataPoint(
            "2024-02-06T09:40:30".parseLocalDateTimeToMillis() - startTime.toMillis(),
            HypnogramHolder.SleepState.AWAKE,
        )
    )

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MainBackgroundColor),
            contentAlignment = Alignment.TopCenter
        ) {
            CircularHypnogram(
                holder = SleepPhasesHolder().apply {
                    setSleepDataPoints(data, endTime = "2024-02-06T09:00:00".parseLocalDateTimeToMillis().toLocalDateTime(), startTime = startTime)
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