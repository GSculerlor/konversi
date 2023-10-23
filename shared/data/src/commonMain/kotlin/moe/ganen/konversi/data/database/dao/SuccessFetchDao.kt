package moe.ganen.konversi.data.database.dao

import kotlinx.datetime.Instant
import moe.ganen.konversi.data.KonversiDatabase

class SuccessFetchDao(private val database: KonversiDatabase) {
    fun getLastCurrenciesFetch(): Instant? {
        return database.databaseQueries.getLastCurrenciesFetch()
            .executeAsOneOrNull()?.last_currrencies_fetch
    }

    fun updateLastCurrenciesFetch(timestamp: Instant) {
        database.databaseQueries.updateLastCurrenciesFetch(last_currrencies_fetch = timestamp)
    }

    fun getLastCurrencyRateFetch(): Instant? {
        return database.databaseQueries.getLastCurrencyRateFetch()
            .executeAsOneOrNull()?.last_currency_rate_fetch
    }

    fun updateLastCurrencyRateFetch(timestamp: Instant) {
        database.databaseQueries.updateLastCurrencyRateFetch(last_currency_rate_fetch = timestamp)
    }
}
