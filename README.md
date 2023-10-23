Konversi
==================

**Konversi** is a fully functional Kotlin Multiplatform (currently only targetting Android)
application that help you see the exchange rates.

## Features

**Konversi** displays content from [Open Exchange Rates](https://openexchangerates.org/). User can
see all currencies and their exchange rates.
This application satisfy the requirements as below:

- [x] Data are locally stored, meaning this application can be used on offline/no internet.
- [x] Data from API are fetched no more frequently than once every 30 minutes. This will ensure that
  app is not reaching bandwith limit.
- [x] User can input and change the currency and see the exchange rate directly.
- [x] Exchange rates are calculated locally, free-tier account don't have access to convert API so
  it calculatted locally.
- [x] Covered with unit tests.

## Screenshots

<img src="https://user-images.githubusercontent.com/6506864/277182394-4fb4a513-632b-4ec2-9746-90fbadf02884.png" width=250 /> <img src="https://user-images.githubusercontent.com/6506864/277182415-e62b37fb-85aa-4a93-aee4-d4aa50d2d5d2.png" width=250 /> <img src="https://user-images.githubusercontent.com/6506864/277182433-639f96d2-28e0-4e0a-a074-df451317a7ff.png" width=250 />

## Tech stacks

The general stacks I used for this application as follows:

- Kotlin Multiplatform
- Ktor (network)
- Sqldelight (database)
- Koin (dependency injection)
- BuildKonfig (build config)
- Gradle version catalog (dependency management)
- Gradle convention plugin (Gradle module configuration)

Android specific stacks:

- AAC ViewModel (View model layer, served as `expect-actual` implementation)
- Jetpack Compose (UI toolkit for Android)
- WorkManager (Background task handler)

For testing related stacks are follows:

- Mockk
- Turbine

## Architecture

**Konversi** architecture follows a reactive programming model with unidirectional data flow. With
the data layer at the bottom, the key concepts are:

- Higher layers react to changes in lower layers.
- Events flow down.
- Data flows up.

The data flow is achieved using streams, implemented using Kotlin Flows.

### Modules

**Konversi** modularization strategy are split the modules based on its responsibility.
**Konversi** also leverage the Gradle's convention plugins to make it easier to shared same Gradle
configuration for modules.
You can see more about the convention plugin on **build-logic** module.
**Konversi** app contains the following types of modules:

- The app module - contains app level and scaffolding classes that bind the rest of the codebase,
  such as `MainActivity`, `KonversiApplication` and app-level controlled components.

- shared:data modules - feature specific modules which are handing data layer of the app. This
  includes the local and network data layer.

- shared:feature modules - feature specific modules which are scoped to handle a single
  responsibility in the app, scoped to its feature.
  Since it doesn't have a huge amount of feature, it only have one feature module.

- core-android:worker modules - Android-specific module that hold the logics for WorkManager,
  specifically for the sync logic on
  Android.

- core-android:design modules - Jetpack Compose module for the app design language. It actully
  scrapped from my other project
  hence you can see the name is not `Konversi` but `Cellinia`.

### Synchronizer using WorkManager

To ensure application serving latest application while respecting the constraint being able to fetch
data no more than once per 30 minutes,
sync process are delegated to WorkManager, meaning that everytime user open the app, it will do the
sync logic on background, making
sure user is not distracted or making the UI janky. To see more about this you can
check `core-android:worker`

## Build

To build the app, you need to add your Open Exchange Rates app ID to data's **build.gradle.kts**
inside the `buildKonfig` block.
Currently, it only serve `dev` flavor on Android, so there's nothing to do more than just click
those run button.

## Code quality

**Konversi** already built with quality in mind. I use ktlint to make sure the project doesn't have
code smell. You can try to run
`lintKotlin` Gradle task to check the code quality and `formatKotlin` to help you format the code to
match the quality.

## Testing

To run the test, you can run `test` Gradle task.

**Konversi** testing approach are heavily depends on the behavior of the application.
That's why the tests are designed to mimic what app flows looks like (for example, user update
inputs or app tries to sync the data).
Unit tests are using mocks provided by Mockk to control the dependencies behavior. While Mockk is
powerful, it limits the unit test to
be run on Android only (while its not a problem since this project only targetting Android, in real
application it will be a downturn).

You can check the unit test on shared modules, both on **data** and **feature** module. While I
tried my best to cover everything in test, some of them may be missing or forgot to add the unit
test.

## Next Improvement

- Better network error displays

Currently, network errors are handled internally when sync. Meaning that it will never crashed when
app failing to fetch the data, but it not displayed nicely to the user.

- Moving away from Mockk

Mockk is a powerful mock library, but it only works on Android. There are several possible
replacement like mockative or mockmp, but all of them are not as powerful as Mockk since it only 
mocks Interface, something that will make it my testing approach harder.

- Network tests

Currently not implementing test for Ktor since I lack of knowledge in Ktor testing approach.

- Screenshot/UI tests

I initially want to have this, but I just don't have time to implement it. But UI still can be
manually check and test without running the app since I provides previews for the UI components.