package moe.ganen.konversi.feature

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.ganen.konversi.data.repository.CurrenciesRepository
import moe.ganen.konversi.data.repository.CurrencyRateRepository
import moe.ganen.konversi.data.utils.NetworkMonitor
import moe.ganen.konversi.data.utils.SyncManager

class HomeViewModel(
    syncManager: SyncManager,
    networkMonitor: NetworkMonitor,
    currenciesRepository: CurrenciesRepository,
    private val currencyRateRepository: CurrencyRateRepository,
) : ViewModel() {

    private var inputJob: Job? = null

    internal var inputtedValue: Double = 1.0

    private val _selectedCurrencyCode: MutableStateFlow<String> = MutableStateFlow("USD")
    val selectedCurrencyCode: StateFlow<String> = _selectedCurrencyCode.asStateFlow()

    val currenciesViewState: StateFlow<CurrenciesViewState> =
        currenciesRepository.currencies.map {
            if (it.isEmpty()) {
                CurrenciesViewState.Empty
            } else {
                CurrenciesViewState.Success(it)
            }
        }.stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CurrenciesViewState.Empty,
        )

    val conversionRatesViewState: StateFlow<ConversionRatesViewState> =
        currencyRateRepository.currencyRate.map {
            if (it.isEmpty()) {
                ConversionRatesViewState.Empty
            } else {
                ConversionRatesViewState.Success(it)
            }
        }.stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ConversionRatesViewState.Empty,
        )

    val isSyncing = syncManager.isSyncing
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    val isOnline = networkMonitor.isOnline
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true,
        )

    fun updateSelectedRate(code: String) {
        _selectedCurrencyCode.value = code
        viewModelScope.launch {
            convertCurrency(
                value = inputtedValue,
                targetCurrencyCode = code,
            )
        }
    }

    fun updateInput(input: String) {
        inputJob?.cancel()
        inputJob = viewModelScope.launch {
            delay(250)
            inputtedValue = input.toDoubleOrNull() ?: 1.0
            convertCurrency(
                value = inputtedValue,
                targetCurrencyCode = _selectedCurrencyCode.value,
            )
        }
    }

    private suspend fun convertCurrency(
        value: Double,
        targetCurrencyCode: String,
    ) {
        currencyRateRepository.convertCurrencyRate(
            value = value,
            targetCurrencyCode = targetCurrencyCode,
        )
    }
}
