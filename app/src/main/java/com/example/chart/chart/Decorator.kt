package com.example.chart.chart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

interface ChartProcessor {
    fun process(scope: DrawScope)
}

interface ChartDecorator {
    val decor: Flow<List<ChartDecor>>
    fun invalidate()
}

@Composable
fun Iterable<ChartDecorator>.collectFlattenAsState() = remember(this) {
    combine(this.map { it.decor }) {
        it.asIterable().flatten()
    }
}.collectAsState(initial = emptyList())

interface ChartDecor {
    fun draw(scope: DrawScope)
}

class DashLineDecorator(
    value: Int,
    data: LineChartData
) : ChartDecorator {

    private val _decor = MutableStateFlow<List<ChartDecor>>(
        listOf(
            DashLineDecor(value, data)
        )
    )
    override val decor: StateFlow<List<ChartDecor>> = _decor
    override fun invalidate() {
    }
}

class DashLineDecor(
    private val value: Int,
    private val data: LineChartData
) : ChartDecor {
    override fun draw(scope: DrawScope) = with(scope) {
        val middleY = lerp(size.height, 0f, data.fractionOf(value))

        drawLine(
            Color(0xFF47494C),
            Offset(0f, middleY),
            Offset(size.width, middleY),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 5.dp.toPx()))
        )
    }
}
