import org.jmailen.gradle.kotlinter.KotlinterExtension
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.kotlinter) apply false
}

buildscript {
    dependencies {
        classpath(libs.ktlint)
        classpath(libs.buildkonfig)
    }
}

// TODO: might as well move it to composite gradle
subprojects {
    apply(plugin = "org.jmailen.kotlinter")

    configure<KotlinterExtension> {
        reporters = arrayOf("html", "plain")
    }

    tasks.withType<LintTask> {
        exclude("**/generated/**", "**/iosMain/**")
        exclude {
            projectDir.toURI().relativize(it.file.toURI()).path.contains("/sqldelight/")
        }
        exclude {
            projectDir.toURI().relativize(it.file.toURI()).path.contains("/ksp/")
        }
        exclude {
            projectDir.toURI().relativize(it.file.toURI()).path.contains("/buildkonfig/")
        }
    }

    tasks.withType<FormatTask> {
        exclude("**/generated/**", "**/iosMain/**")
        exclude {
            projectDir.toURI().relativize(it.file.toURI()).path.contains("/sqldelight/")
        }
        exclude {
            projectDir.toURI().relativize(it.file.toURI()).path.contains("/buildkonfig/")
        }
    }
}

true // Needed to make the Suppress annotation work for the plugins block
