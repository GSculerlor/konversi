package moe.ganen.konversi.feature

import kotlinx.coroutines.CoroutineScope

expect abstract class ViewModel() {
    val coroutineScope: CoroutineScope

    protected open fun onCleared()
}
