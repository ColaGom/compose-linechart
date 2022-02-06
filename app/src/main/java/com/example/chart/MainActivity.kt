package com.example.chart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.example.chart.ui.theme.ChartTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fun generateItems() = List(Random.nextInt(10, 100)) {
            Random.nextInt(500, 1000)
        }

        setContent {
            ChartTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    val (items, setItems) = remember {
                        mutableStateOf(generateItems())
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        val config = LineChartConfig(
                            low = 500,
                            high = 1000,
                            paddingValues = PaddingValues(8.dp),
                            layoutDirection = LocalLayoutDirection.current
                        )

                        SimpleLineChart(
                            config,
                            backgroundDecorators = listOf(DashLineDecorator(700, config)),
                            items = items
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            onClick = {
                                setItems(generateItems())
                            }
                        ) {
                            Text(text = "Reset Data")
                        }
                    }
                }
            }
        }
    }
}

interface LineChartDecorator {
    val config: LineChartConfig
    fun draw(scope: DrawScope)
}

class DashLineDecorator(
    private val value: Int,
    override val config: LineChartConfig
) : LineChartDecorator {

    override fun draw(scope: DrawScope) = with(scope) {
        val middleY = lerp(size.height, 0f, config.fractionOf(value))

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

@Stable
data class LineChartConfig(
    val low: Int,
    val high: Int,
    val paddingValues: PaddingValues,
    val layoutDirection: LayoutDirection
) {
    private val height = high - low

    fun fractionOf(value: Int): Float = (value - low) / height.toFloat()
}

@Composable
fun SimpleLineChart(
    config: LineChartConfig,
    items: List<Int>,
    backgroundDecorators: List<LineChartDecorator>,
    foregroundDecorators: List<LineChartDecorator> = listOf()
) {
    val scaleX = remember { Animatable(0f) }

    LaunchedEffect(items) {
        scaleX.snapTo(0f)
        scaleX.animateTo(
            1f,
            tween(300)
        )
    }

    Canvas(
        modifier = Modifier
            .height(150.dp)
            .alpha(scaleX.value)
            .fillMaxWidth()
    ) {
        scale(scaleX.value, 1f, pivot = Offset(0f, drawContext.size.height / 2)) {
            backgroundDecorators.forEach {
                it.draw(this)
            }

            drawLineChart(
                config = config,
                items = items,
            )

            foregroundDecorators.forEach {
                it.draw(this)
            }
        }
    }
}

fun DrawScope.drawLineChart(
    config: LineChartConfig,
    items: List<Int>,
) {
    val padTop: Float
    val padBottom: Float
    val padStart: Float
    val padEnd: Float

    with(config) {
        padTop = paddingValues.calculateTopPadding().toPx()
        padBottom = paddingValues.calculateBottomPadding().toPx()
        padStart = paddingValues.calculateStartPadding(layoutDirection).toPx()
        padEnd = paddingValues.calculateEndPadding(layoutDirection).toPx()
    }

    val h = size.height - (padTop + padBottom)
    val w = size.width - (padStart + padEnd)

    val step = w / (items.size - 1)

    fun yOf(value: Int): Float {
        return lerp(h, padBottom, config.fractionOf(value))
    }

    fun xOf(position: Int): Float {
        return padStart + position * step
    }

    val start = Offset(xOf(0), yOf(items[0]))
    val end = Offset(xOf(items.lastIndex), yOf(items.last()))

    val path = Path().apply {
        moveTo(start.x, start.y)

        for (i in 1 until items.size) {
            lineTo(xOf(i), yOf(items[i]))
        }
    }

    drawPath(
        path = path,
        color = Color(0xFFFF0045),
        style = Stroke(2.dp.toPx(), join = StrokeJoin.Round)
    )

    drawCircle(
        color = Color(0xFF707070),
        radius = 4.dp.toPx(),
        alpha = .6f,
        center = start
    )

    drawCircle(
        color = Color(0xFF707070),
        radius = 2.dp.toPx(),
        center = start
    )

    drawCircle(
        color = Color(0xFF707070),
        radius = 4.dp.toPx(),
        alpha = .6f,
        center = end
    )

    drawCircle(
        color = Color(0xFFFF0045),
        radius = 2.dp.toPx(),
        center = end
    )
}