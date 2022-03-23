plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("androidx.navigation.safeargs")
}

android {
    val compileSdk: String by extra
    val buildToolsVersion: String by extra
    this.compileSdk = compileSdk.toInt()
    this.buildToolsVersion = buildToolsVersion

    defaultConfig {
        val applicationId: String by extra
        val minSdk: String by extra
        val targetSdk: String by extra
        val versionCode: String by extra
        val versionName: String by extra

        this.applicationId = applicationId
        this.minSdk = minSdk.toInt()
        this.targetSdk = targetSdk.toInt()
        this.versionCode = versionCode.toInt()
        this.versionName = versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        targetCompatibility = JavaVersion.VERSION_1_8
        sourceCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        val jvmTarget: String by extra
        this.jvmTarget = jvmTarget
    }

    // Enables data binding
    buildFeatures {
        dataBinding = true
    }

    sourceSets {
        getByName("androidTest").java.srcDirs("src/androidTest/kotlin")
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:26.6.0"))

    val implementationDependencies: List<String> by project.extra
    implementationDependencies.forEach { dependency ->
        implementation(dependency)
    }

    val testImplementationDependencies: List<String> by project.extra
    testImplementationDependencies.forEach { dependency ->
        testImplementation(dependency)
    }

    val kaptDependencies: List<String> by project.extra
    kaptDependencies.forEach { dependency ->
        kapt(dependency)
    }



}