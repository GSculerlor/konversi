package moe.ganen.konversi.feature

import moe.ganen.konversi.data.model.Currency
import moe.ganen.konversi.data.model.CurrencyRate

sealed interface CurrenciesViewState {
    data class Success(val currencies: List<Currency>) : CurrenciesViewState

    data object Empty : CurrenciesViewState
}

sealed interface ConversionRatesViewState {
    data class Success(val currencyRates: List<CurrencyRate>) : ConversionRatesViewState

    data object Empty : ConversionRatesViewState
}
