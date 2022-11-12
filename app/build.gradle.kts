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
        applicationId = "com.gallery.kakaogallery"
        minSdk = 23
        targetSdk = 33
        versionCode = 1
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    //https://github.com/ReactiveX/RxKotlin
    implementation("io.reactivex.rxjava3:rxkotlin:3.0.0")
    //https://github.com/Reactivex/Rxandroid/wiki
    implementation("io.reactivex.rxjava3:rxandroid:3.0.0")

    //retrofit2 and rx adapter
    implementation("com.squareup.retrofit2:adapter-rxjava3:2.9.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // http log use at retrofit2
    implementation("com.squareup.okhttp3:logging-interceptor:4.2.1")

    // https://developer.android.com/jetpack/androidx/releases/activity?hl=ko
    implementation("androidx.activity:activity-ktx:1.7.0-alpha02")
    // https://developer.android.com/jetpack/androidx/releases/fragment?hl=ko
    implementation("androidx.fragment:fragment-ktx:1.6.0-alpha03")

    //swipe refresh layout
    // https://developer.android.com/jetpack/androidx/releases/swiperefreshlayout?hl=ko
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // glide image
    // https://velog.io/@rjsdnqkr1/Glide-%EB%9D%BC%EC%9D%B4%EB%B8%8C%EB%9F%AC%EB%A6%AC-%EC%82%AC%EC%9A%A9%ED%95%98%EA%B8%B0-yuk1fmwzo1
    // https://github.com/bumptech/glide
    implementation("com.github.bumptech.glide:glide:4.9.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.9.0")

    // dagger-hilt
    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-compiler:2.44")

    // Timber
    implementation("com.jakewharton.timber:timber:5.0.1")

    implementation("androidx.recyclerview:recyclerview:1.2.1")

    //firebase
    implementation(platform("com.google.firebase:firebase-bom:31.0.2"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    testImplementation("org.assertj:assertj-core:3.20.2")
    androidTestImplementation("org.assertj:assertj-core:3.20.2")
    testImplementation("io.mockk:mockk:1.13.2")
    androidTestImplementation("io.mockk:mockk-android:1.13.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.7.2")
}