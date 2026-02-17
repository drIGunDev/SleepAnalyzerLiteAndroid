package de.igor.gun.sleep.analyzer.ui.tools.hypnogram

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.igor.gun.sleep.analyzer.R
import de.igor.gun.sleep.analyzer.misc.millisToDurationCompact
import de.igor.gun.sleep.analyzer.repositories.tools.HypnogramHolder
import de.igor.gun.sleep.analyzer.repositories.tools.computeDistribution


@Composable
fun LinearHypnogram(
    modifier: Modifier = Modifier,
    builder: HypnogramHolder,
) {
    val distribution = builder
        .buildSleepSegments()
        .computeDistribution()

    if (!distribution.isValid) return

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(
            modifier = Modifier.then(modifier)
        ) {
            builder.setWidth(size.width)
            builder.setHeight(size.height)
            val path = builder.buildPath()
            val brush = builder.buildBrush()
            clipRect(left = 0f, top = 0f, right = size.width, bottom = size.height) {
                drawPath(path, brush, size.height)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val colorMap = HypnogramHolder.SleepState.toColorMap()

            if (distribution.absolutMillis.isEmpty()) return@Row
            distribution.relative().forEach {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    val label = getSleepPhaseName(it.key)
                    Text(text = label, color = colorMap[it.key]!!, style = MaterialTheme.typography.labelSmall)
                    Text("%3.0f".format(it.value * 100) + "%", color = colorMap[it.key]!!, style = MaterialTheme.typography.labelSmall)
                    val absoluteMillis = distribution.absolutMillis[it.key] ?: 0f
                    Text(millisToDurationCompact(absoluteMillis), color = colorMap[it.key]!!, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun getSleepPhaseName(sleepPhase: HypnogramHolder.SleepState): String {
    return when (sleepPhase) {
        HypnogramHolder.SleepState.AWAKE -> stringResource(R.string.sleep_phase_awake)
        HypnogramHolder.SleepState.LIGHT_SLEEP -> stringResource(R.string.sleep_phase_l_sleep)
        HypnogramHolder.SleepState.DEEP_SLEEP -> stringResource(R.string.sleep_phase_d_sleep)
        HypnogramHolder.SleepState.REM -> stringResource(R.string.sleep_phase_rem)
    }
}