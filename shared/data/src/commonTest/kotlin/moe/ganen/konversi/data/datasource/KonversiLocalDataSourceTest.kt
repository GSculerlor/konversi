@file:OptIn(ExperimentalCoroutinesApi::class)

package moe.ganen.konversi.data.datasource

import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import moe.ganen.konversi.data.database.dao.CurrenciesDao
import moe.ganen.konversi.data.database.dao.SuccessFetchDao
import moe.ganen.konversi.data.model.Currency
import moe.ganen.konversi.data.model.CurrencyRate
import moe.ganen.konversi.data.repository.local.KonversiLocalDataSource
import moe.ganen.konversi.data.repository.local.SqlDelightKonversiLocalDataSource
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KonversiLocalDataSourceTest {
    private val testScope = TestScope(UnconfinedTestDispatcher())

    private val successFetchDao: SuccessFetchDao = mockk()
    private val currenciesDao: CurrenciesDao = mockk()

    private val _currencyRates: MutableStateFlow<List<CurrencyRate>> = MutableStateFlow(emptyList())
    private val _currency: MutableStateFlow<List<Currency>> = MutableStateFlow(emptyList())

    private val dataSource: KonversiLocalDataSource = SqlDelightKonversiLocalDataSource(
        successFetchDao = successFetchDao,
        currenciesDao = currenciesDao,
    )

    @Test
    fun `test when get last currency rates fetch time and return null`() = testScope.runTest {
        every { successFetchDao.getLastCurrencyRateFetch() } returns null
        val result = dataSource.getLastCurrencyRatesFetchTime()

        assertNull(result)

        coVerify { successFetchDao.getLastCurrencyRateFetch() }
        confirmVerified(successFetchDao)
    }

    @Test
    fun `test when get last currency rates fetch time and return value`() = testScope.runTest {
        val time = Instant.fromEpochMilliseconds(100)

        every { successFetchDao.getLastCurrencyRateFetch() } returns time
        val result = dataSource.getLastCurrencyRatesFetchTime()

        assertNotNull(result)
        assertEquals(time, result)

        coVerify { successFetchDao.getLastCurrencyRateFetch() }
        confirmVerified(successFetchDao)
    }

    @Test
    fun `test when update last currency rate fetch time`() = testScope.runTest {
        val time = Instant.fromEpochMilliseconds(100)
        coJustRun { successFetchDao.updateLastCurrencyRateFetch(any()) }

        dataSource.updateLastCurrencyRateFetchTime(time)

        coVerify { successFetchDao.updateLastCurrencyRateFetch(time) }
        confirmVerified(successFetchDao)
    }

    @Test
    fun `test when get currency rates return list of currency rates stream`() = testScope.runTest {
        coEvery { currenciesDao.getCurrencyRates() } returns _currencyRates

        val expected = listOf(CurrencyRate(Currency("IDR", "Indonesian Rupiah"), 15878.75))
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            currenciesDao.getCurrencyRates().collect()
            _currencyRates.value = expected

            val result = currenciesDao.getCurrencyRates().first()
            assertTrue(result.isNotEmpty())
            assertEquals(expected, result)
        }

        coVerify { currenciesDao.getCurrencyRates() }
        confirmVerified(currenciesDao)

        collectJob.cancel()
    }

    @Test
    fun `test when upsert currency rates`() = testScope.runTest {
        coJustRun { currenciesDao.upsertCurrencyRates(any()) }
        val currencyRates = mapOf("IDR" to 15878.75, "USD" to 1.0)
        dataSource.upsertCurrencyRates(currencyRates)

        coVerify { currenciesDao.upsertCurrencyRates(currencyRates) }
        confirmVerified(currenciesDao)
    }

    @Test
    fun `test when get last currencies fetch time and return null`() = testScope.runTest {
        every { successFetchDao.getLastCurrenciesFetch() } returns null
        val result = dataSource.getLastCurrenciesFetchTime()

        assertNull(result)

        coVerify { successFetchDao.getLastCurrenciesFetch() }
        confirmVerified(successFetchDao)
    }

    @Test
    fun `test when get last currencies fetch time and return value`() = testScope.runTest {
        val time = Instant.fromEpochMilliseconds(100)

        every { successFetchDao.getLastCurrenciesFetch() } returns time
        val result = dataSource.getLastCurrenciesFetchTime()

        assertNotNull(result)
        assertEquals(time, result)

        coVerify { successFetchDao.getLastCurrenciesFetch() }
        confirmVerified(successFetchDao)
    }

    @Test
    fun `test when update last currencies fetch time`() = testScope.runTest {
        val time = Instant.fromEpochMilliseconds(100)
        coJustRun { successFetchDao.updateLastCurrenciesFetch(any()) }

        dataSource.updateLastCurrenciesFetchTime(time)

        coVerify { successFetchDao.updateLastCurrenciesFetch(time) }
        confirmVerified(successFetchDao)
    }

    @Test
    fun `test when get currencies return list of currencies stream`() = testScope.runTest {
        coEvery { currenciesDao.getCurrencies() } returns _currency

        val expected = listOf(Currency("IDR", "Indonesian Rupiah"))
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            currenciesDao.getCurrencies().collect()
            _currency.value = expected

            val result = currenciesDao.getCurrencies().first()
            assertTrue(result.isNotEmpty())
            assertEquals(expected, result)
        }

        coVerify { currenciesDao.getCurrencies() }
        confirmVerified(currenciesDao)

        collectJob.cancel()
    }

    @Test
    fun `test when upsert currencies`() = testScope.runTest {
        coJustRun { currenciesDao.upsertCurrencies(any()) }
        val currencyRates = listOf(Currency("IDR", "Indonesian Rupiah"))
        dataSource.upsertCurrencies(currencyRates)

        coVerify { currenciesDao.upsertCurrencies(currencyRates) }
        confirmVerified(currenciesDao)
    }
}
