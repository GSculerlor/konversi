package moe.ganen.konversi.android.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import moe.ganen.konversi.data.model.Currency
import moe.ganen.konversi.design.CelliniaTheme
import moe.ganen.konversi.design.LocalCelliniaColors
import moe.ganen.konversi.design.LocalCelliniaTypography
import moe.ganen.konversi.design.widget.CelliniaCircularProgressIndicator
import moe.ganen.konversi.design.widget.CelliniaText
import moe.ganen.konversi.feature.CurrenciesViewState

@Composable
fun CurrenciesDialog(
    currenciesViewState: CurrenciesViewState,
    onDismiss: () -> Unit,
    onClickCurrency: (Currency) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
                .background(
                    color = LocalCelliniaColors.current.secondaryContainer,
                    shape = RoundedCornerShape(8.dp),
                )
                .clip(shape = RoundedCornerShape(8.dp)),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            when (currenciesViewState) {
                is CurrenciesViewState.Success ->
                    items(currenciesViewState.currencies.toList()) {
                        CurrencyItem(currency = it, onClickCurrency = { onClickCurrency(it) })
                    }

                CurrenciesViewState.Empty ->
                    item {
                        CelliniaCircularProgressIndicator()
                    }
            }
        }
    }
}

@Composable
private fun CurrencyItem(
    currency: Currency,
    modifier: Modifier = Modifier,
    onClickCurrency: () -> Unit,
) {
    Column(
        modifier =
        modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable { onClickCurrency() }
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.Start,
    ) {
        CelliniaText(
            text = currency.code,
            color = Color.White,
            textStyle = LocalCelliniaTypography.current.contentSmallRegular,
        )
        CelliniaText(
            text = currency.name,
            color = Color.White,
            textStyle = LocalCelliniaTypography.current.contentMediumBold,
        )
    }
}

private class CurrenciesDialogParamProvider :
    CollectionPreviewParameterProvider<CurrenciesViewState>(
        listOf(
            CurrenciesViewState.Empty,
            CurrenciesViewState.Success(
                listOf(
                    Currency("IDR", "Indonesian Rupiah"),
                    Currency("JPY", "Japanese Yen"),
                    Currency("USD", "United States Dollar"),
                ),
            ),
        ),
    )

@Preview
@Composable
private fun CurrenciesDialogPreview(
    @PreviewParameter(CurrenciesDialogParamProvider::class) currenciesViewState: CurrenciesViewState,
) {
    CelliniaTheme {
        CurrenciesDialog(
            currenciesViewState = currenciesViewState,
            onClickCurrency = { },
            onDismiss = { },
        )
    }
}

@Preview
@Composable
private fun CurrencyItemPreview() {
    CelliniaTheme {
        CurrencyItem(currency = Currency("IDR", "Indonesian Rupiah"), onClickCurrency = { })
    }
}
