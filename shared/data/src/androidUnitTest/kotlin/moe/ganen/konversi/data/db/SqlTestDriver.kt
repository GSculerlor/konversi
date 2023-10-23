package moe.ganen.konversi.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import moe.ganen.konversi.data.KonversiDatabase

internal actual fun createTestSqlDriver(): SqlDriver {
    return JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
        KonversiDatabase.Schema.create(this)
    }
}
