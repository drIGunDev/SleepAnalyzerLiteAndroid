package de.igor.gun.sleep.analyzer.ui.screens.report.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.igor.gun.sleep.analyzer.db.DBManager
import de.igor.gun.sleep.analyzer.db.entities.toSleepStateDistribution
import de.igor.gun.sleep.analyzer.misc.toLocalDateTime
import de.igor.gun.sleep.analyzer.misc.toMillis
import de.igor.gun.sleep.analyzer.repositories.tools.CalendarChartHolder
import de.igor.gun.sleep.analyzer.repositories.tools.CalendarChartHolder.Channel
import de.igor.gun.sleep.analyzer.repositories.tools.CalendarChartHolder.Channel.CalendarItem
import de.igor.gun.sleep.analyzer.repositories.tools.HypnogramHolder
import de.igor.gun.sleep.analyzer.ui.theme.AWAKEColor
import de.igor.gun.sleep.analyzer.ui.theme.DSLEEPColor
import de.igor.gun.sleep.analyzer.ui.theme.LSLEEPColor
import de.igor.gun.sleep.analyzer.ui.theme.REMColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min


@HiltViewModel
class ReportViewModel @Inject constructor(
    private val dbManager: DBManager,
) : ViewModel() {

    val hbrHolder = mutableStateOf<CalendarChartHolder?>(null)
    fun requestHBR() {
        viewModelScope.launch(Dispatchers.IO) {
            val results = mutableListOf<CalendarItem>()
            val cacheMap =
                dbManager
                    .getAllCache()
                    .associateBy { it.seriesId }
            dbManager
                .getAllSeries(order = DBManager.SortOrder.ASC)
                .forEach { series ->
                    val startDate = series.startDate
                    val endDate = series.endDate ?: startDate
                    val middleDate = ((startDate.toMillis() + endDate.toMillis()) / 2).toLocalDateTime()
                    cacheMap[series.id]
                        ?.let { cache ->
                            results.add(
                                CalendarItem.Bar(
                                    time = middleDate,
                                    x = middleDate.toMillis().toFloat(),
                                    min = min(cache.minHR, cache.maxHR),
                                    max = max(cache.minHR, cache.maxHR),
                                    color = Color.Red
                                )
                            )
                        }
                }
            if (results.isNotEmpty()) {
                val holder = CalendarChartHolder().apply {
                    setData(results)
                    channel.setWorld(
                        Channel.Region(
                            results.minOf { (it as CalendarItem.Bar).x },
                            results.maxOf { (it as CalendarItem.Bar).x },
                            0f,
                            results.maxOf { (it as CalendarItem.Bar).max }
                        )
                    )
                    setPeriodOfType(CalendarChartHolder.PeriodType.MONTH)
                    yAxisFormatter = { "${it.toInt()}" }
                }
                hbrHolder.value = holder
            } else {
                hbrHolder.value = CalendarChartHolder()
            }
        }
    }

    val hypnogramHolder = mutableStateOf<CalendarChartHolder?>(null)
    fun requestHypnogram() {
        viewModelScope.launch(Dispatchers.IO) {
            val results = getCalendarItems()
            flushItems(results, hypnogramHolder, formatter = { "%.2f".format(it / (60 * 60 * 1000L)) })
        }
    }

    val hypnogramPercentageHolder = mutableStateOf<CalendarChartHolder?>(null)
    fun requestPercentageHypnogram() {
        viewModelScope.launch(Dispatchers.IO) {
            val results = getCalendarItems(transformToPercentage = true)
            flushItems(results, hypnogramPercentageHolder, formatter = { "%.1f".format(it * 100) })
        }
    }

    fun cleanReport() {
        hbrHolder.value = null
        hypnogramHolder.value = null
        hypnogramPercentageHolder.value = null
    }

    private fun getCalendarItems(transformToPercentage: Boolean = false): List<CalendarItem> {
        val results = mutableListOf<CalendarItem>()
        val cacheMap =
            dbManager
                .getAllCache()
                .associateBy { it.seriesId }
        dbManager
            .getAllSeries(order = DBManager.SortOrder.ASC)
            .forEach { series ->
                cacheMap[series.id]
                    ?.let { cache ->
                        cache.toSleepStateDistribution()
                            .also {
                                val startDate = series.startDate
                                val endDate = series.endDate ?: startDate
                                val middleDate = ((startDate.toMillis() + endDate.toMillis()) / 2).toLocalDateTime()
                                if (transformToPercentage) {
                                    val relative = it.relative()
                                    val awakePercentage = Pair(AWAKEColor, relative[HypnogramHolder.SleepState.AWAKE] ?: 0f)
                                    val lightSleepPercentage = Pair(LSLEEPColor, relative[HypnogramHolder.SleepState.LIGHT_SLEEP] ?: 0f)
                                    val deepSleepPercentage = Pair(DSLEEPColor, relative[HypnogramHolder.SleepState.DEEP_SLEEP] ?: 0f)
                                    val remPercentage = Pair(REMColor, relative[HypnogramHolder.SleepState.REM] ?: 0f)
                                    results.add(
                                        CalendarItem.Graph(
                                            time = middleDate,
                                            x = middleDate.toMillis().toFloat(),
                                            listOf(awakePercentage, lightSleepPercentage, deepSleepPercentage, remPercentage)
                                        )
                                    )
                                } else {
                                    val awake = Pair(AWAKEColor, it.absolutMillis[HypnogramHolder.SleepState.AWAKE] ?: 0f)
                                    val lightSleep = Pair(LSLEEPColor, it.absolutMillis[HypnogramHolder.SleepState.LIGHT_SLEEP] ?: 0f)
                                    val deepSleep = Pair(DSLEEPColor, it.absolutMillis[HypnogramHolder.SleepState.DEEP_SLEEP] ?: 0f)
                                    val rem = Pair(REMColor, it.absolutMillis[HypnogramHolder.SleepState.REM] ?: 0f)
                                    results.add(
                                        CalendarItem.Graph(
                                            time = middleDate,
                                            x = middleDate.toMillis().toFloat(),
                                            listOf(awake, lightSleep, deepSleep, rem)
                                        )
                                    )
                                }
                            }
                    }

            }
        return results
    }

    private fun flushItems(
        items: List<CalendarItem>,
        stateHolder: MutableState<CalendarChartHolder?>,
        formatter: (Float) -> String,
    ) {
        if (items.isNotEmpty()) {
            val holder = CalendarChartHolder().apply {
                setData(items)
                channel.setWorld(
                    Channel.Region(
                        items.minOf { (it as CalendarItem.Graph).x },
                        items.maxOf { (it as CalendarItem.Graph).x },
                        0f,
                        items.maxOf { graph -> (graph as CalendarItem.Graph).points.maxOf { it.second } }
                    )
                )
                setPeriodOfType(CalendarChartHolder.PeriodType.MONTH)
                yAxisFormatter = formatter
            }
            stateHolder.value = holder
        } else {
            stateHolder.value = CalendarChartHolder()
        }
    }
}