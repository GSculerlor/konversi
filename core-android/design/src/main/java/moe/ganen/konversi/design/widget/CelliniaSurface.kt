package moe.ganen.konversi.design.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import moe.ganen.konversi.design.LocalCelliniaColors

@Composable
@NonRestartableComposable
fun CelliniaSurface(
    modifier: Modifier = Modifier,
    backgroundColor: Color = LocalCelliniaColors.current.secondaryContainer,
    shape: Shape = RoundedCornerShape(8.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
        modifier
            .background(color = backgroundColor, shape = shape)
            .clip(shape),
        propagateMinConstraints = true,
    ) {
        content()
    }
}

@Composable
@NonRestartableComposable
fun CelliniaClickableSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = LocalCelliniaColors.current.secondaryContainer,
    shape: Shape = RoundedCornerShape(8.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
        modifier
            .background(color = backgroundColor, shape = shape)
            .clip(shape)
            .clickable(
                enabled = enabled,
                onClick = onClick,
            ),
    ) {
        content()
    }
}
