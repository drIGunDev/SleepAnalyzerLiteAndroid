package de.igor.gun.sleep.analyzer.ui.tools.indicators.ppgviewer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import de.igor.gun.sleep.analyzer.services.sensors.SensorDataSource
import de.igor.gun.sleep.analyzer.ui.theme.MainBackgroundColor
import kotlinx.coroutines.isActive

object PPGViewerV2Config {
    const val COLLECTION_PERIOD_SEC = 3.0
    const val DIMMING_FACTOR = 0.2
}

data class PPGIndicatorV2Style(
    val color: Color = Color.Green.copy(alpha = 0.8f),
    val lineWidth: Float = 2f,
    val fillEnabled: Boolean = true,
    val gradientEndColor: Color = MainBackgroundColor,
    val gridNx: Int = 7,
    val gridNy: Int = 4,
    val gridColor: Color = Color.Green.copy(alpha = 0.5f),
    val gridLineWidth: Float = 0.5f,
)

@Composable
fun PPGIndicator(
    sensorDataSource: SensorDataSource,
    modifier: Modifier = Modifier,
    style: PPGIndicatorV2Style = PPGIndicatorV2Style(),
) {
    val density = LocalDensity.current.density
    val chunkCollector = remember { ChunkCollector() }
    val frameSystem = remember { ParticleFrameSystem() }
    var frameTickNanos by remember { mutableLongStateOf(0L) }

    // Collect PPG data and feed into ChunkCollector
    LaunchedEffect(sensorDataSource) {
        sensorDataSource.ppgFlow.collect { samples ->
            if (samples.isNotEmpty()) {
                chunkCollector.add(samples)
            }
        }
    }

    // Animation loop: sequential — one particle per frame, then trigger redraw
    LaunchedEffect(frameSystem) {
        frameSystem.startEnrichment(chunkCollector, this)

        while (isActive) {
            withFrameNanos { nanos ->
                val now = System.nanoTime() / 1_000_000_000.0
                frameSystem.update(now)
                frameTickNanos = nanos
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            frameSystem.stopEnrichment()
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .onSizeChanged { size ->
                // Use dp (like iOS points) for interpolation interval
                // so particle count matches iOS and draw speed is ~1.7 sec
                frameSystem.setInterpolationInterval(size.width.toFloat() / density)
            }
    ) {
        // Read frameTickNanos to trigger redraw on every animation frame
        @Suppress("UNUSED_EXPRESSION")
        frameTickNanos

        if (!frameSystem.isEnrichmentInProgress) return@Canvas

        drawGrid(style)

        val now = System.nanoTime() / 1_000_000_000.0
        val scale = density // scale particle x from dp to pixels

        for (frame in frameSystem.frames) {
            var lastParticle: Particle? = null
            for ((index, particle) in frame.particles.withIndex()) {
                val opacity = (1.0 - (now - particle.creationDate) * PPGViewerV2Config.DIMMING_FACTOR)
                    .coerceIn(0.0, 1.0)
                    .toFloat()

                if (opacity > 0f) {
                    drawParticleSegment(index, lastParticle, particle, opacity, style, scale)
                }
                lastParticle = particle
            }
        }
    }
}

private fun DrawScope.drawParticleSegment(
    index: Int,
    lastParticle: Particle?,
    particle: Particle,
    opacity: Float,
    style: PPGIndicatorV2Style,
    scale: Float,
) {
    if (lastParticle == null) return

    val x0 = (index - 1).toFloat() * scale
    val x1 = index.toFloat() * scale
    val y0 = ((1 - lastParticle.y) * size.height).toFloat()
    val y1 = ((1 - particle.y) * size.height).toFloat()

    val color = style.color.copy(alpha = style.color.alpha * opacity)

    drawLine(
        color = color,
        start = Offset(x0, y0),
        end = Offset(x1, y1),
        strokeWidth = style.lineWidth,
    )

    if (style.fillEnabled) {
        val path = Path().apply {
            moveTo(x0, y0)
            lineTo(x1, y1)
            lineTo(x1, size.height)
            lineTo(x0, size.height)
            close()
        }
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    style.color.copy(alpha = 0.7f * opacity),
                    style.gradientEndColor.copy(alpha = 0f),
                ),
                startY = 0f,
                endY = size.height,
            ),
        )
    }
}

private fun DrawScope.drawGrid(style: PPGIndicatorV2Style) {
    val nx = style.gridNx
    val ny = style.gridNy
    val w = size.width
    val h = size.height
    val dx = w / (nx - 1).coerceAtLeast(1)
    val dy = h / (ny - 1).coerceAtLeast(1)
    val gridColor = style.gridColor
    val strokeStyle = Stroke(width = style.gridLineWidth)

    for (i in 1 until nx - 1) {
        val x = i * dx
        drawLine(gridColor, Offset(x, 0f), Offset(x, h), style.gridLineWidth)
    }

    for (i in 1 until ny - 1) {
        val y = i * dy
        drawLine(gridColor, Offset(0f, y), Offset(w, y), style.gridLineWidth)
    }

    drawRect(
        color = gridColor,
        topLeft = Offset.Zero,
        size = Size(w, h),
        style = strokeStyle,
    )
}
