plugins {
    id("moe.ganen.konversi.library")
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    namespace = "moe.ganen.konversi.worker"
}

dependencies {
    implementation(project(":shared:data"))
    implementation(libs.core.ktx)
    implementation(libs.androidx.work.ktx)
    implementation(libs.koin.androidx.workmanager)
}