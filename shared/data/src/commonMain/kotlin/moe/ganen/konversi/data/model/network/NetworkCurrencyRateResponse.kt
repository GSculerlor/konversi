package moe.ganen.konversi.data.model.network

import kotlinx.serialization.Serializable

@Serializable
data class NetworkCurrencyRateResponse(
    val base: String,
    val rates: Map<String, Double>,
    val timestamp: Long,
)
