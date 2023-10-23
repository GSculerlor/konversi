plugins {
    id("moe.ganen.konversi.library")
    id("moe.ganen.konversi.library.compose")
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    namespace = "moe.ganen.konversi.design"
}

dependencies {
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.foundation.layout)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui.tooling.preview)
    api(libs.androidx.compose.ui.util)

    debugApi(libs.androidx.compose.ui.tooling)

    implementation(libs.core.ktx)
    implementation(libs.coil.compose)
    implementation(libs.accompanist.systemuicontroller)
}