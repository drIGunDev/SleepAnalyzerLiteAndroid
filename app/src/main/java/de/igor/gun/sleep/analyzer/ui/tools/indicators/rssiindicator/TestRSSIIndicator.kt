package de.igor.gun.test.hilt.indicators.rssiindicator

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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.igor.gun.sleep.analyzer.ui.tools.indicators.rssiindicator.RSSIIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
@Preview(showBackground = true)
fun TestRSSIIndicator(innerPadding: PaddingValues = PaddingValues.Absolute()) {
    var rssiLevel by rememberSaveable { mutableFloatStateOf(0f) }
    var isSensorRunning by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isSensorRunning) {
        if (!isSensorRunning) return@LaunchedEffect
        scope.launch(Dispatchers.IO) {
            rssiLevel = 0f
            while (isSensorRunning) {
                delay(500)
                rssiLevel -= 10f
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
        RSSIIndicator(
            modifier = Modifier.size(25.dp, 25.dp),
            rssiLevel = rssiLevel
        )
        Spacer(Modifier.size(10.dp))
        Button(onClick = { isSensorRunning = !isSensorRunning }) {
            Text(text = if (isSensorRunning) "Stop Counter" else "Start Counter")
        }
    }
}
