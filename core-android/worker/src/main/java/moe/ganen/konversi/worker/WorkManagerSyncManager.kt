package moe.ganen.konversi.worker

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import moe.ganen.konversi.data.utils.SyncManager

class WorkManagerSyncManager(private val context: Context) : SyncManager {
    override val isSyncing: Flow<Boolean> =
        WorkManager.getInstance(context).getWorkInfosForUniqueWorkFlow(SyncWorkName)
            .map(List<WorkInfo>::anyRunning)
            .conflate()
}

private fun List<WorkInfo>.anyRunning() = any { it.state == State.RUNNING }
