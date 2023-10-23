package moe.ganen.konversi.data.repository.local

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import moe.ganen.konversi.data.model.Currency
import moe.ganen.konversi.data.model.CurrencyRate

/**
 * Data layer that hold data locally. Note that this will be the main source of truth of the data that
 * displayed to the user.
 */
interface KonversiLocalDataSource {
    /**
     * Get the last success currency rate fetch time.
     */
    fun getLastCurrencyRatesFetchTime(): Instant?

    /**
     * Update the last success rate fetch time.
     */
    fun updateLastCurrencyRateFetchTime(time: Instant)

    /**
     * Get stream of list of [CurrencyRate].
     */
    fun getCurrencyRates(): Flow<List<CurrencyRate>>

    /**
     * Upsert local currency rates.
     */
    fun upsertCurrencyRates(currencyRates: Map<String, Double>)

    /**
     * Get the last success currencies fetch time.
     */
    fun getLastCurrenciesFetchTime(): Instant?

    /**
     * Update the last success rate fetch time.
     */
    fun updateLastCurrenciesFetchTime(time: Instant)

    /**
     * Get stream of list of [Currency].
     */
    fun getCurrencies(): Flow<List<Currency>>

    /**
     * Upsert local currencies.
     */
    fun upsertCurrencies(currencies: List<Currency>)

    fun getCurrencyRate(code: String): Flow<CurrencyRate>
}
