plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    compileSdk = 33

    defaultConfig {
        applicationId = AppConfig.applicationId
        minSdk = AppConfig.minSdk
        targetSdk = AppConfig.targetSdk
        versionCode = AppConfig.versionCode
        versionName = AppConfig.versionName

        testInstrumentationRunner = AppConfig.testInstrumentationRunner
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility =  JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures  {
        //noinspection DataBindingWithoutKapt
        dataBinding = true
    }
    packagingOptions {
        resources {
            excludes.add("META-INF/LICENSE.md")
            excludes.add("META-INF/LICENSE-notice.md")
        }
    }

    testOptions {
        packagingOptions {
            jniLibs {
                useLegacyPackaging = true
            }
        }
    }

    namespace = "com.gallery.kakaogallery"

}

dependencies {
    implementation(Dependencies.Libraries.AndroidX)

    implementation(Dependencies.Libraries.Ktx)

    implementation(Dependencies.Libraries.Retrofit)

    implementation(Dependencies.Libraries.Okhttp)

    implementation(Dependencies.Libraries.Glide)
    annotationProcessor(Dependencies.Libraries.Glide)

    implementation(Dependencies.Libraries.Hilt)
    kapt(Dependencies.Libraries.Hilt)

    Dependencies.Libraries.Firebase.implementation(this)

    implementation(Dependencies.Libraries.Timber)

    implementation(Dependencies.Libraries.SkeletonUi)

    implementation(Dependencies.Libraries.ImagePinchZoom)

    testImplementation(Dependencies.Libraries.CoroutineTest)

    testImplementation(Dependencies.Libraries.Test)

    androidTestImplementation(Dependencies.Libraries.AndroidTest)
}