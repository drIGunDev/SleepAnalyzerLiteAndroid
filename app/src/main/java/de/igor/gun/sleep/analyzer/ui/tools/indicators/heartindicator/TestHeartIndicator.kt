package de.igor.gun.sleep.analyzer.ui.tools.indicators.heartindicator

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
@Preview(showBackground = true)
fun TestHeartIndicator(innerPadding: PaddingValues = PaddingValues.Absolute()) {
    var heartCounter by rememberSaveable { mutableIntStateOf(0) }
    var isHeartCounterRunning by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isHeartCounterRunning) {
        if (!isHeartCounterRunning) return@LaunchedEffect
        scope.launch(Dispatchers.IO) {
            while (isHeartCounterRunning) {
                delay(800)
                heartCounter++
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeartIndicator(
            modifier = Modifier.size(25.dp, 25.dp),
            pulse = heartCounter
        )
        Spacer(Modifier.size(10.dp))
        Button(onClick = { isHeartCounterRunning = !isHeartCounterRunning }) {
            Text(text = if (isHeartCounterRunning) "Stop Counter" else "Start Counter")
        }
    }
}
