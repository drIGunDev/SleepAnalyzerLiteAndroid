package de.igor.gun.sleep.analyzer.ui.tools.indicators.ppgviewer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import de.igor.gun.sleep.analyzer.services.sensors.PPGSource
import de.igor.gun.sleep.analyzer.ui.theme.ConstructionColor
import de.igor.gun.sleep.analyzer.ui.theme.LightBlue
import de.igor.gun.sleep.analyzer.ui.theme.MainBackgroundColor


@Composable
fun PPGIndicator(
    ppgSource: PPGSource,
    modifier: Modifier = Modifier,
    gradientColorStart: Color = ConstructionColor,
    gradientColorEnd: Color = MainBackgroundColor,
    xResolution: Int = 1,
    showStream: Boolean = true,
) {
    DisposableEffect(Unit) {
        onDispose {
            ppgSource.stopStreaming()
        }
    }

    Box(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (!showStream) return@Box

        ppgSource.startStreaming()
        val ppgChunkState = ppgSource.ppgFlow.collectAsState(initial = null)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            ppgSource.setHeight(canvasHeight)
            ppgSource.setMaxBufferSize(canvasWidth.toInt())
            ppgSource.setXResolution(xResolution)

            clipRect(left = 0f, top = 0f, right = canvasWidth, bottom = canvasHeight) {
                drawPath(ppgChunkState, canvasHeight, gradientColorStart, gradientColorEnd)
                drawLine(ppgChunkState, gradientColorStart)
            }
        }
    }
}

private fun DrawScope.drawPath(
    ppgChunkState: State<PPGSource.PPGChunk?>,
    canvasHeight: Float,
    gradientColorStart: Color = Color.Blue,
    gradientColorEnd: Color = LightBlue,
) {
    ppgChunkState.value?.let {
        synchronized(ppgChunkState) {
            if (it.data.isEmpty()) return
            val path = Path()
            path.moveTo(0f, canvasHeight)
            for (i in 0 until it.data.size - 1) {
                path.lineTo(it.data[i + 1].x, it.data[i + 1].y)
            }
            path.lineTo(it.data.last().x, canvasHeight)
            path.close()
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    listOf(gradientColorStart, gradientColorEnd),
                    start = Offset.Zero,
                    end = Offset(0f, canvasHeight)
                ),
            )
        }
    }
}

private fun DrawScope.drawLine(
    ppgChunkState: State<PPGSource.PPGChunk?>,
    color: Color,
) {
    ppgChunkState.value?.let {
        synchronized(ppgChunkState) {
            if (it.data.isEmpty()) return
            var firstPoint = Offset(it.data[0].x, it.data[0].y)
            for (i in 0 until it.data.size - 1) {
                val secondPoint = Offset(it.data[i + 1].x, it.data[i + 1].y)
                drawLine(
                    start = firstPoint,
                    end = secondPoint,
                    color = color
                )
                firstPoint = secondPoint
            }
        }
    }
}