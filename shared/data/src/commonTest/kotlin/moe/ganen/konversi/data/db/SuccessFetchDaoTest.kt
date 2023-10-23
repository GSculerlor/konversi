@file:OptIn(ExperimentalCoroutinesApi::class)

package moe.ganen.konversi.data.db

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import moe.ganen.konversi.data.KonversiDatabase
import moe.ganen.konversi.data.SuccessFetch
import moe.ganen.konversi.data.database.adapter.InstantStringColumnAdapter
import moe.ganen.konversi.data.database.dao.SuccessFetchDao
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SuccessFetchDaoTest {
    private val testScope = TestScope(UnconfinedTestDispatcher())

    private val database = KonversiDatabase(
        createTestSqlDriver(),
        successFetchAdapter = SuccessFetch.Adapter(
            last_currency_rate_fetchAdapter = InstantStringColumnAdapter,
            last_currrencies_fetchAdapter = InstantStringColumnAdapter,
        ),
    )
    private val dao = SuccessFetchDao(database)

    @Test
    fun `test initial table state`() = testScope.runTest {
        val lastCurrenciesFetch = database.databaseQueries.getLastCurrenciesFetch()
            .executeAsOneOrNull()?.last_currrencies_fetch
        assertNull(lastCurrenciesFetch)

        val lastCurrencyRatesFetch = database.databaseQueries.getLastCurrencyRateFetch()
            .executeAsOneOrNull()?.last_currency_rate_fetch
        assertNull(lastCurrencyRatesFetch)
    }

    @Test
    fun `test get last currencies fetch`() = testScope.runTest {
        val time = Instant.fromEpochMilliseconds(100)
        database.databaseQueries.updateLastCurrenciesFetch(time)

        val lastCurrenciesFetch = database.databaseQueries.getLastCurrenciesFetch()
            .executeAsOneOrNull()?.last_currrencies_fetch
        assertEquals(time, lastCurrenciesFetch)

        val result = dao.getLastCurrenciesFetch()
        assertNotNull(result)
        assertEquals(time, result)
    }

    @Test
    fun `test update last currencies fetch`() = testScope.runTest {
        val time = Instant.fromEpochMilliseconds(100)
        database.databaseQueries.updateLastCurrenciesFetch(time)

        val lastCurrenciesFetch = database.databaseQueries.getLastCurrenciesFetch()
            .executeAsOneOrNull()?.last_currrencies_fetch
        assertEquals(time, lastCurrenciesFetch)

        val updatedTime = Instant.fromEpochMilliseconds(200)
        database.databaseQueries.updateLastCurrenciesFetch(updatedTime)

        val newCurrenciesFetch = database.databaseQueries.getLastCurrenciesFetch()
            .executeAsOneOrNull()?.last_currrencies_fetch
        assertEquals(updatedTime, newCurrenciesFetch)

        val result = dao.getLastCurrenciesFetch()
        assertNotNull(result)
        assertEquals(updatedTime, result)
    }

    @Test
    fun `test get last currency rate fetch`() = testScope.runTest {
        val time = Instant.fromEpochMilliseconds(100)
        database.databaseQueries.updateLastCurrencyRateFetch(time)

        val lastCurrencyRatesFetch = database.databaseQueries.getLastCurrencyRateFetch()
            .executeAsOneOrNull()?.last_currency_rate_fetch
        assertEquals(time, lastCurrencyRatesFetch)

        val result = dao.getLastCurrencyRateFetch()
        assertNotNull(result)
        assertEquals(time, result)
    }

    @Test
    fun `test update last currency rate fetch`() = testScope.runTest {
        val time = Instant.fromEpochMilliseconds(100)
        database.databaseQueries.updateLastCurrencyRateFetch(time)

        val lastCurrencyRatesFetch = database.databaseQueries.getLastCurrencyRateFetch()
            .executeAsOneOrNull()?.last_currency_rate_fetch
        assertEquals(time, lastCurrencyRatesFetch)

        val updatedTime = Instant.fromEpochMilliseconds(200)
        database.databaseQueries.updateLastCurrencyRateFetch(updatedTime)

        val newCurrencyRatesFetch = database.databaseQueries.getLastCurrencyRateFetch()
            .executeAsOneOrNull()?.last_currency_rate_fetch
        assertEquals(updatedTime, newCurrencyRatesFetch)

        val result = dao.getLastCurrencyRateFetch()
        assertNotNull(result)
        assertEquals(updatedTime, result)
    }
}
