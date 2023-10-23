@file:OptIn(ExperimentalFoundationApi::class)

package moe.ganen.konversi.android.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import moe.ganen.konversi.android.R
import moe.ganen.konversi.data.model.Currency
import moe.ganen.konversi.data.model.CurrencyRate
import moe.ganen.konversi.design.CelliniaTheme
import moe.ganen.konversi.design.LocalCelliniaColors
import moe.ganen.konversi.design.LocalCelliniaTypography
import moe.ganen.konversi.design.utils.BalanceVisualTransformation
import moe.ganen.konversi.design.utils.InputFilterRegex
import moe.ganen.konversi.design.utils.filteredDecimalText
import moe.ganen.konversi.design.widget.CelliniaCircularProgressIndicator
import moe.ganen.konversi.design.widget.CelliniaSurface
import moe.ganen.konversi.design.widget.CelliniaText
import moe.ganen.konversi.design.widget.CelliniaTextField
import moe.ganen.konversi.feature.ConversionRatesViewState
import moe.ganen.konversi.feature.CurrenciesViewState
import moe.ganen.konversi.feature.HomeViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.DecimalFormat
import java.text.NumberFormat

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val conversionRatesViewState by viewModel.conversionRatesViewState.collectAsState()
    val currenciesViewState by viewModel.currenciesViewState.collectAsState()
    val selectedCurrencyCode by viewModel.selectedCurrencyCode.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()

    HomeScreen(
        modifier = modifier,
        conversionRatesViewState = conversionRatesViewState,
        currenciesViewState = currenciesViewState,
        selectedCurrencyCode = selectedCurrencyCode,
        isSyncing = isSyncing,
        isOnline = isOnline,
        updateSelectedRate = { viewModel.updateSelectedRate(it) },
        updateInput = { viewModel.updateInput(it) },
    )
}

@Composable
private fun HomeScreen(
    conversionRatesViewState: ConversionRatesViewState,
    currenciesViewState: CurrenciesViewState,
    isSyncing: Boolean,
    isOnline: Boolean,
    selectedCurrencyCode: String,
    updateSelectedRate: (String) -> Unit,
    updateInput: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyStaggeredGridState()
    var showAsGrid by remember { mutableStateOf(true) }
    var input by remember { mutableStateOf(TextFieldValue()) }
    var showCurrenciesDialog by rememberSaveable {
        mutableStateOf(false)
    }

    CelliniaSurface(
        modifier = modifier.fillMaxSize(),
        shape = RectangleShape,
        backgroundColor = CelliniaTheme.colors.primaryContainer,
    ) {
        if (showCurrenciesDialog) {
            CurrenciesDialog(
                currenciesViewState = currenciesViewState,
                onDismiss = { showCurrenciesDialog = false },
                onClickCurrency = {
                    updateSelectedRate(it.code)
                    showCurrenciesDialog = false
                },
            )
        }

        Column(
            modifier = Modifier
                .statusBarsPadding()
                .navigationBarsPadding()
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HomeAppbar(modifier = Modifier.fillMaxWidth())
            ConversionInput(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                selectedCurrencyCode = selectedCurrencyCode,
                value = input,
                onValueChanged = {
                    if (!it.text.contains(InputFilterRegex.DecimalInput) || it.text.length > 16) {
                        return@ConversionInput
                    }

                    input = filteredDecimalText(it)
                    updateInput(it.text)
                },
            ) { showCurrenciesDialog = true }
            if (!isOnline) {
                CelliniaText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    text = "Not connected to the internet..."
                )
            }
            CelliniaSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            ) {
                LazyVerticalStaggeredGrid(
                    modifier = Modifier.fillMaxWidth(),
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalItemSpacing = 8.dp,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    columns = StaggeredGridCells.Fixed(2),
                ) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            AnimatedContent(
                                modifier = Modifier.align(Alignment.CenterEnd),
                                targetState = showAsGrid,
                                label = "display type",
                            ) { gridDisplay ->
                                Image(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { showAsGrid = !showAsGrid },
                                    painter = painterResource(id = if (gridDisplay) R.drawable.ic_view_list else R.drawable.ic_view_grid),
                                    colorFilter = ColorFilter.tint(Color.White),
                                    contentDescription = "display type",
                                )
                            }
                        }
                    }
                    conversionRates(
                        isSyncing = isSyncing,
                        conversionRatesViewState = conversionRatesViewState,
                        showAsGrid = showAsGrid,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeAppbar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .height(52.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CelliniaText(
            modifier = Modifier
                .size(24.dp)
                .weight(1f),
            text = "Konversi",
            textStyle = CelliniaTheme.typography.titleMediumBold,
        )
    }
}

@Composable
private fun ConversionInput(
    value: TextFieldValue,
    onValueChanged: (TextFieldValue) -> Unit,
    selectedCurrencyCode: String,
    modifier: Modifier = Modifier,
    onClickCurrency: () -> Unit,
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        CelliniaTextField(
            modifier = Modifier
                .weight(0.75f)
                .fillMaxWidth(),
            placeholder = "1",
            textStyle =
            LocalCelliniaTypography.current.contentMediumRegular.copy(
                color = Color.White,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            visualTransformation = if (LocalInspectionMode.current) VisualTransformation.None else BalanceVisualTransformation(),
            value = value.text,
            onValueChange = { onValueChanged(TextFieldValue(it)) },
        )
        Row(
            modifier = Modifier
                .weight(0.25f)
                .fillMaxHeight()
                .background(
                    color = LocalCelliniaColors.current.secondaryContainer,
                    shape = RoundedCornerShape(8.dp),
                )
                .clip(shape = RoundedCornerShape(8.dp))
                .clickable { onClickCurrency() },
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CelliniaText(text = selectedCurrencyCode, color = Color.White)
            Image(
                modifier = Modifier.size(16.dp),
                painter = painterResource(id = R.drawable.ic_chevron_down),
                colorFilter = ColorFilter.tint(Color.White),
                contentDescription = "search",
            )
        }
    }
}

private fun LazyStaggeredGridScope.conversionRates(
    isSyncing: Boolean,
    conversionRatesViewState: ConversionRatesViewState,
    showAsGrid: Boolean,
) {
    if (isSyncing) {
        item(span = StaggeredGridItemSpan.FullLine) {
            CelliniaCircularProgressIndicator()
        }
    }
    when (conversionRatesViewState) {
        is ConversionRatesViewState.Success ->
            items(
                items = conversionRatesViewState.currencyRates,
                key = { it.currency.code },
                span = { if (showAsGrid) StaggeredGridItemSpan.SingleLane else StaggeredGridItemSpan.FullLine },
            ) {
                ConvertedCurrencyItem(
                    currencyCode = it.currency.code,
                    currencyName = it.currency.name,
                    value = it.rate,
                    modifier = Modifier.animateItemPlacement(),
                )
            }

        ConversionRatesViewState.Empty ->
            item(span = StaggeredGridItemSpan.FullLine) {
                EmptyConvertedCurrencyItem(modifier = Modifier.fillMaxWidth())
            }
    }
}

@Composable
private fun ConvertedCurrencyItem(
    currencyCode: String,
    currencyName: String,
    value: Double,
    modifier: Modifier = Modifier,
) {
    val numberFormat = NumberFormat.getCurrencyInstance().apply {
        (this as DecimalFormat).decimalFormatSymbols.apply {
            currencySymbol = ""
            decimalFormatSymbols = this
        }
        maximumFractionDigits = 4
    }

    Column(
        modifier = modifier
            .background(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp),
            )
            .clip(shape = RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
        CelliniaText(
            text = currencyName,
            color = Color.White,
            textStyle = LocalCelliniaTypography.current.contentSmallRegular,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            verticalAlignment = Alignment.Bottom,
        ) {
            CelliniaText(
                modifier = Modifier.alignByBaseline(),
                text = currencyCode,
                color = Color.White,
                textStyle = LocalCelliniaTypography.current.contentSmallRegular,
            )
            CelliniaText(
                modifier = Modifier.alignByBaseline(),
                text = numberFormat.format(value),
                color = Color.White,
                textStyle = LocalCelliniaTypography.current.titleLargeBold,
            )
        }
    }
}

@Composable
private fun EmptyConvertedCurrencyItem(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        CelliniaText(
            text = "converted currency is empty, spooky...",
            textStyle = CelliniaTheme.typography.titleMediumBold,
        )
    }
}

@Composable
private fun LoadingConvertedCurrencyItem(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        CelliniaCircularProgressIndicator()
    }
}

private class HomeScreenParamProvider :
    CollectionPreviewParameterProvider<Pair<ConversionRatesViewState, Boolean>>(
        listOf(
            ConversionRatesViewState.Empty to false,
            ConversionRatesViewState.Empty to true,
            ConversionRatesViewState.Success(
                listOf(
                    CurrencyRate(
                        currency = Currency("IDR", "Indonesian Rupiah"),
                        rate = 15886.0,
                    ),
                    CurrencyRate(
                        currency = Currency("JPY", "Japanese Yen"),
                        rate = 149.85,
                    ),
                    CurrencyRate(
                        currency = Currency("USD", "United State Dollar"),
                        rate = 1.0,
                    ),
                ),
            ) to false,
            ConversionRatesViewState.Success(
                listOf(
                    CurrencyRate(
                        currency = Currency("IDR", "Indonesian Rupiah"),
                        rate = 15886.0,
                    ),
                    CurrencyRate(
                        currency = Currency("JPY", "Japanese Yen"),
                        rate = 149.85,
                    ),
                    CurrencyRate(
                        currency = Currency("USD", "United State Dollar"),
                        rate = 1.0,
                    ),
                ),
            ) to true,
        ),
    )

@Preview
@Composable
private fun HomeScreenPreview(@PreviewParameter(HomeScreenParamProvider::class) params: Pair<ConversionRatesViewState, Boolean>) {
    val (conversionRatesViewState, isSyncing) = params

    CelliniaTheme {
        HomeScreen(
            conversionRatesViewState = conversionRatesViewState,
            currenciesViewState = CurrenciesViewState.Empty,
            isSyncing = isSyncing,
            isOnline = isSyncing,
            selectedCurrencyCode = "IDR",
            updateSelectedRate = { },
            updateInput = { },
        )
    }
}

@Preview
@Composable
private fun HomeAppbarPreview() {
    CelliniaTheme {
        HomeAppbar()
    }
}

@Preview
@Composable
private fun ConversionTextFieldPreview() {
    CelliniaTheme {
        ConversionInput(
            value = TextFieldValue("10,000.00"),
            onValueChanged = { },
            selectedCurrencyCode = "IDR",
        ) { }
    }
}

@Preview
@Composable
private fun ConvertedCurrencyItemPreview() {
    CelliniaTheme {
        ConvertedCurrencyItem(
            currencyCode = "IDR",
            currencyName = "Indonesian Rupiah",
            value = 50000.00,
        )
    }
}

@Preview
@Composable
private fun EmptyConvertedCurrencyItemPreview() {
    CelliniaTheme {
        EmptyConvertedCurrencyItem()
    }
}

@Preview
@Composable
private fun ErrorConvertedCurrencyItemPreview() {
    CelliniaTheme {
        LoadingConvertedCurrencyItem()
    }
}
