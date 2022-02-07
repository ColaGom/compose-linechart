package com.example.chart.chart

import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.util.lerp

@Stable
class LineChartProcessor(
    private val data: LineChartData
) : ChartProcessor {
    lateinit var charItems: List<Offset>

    private var h: Float = 0f
    private var w: Float = 0f
    private var padTop: Float = 0f
    private var padBottom: Float = 0f
    private var padStart: Float = 0f
    private var padEnd: Float = 0f
    private var step: Float = 0f

    private fun yOf(value: Int): Float {
        return lerp(h, padBottom, data.fractionOf(value))
    }

    private fun xOf(position: Int): Float {
        return padStart + position * step
    }

    val start get() = charItems.first()
    val end get() = charItems.last()

    fun path() = Path().apply {
        charItems.forEachIndexed { index, offset ->
            if (index == 0) moveTo(offset.x, offset.y)
            else lineTo(offset.x, offset.y)
        }
    }

    override fun process(drawScope: DrawScope) = with(drawScope) {
        with(data) {
            padTop = paddingValues.calculateTopPadding().toPx()
            padBottom = paddingValues.calculateBottomPadding().toPx()
            padStart = paddingValues.calculateStartPadding(layoutDirection).toPx()
            padEnd = paddingValues.calculateEndPadding(layoutDirection).toPx()
        }

        h = size.height - (padTop + padBottom)
        w = size.width - (padStart + padEnd)

        step = w / (data.items.size - 1)

        charItems = data.items.mapIndexed { index, item ->
            Offset(xOf(index), yOf(item.value))
        }
    }
}