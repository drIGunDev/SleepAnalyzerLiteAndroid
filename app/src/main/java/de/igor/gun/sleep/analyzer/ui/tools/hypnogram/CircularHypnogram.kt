package de.igor.gun.sleep.analyzer.ui.tools.hypnogram


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import de.igor.gun.sleep.analyzer.repositories.tools.SleepPhasesHolder
import de.igor.gun.sleep.analyzer.repositories.tools.SleepPhasesHolder.Companion.BAR_THICKNESS
import de.igor.gun.sleep.analyzer.repositories.tools.SleepPhasesHolder.Companion.FONT_SIZE
import de.igor.gun.sleep.analyzer.repositories.tools.SleepPhasesHolder.Companion.H_MARKER_SIZE
import de.igor.gun.sleep.analyzer.repositories.tools.SleepPhasesHolder.Companion.INNER_GAP
import de.igor.gun.sleep.analyzer.ui.theme.AWAKEColor
import de.igor.gun.sleep.analyzer.ui.theme.ConstructionColor
import de.igor.gun.sleep.analyzer.ui.theme.MainWhiteColor
import de.igor.gun.sleep.analyzer.ui.theme.Yellow
import de.igor.gun.sleep.analyzer.ui.tools.calendar.drawPoint
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun CircularHypnogram(
    holder: SleepPhasesHolder,
    modifier: Modifier = Modifier,
    showRunner: Boolean = true,
    invalidateRunner: MutableState<Boolean> = mutableStateOf(false),
    showSleepStates: Boolean = true,
    context: @Composable (Modifier) -> Unit = {},
) {
    val w = remember { mutableStateOf(0.dp) }
    val runnerPosition = rememberSaveable { mutableFloatStateOf(0f) }

    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .then(modifier)
            .onSizeChanged {
                with(density) {
                    w.value = it.width.toDp()
                }
            }
            .height(w.value),
        contentAlignment = Alignment.Center
    ) {
        val textMeasurer = rememberTextMeasurer()
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val radius = size.minDimension / 2
            drawSleepPhases(radius, holder, showSleepStates)
            drawDial(radius, textMeasurer)
            if (showRunner) drawRunner(runnerPosition, radius, invalidateRunner)
        }
        context(modifier)
    }
}

private fun DrawScope.drawDial(
    radius: Float,
    textMeasurer: TextMeasurer,
) {
    val outerRadius = computeMeasurementsRadius(radius)
    drawCircle(
        ConstructionColor,
        radius = radius,
        style = Stroke(
            SleepPhasesHolder.DIAL_THICKNESS.toPx(),
        )
    )
    val innerRadius = computeInnerRadius(radius)
    drawCircle(
        ConstructionColor,
        radius = innerRadius,
        style = Stroke(
            SleepPhasesHolder.DIAL_THICKNESS.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
        )
    )
    val angleStep = 360 / 12
    var currentAngle = -90

    for (i in 0..11) {
        val x = cos(Math.toRadians(currentAngle.toDouble())) * outerRadius + size.center.x
        val y = sin(Math.toRadians(currentAngle.toDouble())) * outerRadius + size.center.y

        if (i % 3 == 0) {
            val text = if (i == 0) "12" else i.toString()
            val textLayoutResult = textMeasurer.measure(
                text = text,
                style = TextStyle(color = MainWhiteColor, fontSize = FONT_SIZE, fontFamily = FontFamily.Serif)
            )
            val r2 = 0.dp.toPx()
            val x2 = cos(Math.toRadians(currentAngle.toDouble())) * (outerRadius + r2) + size.center.x
            val y2 = sin(Math.toRadians(currentAngle.toDouble())) * (outerRadius + r2) + size.center.y
            drawText(
                textLayoutResult = textLayoutResult,
                color = MainWhiteColor,
                Offset(
                    x2.toFloat() - textLayoutResult.size.width / 2,
                    y2.toFloat() - textLayoutResult.size.height / 2
                ),
            )
        } else {
            val ovalSize = H_MARKER_SIZE.toPx()
            drawOval(
                ConstructionColor,
                Offset(x.toFloat() - ovalSize, y.toFloat() - ovalSize),
                size = Size(ovalSize * 2, ovalSize * 2)
            )
        }
        currentAngle += angleStep
    }
}

private fun DrawScope.drawSleepPhases(
    radius: Float,
    holder: SleepPhasesHolder,
    showSleepStates: Boolean,
) {
    val segments = holder.buildSegments()
    if (segments.isEmpty()) return

    val measurementsRadius = computeMeasurementsRadius(radius)

    var startAngle: Float
    var duration = 0f
    val hourMillis = 60 * 1000 * 2

    for (segment in segments) {
        startAngle = segment.startAngle
        val color: Color = if (showSleepStates) {
            SleepPhasesHolder.colorMap[segment.state]!!
        } else {
            MainWhiteColor
        }
        val partDuration = segment.duration / hourMillis
        drawArc(
            startAngle = startAngle,
            duration = partDuration,
            color = color,
            radius = measurementsRadius
        )
        startAngle += partDuration
        duration += partDuration
    }
    drawCap(
        segments[0].startAngle,
        duration,
        SleepPhasesHolder.colorMap[segments.first().state] ?: AWAKEColor,
        SleepPhasesHolder.colorMap[segments.last().state] ?: AWAKEColor,
        measurementsRadius
    )
}

private fun DrawScope.drawArc(
    startAngle: Float,
    duration: Float,
    color: Color,
    radius: Float,
) {
    val size = Size(radius * 2, radius * 2)
    val topLeft = Offset((this.size.width - size.width) / 2, (this.size.height - size.height) / 2)
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = duration,
        useCenter = false,
        topLeft = topLeft,
        size = size,
        style = Stroke(width = BAR_THICKNESS.toPx(), cap = StrokeCap.Butt),
        blendMode = BlendMode.Lighten
    )
}

private fun DrawScope.drawCap(
    angle: Float,
    duration: Float,
    colorStart: Color,
    colorEnd: Color,
    radius: Float,
) {
    drawDot(angle, colorStart, radius)
    drawDot(angle + duration, colorEnd, radius)
}

private fun DrawScope.drawDot(
    angle: Float,
    color: Color,
    radius: Float,
) {
    val x = center.x + cos(Math.toRadians(angle.toDouble())) * radius
    val y = center.y + sin(Math.toRadians(angle.toDouble())) * radius
    drawCircle(
        color = color,
        radius = BAR_THICKNESS.toPx() / 2,
        center = Offset(x.toFloat(), y.toFloat())
    )
}

private fun DrawScope.drawRunner(
    runnerPosition: MutableFloatState,
    radius: Float,
    invalidateRunner: MutableState<Boolean>,
) {
    val angle = (runnerPosition.floatValue - 90f).toDouble()
    val innerRadius = computeInnerRadius(radius)
    val x = center.x + cos(Math.toRadians(angle)) * innerRadius
    val y = center.y + sin(Math.toRadians(angle)) * innerRadius
    drawPoint(
        color = Yellow,
        point = Offset(x.toFloat(), y.toFloat()),
    )
    if (invalidateRunner.value) runnerPosition.floatValue += 30f
    invalidateRunner.value = false
}

private fun DrawScope.computeMeasurementsRadius(radius: Float) = radius - BAR_THICKNESS.toPx() / 2 - INNER_GAP.toPx()
private fun DrawScope.computeInnerRadius(radius: Float) = computeMeasurementsRadius(radius) - BAR_THICKNESS.toPx() - INNER_GAP.toPx() / 2