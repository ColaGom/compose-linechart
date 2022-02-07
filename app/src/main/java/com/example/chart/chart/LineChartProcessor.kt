package com.example.chart.chart

import android.util.Log
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.util.lerp
import kotlin.math.abs

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
    var path = Path()

    var prev = Size.Unspecified

    override fun process(drawScope: DrawScope) = with(drawScope) {
        if (prev == size) {
            return
        }
        Log.d("GNO", "process")
        prev = size.copy()

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

        path = Path().apply {
            charItems.forEachIndexed { index, offset ->
                if (index == 0) moveTo(offset.x, offset.y)
                else lineTo(offset.x, offset.y)
            }
        }
    }

    fun nearestValueBy(offset: Offset, withNearestValue: (ChartValue, Offset) -> Unit) {
        charItems.withIndex().minByOrNull { abs(it.value.x - offset.x) }?.let {
            withNearestValue(data[it.index], it.value)
        }
    }
}