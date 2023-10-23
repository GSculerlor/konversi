package moe.ganen.konversi.design.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import moe.ganen.konversi.design.LocalCelliniaColors
import moe.ganen.konversi.design.LocalCelliniaTypography

@Composable
fun CelliniaTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    CelliniaClickableSurface(
        modifier =
        modifier
            .semantics { role = Role.Button }
            .defaultMinSize(minWidth = 72.dp)
            .height(48.dp),
        onClick = onClick,
        enabled = enabled,
        backgroundColor = Color.Transparent,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CelliniaText(
                text = text,
                color = LocalCelliniaColors.current.actionContainer,
                textStyle = LocalCelliniaTypography.current.titleSmallRegular,
            )
        }
    }
}
