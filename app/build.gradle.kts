plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.qomarhsn.webgpt"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.qomarhsn.webgpt"
        minSdk = 21
        targetSdk = 36
        versionCode = 201
        versionName = "2.0.1"

        vectorDrawables.useSupportLibrary = true
    }

    val keystoreFile = file("release.keystore")
    val hasKeystore = keystoreFile.exists()

    signingConfigs {
        create("release") {
            if (hasKeystore) {
                storeFile = keystoreFile
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (hasKeystore) signingConfigs.getByName("release") else null
            isDebuggable = false
            isJniDebuggable = false
            renderscriptOptimLevel = 3
        }
        
        create("releaseUnsigned") {
            initWith(getByName("release"))
            signingConfig = null
            applicationIdSuffix = null
        }
        
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            isDebuggable = true
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
        buildConfig = false
        viewBinding = false
        dataBinding = false
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/*.kotlin_module"
        }
    }
}

dependencies {
}
