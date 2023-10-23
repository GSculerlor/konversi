package moe.ganen.konversi.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope

actual abstract class ViewModel actual constructor() : ViewModel() {
    actual val coroutineScope: CoroutineScope = viewModelScope

    actual override fun onCleared() {
        super.onCleared()
    }
}
