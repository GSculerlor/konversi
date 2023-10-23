package moe.ganen.konversi.design.widget

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.ganen.konversi.design.LocalCelliniaColors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun CelliniaCircularProgressIndicator(
    modifier: Modifier = Modifier,
    startAngle: Float = 270f,
    indicatorColor: Color = LocalCelliniaColors.current.actionContainer,
    trackColor: Color = LocalCelliniaColors.current.actionContainer.copy(alpha = 0.1f),
    strokeWidth: Dp = 4.dp,
) {
    val transition = rememberInfiniteTransition(label = "transition")
    val currentRotation by transition.animateValue(
        0,
        5,
        Int.VectorConverter,
        infiniteRepeatable(
            animation = tween(
                durationMillis = 1332 * 5,
                easing = LinearEasing,
            ),
        ),
        label = "current rotation",
    )
    // How far forward (degrees) the base point should be from the start point
    val baseRotation by transition.animateFloat(
        0f,
        286f,
        infiniteRepeatable(
            animation = tween(
                durationMillis = 1332,
                easing = LinearEasing,
            ),
        ),
        label = "base rotation",
    )
    // How far forward (degrees) both the head and tail should be from the base point
    val endAngle by transition.animateFloat(
        0f,
        290f,
        infiniteRepeatable(
            animation = keyframes {
                durationMillis = (1332 * 0.5).toInt() + (1332 * 0.5).toInt()
                0f at 0
                290f at (1332 * 0.5).toInt()
            },
        ),
        label = "end angle",
    )

    val startProgressAngle by transition.animateFloat(
        0f,
        290f,
        infiniteRepeatable(
            animation = keyframes {
                durationMillis = (1332 * 0.5).toInt() + (1332 * 0.5).toInt()
                0f at (1332 * 0.5).toInt()
                290f at durationMillis
            },
        ),
        label = "start angle",
    )

    Spacer(
        modifier
            .progressSemantics()
            .size(24.dp)
            .focusable()
            .drawWithCache
            {
                val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)

                val currentRotationAngleOffset = (currentRotation * (286f + 290f) % 360f) % 360f
                // How long a line to draw using the start angle as a reference point
                val sweep = abs(endAngle - startProgressAngle)

                // Offset by the constant offset and the per rotation offset
                val offset = (startAngle + currentRotationAngleOffset + baseRotation) % 360f

                onDrawWithContent {
                    drawCircularIndicator(0f, 360f, trackColor, stroke)
                    drawIndeterminateCircularIndicator(
                        startProgressAngle + offset,
                        sweep,
                        indicatorColor,
                        stroke,
                    )
                }
            },
    )
}

private fun DrawScope.drawIndeterminateCircularIndicator(
    startAngle: Float,
    sweep: Float,
    color: Color,
    stroke: Stroke,
) {
    // When the start and end angles are in the same place, we still want to draw a small sweep, so
    // the stroke caps get added on both ends and we draw the correct minimum length arc
    val adjustedSweep = max(sweep, 0.1f)

    drawCircularIndicator(startAngle, adjustedSweep, color, stroke)
}

private fun DrawScope.drawCircularIndicator(
    startAngle: Float,
    sweep: Float,
    color: Color,
    stroke: Stroke,
) {
    val diameter = min(size.width, size.height)
    val diameterOffset = stroke.width / 2
    val arcDimen = diameter - 2 * diameterOffset
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = Offset(
            diameterOffset + (size.width - diameter) / 2,
            diameterOffset + (size.height - diameter) / 2,
        ),
        size = Size(arcDimen, arcDimen),
        style = stroke,
    )
}
