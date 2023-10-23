package moe.ganen.konversi.data.database.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import moe.ganen.konversi.data.KonversiDatabase
import moe.ganen.konversi.data.model.Currency
import moe.ganen.konversi.data.model.CurrencyRate

class CurrenciesDao(private val database: KonversiDatabase) {
    fun getCurrencies(): Flow<List<Currency>> {
        return database.databaseQueries.getCurrencies().asFlow().mapToList(Dispatchers.IO)
            .map { currencies -> currencies.map { Currency(it.code, it.name) } }
    }

    fun upsertCurrencies(currencies: List<Currency>) {
        currencies.toList().forEach { (code, name) ->
            database.databaseQueries.addCurrencies(code, name)
        }
    }

    fun getCurrencyRates(): Flow<List<CurrencyRate>> {
        return database.databaseQueries.getCurrencyRates().asFlow().mapToList(Dispatchers.IO)
            .map { currencyRates ->
                currencyRates.map { currencyRate ->
                    CurrencyRate(
                        currency = Currency(currencyRate.code, currencyRate.name),
                        rate = currencyRate.rate,
                    )
                }
            }
    }

    fun getCurrencyRate(code: String): Flow<CurrencyRate> {
        return database.databaseQueries.getCurrencyRate(code).asFlow().mapToOne(Dispatchers.IO)
            .map { currencyRate ->
                CurrencyRate(
                    currency = Currency(currencyRate.code, currencyRate.name),
                    rate = currencyRate.rate,
                )
            }
    }

    fun upsertCurrencyRates(currencyRates: Map<String, Double>) {
        currencyRates.toList().forEach { (code, rate) ->
            database.databaseQueries.addCurrencyRates(code, rate)
        }
    }
}
