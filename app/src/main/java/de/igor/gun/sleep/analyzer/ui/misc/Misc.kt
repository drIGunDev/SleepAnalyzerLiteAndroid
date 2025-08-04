package de.igor.gun.sleep.analyzer.ui.misc

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class PaddingElement(
    private var start: Dp = 0.dp,
    private var top: Dp = 0.dp,
    private var end: Dp = 0.dp,
    private var bottom: Dp = 0.dp,
) {
    fun toModifier() = Modifier.padding(start = start, top = top, end = end, bottom = bottom)
}