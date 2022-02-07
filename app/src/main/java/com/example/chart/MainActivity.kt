package com.example.chart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.chart.chart.*
import com.example.chart.ui.theme.ChartTheme
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fun generateItems(): List<ChartValue> {
            val cal = Calendar.getInstance()
            return List(Random.nextInt(10, 100)) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                ChartValue(
//                    it * 10,
                    Random.nextInt(100, 1000),
                    cal.time
                )
            }
        }

        fun generateChartData(layoutDirection: LayoutDirection) = LineChartData(
            items = generateItems(),
            paddingValues = PaddingValues(horizontal = 8.dp),
            layoutDirection = layoutDirection
        )

        setContent {
            ChartTheme {
                Surface(color = MaterialTheme.colors.background) {
                    val layoutDirection = LocalLayoutDirection.current
                    val (data, setData) = remember {
                        mutableStateOf(generateChartData(layoutDirection))
                    }
                    val (lastOffset, setOffset) = remember {
                        mutableStateOf(Offset.Unspecified)
                    }
                    val chartPadding = PaddingValues(horizontal = 8.dp)

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        ConstraintLayout(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            val (title, chart, rightBar, dateHolder) = createRefs()

                            Text(
                                modifier = Modifier.constrainAs(title) { },
                                text = "자산변동",
                                color = Color(0xFFEB303A),
                                fontSize = 12.sp
                            )

                            CompositionLocalProvider(
                                LocalTextStyle provides MaterialTheme.typography.body1.copy(
                                    fontSize = 10.sp
                                )
                            ) {
                                LineChart(
                                    modifier = Modifier
                                        .height(110.dp)
                                        .constrainAs(chart) {
                                            width = Dimension.fillToConstraints
                                            top.linkTo(title.bottom, 6.dp)
                                            start.linkTo(parent.start)
                                            end.linkTo(rightBar.start, 8.dp)
                                        },
                                    data = data,
                                    onDrawLast = {
                                        setOffset(it)
                                    }
                                )
                                LineChartRightBar(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .constrainAs(rightBar) {
                                            height = Dimension.fillToConstraints
                                            top.linkTo(chart.top)
                                            bottom.linkTo(chart.bottom)
                                            start.linkTo(chart.end)
                                            end.linkTo(parent.end)
                                        },
                                    lastOffset = lastOffset,
                                    low = data.low,
                                    high = data.high,
                                    last = data.last
                                )
                            }

                            ChartDateHolder(
                                modifier = Modifier
                                    .constrainAs(dateHolder) {
                                        width = Dimension.fillToConstraints
                                        top.linkTo(chart.bottom, 12.dp)
                                        end.linkTo(chart.end)
                                        start.linkTo(chart.start)
                                    },
                                data = data
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            onClick = {
                                setData(generateChartData(layoutDirection))
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

val sdf = SimpleDateFormat("yyyy/MM/dd")

@Composable
fun ChartDateHolder(
    modifier: Modifier,
    data: LineChartData,
    selected: Date? = null
) {
    if (selected != null) {
        return
    }

    Row(
        modifier = modifier
    ) {
        Text(
            text = sdf.format(data.startAt),
            fontSize = 12.sp,
            color = Color.Gray
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = sdf.format(data.endAt),
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun LineChartRightBar(
    modifier: Modifier,
    low: Int,
    high: Int,
    last: Int,
    lastOffset: Offset
) {
    Box(modifier = modifier) {
        Column {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .adjustToCenter(),
                text = high.toString(),
                textAlign = TextAlign.End,
                color = Color(0xFFEB303A)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                modifier = Modifier.fillMaxWidth().adjustToCenter(true),
                text = low.toString(),
                textAlign = TextAlign.End,
                color = Color(0xFFEB303A)
            )
        }

        if (!lastOffset.isUnspecified) {
            Text(
                modifier = Modifier
                    .absoluteOffset {
                        IntOffset(
                            0,
                            lastOffset.y.toInt()
                        )
                    }
                    .fillMaxWidth()
                    .adjustToCenter()
                    .background(Color.Red),
                textAlign = TextAlign.End,
                text = last.toString(),
                color = Color.White,
            )
        }
    }
}

private fun Modifier.adjustToCenter(reverse: Boolean = false) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height) {
        placeable.placeRelative(0, (if (reverse) 1 else -1) * (placeable.height / 2))
    }
}

@Stable
interface DragListener {
    fun onDrag(offset: Offset)
    fun onDragStart(offset: Offset)
    fun onDragEnd()
}

@Composable
fun LineChart(
    modifier: Modifier = Modifier,
    data: LineChartData,
    onDrawLast: (Offset) -> Unit,
) {
    val processor = remember(data) {
        LineChartProcessor(data)
    }

    val backgroundDecorators = remember(data) {
        listOf<ChartDecorator>(DashLineDecorator(700, data))
    }

    val foregroundDecorators = remember(processor) {
        listOf<ChartDecorator>()
    }

    InternalLineChart(
        modifier = modifier,
        backgroundDecorators = backgroundDecorators,
        foregroundDecorators = foregroundDecorators,
        processor = processor,
        onDrawLast = onDrawLast
    )
}

@Composable
private fun InternalLineChart(
    modifier: Modifier = Modifier,
    processor: LineChartProcessor,
    backgroundDecorators: List<ChartDecorator>,
    foregroundDecorators: List<ChartDecorator> = listOf(),
    onDrawLast: (Offset) -> Unit
) {
    val backgroundDecors = backgroundDecorators.map {
        val childDecors by it.decor.collectAsState(initial = emptyList())
        childDecors
    }.flatten()

    val foregroundDecors = foregroundDecorators.map {
        val childDecors by it.decor.collectAsState(initial = emptyList())
        childDecors
    }.flatten()

    val dragListeners = remember {
        mutableListOf<DragListener>().apply {
            addAll(backgroundDecorators.filterIsInstance<DragListener>())
            addAll(foregroundDecorators.filterIsInstance<DragListener>())
        }
    }

    Canvas(
        modifier = modifier
//            .border(1.dp, Color.Red)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset -> dragListeners.forEach { it.onDragStart(offset) } },
                    onDragCancel = { dragListeners.forEach { it.onDragEnd() } },
                    onDragEnd = { dragListeners.forEach { it.onDragEnd() } },
                    onDrag = { change, dragAmount ->
                        dragListeners.forEach { it.onDrag(offset = change.position) }
                    }
                )
            }
    ) {
        processor.process(this)

        backgroundDecors.forEach {
            it.draw(this)
        }

        drawLineChart(processor)

        foregroundDecors.forEach {
            it.draw(this)
        }

        onDrawLast(processor.end)
    }
}

fun DrawScope.drawLineChart(
    processor: LineChartProcessor,
) {
    drawPath(
        path = processor.path(),
        color = Color(0xFFFF0045),
        style = Stroke(2.dp.toPx(), join = StrokeJoin.Round)
    )

    drawCircle(
        color = Color(0xFF707070),
        radius = 4.dp.toPx(),
        alpha = .6f,
        center = processor.start
    )

    drawCircle(
        color = Color(0xFF707070),
        radius = 2.dp.toPx(),
        center = processor.start
    )

    drawCircle(
        color = Color(0xFF707070),
        radius = 4.dp.toPx(),
        alpha = .6f,
        center = processor.end
    )

    drawCircle(
        color = Color(0xFFFF0045),
        radius = 2.dp.toPx(),
        center = processor.end
    )
}