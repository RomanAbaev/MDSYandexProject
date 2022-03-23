val androidGradleVersion: String by extra
val kotlinVersion: String by extra
val googleServicesVersion: String by extra
val firebaseCrashlyticsGradleVersion: String by extra
val navigationSafeArgsGradlePluginVersion: String by extra


var pluginDependencies: List<String> by extra
pluginDependencies = listOf(
    "com.android.tools.build:gradle:$androidGradleVersion",
    "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion",
    "com.google.gms:google-services:$googleServicesVersion",
    "com.google.firebase:firebase-crashlytics-gradle:$firebaseCrashlyticsGradleVersion",
    "androidx.navigation:navigation-safe-args-gradle-plugin:$navigationSafeArgsGradlePluginVersion"
)