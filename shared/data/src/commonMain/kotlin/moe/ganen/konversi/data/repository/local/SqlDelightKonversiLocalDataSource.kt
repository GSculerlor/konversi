package moe.ganen.konversi.data.repository.local

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import moe.ganen.konversi.data.database.dao.CurrenciesDao
import moe.ganen.konversi.data.database.dao.SuccessFetchDao
import moe.ganen.konversi.data.model.Currency
import moe.ganen.konversi.data.model.CurrencyRate

class SqlDelightKonversiLocalDataSource(
    private val successFetchDao: SuccessFetchDao,
    private val currenciesDao: CurrenciesDao,
) : KonversiLocalDataSource {
    override fun getLastCurrencyRatesFetchTime(): Instant? {
        return successFetchDao.getLastCurrencyRateFetch()
    }

    override fun updateLastCurrencyRateFetchTime(time: Instant) {
        successFetchDao.updateLastCurrencyRateFetch(time)
    }

    override fun getCurrencyRates(): Flow<List<CurrencyRate>> {
        return currenciesDao.getCurrencyRates()
    }

    override fun upsertCurrencyRates(currencyRates: Map<String, Double>) {
        currenciesDao.upsertCurrencyRates(currencyRates)
    }

    override fun getLastCurrenciesFetchTime(): Instant? {
        return successFetchDao.getLastCurrenciesFetch()
    }

    override fun updateLastCurrenciesFetchTime(time: Instant) {
        successFetchDao.updateLastCurrenciesFetch(time)
    }

    override fun getCurrencies(): Flow<List<Currency>> {
        return currenciesDao.getCurrencies()
    }

    override fun upsertCurrencies(currencies: List<Currency>) {
        currenciesDao.upsertCurrencies(currencies)
    }

    override fun getCurrencyRate(code: String): Flow<CurrencyRate> {
        return currenciesDao.getCurrencyRate(code)
    }
}
