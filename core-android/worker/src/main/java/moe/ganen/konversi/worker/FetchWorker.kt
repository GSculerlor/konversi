package moe.ganen.konversi.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import moe.ganen.konversi.data.repository.CurrenciesRepository
import moe.ganen.konversi.data.repository.CurrencyRateRepository
import moe.ganen.konversi.data.utils.Synchronizer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FetchWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams), KoinComponent, Synchronizer {

    private val currenciesRepository: CurrenciesRepository by inject()
    private val currencyRateRepository: CurrencyRateRepository by inject()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // waiting all sync to success. if one of them is failed mark sync process as failed too.
        // this is to make sure the app behavior is correct (since there's no point if one of them is failing).
        val syncedSuccessfully = awaitAll(
            async { currenciesRepository.sync() },
            async { currencyRateRepository.sync() },
        ).all { it }

        if (syncedSuccessfully) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    companion object {
        fun startUpSyncWork() = OneTimeWorkRequestBuilder<FetchWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .setInputData(
                Data.Builder()
                    .putString("WorkerClassName", FetchWorker::class.qualifiedName)
                    .build(),
            ).build()
    }
}
