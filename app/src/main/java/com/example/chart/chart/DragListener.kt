package com.example.chart.chart

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

@Stable
interface DragListener {
    fun onDrag(offset: Offset)
    fun onDragStart(offset: Offset)
    fun onDragEnd()
}

fun Modifier.bindDragListener(listeners: Iterable<DragListener>) = pointerInput(Unit) {
    detectTapGestures(
        onPress = { offset ->
            listeners.forEach { it.onDragStart(offset) }
            awaitRelease()
            listeners.forEach { it.onDragEnd() }
        },
    )
}.pointerInput(Unit) {
    detectDragGestures(
        onDragStart = { offset -> listeners.forEach { it.onDragStart(offset) } },
        onDragCancel = { listeners.forEach { it.onDragEnd() } },
        onDragEnd = { listeners.forEach { it.onDragEnd() } },
        onDrag = { change, dragAmount ->
            listeners.forEach { it.onDrag(offset = change.position) }
        }
    )
}
