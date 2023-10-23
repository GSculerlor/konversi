package moe.ganen.konversi.data.di

import android.app.Application
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.ktor.client.engine.android.Android
import moe.ganen.konversi.data.KonversiDatabase
import moe.ganen.konversi.data.utils.ConnectivityNetworkMonitor
import moe.ganen.konversi.data.utils.NetworkMonitor
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformDataModule(): Module =
    module {
        single { Android.create() }
        single<NetworkMonitor> { ConnectivityNetworkMonitor(get()) }
        single<SqlDriver> {
            AndroidSqliteDriver(
                schema = KonversiDatabase.Schema,
                context = get<Application>(),
                name = "konversi.db",
                callback =
                object : AndroidSqliteDriver.Callback(KonversiDatabase.Schema) {
                    override fun onConfigure(db: SupportSQLiteDatabase) {
                        db.enableWriteAheadLogging()
                        db.setForeignKeyConstraintsEnabled(true)
                    }
                },
            )
        }
    }
