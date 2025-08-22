package de.igor.gun.sleep.analyzer.ui.screens.report

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import de.igor.gun.sleep.analyzer.R
import de.igor.gun.sleep.analyzer.repositories.tools.CalendarChartHolder
import de.igor.gun.sleep.analyzer.ui.misc.ShowProgressBar
import de.igor.gun.sleep.analyzer.ui.misc.viewModel
import de.igor.gun.sleep.analyzer.ui.screens.report.model.ReportViewModel
import de.igor.gun.sleep.analyzer.ui.theme.AWAKEColor
import de.igor.gun.sleep.analyzer.ui.theme.DSLEEPColor
import de.igor.gun.sleep.analyzer.ui.theme.LSLEEPColor
import de.igor.gun.sleep.analyzer.ui.theme.MainWhiteColor
import de.igor.gun.sleep.analyzer.ui.theme.REMColor
import de.igor.gun.sleep.analyzer.ui.tools.calendar.CalendarChart


@Composable
fun ReportTab() {
    val viewModel = viewModel<ReportViewModel>()

    val hbrHolder by viewModel.hbrHolder
    val hypnogramHolder by viewModel.hypnogramHolder
    val hypnogramPercentageHolder by viewModel.hypnogramPercentageHolder

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (hbrHolder == null) {
            viewModel.requestHBR()
        }
        if (hypnogramHolder == null) {
            viewModel.requestHypnogram()
        }
        if (hypnogramPercentageHolder == null) {
            viewModel.requestPercentageHypnogram()
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.cleanReport()
    }

    var awake by rememberSaveable { mutableStateOf(true) }
    var deepSleep by rememberSaveable { mutableStateOf(true) }
    var lowSleep by rememberSaveable { mutableStateOf(true) }
    var rem by rememberSaveable { mutableStateOf(true) }
    val showHBR = rememberSaveable { mutableStateOf(true) }
    val showHypnogram = rememberSaveable { mutableStateOf(false) }
    val showHypnogramPercentage = rememberSaveable { mutableStateOf(false) }
    val invalidate = rememberSaveable { mutableStateOf(false) }

    if (hbrHolder == null ||
        hypnogramHolder == null ||
        hypnogramPercentageHolder == null
    ) {
        ShowProgressBar()
        return
    }

    val holderList = remember { mutableStateListOf(hbrHolder!!, hypnogramHolder!!, hypnogramPercentageHolder!!) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CalendarChart(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 16.dp),
            chartHolders = holderList,
            periodTypes = listOf(
                CalendarChartHolder.PeriodType.DAY,
                CalendarChartHolder.PeriodType.WEEK,
                CalendarChartHolder.PeriodType.MONTH
            ),
            visibleLines = listOf(awake, lowSleep, deepSleep, rem),
            activeHolderIndex = rememberSaveable { mutableIntStateOf(0) },
            invalidate = invalidate
        ) { _, activeHolder ->
            Row(verticalAlignment = Alignment.Top) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = showHBR.value,
                            onClick = {
                                showHBR.value = true
                                showHypnogram.value = false
                                showHypnogramPercentage.value = false
                                activeHolder.value = 0
                            }
                        )
                        val hbrText = stringResource(R.string.report_hbr)
                        Text(hbrText, color = MainWhiteColor)
                        RadioButton(
                            selected = showHypnogram.value,
                            onClick = {
                                showHypnogram.value = true
                                showHypnogramPercentage.value = false
                                showHBR.value = false
                                activeHolder.value = 1
                            }
                        )
                        val hypnogramHText = stringResource(R.string.report_hypnogram_h)
                        Text(hypnogramHText, color = MainWhiteColor)

                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = showHypnogramPercentage.value,
                            onClick = {
                                showHypnogramPercentage.value = true
                                showHypnogram.value = false
                                showHBR.value = false
                                activeHolder.value = 2
                            }
                        )
                        val hypnogramPText = stringResource(R.string.report_hypnogram_p)
                        Text(hypnogramPText, color = MainWhiteColor)
                    }
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
                                        onCheckedChange = { awake = !awake; invalidate.value = true }
                                    )
                                    val awakeText = stringResource(R.string.sleep_phase_awake)
                                    Text(awakeText, color = AWAKEColor, style = MaterialTheme.typography.labelSmall)
                                }
                                Row(modifier = Modifier.height(rowHeight), verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = lowSleep,
                                        onCheckedChange = { lowSleep = !lowSleep; invalidate.value = true }
                                    )
                                    val lowSleepText = stringResource(R.string.sleep_phase_l_sleep)
                                    Text(lowSleepText, color = LSLEEPColor, style = MaterialTheme.typography.labelSmall)
                                }
                                Row(modifier = Modifier.height(rowHeight), verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = deepSleep,
                                        onCheckedChange = { deepSleep = !deepSleep; invalidate.value = true }
                                    )
                                    val deepSleepText = stringResource(R.string.sleep_phase_d_sleep)
                                    Text(deepSleepText, color = DSLEEPColor, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Row(modifier = Modifier.height(rowHeight), verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = rem,
                                        onCheckedChange = { rem = !rem; invalidate.value = true }
                                    )
                                    val remText = stringResource(R.string.sleep_phase_rem)
                                    Text(remText, color = REMColor, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}