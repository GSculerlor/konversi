import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.ksp)
    id("kotlinx-serialization")
    id("com.codingfeline.buildkonfig")
}

android {
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    namespace = "moe.ganen.konversi.data"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging {
        resources {
            excludes += "META-INF/*"
        }
    }
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    targetHierarchy.default()
    androidTarget()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines)
                implementation(libs.kotlinx.serialization)
                api(libs.kotlinx.datetime)

                implementation(libs.slf4j)
                implementation(libs.bundles.ktor.common)

                implementation(libs.sqldelight.coroutines)
                implementation(libs.sqldelight.primitive)

                implementation(libs.koin.core)
                implementation(libs.koin.test)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.turbine)
                implementation(libs.mockk)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(libs.ktor.client.android)
                implementation(libs.sqldelight.android)
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(libs.sqldelight.sqlite)
            }
        }
    }
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
}

sqldelight {
    databases {
        create("KonversiDatabase") {
            packageName.set("moe.ganen.konversi.data")
        }
    }
}

buildkonfig {
    packageName = "moe.ganen.konversi.data"

    defaultConfigs {
        buildConfigField(STRING, "OPEN_EXCHANGE_RATES_APP_ID", "0fb7d314d7f84870880293f4271e0929")
    }
}