package moe.ganen.konversi.data.database

import app.cash.sqldelight.db.SqlDriver
import moe.ganen.konversi.data.KonversiDatabase
import moe.ganen.konversi.data.SuccessFetch
import moe.ganen.konversi.data.database.adapter.InstantStringColumnAdapter

class KonversiDatabaseFactory(private val driver: SqlDriver) {
    fun build(): KonversiDatabase {
        return KonversiDatabase(
            driver = driver,
            successFetchAdapter =
            SuccessFetch.Adapter(
                last_currency_rate_fetchAdapter = InstantStringColumnAdapter,
                last_currrencies_fetchAdapter = InstantStringColumnAdapter,
            ),
        )
    }
}
