package moe.ganen.konversi.data.utils

/**
 * Helper object for handling currency conversion-related action.
 */
object CurrencyHelper {

    /**
     * Calculate rate from currency x to currency y given their rate to USD.
     * Note that precision-loss might happened here since we only calculate raw values.
     */
    fun calculateRate(value: Double, initialToUSD: Double, targetToUSD: Double): Double {
        // if one of them is NaN then throw IllegalArgumentException.
        require(!(initialToUSD.isNaN() || targetToUSD.isNaN() || value.isNaN())) {
            throw IllegalArgumentException("Value shouldn't be NaN")
        }

        // if one of them is infinite then return 0.0 (we don't want to calculate infinite number anyways).
        require(!(initialToUSD.isInfinite() || targetToUSD.isInfinite() || value.isInfinite())) {
            throw IllegalArgumentException("Value shouldn't be infinite")
        }

        // if one of them is 0 then return 0.0
        require(!(initialToUSD == 0.0 || targetToUSD == 0.0)) {
            throw IllegalArgumentException("Value shouldn't be zero")
        }

        return value / initialToUSD * targetToUSD
    }
}
