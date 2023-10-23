package moe.ganen.konversi.feature.di

import moe.ganen.konversi.feature.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val featureModule = module {
    viewModel {
        HomeViewModel(
            syncManager = get(),
            networkMonitor = get(),
            currenciesRepository = get(),
            currencyRateRepository = get(),
        )
    }
}
