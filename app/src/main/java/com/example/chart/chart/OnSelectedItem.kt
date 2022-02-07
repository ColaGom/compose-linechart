package com.example.chart.chart

import androidx.compose.ui.geometry.Offset

fun interface OnSelectedPointListener {
    fun onSelect(offset: Offset, position: Int)
}