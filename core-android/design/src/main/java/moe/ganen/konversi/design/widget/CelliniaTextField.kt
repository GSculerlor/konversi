package moe.ganen.konversi.design.widget

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import moe.ganen.konversi.design.LocalCelliniaColors
import moe.ganen.konversi.design.LocalCelliniaTypography
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun CelliniaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalCelliniaTypography.current.contentMediumRegular,
    placeholder: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val textColor =
        textStyle.color.takeOrElse {
            LocalCelliniaColors.current.content
        }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    BasicTextField(
        value = value,
        modifier = modifier.defaultMinSize(minWidth = 280.dp, minHeight = 52.dp),
        onValueChange = onValueChange,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = mergedTextStyle,
        cursorBrush = SolidColor(LocalCelliniaColors.current.content),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        decorationBox = @Composable { innerTextField ->
            val background: @Composable () -> Unit = {
                Box(
                    modifier =
                    Modifier
                        .background(
                            color = LocalCelliniaColors.current.secondaryContainer,
                            shape = RoundedCornerShape(8.dp),
                        ),
                )
            }

            val transformedText =
                remember(value, visualTransformation) {
                    visualTransformation.filter(AnnotatedString(value))
                }.text.text
            val transition =
                updateTransition(transformedText.isEmpty(), label = "TextFieldInputState")
            val placeholderOpacity by transition.animateFloat(
                label = "PlaceholderOpacity",
                transitionSpec = {
                    tween(
                        durationMillis = 60,
                        delayMillis = 60,
                        easing = LinearEasing,
                    )
                },
            ) {
                if (it) 1f else 0f
            }

            val placeholderText: @Composable ((Modifier) -> Unit)? =
                if (placeholder != null && transformedText.isEmpty() && placeholderOpacity > 0f) {
                    @Composable { modifier ->
                        Box(modifier = modifier.alpha(placeholderOpacity)) {
                            CelliniaText(
                                text = placeholder,
                                textStyle = textStyle,
                                color = textColor.copy(alpha = 0.5f),
                            )
                        }
                    }
                } else {
                    null
                }

            FernTextFieldDecorationBox(
                modifier = Modifier,
                textField = innerTextField,
                placeholder = placeholderText,
                background = background,
                singleLine = singleLine,
                paddingValues = PaddingValues(16.dp),
            )
        },
    )
}

@Composable
private fun FernTextFieldDecorationBox(
    modifier: Modifier,
    textField: @Composable () -> Unit,
    placeholder: @Composable ((Modifier) -> Unit)?,
    background: @Composable () -> Unit,
    singleLine: Boolean,
    paddingValues: PaddingValues,
) {
    val measurePolicy =
        remember(singleLine, paddingValues) {
            FernTextFieldMeasurePolicy(singleLine, paddingValues)
        }
    val layoutDirection = LocalLayoutDirection.current

    Layout(
        modifier = modifier,
        measurePolicy = measurePolicy,
        content = {
            val startPadding = paddingValues.calculateStartPadding(layoutDirection)
            val endPadding = paddingValues.calculateEndPadding(layoutDirection)

            val textPadding =
                Modifier
                    .heightIn(min = 24.dp)
                    .wrapContentHeight()
                    .padding(start = startPadding, end = endPadding)

            Box(
                modifier = Modifier.layoutId(BackgroundLayoutId),
                propagateMinConstraints = true,
            ) {
                background()
            }
            if (placeholder != null) {
                placeholder(
                    Modifier
                        .layoutId(PlaceholderLayoutId)
                        .then(textPadding),
                )
            }
            Box(
                modifier = Modifier
                    .layoutId(TextFieldLayoutId)
                    .then(textPadding),
                propagateMinConstraints = true,
            ) {
                textField()
            }
        },
    )
}

private class FernTextFieldMeasurePolicy(
    private val singleLine: Boolean,
    private val paddingValues: PaddingValues,
) : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints,
    ): MeasureResult {
        val topPaddingValue = paddingValues.calculateTopPadding().roundToPx()
        val bottomPaddingValue = paddingValues.calculateBottomPadding().roundToPx()

        val textFieldConstraints =
            constraints
                .copy(minHeight = 0)
                .offset(vertical = -topPaddingValue - bottomPaddingValue)

        val textFieldPlaceable =
            measurables
                .first { it.layoutId == TextFieldLayoutId }
                .measure(textFieldConstraints)

        val placeholderConstraints = textFieldConstraints.copy(minWidth = 0)
        val placeholderPlaceable =
            measurables
                .find { it.layoutId == PlaceholderLayoutId }
                ?.measure(placeholderConstraints)

        val width =
            max(
                max(textFieldPlaceable.width, placeholderPlaceable?.width ?: 0),
                constraints.minWidth,
            )
        val height =
            calculateHeight(
                textFieldHeight = textFieldPlaceable.height,
                placeholderHeight = placeholderPlaceable?.height ?: 0,
                constraints = constraints,
                density = density,
                paddingValues = paddingValues,
            )

        val containerPlaceable =
            measurables.first { it.layoutId == BackgroundLayoutId }.measure(
                Constraints(
                    minWidth = if (width != Constraints.Infinity) width else 0,
                    maxWidth = width,
                    minHeight = if (height != Constraints.Infinity) height else 0,
                    maxHeight = height,
                ),
            )

        return layout(width, height) {
            val topPadding = (paddingValues.calculateTopPadding().value * density).roundToInt()

            fun calculateVerticalPosition(placeable: Placeable): Int {
                return if (singleLine) {
                    Alignment.CenterVertically.align(placeable.height, height)
                } else {
                    topPadding
                }
            }

            containerPlaceable.place(IntOffset.Zero)
            textFieldPlaceable.placeRelative(0, calculateVerticalPosition(textFieldPlaceable))
            placeholderPlaceable?.placeRelative(0, calculateVerticalPosition(placeholderPlaceable))
        }
    }
}

private fun calculateHeight(
    textFieldHeight: Int,
    placeholderHeight: Int,
    constraints: Constraints,
    density: Float,
    paddingValues: PaddingValues,
): Int {
    val verticalPadding =
        density * (paddingValues.calculateTopPadding() + paddingValues.calculateBottomPadding()).value
    val heightWithPadding = verticalPadding + maxOf(textFieldHeight, placeholderHeight)

    return max(
        constraints.minHeight,
        heightWithPadding.roundToInt(),
    )
}

internal const val PlaceholderLayoutId = "placeholder"
internal const val TextFieldLayoutId = "textField"
internal const val BackgroundLayoutId = "background"
