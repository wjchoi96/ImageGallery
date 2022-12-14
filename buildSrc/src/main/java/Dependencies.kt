import org.gradle.kotlin.dsl.DependencyHandlerScope

interface ImplementationItem {
    val implementations: List<String>
}

interface ClassPathItem {
    val classPaths: List<String>
}

interface AndroidTestImplementationItem {
    val androidTestImplementations: List<String>
}

interface TestImplementationItem {
    val testImplementations: List<String>
}

interface KaptItem {
    val kapts: List<String>
}

interface AnnotationProcessorItem {
    val annotationProcessors: List<String>
}

object Dependencies {
    object Version {
        //Gradle
        const val gradle = "7.4"
        const val gradlePlugIn = "7.3.1"
        const val kotlinGradlePlugIn = "1.6.20"

        //AndroidX
        const val appCompat = "1.5.1"
        const val material = "1.7.0"
        const val constraintLayout = "2.1.4"
        const val swipeRefreshLayout = "1.1.0"
        const val recyclerView = "1.2.1"
        const val splashScreen = "1.0.0"

        //Ktx
        const val ktx = "1.9.0"
        const val activityKtx = "1.7.0-alpha02"
        const val fragmentKtx = "1.6.0-alpha03"

        //Rx
        const val rxKotlin = "3.0.0"
        const val rxAndroid = "3.0.0"

        //Retrofit
        const val retrofit = "2.9.0"

        //Okhttp
        const val okhttp = "4.10.0"

        //Glide
        const val glide = "4.9.0"

        //Dagger-Hilt
        const val hilt = "2.44"

        //Timber
        const val timber = "5.0.1"

        //Android-Shimmer
        const val shimmer = "0.5.0"

        //Image pinch zoom
        const val TouchImageView = "3.2.1"

        //Firebase
        const val firebase = "31.0.2"
        const val googleServiceClassPath = "4.3.14"
        const val crashlyticsClassPath = "2.9.2"

        //Coroutine Test
        const val coroutineTest = "1.6.4"

        //Test
        const val junit = "1.1.3"
        const val assertj = "3.20.2"
        const val mockk = "1.13.2"

        //Android Test
        const val espresso = "3.4.0"

    }

    object Libraries {
        object GradlePlugIn : ClassPathItem {
            private const val gradlePlugIn = "com.android.tools.build:gradle:${Dependencies.Version.gradlePlugIn}"
            private const val kotlinGradlePlugIn = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Dependencies.Version.kotlinGradlePlugIn}"

            override val classPaths = listOf(
                gradlePlugIn, kotlinGradlePlugIn
            )
        }

        // https://developer.android.com/jetpack/androidx/releases/swiperefreshlayout?hl=ko
        object AndroidX : ImplementationItem {
            private const val appCompat = "androidx.appcompat:appcompat:${Dependencies.Version.appCompat}"
            private const val material = "com.google.android.material:material:${Dependencies.Version.material}"
            private const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Dependencies.Version.constraintLayout}"
            private const val swipeRefreshLayout = "androidx.swiperefreshlayout:swiperefreshlayout:${Dependencies.Version.swipeRefreshLayout}"
            private const val recyclerView = "androidx.recyclerview:recyclerview:${Dependencies.Version.recyclerView}"
            private const val splashScreen = "androidx.core:core-splashscreen:${Dependencies.Version.splashScreen}"

            override val implementations = listOf(
                appCompat, material, constraintLayout, swipeRefreshLayout, recyclerView, splashScreen
            )
        }

        // https://developer.android.com/jetpack/androidx/releases/activity?hl=ko
        // https://developer.android.com/jetpack/androidx/releases/fragment?hl=ko
        object Ktx : ImplementationItem {
            private const val ktxCore = "androidx.core:core-ktx:${Version.ktx}"
            private const val activityKtx = "androidx.activity:activity-ktx:${Dependencies.Version.activityKtx}"
            private const val fragmentKtx = "androidx.fragment:fragment-ktx:${Dependencies.Version.fragmentKtx}"

            override val implementations = listOf(
                ktxCore, activityKtx, fragmentKtx
            )
        }

        //https://github.com/ReactiveX/RxKotlin
        //https://github.com/Reactivex/Rxandroid/wiki
        object Rx : ImplementationItem {
            private const val rxKotlin = "io.reactivex.rxjava3:rxkotlin:${Dependencies.Version.rxKotlin}"
            private const val rxAndroid = "io.reactivex.rxjava3:rxandroid:${Dependencies.Version.rxAndroid}"

            override val implementations = listOf(
                rxKotlin, rxAndroid
            )
        }

        object Retrofit : ImplementationItem {
            private const val retrofit = "com.squareup.retrofit2:retrofit:${Dependencies.Version.retrofit}"
            private const val gsonConverter = "com.squareup.retrofit2:converter-gson:${Version.retrofit}"

            override val implementations = listOf(
                retrofit, gsonConverter
            )
        }

        object Okhttp : ImplementationItem {
            private const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:${Version.okhttp}"

            override val implementations = listOf(
                loggingInterceptor
            )
        }

        //https://github.com/bumptech/glide
        object Glide : ImplementationItem, AnnotationProcessorItem {
            private const val glide = "com.github.bumptech.glide:glide:${Dependencies.Version.glide}"
            private const val glideCompiler = "com.github.bumptech.glide:compiler:${Version.glide}"

            override val implementations = listOf(glide)
            override val annotationProcessors = listOf(glideCompiler)
        }

        object Hilt : ImplementationItem, KaptItem, ClassPathItem {
            private const val hilt = "com.google.dagger:hilt-android:${Dependencies.Version.hilt}"
            private const val hiltCompiler = "com.google.dagger:hilt-compiler:${Version.hilt}"
            private const val hiltPlugIn = "com.google.dagger:hilt-android-gradle-plugin:${Version.hilt}"

            override val implementations = listOf(hilt)
            override val kapts = listOf(hiltCompiler)
            override val classPaths = listOf(hiltPlugIn)
        }

        object Timber : ImplementationItem {
            private const val timber = "com.jakewharton.timber:timber:${Dependencies.Version.timber}"

            override val implementations = listOf(timber)
        }

        object SkeletonUi : ImplementationItem {
            private const val shimmer = "com.facebook.shimmer:shimmer:${Dependencies.Version.shimmer}"

            override val implementations = listOf(shimmer)
        }

        object ImagePinchZoom : ImplementationItem {
            private const val TouchImageView = "com.github.MikeOrtiz:TouchImageView:${Dependencies.Version.TouchImageView}"

            override val implementations = listOf(TouchImageView)
        }

        object Firebase : ClassPathItem {
            private const val bom = "com.google.firebase:firebase-bom:${Version.firebase}"
            private const val analytics = "com.google.firebase:firebase-analytics-ktx"
            private const val crashlytics = "com.google.firebase:firebase-crashlytics-ktx"
            private const val crashlyticsClassPath = "com.google.firebase:firebase-crashlytics-gradle:${Dependencies.Version.crashlyticsClassPath}"
            private const val googleServiceClassPath = "com.google.gms:google-services:${Dependencies.Version.googleServiceClassPath}"

            fun implementation(scope: DependencyHandlerScope) = with(scope) {
                add("implementation", platform(bom))
                add("implementation", analytics)
                add("implementation", crashlytics)
            }

            override val classPaths = listOf(
                googleServiceClassPath, crashlyticsClassPath
            )
        }

        object CoroutineTest : TestImplementationItem {
            private const val coroutineTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Dependencies.Version.coroutineTest}"

            override val testImplementations = listOf(
                coroutineTest
            )
        }

        object Test : TestImplementationItem {
            private const val junit = "junit:junit:"
            private const val assertj = "org.assertj:assertj-core:${Dependencies.Version.assertj}"
            private const val mockk = "io.mockk:mockk:${Dependencies.Version.mockk}"
            private const val mockWebServer = "com.squareup.okhttp3:mockwebserver:${Version.okhttp}"

            override val testImplementations = listOf(
                junit, assertj, mockk, mockWebServer
            )
        }

        object AndroidTest : AndroidTestImplementationItem {
            private const val junit = "androidx.test.ext:junit:${Dependencies.Version.junit}"
            private const val espressoCore = "androidx.test.espresso:espresso-core:${Version.espresso}"
            private const val assertj = "org.assertj:assertj-core:${Dependencies.Version.assertj}"
            private const val mockk = "io.mockk:mockk-android:${Dependencies.Version.mockk}"

            override val androidTestImplementations = listOf(
                junit, espressoCore, assertj, mockk
            )
        }
    }
}