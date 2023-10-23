package moe.ganen.konversi.data.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import moe.ganen.konversi.data.BuildKonfig
import moe.ganen.konversi.data.KonversiDatabase
import moe.ganen.konversi.data.database.KonversiDatabaseFactory
import moe.ganen.konversi.data.database.dao.CurrenciesDao
import moe.ganen.konversi.data.database.dao.SuccessFetchDao
import moe.ganen.konversi.data.repository.CurrenciesRepository
import moe.ganen.konversi.data.repository.CurrenciesRepositoryImpl
import moe.ganen.konversi.data.repository.CurrencyRateRepository
import moe.ganen.konversi.data.repository.CurrencyRateRepositoryImpl
import moe.ganen.konversi.data.repository.local.KonversiLocalDataSource
import moe.ganen.konversi.data.repository.local.SqlDelightKonversiLocalDataSource
import moe.ganen.konversi.data.repository.network.KonversiNetworkDataSource
import moe.ganen.konversi.data.repository.network.KtorKonversiNetworkDataSource
import moe.ganen.konversi.data.utils.ConnectivityNetworkMonitor
import moe.ganen.konversi.data.utils.HttpExceptions
import moe.ganen.konversi.data.utils.NetworkMonitor
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val dataModules: List<Module> =
    listOf(sharedNetworkDataModule(true), sharedLocalDataModule(), platformDataModule())

fun sharedLocalDataModule() =
    module {
        single<KonversiDatabase> {
            KonversiDatabaseFactory(driver = get()).build()
        }
        single { SuccessFetchDao(database = get()) }
        single { CurrenciesDao(database = get()) }
        single<KonversiLocalDataSource> {
            SqlDelightKonversiLocalDataSource(
                successFetchDao = get(),
                currenciesDao = get(),
            )
        }
    }

fun sharedNetworkDataModule(enableNetworkLogs: Boolean) =
    module {
        single { createJson() }
        single {
            createHttpClient(
                httpClientEngine = get(),
                json = get(),
                enableNetworkLogs = enableNetworkLogs,
            )
        }
        single<String>(named("baseUrl")) { "https://openexchangerates.org/api" }
        single<String>(named("appId")) { BuildKonfig.OPEN_EXCHANGE_RATES_APP_ID }
        single<KonversiNetworkDataSource> {
            KtorKonversiNetworkDataSource(
                httpClient = get(),
                baseUrl = get(qualifier = named("baseUrl")),
                appId = get(qualifier = named("appId")),
            )
        }
        single<CurrencyRateRepository> {
            CurrencyRateRepositoryImpl(
                localDataSource = get(),
                networkDataSource = get(),
            )
        }
        single<CurrenciesRepository> {
            CurrenciesRepositoryImpl(
                localDataSource = get(),
                networkDataSource = get(),
            )
        }
    }

fun createJson() =
    Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

fun createHttpClient(
    httpClientEngine: HttpClientEngine,
    json: Json,
    enableNetworkLogs: Boolean,
) = HttpClient(httpClientEngine) {
    expectSuccess = true

    install(ContentNegotiation) {
        json(json)
    }
    if (enableNetworkLogs) {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.NONE
        }
    }

    HttpResponseValidator {
        validateResponse { response ->
            if (!response.status.isSuccess()) {
                val httpFailureReason = when (response.status) {
                    HttpStatusCode.BadRequest -> "Client requested rates for an unsupported base currency"
                    HttpStatusCode.Unauthorized -> "App Client ID is invalid or missing"
                    HttpStatusCode.Forbidden -> "Access restricted"
                    HttpStatusCode.NotFound -> "Client requested a non-existent resource/route"
                    HttpStatusCode.TooManyRequests -> "Client doesn't have permission to access requested route/feature"
                    else -> "Network error!"
                }

                throw HttpExceptions(
                    response = response,
                    cachedResponseText = response.bodyAsText(),
                    failureReason = httpFailureReason,
                )
            }
        }
    }
}

expect fun platformDataModule(): Module
