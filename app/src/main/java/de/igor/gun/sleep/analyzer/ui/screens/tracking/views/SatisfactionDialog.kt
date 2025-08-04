package de.igor.gun.sleep.analyzer.ui.screens.tracking.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.igor.gun.sleep.analyzer.R
import de.igor.gun.sleep.analyzer.db.entities.Series
import de.igor.gun.sleep.analyzer.ui.theme.DialogBackground
import de.igor.gun.sleep.analyzer.ui.tools.dialog.ActionScope
import de.igor.gun.sleep.analyzer.ui.tools.dialog.DialogActionButtons
import de.igor.gun.sleep.analyzer.ui.tools.dialog.DialogTitle


const val cancelSatisfactionDialog = -1

@Composable
fun ShowSatisfactionDialog(
    modifier: Modifier = Modifier,
    shouldShowDialog: MutableState<Boolean>,
    onDismissDialog: () -> Unit = { shouldShowDialog.value = false },
    selectHandler: (Int) -> Unit = {},
) {
    val satisfactionsEmoji = listOf(
        Series.Satisfaction.BAD.toEmoji(),
        Series.Satisfaction.NEUTRAL.toEmoji(),
        Series.Satisfaction.GOOD.toEmoji()
    )

    if (!shouldShowDialog.value) return

    Dialog(onDismissRequest = { onDismissDialog() }) {
        Surface(
            color = DialogBackground,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .then(modifier)
                    .padding(start = 20.dp, end = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val title = stringResource(R.string.sleep_satisfaction_title)
                DialogTitle(title)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, top = 20.dp)
                ) {
                    Text(text = stringResource(R.string.sleep_rating_acquire_text), style = MaterialTheme.typography.labelMedium)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for ((index, emoji) in satisfactionsEmoji.withIndex()) {
                        Text(
                            modifier = Modifier.clickable {
                                onDismissDialog()
                                selectHandler(index)
                            },
                            text = emoji,
                            fontSize = MaterialTheme.typography.headlineLarge.fontSize
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                DialogActionButtons(
                    showPositiveButton = remember { mutableStateOf(false) },
                    negativeButtonAction = ActionScope(stringResource(R.string.cancel)) {
                        selectHandler(cancelSatisfactionDialog)
                        onDismissDialog()
                    }
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun TestSatisfactionDialog() {
    Box(modifier = Modifier.fillMaxSize()) {
        val shouldShowDialog = rememberSaveable { mutableStateOf(true) }
        ShowSatisfactionDialog(shouldShowDialog = shouldShowDialog)
    }
}