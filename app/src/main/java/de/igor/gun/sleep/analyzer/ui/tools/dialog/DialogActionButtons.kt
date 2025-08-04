package de.igor.gun.sleep.analyzer.ui.tools.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.igor.gun.sleep.analyzer.R


@Composable
@Preview(showBackground = true)
fun DialogActionButtons(
    positiveButtonAction: ActionScope = ActionScope { },
    showNegativeButton: MutableState<Boolean> = rememberSaveable { mutableStateOf(true) },
    negativeButtonAction: ActionScope = ActionScope { },
    showPositiveButton: MutableState<Boolean> = rememberSaveable { mutableStateOf(true) },
) {
    val positiveTitle = stringResource(R.string.ok)
    val negativeTitle = stringResource(R.string.cancel)
    positiveButtonAction.text = positiveButtonAction.text ?: rememberSaveable { mutableStateOf(positiveTitle) }
    negativeButtonAction.text = negativeButtonAction.text ?: rememberSaveable { mutableStateOf(negativeTitle) }
    positiveButtonAction.show = showPositiveButton
    negativeButtonAction.show = showNegativeButton

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Spacer(modifier = Modifier.padding(bottom = 20.dp))
        Row {
            negativeButtonAction.Button()
            positiveButtonAction.Button()
        }
        Spacer(modifier = Modifier.padding(bottom = 20.dp))
    }
}

class ActionScope(
    var text: MutableState<String>? = null,
    private val onClick: (ActionScope) -> Unit
) {
    var show: MutableState<Boolean>? = null

    constructor(
        text: String,
        onClick: (ActionScope) -> Unit
    ) : this(mutableStateOf(text), onClick)

    @Composable
    fun Button() {
        if (show?.value != true) return
        TextButton(onClick = { show?.let { onClick(this) } }) {
            text?.let {
                Text(text = it.value)
            }
        }
    }
}