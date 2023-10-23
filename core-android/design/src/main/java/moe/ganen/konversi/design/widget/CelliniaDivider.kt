package moe.ganen.konversi.design.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CelliniaDivider(
    modifier: Modifier = Modifier,
    height: Dp = 1.dp,
    color: Color = Color.Unspecified,
) {
    val dividerColor =
        if (color == Color.Unspecified) {
            Color(0xFF3B3B3F)
        } else {
            color
        }

    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .background(color = dividerColor),
    )
}
