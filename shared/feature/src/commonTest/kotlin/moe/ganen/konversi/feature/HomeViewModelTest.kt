@file:OptIn(ExperimentalCoroutinesApi::class)

package moe.ganen.konversi.feature

import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import moe.ganen.konversi.data.model.Currency
import moe.ganen.konversi.data.model.CurrencyRate
import moe.ganen.konversi.data.repository.CurrenciesRepository
import moe.ganen.konversi.data.repository.CurrencyRateRepository
import moe.ganen.konversi.data.utils.NetworkMonitor
import moe.ganen.konversi.data.utils.SyncManager
import moe.ganen.konversi.feature.utils.MainDispatcherRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    // backing-up state flows for mock, making sure we can control or mock what's the expected stream looks like.
    private val _currencies = MutableStateFlow<List<Currency>>(emptyList())
    private val _currencyRate = MutableStateFlow<List<CurrencyRate>>(emptyList())
    private val _isSync = MutableStateFlow(false)
    private val _isOnline = MutableStateFlow(false)

    private val currenciesRepository: CurrenciesRepository = mockk {
        every { currencies } returns _currencies
    }

    private val currencyRateRepository: CurrencyRateRepository = mockk {
        every { currencyRate } returns _currencyRate
    }

    private val syncManager: SyncManager = mockk {
        every { isSyncing } returns _isSync
    }

    private val networkMonitor: NetworkMonitor = mockk {
        every { isOnline } returns _isOnline
    }

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        viewModel =
            HomeViewModel(syncManager, networkMonitor, currenciesRepository, currencyRateRepository)
    }

    /**
     * WHEN: user input value on text field
     * THEN: inputted value will be updated and will invoke convert currency rate.
     */
    @Test
    fun `when user update input then inputted value will be updated`() = runTest {
        coJustRun { currencyRateRepository.convertCurrencyRate(any(), any()) }

        // assert initial value
        assertEquals(1.0, viewModel.inputtedValue, 0.0)

        viewModel.updateInput("10000.0")
        advanceUntilIdle()
        assertEquals(10000.0, viewModel.inputtedValue, 0.0)
        coVerify { currencyRateRepository.convertCurrencyRate(10000.0, any()) }

        viewModel.updateInput("ayaya")
        advanceUntilIdle()
        assertEquals(1.0, viewModel.inputtedValue, 0.0)
        coVerify { currencyRateRepository.convertCurrencyRate(1.0, any()) }

        coVerify { currenciesRepository.currencies }
        confirmVerified(currenciesRepository)
    }

    /**
     * WHEN: user update or select new currency rate.
     * THEN: currency rate will change and invoke convert currency rate.
     */
    @Test
    fun `test when user update selected rate`() = runTest {
        coJustRun { currencyRateRepository.convertCurrencyRate(any(), any()) }

        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.selectedCurrencyCode.collect()
        }

        assertEquals("USD", viewModel.selectedCurrencyCode.value)

        viewModel.updateSelectedRate("IDR")
        assertEquals("IDR", viewModel.selectedCurrencyCode.value)
        coVerify { currencyRateRepository.convertCurrencyRate(any(), "IDR") }

        // Open Exchange Rate code is not always 3-letter
        viewModel.updateSelectedRate("AYAYA")
        assertEquals("AYAYA", viewModel.selectedCurrencyCode.value)
        coVerify { currencyRateRepository.convertCurrencyRate(any(), "AYAYA") }

        collectJob.cancel()
        coVerify { currencyRateRepository.currencyRate }
        confirmVerified(currencyRateRepository)
    }

    /**
     * WHEN: UI is collecting initial currencies view state.
     * THEN: Collected view state should be Empty.
     */
    @Test
    fun `test when initial collect currencies view state then it should be empty`() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.currenciesViewState.collect()
        }

        assertTrue(viewModel.currenciesViewState.value is CurrenciesViewState.Empty)
        collectJob.cancel()
    }

    /**
     * GIVEN: Initial currencies view state is empty.
     * WHEN: New value is being emitted to currencies stream.
     * THEN: Collected currencies view state should be success and non-empty.
     */
    @Test
    fun `test when currencies updated then currencies view state is updated`() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.currenciesViewState.collect()
        }
        assertTrue(viewModel.currenciesViewState.value is CurrenciesViewState.Empty)

        // mocking new value emitted to the currencies view state.
        _currencies.emit(listOf(Currency("IDR", "Indonesian Rupiah")))

        val collected = viewModel.currenciesViewState.value
        assertTrue(collected is CurrenciesViewState.Success)
        assertTrue(collected.currencies.isNotEmpty())

        collectJob.cancel()
    }

    /**
     * WHEN: UI is collecting initial conversion rates view state.
     * THEN: Collected view state should be Empty.
     */
    @Test
    fun `test when initial collect conversion rate view state then it should be empty`() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.conversionRatesViewState.collect()
        }

        assertTrue(viewModel.conversionRatesViewState.value is ConversionRatesViewState.Empty)
        collectJob.cancel()
    }

    /**
     * GIVEN: Initial currencies view state is empty.
     * WHEN: New value is being emitted to currencies rate stream.
     * THEN: Collected currency rates view state should be success and non-empty.
     */
    @Test
    fun `test when conversion rate updated then conversion rate view state is updated`() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.conversionRatesViewState.collect()
        }
        assertTrue(viewModel.conversionRatesViewState.value is ConversionRatesViewState.Empty)

        // mocking new value emitted to the currency rate view state.
        _currencyRate.emit(
            listOf(
                CurrencyRate(
                    Currency("IDR", "Indonesian Rupiah"),
                    rate = 15800.0,
                ),
            ),
        )

        val collected = viewModel.conversionRatesViewState.value
        assertTrue(collected is ConversionRatesViewState.Success)
        assertTrue(collected.currencyRates.isNotEmpty())

        collectJob.cancel()
    }

    /**
     * WHEN: Sync manager is running.
     * THEN: is syncing is true, indicating loading.
     */
    @Test
    fun `test when sync manager still running then show loading`() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            syncManager.isSyncing.collect()

            // mocking is loading
            _isSync.value = true
            assertTrue(viewModel.isSyncing.value)
        }
        collectJob.cancel()
    }

    /**
     * WHEN: Sync manager is not running.
     * THEN: is syncing is false, indicating not loading.
     */
    @Test
    fun `test when sync manager not running then not showing loading`() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            syncManager.isSyncing.collect()

            // mocking not loading
            _isSync.value = false
            assertFalse(viewModel.isSyncing.value)
        }
        collectJob.cancel()
    }
}
