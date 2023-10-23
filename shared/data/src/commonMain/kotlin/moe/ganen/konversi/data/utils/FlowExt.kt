package moe.ganen.konversi.data.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Flow extension function to collect it and continue the stream as [MutableStateFlow].
 */
fun <T> Flow<T>.mutableStateIn(
    scope: CoroutineScope,
    initialValue: T,
): MutableStateFlow<T> {
    val flow = MutableStateFlow(value = initialValue)

    scope.launch {
        this@mutableStateIn.collect(flow)
    }

    return flow
}
