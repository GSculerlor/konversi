package moe.ganen.konversi.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.rememberSystemUiController

val OutfitFamily =
    FontFamily(
        fonts =
        listOf(
            Font(
                resId = R.font.outfit_light,
                weight = FontWeight.W300,
                style = FontStyle.Normal,
            ),
            Font(
                resId = R.font.outfit,
                weight = FontWeight.W400,
                style = FontStyle.Normal,
            ),
            Font(
                resId = R.font.outfit_medium,
                weight = FontWeight.W500,
                style = FontStyle.Normal,
            ),
            Font(
                resId = R.font.outfit_semibold,
                weight = FontWeight.W600,
                style = FontStyle.Normal,
            ),
            Font(
                resId = R.font.outfit_bold,
                weight = FontWeight.W700,
                style = FontStyle.Normal,
            ),
        ),
    )

@Immutable
data class CelliniaColors(
    val background: Color,
    val onBackground: Color,
    val primaryContainer: Color,
    val secondaryContainer: Color,
    val actionContainer: Color,
    val errorContainer: Color,
    val text: Color,
    val content: Color,
)

@Immutable
data class CelliniaTypography(
    val titleLargeBold: TextStyle,
    val titleMediumBold: TextStyle,
    val titleSmallBold: TextStyle,
    val titleSmallRegular: TextStyle,
    val contentMediumBold: TextStyle,
    val contentMediumRegular: TextStyle,
    val contentSmallBold: TextStyle,
    val contentSmallRegular: TextStyle,
)

val LocalCelliniaColors =
    staticCompositionLocalOf {
        CelliniaColors(
            content = Color.Unspecified,
            primaryContainer = Color.Unspecified,
            onBackground = Color.Unspecified,
            background = Color.Unspecified,
            errorContainer = Color.Unspecified,
            secondaryContainer = Color.Unspecified,
            actionContainer = Color.Unspecified,
            text = Color.Unspecified,
        )
    }

val LocalCelliniaTypography =
    staticCompositionLocalOf {
        CelliniaTypography(
            titleLargeBold = TextStyle.Default,
            titleMediumBold = TextStyle.Default,
            titleSmallBold = TextStyle.Default,
            titleSmallRegular = TextStyle.Default,
            contentMediumBold = TextStyle.Default,
            contentMediumRegular = TextStyle.Default,
            contentSmallBold = TextStyle.Default,
            contentSmallRegular = TextStyle.Default,
        )
    }

@Composable
fun CelliniaTheme(content: @Composable () -> Unit) {
    val celliniaColors =
        CelliniaColors(
            background = Color(0xFF1B1B1F),
            onBackground = Color.White,
            primaryContainer = Color(0xFF151515),
            secondaryContainer = Color(0xFF1B1B1B),
            actionContainer = Color(0xFF4B76C2),
            errorContainer = Color(0xFFc04f35),
            content = Color.White,
            text = Color.White,
        )
    val celliniaTypography =
        CelliniaTypography(
            titleLargeBold =
            TextStyle(
                fontFamily = OutfitFamily,
                fontWeight = FontWeight.W700,
                fontSize = 24.sp,
                lineHeight = 36.sp,
            ),
            titleMediumBold =
            TextStyle(
                fontFamily = OutfitFamily,
                fontWeight = FontWeight.W600,
                fontSize = 18.sp,
                lineHeight = 28.sp,
            ),
            titleSmallBold =
            TextStyle(
                fontFamily = OutfitFamily,
                fontWeight = FontWeight.W600,
                fontSize = 16.sp,
                lineHeight = 24.sp,
            ),
            titleSmallRegular =
            TextStyle(
                fontFamily = OutfitFamily,
                fontWeight = FontWeight.W400,
                fontSize = 16.sp,
                lineHeight = 24.sp,
            ),
            contentMediumBold =
            TextStyle(
                fontFamily = OutfitFamily,
                fontWeight = FontWeight.W600,
                fontSize = 14.sp,
                lineHeight = 22.sp,
            ),
            contentMediumRegular =
            TextStyle(
                fontFamily = OutfitFamily,
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 18.sp,
            ),
            contentSmallBold =
            TextStyle(
                fontFamily = OutfitFamily,
                fontWeight = FontWeight.W600,
                fontSize = 12.sp,
                lineHeight = 18.sp,
            ),
            contentSmallRegular =
            TextStyle(
                fontFamily = OutfitFamily,
                fontWeight = FontWeight.W400,
                fontSize = 12.sp,
                lineHeight = 18.sp,
            ),
        )

    val systemUiController = rememberSystemUiController()

    CompositionLocalProvider(
        LocalCelliniaColors provides celliniaColors,
        LocalCelliniaTypography provides celliniaTypography,
        content = content,
    )

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = false,
        )
    }
}

object CelliniaTheme {
    val colors: CelliniaColors
        @Composable
        get() = LocalCelliniaColors.current

    val typography: CelliniaTypography
        @Composable
        get() = LocalCelliniaTypography.current
}
