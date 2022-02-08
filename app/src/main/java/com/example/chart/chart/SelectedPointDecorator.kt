package com.example.chart.chart

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.example.chart.OnSelectedValueListener
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.*

class VerticalLineDecor(
    private val offset: Offset
) : ChartDecor {
    override fun draw(scope: DrawScope) = with(scope) {
        drawRect(Color(0x50000000))

        drawLine(
            Color.Gray,
            strokeWidth = (1).dp.toPx(),
            cap = StrokeCap.Round,
            start = Offset(offset.x, size.height + 10f),
            end = Offset(offset.x, -10f)
        )
    }
}

class SelectedPointDecorator(
    private val processor: LineChartProcessor,
    private val onSelectedValue: OnSelectedValueListener
) : ChartDecorator, DragListener {
    private val _selectedOffset = MutableStateFlow(Offset.Unspecified)
    override val decor: Flow<List<ChartDecor>> =
        _selectedOffset.map {
            if (it.isUnspecified) emptyList()
            else listOf(VerticalLineDecor(it))
        }.stateIn(MainScope(), SharingStarted.Eagerly, emptyList())

    override fun invalidate() {}

    override fun onDrag(offset: Offset) {
        processor.nearestValueBy(offset) { value, offset ->
            _selectedOffset.value = offset
            onSelectedValue(value to offset)
        }
    }

    override fun onDragStart(offset: Offset) {
        processor.nearestValueBy(offset) { value, offset ->
            _selectedOffset.value = offset
            onSelectedValue(value to offset)
        }
    }

    override fun onDragEnd() {
        _selectedOffset.value = Offset.Unspecified
        onSelectedValue(null)
    }
}


