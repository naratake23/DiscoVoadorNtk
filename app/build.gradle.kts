plugins {
    id("com.android.application")
    kotlin("android")                      // Kotlin DSL
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
}


android {
    namespace = "com.liberty.discovoadorntk"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.liberty.discovoadorntk"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    sourceSets {
        // pega o sourceSet chamado "main" e adiciona o src/main/java
        getByName("main") {
            kotlin.srcDir("src/main/java")
        }
        // idem para "debug"
        getByName("debug") {
            kotlin.srcDir("src/debug/java")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true           // ativa R8 (minify/obfuscate/shrink)
            isShrinkResources = true         // remove recursos não usados
            isDebuggable = false             // release é release
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Durante o diagnóstico inicial, você pode ligar estas duas:
            // (depois desligue p/ reduzir tamanho)
            // multiDexEnabled = false
//            buildConfigField("boolean", "ENABLE_DIAGNOSTICS", "true")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true

        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"

            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/*.kotlin_module"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

dependencies {



    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation("androidx.activity:activity-compose:1.9.0")
//    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
//    implementation(platform("androidx.compose:compose-bom:2022.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.6.8")

//    implementation("androidx.test:core-ktx:1.6.1")
    androidTestImplementation("androidx.test:core-ktx:1.6.1")

    implementation("androidx.preference:preference-ktx:1.2.1")
//    implementation("androidx.compose.ui:ui-desktop:1.6.8")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

//firebase
//    implementation("com.google.firebase:firebase-auth:23.0.0")
//    implementation("com.google.firebase:firebase-firestore:25.0.0")
//    implementation("androidx.appcompat:appcompat:1.7.0")
//    implementation("com.google.android.material:material:1.12.0")
//    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    // TOD: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")

//para adicionar imagens
    implementation("io.coil-kt:coil-compose:2.6.0")





    implementation("com.google.dagger:hilt-android:2.46.1")
    kapt("com.google.dagger:hilt-compiler:2.46.1")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    implementation("androidx.hilt:hilt-work:1.0.0")


    //habilita o hiltViewModel
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("androidx.navigation:navigation-compose:2.7.7")

    //Compose integration with Lifecycle ViewModel (opcional - coleta o status da viewmodel?)
    //para obter o contexto do viewmodel e do compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    //para coletar flows e compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")


    //Data store para persistir dados de configuração de tipos primitivos
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.datastore:datastore:1.1.1")
    implementation("androidx.datastore:datastore-preferences-core:1.1.1")

// Google Credentials
    implementation("com.google.auth:google-auth-library-oauth2-http:1.23.0")

// traz o transport com suporte a OkHttp3
//    implementation("com.auth0:java-jwt:4.4.0")
    // Para assinatura RSA funcionar corretamente em Android (DEBUG only)
//      debugImplementation("org.bouncycastle:bcprov-jdk15to18:1.70")

    // https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp
//    implementation("com.squareup.okhttp3:okhttp:4.12.0")
//    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

//Hilt-ViewModel integration, se você usar Hilt
//    implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")

//    implementation("com.squareup.moshi:moshi:1.15.0")
//    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
//    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")

//    implementation("androidx.work:work-runtime:2.8.1")
    implementation("androidx.work:work-runtime-ktx:2.8.1")


    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
//
//
//    implementation(project(":corentk"))
}