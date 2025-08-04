package de.igor.gun.sleep.analyzer.ui.tools.chart

import android.graphics.Color
import android.graphics.PointF
import de.igor.gun.sleep.analyzer.repositories.tools.ChartBuilder


class ChartTestData {
    enum class SimpleChartIds {
        TEST
    }

    fun getData(): ChartBuilder {
        val data = mutableListOf<PointF>()
        data.add(PointF(1f, 1f))
        data.add(PointF(2f, 2f))
        data.add(PointF(3f, 0f))
        data.add(PointF(4f, 5f))
        data.add(PointF(5f, -1f))

        val chart = ChartBuilder.ChartPresentation.Chart(
            id = SimpleChartIds.TEST.name,
            points = data,
            yAxisLettering = true,
            color = Color.BLUE
        )
        return ChartBuilder(ChartBuilder.Screen.default()).apply {
            this.addChart(chart)
        }
    }
}