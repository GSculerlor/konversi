package moe.ganen.konversi.data.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.concurrent.CancellationException

interface Synchronizer {
    suspend fun syncData(
        lastSuccessFetchReader: () -> Instant?,
        lastSuccessFetchUpdater: (Instant) -> Unit,
        stillOnValidTimeRange: (Instant?) -> Boolean,
        update: suspend () -> Unit,
    ) = suspendRunCatching {
        val lastSuccessTime = lastSuccessFetchReader()
        if (!stillOnValidTimeRange(lastSuccessTime)) {
            lastSuccessFetchUpdater(Clock.System.now())
            update()
        }
    }.isSuccess

    /**
     * Syntactic sugar to call [Syncable.syncWith] while omitting the synchronizer argument
     */
    suspend fun Syncable.sync() = this@sync.syncWith(this@Synchronizer)
}

interface Syncable {
    /**
     * Synchronizes the local database backing the repository with the network.
     * Returns if the sync was successful or not.
     */
    suspend fun syncWith(synchronizer: Synchronizer): Boolean
}

/**
 * Reports on if synchronization is in progress
 */
interface SyncManager {
    val isSyncing: Flow<Boolean>
}

private suspend fun <T> suspendRunCatching(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (cancellationException: CancellationException) {
    throw cancellationException
} catch (exception: Exception) {
    Result.failure(exception)
}
