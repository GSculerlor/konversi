package moe.ganen.konversi.data.utils

import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class CurrencyHelperTest {

    @Test(expected = IllegalArgumentException::class)
    fun `test when trying to calculate NaN input`() = runTest {
        CurrencyHelper.calculateRate(value = Double.NaN, initialToUSD = 1.0, targetToUSD = 1.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test when trying to calculate NaN initial to USD rate`() = runTest {
        CurrencyHelper.calculateRate(value = 1.0, initialToUSD = Double.NaN, targetToUSD = 1.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test when trying to calculate NaN target to USD rate`() = runTest {
        CurrencyHelper.calculateRate(value = 1.0, initialToUSD = 1.0, targetToUSD = Double.NaN)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test when trying to calculate infinite input`() = runTest {
        CurrencyHelper.calculateRate(
            value = Double.POSITIVE_INFINITY,
            initialToUSD = 1.0,
            targetToUSD = 1.0,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test when trying to calculate infinite initial to USD rate`() = runTest {
        CurrencyHelper.calculateRate(
            value = 1.0,
            initialToUSD = Double.POSITIVE_INFINITY,
            targetToUSD = 1.0,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test when trying to calculate infinite target to USD rate`() = runTest {
        CurrencyHelper.calculateRate(
            value = 1.0,
            initialToUSD = 1.0,
            targetToUSD = Double.POSITIVE_INFINITY,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test when trying to calculate zero initial to USD rate`() = runTest {
        CurrencyHelper.calculateRate(value = 1.0, initialToUSD = 0.0, targetToUSD = 1.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test when trying to calculate zero target to USD rate`() = runTest {
        CurrencyHelper.calculateRate(value = 1.0, initialToUSD = 1.0, targetToUSD = 0.0)
    }

    /**
     * Assuming USD to IDR = 15,878.75.
     * Expected conversion from IDR to USD should be 0.000063.
     */
    @Test
    fun `test convert USD to IDR`() = runTest {
        val idrToUsd =
            CurrencyHelper.calculateRate(value = 1.0, initialToUSD = 15878.75, targetToUSD = 1.0)
        assertEquals(0.000063, idrToUsd, 0.01)
    }

    /**
     * Assuming USD to IDR = 15,878.75 and USD to JPY = 149.86.
     * Expected conversion from IDR to JPY should be 0.0094.
     * Expected conversion from JPY to IDR should be 105.96.
     */
    @Test
    fun `test convert IDR to JPY`() = runTest {
        val idrToJpy =
            CurrencyHelper.calculateRate(value = 1.0, initialToUSD = 15878.75, targetToUSD = 149.86)
        assertEquals(0.0094, idrToJpy, 0.01)

        val jpyToIdr =
            CurrencyHelper.calculateRate(value = 1.0, initialToUSD = 149.86, targetToUSD = 15878.75)
        assertEquals(105.96, jpyToIdr, 0.01)
    }

    @Test
    fun `test convert USD to USD`() = runTest {
        val usdToUsd =
            CurrencyHelper.calculateRate(value = 1.0, initialToUSD = 1.0, targetToUSD = 1.0)
        assertEquals(1.0, usdToUsd, 0.0)
    }

    /**
     * Assuming USD to IDR = 15,878.75.
     **/
    @Test
    fun `test convert to same non USD currency`() = runTest {
        val idrToUsd =
            CurrencyHelper.calculateRate(value = 1.0, initialToUSD = 15878.75, targetToUSD = 1.0)

        val usdToIdr =
            CurrencyHelper.calculateRate(value = 1.0, initialToUSD = 1.0, targetToUSD = idrToUsd)

        assertEquals(0.000063, usdToIdr, 0.1)
    }
}
