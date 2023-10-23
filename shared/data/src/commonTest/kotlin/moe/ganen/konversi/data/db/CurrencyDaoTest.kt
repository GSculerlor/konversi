@file:OptIn(ExperimentalCoroutinesApi::class)

package moe.ganen.konversi.data.db

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import moe.ganen.konversi.data.KonversiDatabase
import moe.ganen.konversi.data.SuccessFetch
import moe.ganen.konversi.data.database.adapter.InstantStringColumnAdapter
import moe.ganen.konversi.data.database.dao.CurrenciesDao
import moe.ganen.konversi.data.model.Currency
import moe.ganen.konversi.data.model.CurrencyRate
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CurrencyDaoTest {

    private val testScope = TestScope(UnconfinedTestDispatcher())

    private val database = KonversiDatabase(
        createTestSqlDriver(),
        successFetchAdapter = SuccessFetch.Adapter(
            last_currency_rate_fetchAdapter = InstantStringColumnAdapter,
            last_currrencies_fetchAdapter = InstantStringColumnAdapter,
        ),
    )
    private val dao = CurrenciesDao(database)

    @Test
    fun `test when get currencies`() = testScope.runTest {
        val expected = listOf(Currency("IDR", "Indonesian Rupiah"))

        val dbQueries = database.databaseQueries
        dbQueries.addCurrencies("IDR", "Indonesian Rupiah")

        val currentCurrencies = dbQueries.getCurrencies().executeAsList()
        assertTrue(currentCurrencies.isNotEmpty())

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            assertEquals(expected, dao.getCurrencies().first())
        }
    }

    @Test
    fun `test when upsert currencies`() = testScope.runTest {
        val expected = listOf(
            Currency("IDR", "Indonesian Rupiah"),
            Currency("USD", "United States Dollar"),
        )

        val dbQueries = database.databaseQueries
        dbQueries.addCurrencies("IDR", "Indonesian Rupiah")

        dao.upsertCurrencies(listOf(Currency("USD", "United States Dollar")))

        val currentCurrencies = dbQueries.getCurrencies().executeAsList()
        assertEquals(2, currentCurrencies.size)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            assertEquals(expected, dao.getCurrencies().first())
        }
    }

    @Test
    fun `test when get currency rates`() = testScope.runTest {
        val expected = listOf(
            CurrencyRate(Currency("IDR", "Indonesian Rupiah"), 15878.75),
        )

        val dbQueries = database.databaseQueries
        dbQueries.addCurrencies("IDR", "Indonesian Rupiah")
        dbQueries.addCurrencyRates("IDR", 15878.75)

        val currentCurrencies = dbQueries.getCurrencies().executeAsList()
        assertTrue(currentCurrencies.isNotEmpty())

        val currentCurrencyRates = dbQueries.getCurrencyRates().executeAsList()
        assertTrue(currentCurrencyRates.isNotEmpty())

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            assertEquals(expected, dao.getCurrencyRates().first())
        }
    }

    @Test
    fun `test when get currency rate`() = testScope.runTest {
        val expected = CurrencyRate(Currency("IDR", "Indonesian Rupiah"), 15878.75)

        val dbQueries = database.databaseQueries
        dbQueries.addCurrencies("IDR", "Indonesian Rupiah")
        dbQueries.addCurrencyRates("IDR", 15878.75)

        val currentCurrencies = dbQueries.getCurrencies().executeAsList()
        assertTrue(currentCurrencies.isNotEmpty())

        val currentCurrencyRates = dbQueries.getCurrencyRates().executeAsList()
        assertTrue(currentCurrencyRates.isNotEmpty())

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            assertEquals(expected, dao.getCurrencyRate("IDR").first())
        }
    }

    @Test
    fun `test when upsert currency rate`() = testScope.runTest {
        val expected = listOf(
            CurrencyRate(Currency("IDR", "Indonesian Rupiah"), 15878.75),
            CurrencyRate(Currency("USD", "United States Dollar"), 1.0),
        )

        val dbQueries = database.databaseQueries
        dbQueries.addCurrencies("IDR", "Indonesian Rupiah")
        dbQueries.addCurrencies("USD", "United States Dollar")

        dao.upsertCurrencyRates(mapOf("IDR" to 15878.75, "USD" to 1.0))

        val currentCurrencies = dbQueries.getCurrencyRates().executeAsList()
        assertEquals(2, currentCurrencies.size)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            assertEquals(expected, dao.getCurrencyRates().first())
        }
    }

    @Test
    fun `test when upsert currency rate but currency code is invalid`() = testScope.runTest {
        val expected = listOf(
            CurrencyRate(Currency("IDR", "Indonesian Rupiah"), 15878.75),
        )

        val dbQueries = database.databaseQueries
        dbQueries.addCurrencies("IDR", "Indonesian Rupiah")

        dao.upsertCurrencyRates(mapOf("IDR" to 15878.75, "USD" to 1.0))

        val currentCurrencies = dbQueries.getCurrencyRates().executeAsList()
        // only IDR will be returned.
        assertEquals(1, currentCurrencies.size)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            assertEquals(expected, dao.getCurrencyRates().first())
        }
    }
}
