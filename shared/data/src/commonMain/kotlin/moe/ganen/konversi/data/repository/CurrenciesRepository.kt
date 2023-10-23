package moe.ganen.konversi.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Instant
import moe.ganen.konversi.data.model.Currency
import moe.ganen.konversi.data.repository.local.KonversiLocalDataSource
import moe.ganen.konversi.data.repository.network.KonversiNetworkDataSource
import moe.ganen.konversi.data.utils.FetchTimeManager
import moe.ganen.konversi.data.utils.Syncable
import moe.ganen.konversi.data.utils.Synchronizer
import moe.ganen.konversi.data.utils.mutableStateIn

/**
 * Data layer implementation for handling currency data flow.
 */
interface CurrenciesRepository : FetchTimeManager, Syncable {
    /**
     * Stream of list of [Currency].
     */
    val currencies: StateFlow<List<Currency>>
}

class CurrenciesRepositoryImpl(
    private val networkDataSource: KonversiNetworkDataSource,
    private val localDataSource: KonversiLocalDataSource,
) : CurrenciesRepository {

    override val currencies: StateFlow<List<Currency>>
        get() = localDataSource.getCurrencies()
            .mutableStateIn(CoroutineScope(Dispatchers.IO), emptyList())

    override fun getLastFetchTime(): Instant? {
        return localDataSource.getLastCurrenciesFetchTime()
    }

    override fun updateLastFetchTime(time: Instant) {
        localDataSource.updateLastCurrenciesFetchTime(time)
    }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean {
        return synchronizer.syncData(
            lastSuccessFetchReader = ::getLastFetchTime,
            lastSuccessFetchUpdater = ::updateLastFetchTime,
            stillOnValidTimeRange = ::stillOnValidRange,
            update = {
                val newValue = networkDataSource.fetchCurrencies()
                localDataSource.upsertCurrencies(newValue)
            },
        )
    }
}
