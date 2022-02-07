package com.example.chart.chart

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.LayoutDirection
import java.util.*

data class ChartValue(
    val value: Int,
    val date: Date
)

@Stable
data class LineChartData(
    val items: List<ChartValue>,
    val low: Int = items.minOf { it.value },
    val high: Int = items.maxOf { it.value },
    val paddingValues: PaddingValues,
    val layoutDirection: LayoutDirection,
) {
    private val diff = high - low

    fun fractionOf(value: Int): Float = (value - low) / diff.toFloat()

    val startAt = items.minOf { it.date.time }
    val endAt = items.maxOf { it.date.time }
    val last = items.last().value

    operator fun get(idx: Int) = items[idx]
}