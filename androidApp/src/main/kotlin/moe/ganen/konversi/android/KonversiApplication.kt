package moe.ganen.konversi.android

import android.app.Application
import moe.ganen.konversi.data.di.dataModules
import moe.ganen.konversi.feature.di.featureModule
import moe.ganen.konversi.worker.Sync
import moe.ganen.konversi.worker.di.workerModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin

class KonversiApplication : Application(), KoinComponent {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@KonversiApplication)
            workManagerFactory()

            modules(dataModules)
            modules(featureModule, workerModule)
        }

        Sync.initialize(context = this)
    }
}
