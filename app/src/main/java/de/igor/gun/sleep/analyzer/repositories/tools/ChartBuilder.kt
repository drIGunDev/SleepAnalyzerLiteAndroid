package de.igor.gun.sleep.analyzer.repositories.tools

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.text.TextPaint
import android.view.ScaleGestureDetector
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import kotlin.math.sign


fun List<PointF>.maxX(): Float = this.maxOf { it.x }
fun List<PointF>.minX(): Float = this.minOf { it.x }
fun List<PointF>.maxY(): Float = this.maxOf { it.y }
fun List<PointF>.minY(): Float = this.minOf { it.y }

@SuppressLint("DefaultLocale")
fun millisToDuration(millis: Float): String {
    val seconds = millis.toLong() / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return if (hours > 0) {
        String.format("%2dh.%2dm.", hours, minutes % 60)
    } else if (minutes > 0) {
        String.format("%2dm.%2ds.", minutes % 60, seconds % 60)
    } else if (seconds > 0) {
        String.format("%2ds.", seconds % 60)
    } else {
        ""
    }
}

class ChartBuilder(val screen: Screen) {
    enum class XAxisType {
        NONE,
        DATE
    }

    class Screen {
        var width: Int = 0
        var height: Int = 0
        var density: Float = 3f
        var strokeWith: Float = 3f
        var paddingLeft: Int = 0
        var paddingRight: Int = 0
        var paddingTop: Int = 0
        var paddingBottom: Int = 0
        var xAxisCount: Int = 0
        var yAxisCount: Int = 0
        var axisColor: Int = 0
        var axisStrokeWith: Float = 3f
        var axisTextSize: Float = 10f
        var axisTextColor: Int = 0
        var isNavigable: Boolean = false
        var scaledMaxX: Float
        var scaledMinX: Float
        var scaleFactor: Float = 1f
        var xAxisType: XAxisType = XAxisType.NONE

        init {
            scaledMinX = 0f
            scaledMaxX = width.toFloat()
        }

        companion object {
            fun default(): Screen {
                return Screen().apply {
                    height = 360
                    width = 837
                    paddingBottom = 0
                    paddingLeft = 0
                    paddingRight = 0
                    paddingTop = 0
                    xAxisCount = 4
                    yAxisCount = 4
                    axisStrokeWith = 1f
                    strokeWith = 4f
                    axisColor = Color.GRAY
                    axisTextColor = "#992B23".toColorInt()
                    resetXScale()
                }
            }
        }

        fun resetXScale() = run { scaledMinX = 0f; scaledMaxX = width.toFloat() }
    }

    sealed interface ChartPresentation {
        data class Chart(
            val id: String,
            val points: List<PointF>,
            val yAxisLettering: Boolean = false,
            val color: Int = Color.BLACK,
            val fillColor: Int? = null,
        ) : ChartPresentation {
            var maxX: Float = 0f
            var minX: Float = 0f
            var maxY: Float = 0f
            var minY: Float = 0f

            init {
                synchronized(points) {
                    if (points.isNotEmpty()) {
                        resetScale()
                    }
                }
            }

            fun rescaleY(minY: Float, maxY: Float) {
                this.minY = minY
                this.maxY = maxY
            }

            fun rescaleX(minX: Float, maxX: Float) {
                this.minX = minX
                this.maxX = maxX
            }

            fun resetScale() {
                synchronized(points) {
                    maxX = points.maxX()
                    minX = points.minX()
                    maxY = points.maxY()
                    minY = points.minY()
                }
            }
        }

        data class Image(val bitmap: Bitmap) : ChartPresentation
    }

    fun setWidth(width: Int) = run { screen.width = width; screen.resetXScale(); this }
    fun setHeight(height: Int) = run { screen.height = height; this }
    fun setStrokeWidth(strokeWidth: Float) = run { screen.strokeWith = strokeWidth; this }

    fun setPadding(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0) =
        with(screen) {
            paddingLeft = left
            paddingRight = right
            paddingTop = top
            paddingBottom = bottom
        }

    fun setXAxisCount(count: Int) = run { screen.xAxisCount = count; this }
    fun setYAxisCount(count: Int) = run { screen.yAxisCount = count; this }
    fun setAxisColor(color: Int) = run { screen.axisColor = color; this }
    fun setAxisStrokeWith(width: Float) = run { screen.axisStrokeWith = width; this }
    fun setAxisTextSize(size: Float) = run { screen.axisTextSize = size; this }
    fun setAxisTextColor(color: Int) = run { screen.axisTextColor = color; this }
    fun setAxisType(type: XAxisType) = run { screen.xAxisType = type; this }
    fun setIsNavigable(isNavigable: Boolean) = run { screen.isNavigable = isNavigable; this }

    fun addChart(chart: ChartPresentation.Chart) = run { synchronized(charts) { charts.add(chart) }; this }
    fun addChart(chart: ChartPresentation.Image) = run { synchronized(charts) { charts.add(chart) }; this }
    fun clear() = run { synchronized(charts) { charts.clear() }; this }

    fun getChart(id: String): ChartPresentation.Chart? {
        synchronized(charts) {
            val result: List<ChartPresentation.Chart> = charts.mapNotNull { chart ->
                synchronized(chart) {
                    when (chart) {
                        is ChartPresentation.Chart -> if (chart.id == id) chart else null
                        else -> null
                    }
                }
            }
            return result.firstOrNull()
        }
    }

    fun drawCharts(canvas: Canvas) {
        synchronized(charts) {
            charts.forEach { chart ->
                synchronized(chart) {
                    when (chart) {
                        is ChartPresentation.Chart -> {
                            if (chart.fillColor != null) {
                                drawPath(canvas, chart)
                            } else {
                                drawLine(canvas, chart)
                            }
                            drawAxis(canvas, chart, screen.xAxisType)
                        }

                        is ChartPresentation.Image -> reDrawBitmap(canvas, chart.bitmap)
                    }
                }
            }
        }
    }

    fun buildBitmap() = run {
        createBitmap(screen.width, screen.height).let { bitmap ->
            Canvas(bitmap).also { drawCharts(canvas = it) }
            bitmap
        }
    }

    fun scaleGesture(scaleGestureDetector: ScaleGestureDetector) {
        val distanceX = (scaleGestureDetector.currentSpanX - scaleGestureDetector.previousSpan) / SCALE_FACTOR
        scaleGesture(distanceX)
    }

    fun scaleGesture(distanceX: Float) {
        with(screen) {
            if (scaledMaxX + minScaleFactor * sign(distanceX) > scaledMinX - minScaleFactor * sign(distanceX)) {
                val del = scaledMaxX - scaledMinX
                val delL = width / 2f - scaledMinX
                val delR = scaledMaxX - width / 2f
                val scaleL = delL / del
                val scaleR = delR / del
                scaledMinX -= distanceX * scaleL / scaleFactor
                scaledMaxX += distanceX * scaleR / scaleFactor
            }
        }
    }

    fun scrollGesture(distanceX: Float) {
        with(screen) {
            if ((distanceX < 0 && scaledMinX < scrollLimitLeft) || (distanceX > 0 && scaledMaxX > scrollLimitRight)) {
                scaledMinX -= distanceX
                scaledMaxX -= distanceX
            }
        }
    }

    fun resetXScale() = screen.resetXScale()

    private var charts: ArrayList<ChartPresentation> = arrayListOf()

    private fun screenX(index: Int, chart: ChartPresentation.Chart) = synchronized(chart.points) {
        ((chart.points[index].x - chart.minX) / (chart.maxX - chart.minX)) * ((screen.scaledMaxX - screen.scaledMinX) - (screen.paddingLeft + screen.paddingRight)) + screen.paddingLeft + screen.scaledMinX
    }

    private fun screenY(index: Int, chart: ChartPresentation.Chart) = synchronized(chart.points) {
        ((chart.maxY - chart.points[index].y) / (chart.maxY - chart.minY)) * (screen.height - (screen.paddingBottom + screen.paddingTop)) + screen.paddingTop
    }

    private fun drawLine(canvas: Canvas, chart: ChartPresentation.Chart) {
        synchronized(chart.points) {
            if (chart.points.size < 2) return

            val paint = Paint().apply {
                style = Paint.Style.FILL
                strokeWidth = screen.strokeWith
                color = chart.color
                isAntiAlias = true
            }

            var lastIndex = 0
            for (i in 1..<chart.points.size) {
                val x0 = screenX(lastIndex, chart)
                val y0 = screenY(lastIndex, chart)
                val x1 = screenX(i, chart)
                val y1 = screenY(i, chart)
                canvas.drawLine(x0, y0, x1, y1, paint)
                lastIndex = i
            }
        }
    }

    private fun drawPath(canvas: Canvas, chart: ChartPresentation.Chart) {
        synchronized(chart.points) {
            if (chart.points.size < 2) return
            val paint = Paint().apply {
                style = Paint.Style.FILL
                strokeWidth = screen.strokeWith
                color = chart.fillColor ?: chart.color
                isAntiAlias = true
            }
            val path = Path()
            path.moveTo(0f, screen.height.toFloat())
            for (i in 1..<chart.points.size) {
                path.lineTo(screenX(i, chart), screenY(i, chart))
            }
            path.lineTo(chart.points.last().x, screen.height.toFloat())
            path.close()
            canvas.drawPath(path, paint)
        }
    }

    private fun drawXAxis(chart: ChartPresentation.Chart, canvas: Canvas, paint: Paint, xAxisType: XAxisType) {
        if (screen.xAxisCount == 0) return

        val textPaint = TextPaint().apply {
            textSize = screen.axisTextSize * screen.density
            style = Paint.Style.FILL
            strokeWidth = screen.axisStrokeWith
            color = screen.axisTextColor
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val deltaX = screen.width / screen.xAxisCount
        val deltaTime = chart.maxX - chart.minX
        val height = screen.height
        val y0 = 0f
        for (i in (0..screen.xAxisCount)) {
            val x0 = (i * deltaX).toFloat()
            val time = (i * deltaTime) / screen.xAxisCount + chart.minX
            val y1 = height.toFloat()
            canvas.drawLine(x0, y0, x0, y1, paint)
            when (xAxisType) {
                XAxisType.DATE -> {
                    val timeMarker = millisToDuration(time)
                    val h = paint.descent() - paint.ascent()
                    canvas.drawText(timeMarker, x0, y1 - h, textPaint)
                }

                else -> {}
            }
        }
    }

    private fun drawYAxis(chart: ChartPresentation.Chart, canvas: Canvas, paint: Paint) {
        if (screen.yAxisCount == 0) return

        val textPaint = TextPaint().apply {
            textSize = screen.axisTextSize * screen.density
            style = Paint.Style.FILL
            strokeWidth = screen.axisStrokeWith
            color = screen.axisTextColor
            isAntiAlias = true
        }

        val valueDelta = (chart.maxY - chart.minY) / screen.yAxisCount
        val screenDeltaY = screen.height / screen.yAxisCount
        val width = screen.width
        val x0 = 0f

        for (i in 0..screen.yAxisCount) {
            val y0 = (i * screenDeltaY).toFloat()
            val x1 = width.toFloat()
            canvas.drawLine(x0, y0, x1, y0, paint)

            if (chart.yAxisLettering && i < screen.yAxisCount) {
                val value = chart.maxY - valueDelta * i
                val strValue = String.format("%d", value.toInt())
                val h = textPaint.descent() - textPaint.ascent()
                canvas.drawText(strValue, 0, strValue.length, x0, y0 + h, textPaint)
            }
        }
    }

    private fun drawAxis(canvas: Canvas, chart: ChartPresentation.Chart, xAxisType: XAxisType) {
        val paint = Paint().apply {
            style = Paint.Style.FILL
            strokeWidth = screen.axisStrokeWith
            color = screen.axisColor
            isAntiAlias = true
        }

        drawXAxis(chart, canvas, paint, xAxisType)
        drawYAxis(chart, canvas, paint)
    }

    private fun reDrawBitmap(canvas: Canvas, bitmap: Bitmap) {
        val src = Rect(0, 0, bitmap.width, bitmap.height)
        val dst = Rect(
            screen.paddingLeft,
            screen.paddingTop,
            (screen.width - (screen.paddingLeft + screen.paddingRight)),
            (screen.height - (screen.paddingBottom + screen.paddingTop))
        )
        canvas.drawBitmap(bitmap, src, dst, Paint().apply { isAntiAlias = true })
    }

    private val minScaleFactor: Float get() = screen.width / 2f
    private val scrollLimitRight: Float get() = screen.width.toFloat()
    private val scrollLimitLeft: Float get() = 0f

    private companion object {
        const val SCALE_FACTOR = 3f
    }
}