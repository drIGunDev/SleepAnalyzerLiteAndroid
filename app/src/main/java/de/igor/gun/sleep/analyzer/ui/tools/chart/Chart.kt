package de.igor.gun.sleep.analyzer.ui.tools.chart

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import de.igor.gun.sleep.analyzer.repositories.tools.ChartBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.abs


@SuppressLint("ReturnFromAwaitPointerEventScope")
@Composable
fun Chart(
    modifier: Modifier = Modifier,
    builder: ChartBuilder = ChartBuilder(ChartBuilder.Screen.default()),
    invalidate: MutableState<Boolean> = rememberSaveable { mutableStateOf(true) },
    drawOffscreen: Boolean = false,
) {
    var distance by rememberSaveable { mutableFloatStateOf(0f) }
    var position by rememberSaveable { mutableFloatStateOf(0f) }
    var isMeasuringAllowed by rememberSaveable { mutableStateOf(true) }
    var lastPressTime by rememberSaveable { mutableLongStateOf(0L) }
    val bitmap = remember { mutableStateOf<ImageBitmap?>(null) }
    val scope = rememberCoroutineScope()
    var isBitmapBuildBlocked by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    if (drawOffscreen) {
        LaunchedEffect(invalidate.value) {
            if (isBitmapBuildBlocked) return@LaunchedEffect
            scope.launch(Dispatchers.IO) {
                synchronized(this) {
                    try {
                        isBitmapBuildBlocked = true
                        bitmap.value = builder.buildBitmap().asImageBitmap()
                    } catch (e: Exception) {
                        Timber.e("Error while building bitmap: $e")
                    } finally {
                        isBitmapBuildBlocked = false
                    }
                }
            }
        }
    }

    Canvas(
        modifier = Modifier
            .then(modifier)
            .onSizeChanged {
                builder.screen.width = it.width
                builder.screen.height = it.height
                builder.screen.density = context.resources.displayMetrics.density
                builder.resetXScale()
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {

                    fun closedPosition(lastPosition: Float, position1: Float, position2: Float): Float {
                        return if (abs(position1 - lastPosition) < abs(position2 - lastPosition)) {
                            position1
                        } else {
                            position2
                        }
                    }

                    while (true) {
                        val event = awaitPointerEvent()

                        if (drawOffscreen) continue

                        if (event.changes.isEmpty()) continue

                        when (event.type) {
                            PointerEventType.Release -> {
                                distance = -1f
                                isMeasuringAllowed = false
                            }

                            PointerEventType.Press -> {
                                if (System.currentTimeMillis() - lastPressTime < 500 && event.changes.size == 1) {
                                    builder.resetXScale()
                                    invalidate.value = true
                                    continue
                                }
                                lastPressTime = System.currentTimeMillis()
                                val firstPosition = event.changes.first().position.x
                                position = firstPosition
                                distance = -1f
                                isMeasuringAllowed = true
                            }

                            PointerEventType.Move -> {
                                lastPressTime = 0
                                if (!isMeasuringAllowed) continue
                                if (event.changes.size == 2) {
                                    val firstPosition = event.changes.first().position.x
                                    val secondPosition = event.changes.last().position.x
                                    if (distance == -1f) {
                                        distance = abs(firstPosition - secondPosition)
                                        continue
                                    }
                                    position = closedPosition(position, firstPosition, secondPosition)
                                    val newDistance = abs(firstPosition - secondPosition)
                                    val delta = newDistance - distance
                                    distance = newDistance
                                    builder.scaleGesture(delta)
                                    invalidate.value = true
                                }
                                if (event.changes.size == 1) {
                                    val firstPosition = event.changes.first().position.x
                                    val dragAmount = position - firstPosition
                                    position = firstPosition
                                    builder.scrollGesture(dragAmount)
                                    invalidate.value = true
                                }
                            }
                        }
                    }
                }
            }
    ) {
        if (invalidate.value) invalidate.value = false

        clipRect(0f, 0f, size.width, size.height) {
            if (drawOffscreen) {
                bitmap.value?.let { drawImage(it) }
            } else {
                drawIntoCanvas { canvas ->
                    with(canvas.nativeCanvas) {
                        builder.drawCharts(this)
                    }
                }
            }
        }
    }
}