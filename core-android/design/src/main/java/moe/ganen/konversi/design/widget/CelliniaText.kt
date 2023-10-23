package moe.ganen.konversi.design.widget

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import moe.ganen.konversi.design.CelliniaTheme

@Composable
fun CelliniaText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CelliniaTheme.colors.text,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    textStyle: TextStyle = CelliniaTheme.typography.contentMediumRegular,
) {
    val mergedStyle =
        textStyle.merge(
            TextStyle(
                color = color,
                textAlign = textAlign,
                textDecoration = textDecoration,
                letterSpacing = letterSpacing,
            ),
        )
    BasicText(
        text,
        modifier,
        mergedStyle,
        onTextLayout,
        overflow,
        softWrap,
        maxLines,
        minLines,
    )
}

@Composable
fun CelliniaText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = CelliniaTheme.colors.text,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    textStyle: TextStyle = CelliniaTheme.typography.contentMediumRegular,
) {
    val mergedStyle =
        textStyle.merge(
            TextStyle(
                color = color,
                textAlign = textAlign,
                textDecoration = textDecoration,
                letterSpacing = letterSpacing,
            ),
        )
    BasicText(
        text,
        modifier,
        mergedStyle,
        onTextLayout,
        overflow,
        softWrap,
        maxLines,
        minLines,
    )
}
