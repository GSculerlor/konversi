package moe.ganen.konversi.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import moe.ganen.konversi.data.model.CurrencyRate
import moe.ganen.konversi.data.repository.local.KonversiLocalDataSource
import moe.ganen.konversi.data.repository.network.KonversiNetworkDataSource
import moe.ganen.konversi.data.utils.CurrencyHelper
import moe.ganen.konversi.data.utils.FetchTimeManager
import moe.ganen.konversi.data.utils.Syncable
import moe.ganen.konversi.data.utils.Synchronizer
import moe.ganen.konversi.data.utils.mutableStateIn

/**
 * Data layer implementation for handling currency rate data flow.
 */
interface CurrencyRateRepository : FetchTimeManager, Syncable {
    /**
     * Stream of list of [CurrencyRate].
     */
    val currencyRate: StateFlow<List<CurrencyRate>>

    suspend fun convertCurrencyRate(value: Double, targetCurrencyCode: String)
}

/**
 * Implementation of [CurrencyRateRepository].
 */
class CurrencyRateRepositoryImpl(
    private val networkDataSource: KonversiNetworkDataSource,
    private val localDataSource: KonversiLocalDataSource,
) : CurrencyRateRepository {

    private val _currencyRate: MutableStateFlow<List<CurrencyRate>> =
        localDataSource.getCurrencyRates().mutableStateIn(
            CoroutineScope(Dispatchers.IO),
            emptyList(),
        )

    override val currencyRate: StateFlow<List<CurrencyRate>>
        get() = _currencyRate.asStateFlow()

    override fun getLastFetchTime(): Instant? {
        return localDataSource.getLastCurrencyRatesFetchTime()
    }

    override fun updateLastFetchTime(time: Instant) {
        localDataSource.updateLastCurrencyRateFetchTime(time)
    }

    override suspend fun convertCurrencyRate(
        value: Double,
        targetCurrencyCode: String,
    ) {
        val targetCurrencyRate = localDataSource.getCurrencyRate(targetCurrencyCode).first()

        localDataSource.getCurrencyRates().collectLatest { currencyRates ->
            _currencyRate.value = currencyRates.map {
                it.copy(
                    rate = CurrencyHelper.calculateRate(
                        value = value,
                        targetToUSD = it.rate,
                        initialToUSD = targetCurrencyRate.rate,
                    ),
                )
            }
        }
    }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean {
        return synchronizer.syncData(
            lastSuccessFetchReader = ::getLastFetchTime,
            lastSuccessFetchUpdater = ::updateLastFetchTime,
            stillOnValidTimeRange = ::stillOnValidRange,
            update = {
                val newValue = networkDataSource.fetchCurrencyRates()
                localDataSource.upsertCurrencyRates(newValue.rates)
            },
        )
    }
}
