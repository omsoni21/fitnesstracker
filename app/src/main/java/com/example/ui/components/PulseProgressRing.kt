package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FieryOrange
import com.example.ui.theme.NeonLime
import com.example.ui.theme.NeonLimeDark

@Composable
fun PulseProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp,
    strokeWidth: Dp = 14.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    gradientColors: List<Color> = listOf(NeonLime, NeonLimeDark),
    content: @Composable () -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progress_anim"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            // Background Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Animated Progress Arc
            if (animatedProgress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = gradientColors,
                        center = Offset(this.size.width / 2f, this.size.height / 2f)
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        content()
    }
}

@Composable
fun DualPulseProgressRing(
    outerProgress: Float, // Steps
    innerProgress: Float, // Calories
    modifier: Modifier = Modifier,
    size: Dp = 210.dp,
    strokeWidth: Dp = 12.dp,
    content: @Composable () -> Unit = {}
) {
    val animOuter by animateFloatAsState(
        targetValue = outerProgress.coerceIn(0f, 1f),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "outer_anim"
    )
    val animInner by animateFloatAsState(
        targetValue = innerProgress.coerceIn(0f, 1f),
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "inner_anim"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val gap = 6.dp.toPx()

            // Outer Ring: Steps (Neon Lime)
            val outerArcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val outerTopLeft = Offset(strokePx / 2f, strokePx / 2f)

            drawArc(
                color = Color(0xFF262C38),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = outerTopLeft,
                size = outerArcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            if (animOuter > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(NeonLime, Color(0xFF99E600), NeonLime),
                        center = Offset(this.size.width / 2f, this.size.height / 2f)
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animOuter,
                    useCenter = false,
                    topLeft = outerTopLeft,
                    size = outerArcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }

            // Inner Ring: Calories (Fiery Orange / Flame)
            val innerStrokePx = (strokeWidth * 0.85f).toPx()
            val innerRadiusOffset = strokePx + gap
            val innerArcSize = Size(
                this.size.width - (innerRadiusOffset * 2f),
                this.size.height - (innerRadiusOffset * 2f)
            )
            val innerTopLeft = Offset(innerRadiusOffset, innerRadiusOffset)

            drawArc(
                color = Color(0xFF1E222B),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = innerTopLeft,
                size = innerArcSize,
                style = Stroke(width = innerStrokePx, cap = StrokeCap.Round)
            )

            if (animInner > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(FieryOrange, Color(0xFFFF9100), FieryOrange),
                        center = Offset(this.size.width / 2f, this.size.height / 2f)
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animInner,
                    useCenter = false,
                    topLeft = innerTopLeft,
                    size = innerArcSize,
                    style = Stroke(width = innerStrokePx, cap = StrokeCap.Round)
                )
            }
        }

        content()
    }
}
