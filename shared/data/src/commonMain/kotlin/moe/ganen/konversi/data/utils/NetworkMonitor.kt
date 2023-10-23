package moe.ganen.konversi.data.utils

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    val isOnline: Flow<Boolean>
}

expect class ConnectivityNetworkMonitor : NetworkMonitor
