package de.igor.gun.sleep.analyzer.ui.tools.indicators.batteryindicator

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Yellow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
@Preview(showBackground = true)
fun TestBatteryIndicator(innerPadding: PaddingValues = PaddingValues.Absolute()) {
    var isCharging by rememberSaveable { mutableStateOf(false) }
    var state by rememberSaveable { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isCharging) {
        if (!isCharging) return@LaunchedEffect
        scope.launch(Dispatchers.IO) {
            state = 0
            while (isCharging && state < 100) {
                delay(80)
                state++
            }
            isCharging = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BatteryIndicator(
            modifier = Modifier
                .size(15.dp, 40.dp)
                .border(width = 0.dp, color = Color.Black),
            state = state,
            params = BatteryIndicatorParams().copy(backgroundColor = Yellow, chargingColor = Green)
        )
        Spacer(Modifier.size(10.dp))
        Button(onClick = { isCharging = !isCharging }) {
            Text(text = if (isCharging) "Stop Test" else "Start Test")
        }
    }
}