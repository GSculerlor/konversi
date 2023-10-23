@file:OptIn(ExperimentalCoroutinesApi::class)

package moe.ganen.konversi.data.repository

import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import moe.ganen.konversi.data.model.Currency
import moe.ganen.konversi.data.repository.local.KonversiLocalDataSource
import moe.ganen.konversi.data.repository.network.KonversiNetworkDataSource
import moe.ganen.konversi.data.utils.Synchronizer
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CurrenciesRepositoryTest {

    private val testScope = TestScope(UnconfinedTestDispatcher())

    private val _currencies = MutableStateFlow<List<Currency>>(emptyList())

    private val networkDataSource: KonversiNetworkDataSource = mockk()
    private val localDataSource: KonversiLocalDataSource = mockk {
        coEvery { getCurrencies() } returns _currencies
    }

    private lateinit var repository: CurrenciesRepository

    // mocking synchronizer (which is the worker on the actual implementation)
    private val synchronizer: Synchronizer = object : Synchronizer {
        override suspend fun syncData(
            lastSuccessFetchReader: () -> Instant?,
            lastSuccessFetchUpdater: (Instant) -> Unit,
            stillOnValidTimeRange: (Instant?) -> Boolean,
            update: suspend () -> Unit,
        ): Boolean {
            return super.syncData(
                lastSuccessFetchReader = repository::getLastFetchTime,
                lastSuccessFetchUpdater = repository::updateLastFetchTime,
                stillOnValidTimeRange = repository::stillOnValidRange,
                update = {
                    val newValue = networkDataSource.fetchCurrencies()
                    localDataSource.upsertCurrencies(newValue)
                },
            )
        }
    }

    private val mockNetworkCurrenciesResponse = listOf(
        Currency("IDR", "Indonesian Rupiah"),
        Currency("JPY", "Japanese Yen"),
        Currency("USD", "United States Dollar"),
    )

    @Before
    fun setup() {
        repository = CurrenciesRepositoryImpl(
            networkDataSource = networkDataSource, localDataSource = localDataSource,
        )
    }

    /**
     * WHEN: Invoke repository's get last fetch time.
     * THEN: Local data source's get currencies fetch time will be invoked.
     */
    @Test
    fun `test when get last fetch time from local data source`() =
        testScope.runTest {
            val currentTime = Instant.fromEpochMilliseconds(0)
            every { localDataSource.getLastCurrenciesFetchTime() } returns currentTime

            val result = repository.getLastFetchTime()
            assertEquals(currentTime, result)

            coVerify { localDataSource.getLastCurrenciesFetchTime() }
            confirmVerified(localDataSource)
        }

    /**
     * WHEN: Invoke repository's update last fetch time.
     * THEN: Local data source's update currencies fetch time will be invoked.
     */
    @Test
    fun `test when update last fetch time then local data source will update last fetch time`() =
        testScope.runTest {
            coJustRun { localDataSource.updateLastCurrenciesFetchTime(any()) }

            val currentTime = Instant.fromEpochMilliseconds(0)
            repository.updateLastFetchTime(currentTime)

            coVerify { localDataSource.updateLastCurrenciesFetchTime(currentTime) }
            confirmVerified(localDataSource)
        }

    /**
     * WHEN: Last currencies fetch time is null (indicate that never fetch currencies)
     * THEN: Synchronizer will do actual sync.
     */
    @Test
    fun `test when get last currencies fetch time and result is null then try to sync`() =
        testScope.runTest {
            // mock initial fetch time
            coEvery { localDataSource.getLastCurrenciesFetchTime() } returns null

            // mock time validation action.
            coJustRun { localDataSource.updateLastCurrenciesFetchTime(any()) }

            // mock data fetch and sync action.
            coEvery { networkDataSource.fetchCurrencies() } returns mockNetworkCurrenciesResponse
            coJustRun { localDataSource.upsertCurrencies(any()) }

            val result = repository.getLastFetchTime()
            assertNull(result)

            val syncResult = repository.syncWith(synchronizer)
            assertTrue(syncResult)

            // verify all calls.
            coVerify(exactly = 2) { localDataSource.getLastCurrenciesFetchTime() }
            coVerify { localDataSource.updateLastCurrenciesFetchTime(any()) }
            coVerify { localDataSource.upsertCurrencies(any()) }
            coVerify { networkDataSource.fetchCurrencies() }
            confirmVerified(localDataSource)
            confirmVerified(networkDataSource)
        }

    /**
     * GIVEN: Last currencies fetch time is null (indicate that never fetch currencies)
     * WHEN: Error when update (exception during update block)
     * THEN: Synchronizer will do actual sync but return false.
     */
    @Test
    fun `test when get last currencies fetch time and result is null then try to sync but exception when update`() =
        testScope.runTest {
            // mock initial fetch time
            coEvery { localDataSource.getLastCurrenciesFetchTime() } returns null

            // mock time validation action.
            coJustRun { localDataSource.updateLastCurrenciesFetchTime(any()) }

            // mock data fetch and sync action. not that this fetchCurrencies will throw exception since it tries
            // to cast Unit as NetworkCurrencyRateResponse.
            coJustRun { networkDataSource.fetchCurrencies() }
            coJustRun { localDataSource.upsertCurrencies(any()) }

            val result = repository.getLastFetchTime()
            assertNull(result)

            val syncResult = repository.syncWith(synchronizer)
            TestCase.assertFalse(syncResult)

            // verify all calls.
            coVerify(exactly = 2) { localDataSource.getLastCurrenciesFetchTime() }
            coVerify { localDataSource.updateLastCurrenciesFetchTime(any()) }
            coVerify { networkDataSource.fetchCurrencies() }
            confirmVerified(localDataSource)
            confirmVerified(networkDataSource)
        }

    /**
     * WHEN: Last currencies fetch time is more than 30 minutes.
     * THEN: Synchronizer will do actual sync and return true.
     */
    @Test
    fun `test when get last currencies fetch time and result is more than 30 minutes then try to sync`() =
        testScope.runTest {
            // mock initial fetch time
            coEvery { localDataSource.getLastCurrenciesFetchTime() } returns Instant.fromEpochMilliseconds(
                0,
            )

            // mock time validation action.
            coJustRun { localDataSource.updateLastCurrenciesFetchTime(any()) }

            // mock data fetch and sync action.
            coEvery { networkDataSource.fetchCurrencies() } returns mockNetworkCurrenciesResponse
            coJustRun { localDataSource.upsertCurrencies(any()) }

            val result = repository.getLastFetchTime()
            assertNotNull(result)

            val syncResult = repository.syncWith(synchronizer)
            assertTrue(syncResult)

            // verify all calls.
            coVerify(exactly = 2) { localDataSource.getLastCurrenciesFetchTime() }
            coVerify { localDataSource.updateLastCurrenciesFetchTime(any()) }
            coVerify { localDataSource.upsertCurrencies(any()) }
            coVerify { networkDataSource.fetchCurrencies() }
            confirmVerified(localDataSource)
            confirmVerified(networkDataSource)
        }

    /**
     * GIVEN: Last currencies fetch time is more than 30 minutes.
     * WHEN: Error when update (exception during update block)
     * THEN: Synchronizer will do actual sync but return false.
     */
    @Test
    fun `test when get last currencies fetch time and result is more than 30 minutes then try to sync but exception when update`() =
        testScope.runTest {
            // mock initial fetch time
            coEvery { localDataSource.getLastCurrenciesFetchTime() } returns Instant.fromEpochMilliseconds(
                0,
            )

            // mock time validation action.
            coJustRun { localDataSource.updateLastCurrenciesFetchTime(any()) }

            // mock data fetch and sync action. not that this fetchCurrencies will throw exception since it tries
            // to cast Unit as List<Currency>.
            coJustRun { networkDataSource.fetchCurrencies() }
            coJustRun { localDataSource.upsertCurrencies(any()) }

            val result = repository.getLastFetchTime()
            assertNotNull(result)

            val syncResult = repository.syncWith(synchronizer)
            TestCase.assertFalse(syncResult)

            // verify all calls.
            coVerify(exactly = 2) { localDataSource.getLastCurrenciesFetchTime() }
            coVerify { localDataSource.updateLastCurrenciesFetchTime(any()) }
            coVerify { networkDataSource.fetchCurrencies() }
            confirmVerified(localDataSource)
            confirmVerified(networkDataSource)
        }
}
