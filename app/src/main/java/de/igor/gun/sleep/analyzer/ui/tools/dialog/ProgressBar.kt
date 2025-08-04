package de.igor.gun.sleep.analyzer.ui.tools.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.igor.gun.sleep.analyzer.ui.misc.PaddingElement


@Composable
@Preview(showBackground = true)
fun ProgressBar(
    showProgressBar: MutableState<Boolean> = rememberSaveable { mutableStateOf(true) },
    progressText: MutableState<String> = rememberSaveable { mutableStateOf("Progress: 50%") },
    progress: MutableFloatState = rememberSaveable { mutableFloatStateOf(0.5f) },
    padding: PaddingElement = PaddingElement(0.dp, 70.dp, 0.dp, 0.dp),
) {
    if (showProgressBar.value) {
        Column(
            modifier = Modifier
                .then(padding.toModifier())
                .fillMaxWidth(),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            if (progressText.value.isNotEmpty()) {
                Text(progressText.value, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
            }
            LinearProgressIndicator(
                progress = { progress.floatValue },
            )
        }
    }
}