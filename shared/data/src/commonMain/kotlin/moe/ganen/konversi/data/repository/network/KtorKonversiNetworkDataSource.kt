package moe.ganen.konversi.data.repository.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import moe.ganen.konversi.data.model.Currency
import moe.ganen.konversi.data.model.network.NetworkCurrencyRateResponse

class KtorKonversiNetworkDataSource(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val appId: String,
) : KonversiNetworkDataSource {
    override suspend fun fetchCurrencyRates(): NetworkCurrencyRateResponse {
        return httpClient.get(
            "$baseUrl/latest.json?app_id=$appId&base=USD&prettyprint=true&show_alternative=false",
        ) {
            header(HttpHeaders.Accept, "application/json")
        }.body<NetworkCurrencyRateResponse>()
    }

    override suspend fun fetchCurrencies(): List<Currency> {
        return httpClient.get(
            "$baseUrl/currencies.json?app_id=$appId&prettyprint=true&show_alternative=false&show_inactive=false",
        ) {
            header(HttpHeaders.Accept, "application/json")
        }.body<Map<String, String>>().map {
            Currency(it.key, it.value)
        }
    }
}
