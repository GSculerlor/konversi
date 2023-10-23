package moe.ganen.konversi.data.repository.network

import moe.ganen.konversi.data.model.Currency
import moe.ganen.konversi.data.model.network.NetworkCurrencyRateResponse

/**
 * Data layer that connect to the third-party API (Open Exchange Rates).
 */
interface KonversiNetworkDataSource {
    /**
     * Fetch currency rates via get latest on Open Exchange Rates.
     */
    suspend fun fetchCurrencyRates(): NetworkCurrencyRateResponse

    /**
     * Fetch currencies via get currencies on Open Exchange Rates.
     */
    suspend fun fetchCurrencies(): List<Currency>
}
