@file:OptIn(ExperimentalCoroutinesApi::class)

package moe.ganen.konversi.data.repository

import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import moe.ganen.konversi.data.model.CurrencyRate
import moe.ganen.konversi.data.model.network.NetworkCurrencyRateResponse
import moe.ganen.konversi.data.repository.local.KonversiLocalDataSource
import moe.ganen.konversi.data.repository.network.KonversiNetworkDataSource
import moe.ganen.konversi.data.utils.Synchronizer
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CurrencyRateRepositoryTest {
    private val testScope = TestScope(UnconfinedTestDispatcher())

    private val _currencyRates = MutableStateFlow<List<CurrencyRate>>(emptyList())

    private val networkDataSource: KonversiNetworkDataSource = mockk()
    private val localDataSource: KonversiLocalDataSource = mockk {
        coEvery { getCurrencyRates() } returns _currencyRates
    }

    private lateinit var repository: CurrencyRateRepository

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
                    val newValue = networkDataSource.fetchCurrencyRates()
                    localDataSource.upsertCurrencyRates(newValue.rates)
                },
            )
        }
    }

    private val mockNetworkCurrencyRateResponse = NetworkCurrencyRateResponse(
        base = "USD",
        timestamp = 0L,
        rates = mapOf("IDR" to 15000.0),
    )

    @Before
    fun setup() {
        repository = CurrencyRateRepositoryImpl(
            networkDataSource = networkDataSource, localDataSource = localDataSource,
        )
    }

    /**
     * WHEN: Invoke repository's get last fetch time.
     * THEN: Local data source's get currency rate fetch time will be invoked.
     */
    @Test
    fun `test when get last fetch time from local data source`() =
        testScope.runTest {
            val currentTime = Instant.fromEpochMilliseconds(0)
            every { localDataSource.getLastCurrencyRatesFetchTime() } returns currentTime

            val result = repository.getLastFetchTime()
            assertEquals(currentTime, result)

            coVerify { localDataSource.getCurrencyRates() }
            coVerify { localDataSource.getLastCurrencyRatesFetchTime() }
            confirmVerified(localDataSource)
        }

    /**
     * WHEN: Invoke repository's update last fetch time.
     * THEN: Local data source's update currency rate fetch time will be invoked.
     */
    @Test
    fun `test when update last fetch time then local data source will update last fetch time`() =
        testScope.runTest {
            coJustRun { localDataSource.updateLastCurrencyRateFetchTime(any()) }

            val currentTime = Instant.fromEpochMilliseconds(0)
            repository.updateLastFetchTime(currentTime)

            coVerify { localDataSource.getCurrencyRates() }
            coVerify { localDataSource.updateLastCurrencyRateFetchTime(currentTime) }
            confirmVerified(localDataSource)
        }

    /**
     * WHEN: Last currency rate fetch time is null (indicate that never fetch currency rate)
     * THEN: Synchronizer will do actual sync.
     */
    @Test
    fun `test when get last currency rates fetch time and result is null then try to sync`() =
        testScope.runTest {
            // mock initial fetch time
            coEvery { localDataSource.getLastCurrencyRatesFetchTime() } returns null

            // mock time validation action.
            coJustRun { localDataSource.updateLastCurrencyRateFetchTime(any()) }

            // mock data fetch and sync action.
            coEvery { networkDataSource.fetchCurrencyRates() } returns mockNetworkCurrencyRateResponse
            coJustRun { localDataSource.upsertCurrencyRates(any()) }

            val result = repository.getLastFetchTime()
            assertNull(result)

            val syncResult = repository.syncWith(synchronizer)
            assertTrue(syncResult)

            // verify all calls.
            coVerify { localDataSource.getCurrencyRates() }
            coVerify(exactly = 2) { localDataSource.getLastCurrencyRatesFetchTime() }
            coVerify { localDataSource.updateLastCurrencyRateFetchTime(any()) }
            coVerify { localDataSource.upsertCurrencyRates(any()) }
            coVerify { networkDataSource.fetchCurrencyRates() }
            confirmVerified(localDataSource)
            confirmVerified(networkDataSource)
        }

    /**
     * GIVEN: Last currency rate fetch time is null (indicate that never fetch currency rate)
     * WHEN: Error when update (exception during update block)
     * THEN: Synchronizer will do actual sync but return false.
     */
    @Test
    fun `test when get last currency rates fetch time and result is null then try to sync but exception when update`() =
        testScope.runTest {
            // mock initial fetch time
            coEvery { localDataSource.getLastCurrencyRatesFetchTime() } returns null

            // mock time validation action.
            coJustRun { localDataSource.updateLastCurrencyRateFetchTime(any()) }

            // mock data fetch and sync action. not that this fetchCurrencyRates will throw exception since it tries
            // to cast Unit as NetworkCurrencyRateResponse.
            coJustRun { networkDataSource.fetchCurrencyRates() }
            coJustRun { localDataSource.upsertCurrencyRates(any()) }

            val result = repository.getLastFetchTime()
            assertNull(result)

            val syncResult = repository.syncWith(synchronizer)
            assertFalse(syncResult)

            // verify all calls.
            coVerify { localDataSource.getCurrencyRates() }
            coVerify(exactly = 2) { localDataSource.getLastCurrencyRatesFetchTime() }
            coVerify { localDataSource.updateLastCurrencyRateFetchTime(any()) }
            coVerify { networkDataSource.fetchCurrencyRates() }
            confirmVerified(localDataSource)
            confirmVerified(networkDataSource)
        }

    /**
     * WHEN: Last currency rate fetch time is more than 30 minutes.
     * THEN: Synchronizer will do actual sync and return true.
     */
    @Test
    fun `test when get last currency rates fetch time and result is more than 30 minutes then try to sync`() =
        testScope.runTest {
            // mock initial fetch time
            coEvery { localDataSource.getLastCurrencyRatesFetchTime() } returns Instant.fromEpochMilliseconds(
                0,
            )

            // mock time validation action.
            coJustRun { localDataSource.updateLastCurrencyRateFetchTime(any()) }

            // mock data fetch and sync action.
            coEvery { networkDataSource.fetchCurrencyRates() } returns mockNetworkCurrencyRateResponse
            coJustRun { localDataSource.upsertCurrencyRates(any()) }

            val result = repository.getLastFetchTime()
            assertNotNull(result)

            val syncResult = repository.syncWith(synchronizer)
            assertTrue(syncResult)

            // verify all calls.
            coVerify { localDataSource.getCurrencyRates() }
            coVerify(exactly = 2) { localDataSource.getLastCurrencyRatesFetchTime() }
            coVerify { localDataSource.updateLastCurrencyRateFetchTime(any()) }
            coVerify { localDataSource.upsertCurrencyRates(any()) }
            coVerify { networkDataSource.fetchCurrencyRates() }
            confirmVerified(localDataSource)
            confirmVerified(networkDataSource)
        }

    /**
     * GIVEN: Last currency rate fetch time is more than 30 minutes.
     * WHEN: Error when update (exception during update block)
     * THEN: Synchronizer will do actual sync but return false.
     */
    @Test
    fun `test when get last currency rates fetch time and result is more than 30 minutes then try to sync but exception when update`() =
        testScope.runTest {
            // mock initial fetch time
            coEvery { localDataSource.getLastCurrencyRatesFetchTime() } returns Instant.fromEpochMilliseconds(
                0,
            )

            // mock time validation action.
            coJustRun { localDataSource.updateLastCurrencyRateFetchTime(any()) }

            // mock data fetch and sync action. not that this fetchCurrencyRates will throw exception since it tries
            // to cast Unit as NetworkCurrencyRateResponse.
            coJustRun { networkDataSource.fetchCurrencyRates() }
            coJustRun { localDataSource.upsertCurrencyRates(any()) }

            val result = repository.getLastFetchTime()
            assertNotNull(result)

            val syncResult = repository.syncWith(synchronizer)
            assertFalse(syncResult)

            // verify all calls.
            coVerify { localDataSource.getCurrencyRates() }
            coVerify(exactly = 2) { localDataSource.getLastCurrencyRatesFetchTime() }
            coVerify { localDataSource.updateLastCurrencyRateFetchTime(any()) }
            coVerify { networkDataSource.fetchCurrencyRates() }
            confirmVerified(localDataSource)
            confirmVerified(networkDataSource)
        }

    /**
     * GIVEN: currency rate USD to IDR is 15,878.75 and USD to JPY is 149.86.
     * WHEN: trying to convert currency rates to IDR
     * THEN: currency rates will updated to its rate compared to IDR.
     */
    @Test
    fun `test when convert currency and success`() = testScope.runTest {
    }
}
