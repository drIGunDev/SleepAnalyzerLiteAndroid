package de.igor.gun.sleep.analyzer.ui.tools.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.igor.gun.sleep.analyzer.misc.daysAgoMillis
import de.igor.gun.sleep.analyzer.misc.toLocalDateTime
import de.igor.gun.sleep.analyzer.repositories.tools.CalendarChartHolder
import de.igor.gun.sleep.analyzer.repositories.tools.CalendarChartHolder.Channel
import de.igor.gun.sleep.analyzer.repositories.tools.CalendarChartHolder.Channel.CalendarItem
import de.igor.gun.sleep.analyzer.repositories.tools.CalendarChartHolder.Companion.DAY_IN_MILLIS
import de.igor.gun.sleep.analyzer.ui.theme.AWAKEColor
import de.igor.gun.sleep.analyzer.ui.theme.DSLEEPColor
import de.igor.gun.sleep.analyzer.ui.theme.LSLEEPColor
import de.igor.gun.sleep.analyzer.ui.theme.MainBackgroundColor
import de.igor.gun.sleep.analyzer.ui.theme.MainWhiteColor
import de.igor.gun.sleep.analyzer.ui.theme.REMColor
import java.time.LocalDateTime
import kotlin.math.max
import kotlin.math.min


@Composable
@Preview(showBackground = true)
fun UseCalendarChart() {
    val worldMinX = 0
    val worldMaxX = 50
    fun generateHBR(): List<CalendarItem> {
        val startDay = LocalDateTime.now().daysAgoMillis(worldMaxX)
        return (worldMinX..<worldMaxX).map { i ->
            val x = i * DAY_IN_MILLIS + startDay //+ ((-1) * (0..1).random()) * (0..100000000).random()
            val y1 = (40..60).random().toFloat()
            val y2 = (70..160).random().toFloat()
            CalendarItem.Bar(x.toLocalDateTime(), x.toFloat(), min(y1, y2), max(y1, y2), Color.Red)
        }
    }

    fun generateHypnogram(): List<CalendarItem> {
        fun getRandom(): Float = (DAY_IN_MILLIS * (0..100).random()).toFloat() / 100
        val startDay = LocalDateTime.now().daysAgoMillis(worldMaxX)
        return (worldMinX..<worldMaxX).map { i ->
            val x = i * DAY_IN_MILLIS + startDay //+ ((-1) * (0..1).random()) * (0..100000000).random()
            val awake = Pair(AWAKEColor, getRandom())
            val lightSleep = Pair(LSLEEPColor, getRandom())
            val deepSleep = Pair(DSLEEPColor, getRandom())
            val rem = Pair(REMColor, getRandom())
            CalendarItem.Graph(x.toLocalDateTime(), x.toFloat(), listOf(awake, lightSleep, deepSleep, rem))
        }
    }

    val hBRChartBuilder = CalendarChartHolder().apply {
        val data = generateHBR()
        setData(data)
        val dataPadding = 1f
        channel.setWorld(
            Channel.Region(
                data.minOf { (it as CalendarItem.Bar).x },
                data.maxOf { (it as CalendarItem.Bar).x },
                0f,
                data.maxOf { (it as CalendarItem.Bar).max } * dataPadding
            )
        )
        setPeriodOfType(CalendarChartHolder.PeriodType.MONTH)
        yAxisFormatter = { "${it.toInt()} hr." }
    }

    val hypnoChartBuilder = CalendarChartHolder().apply {
        val data = generateHypnogram()
        setData(data)
        val dataPadding = 1f
        channel.setWorld(
            Channel.Region(
                data.minOf { (it as CalendarItem.Graph).x },
                data.maxOf { (it as CalendarItem.Graph).x },
                0f,
                data.maxOf { graph -> (graph as CalendarItem.Graph).points.maxOf { it.second } } * dataPadding
            )
        )
        setPeriodOfType(CalendarChartHolder.PeriodType.MONTH)
        yAxisFormatter = { (it / (60 * 60 * 1000L)).toString() }
    }

    var awake by rememberSaveable { mutableStateOf(true) }
    var deepSleep by rememberSaveable { mutableStateOf(true) }
    var lowSleep by rememberSaveable { mutableStateOf(true) }
    var rem by rememberSaveable { mutableStateOf(true) }
    val showHBR = rememberSaveable { mutableStateOf(true) }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MainBackgroundColor),
            contentAlignment = Alignment.Center,
        ) {
            CalendarChart(
                modifier = Modifier
                    .fillMaxSize()
//                    .width(400.dp)
//                    .height(500.dp)
                    .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 16.dp),
                chartHolders = listOf(hBRChartBuilder, hypnoChartBuilder),
                periodTypes = listOf(
                    CalendarChartHolder.PeriodType.DAY,
                    CalendarChartHolder.PeriodType.WEEK,
                    CalendarChartHolder.PeriodType.MONTH
                ),
                visibleLines = listOf(awake, deepSleep, lowSleep, rem),
                activeHolderIndex = rememberSaveable { mutableIntStateOf(0) },
            ) { _, activeHolder ->
                Row(verticalAlignment = Alignment.Top) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = showHBR.value,
                            onClick = {
                                showHBR.value = !showHBR.value
                                activeHolder.value = 0
                            }
                        )
                        Text("HBR(bpm)", color = MainWhiteColor)
                        RadioButton(
                            selected = !showHBR.value,
                            onClick = {
                                showHBR.value = !showHBR.value
                                activeHolder.value = 1
                            }
                        )
                        Text("Hypnogram (h)", color = MainWhiteColor)
                    }
                }
                AnimatedVisibility(!showHBR.value) {
                    Row {
                        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            val rowHeight = 30.dp
                            Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Row(modifier = Modifier.height(rowHeight), verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = awake,
                                            onCheckedChange = { awake = !awake }
                                        )
                                        Text("Awake", color = AWAKEColor, style = MaterialTheme.typography.labelSmall)
                                    }
                                    Row(modifier = Modifier.height(rowHeight), verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = lowSleep,
                                            onCheckedChange = { lowSleep = !lowSleep }
                                        )
                                        Text("Low sleep", color = LSLEEPColor, style = MaterialTheme.typography.labelSmall)
                                    }
                                    Row(modifier = Modifier.height(rowHeight), verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = deepSleep,
                                            onCheckedChange = { deepSleep = !deepSleep }
                                        )
                                        Text("Deap sleep", color = DSLEEPColor, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Row(modifier = Modifier.height(rowHeight), verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = rem,
                                            onCheckedChange = { rem = !rem }
                                        )
                                        Text("REM", color = REMColor, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}