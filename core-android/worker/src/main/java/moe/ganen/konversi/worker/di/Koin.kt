package moe.ganen.konversi.worker.di

import moe.ganen.konversi.data.utils.SyncManager
import moe.ganen.konversi.worker.WorkManagerSyncManager
import org.koin.dsl.module

val workerModule = module {
    single<SyncManager> { WorkManagerSyncManager(get()) }
}
