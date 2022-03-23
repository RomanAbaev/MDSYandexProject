val kotlinVersion: String by extra
val androidxCoreKtxVersion: String by extra
val androidxAppCompatVersion: String by extra
val materialDesignVersion: String by extra
val constraintLayoutVersion: String by extra
val junitVersion: String by extra

val navigationVersion: String by extra
val fragmentKtx: String by extra
val lifecycleExtensionVersion: String by extra
val lifecycleKtxVersion: String by extra
val coroutineVersion: String by extra
val retrofitVersion: String by extra
val okhttp3Version: String by extra
val retrofit2KotlinCoroutinesAdapterVersion: String by extra
val glideVersion: String by extra
val moshiVersion: String by extra
val roomVersion: String by extra
val jodaVersion: String by extra
val viewpager2Version: String by extra
val chartVersion: String by extra
val dagger2Version: String by extra
val roomTestingVersion: String by extra


var implementationDependencies: List<String> by extra
implementationDependencies = listOf(
    "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion",
    "androidx.core:core-ktx:$androidxCoreKtxVersion",
    "androidx.appcompat:appcompat:$androidxAppCompatVersion",
    "com.google.android.material:material:$materialDesignVersion",
    "androidx.constraintlayout:constraintlayout:$constraintLayoutVersion",
    "androidx.navigation:navigation-fragment-ktx:$navigationVersion",
    "androidx.navigation:navigation-ui-ktx:$navigationVersion",
    "androidx.fragment:fragment-ktx:$fragmentKtx",
    "androidx.lifecycle:lifecycle-extensions:$lifecycleExtensionVersion",
    "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleKtxVersion",
    "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleKtxVersion",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion",
    "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutineVersion",
    "com.squareup.retrofit2:retrofit:$retrofitVersion",
    "com.squareup.retrofit2:converter-moshi:$retrofitVersion",
    "com.squareup.okhttp3:okhttp:$okhttp3Version",
    "com.squareup.okhttp3:logging-interceptor:$okhttp3Version",
    "com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:$retrofit2KotlinCoroutinesAdapterVersion",
    "com.github.bumptech.glide:glide:$glideVersion",
    "com.squareup.moshi:moshi:$moshiVersion",
    "com.squareup.moshi:moshi-kotlin:$moshiVersion",
    "androidx.room:room-runtime:$roomTestingVersion",
    "androidx.room:room-ktx:$roomVersion",
    "joda-time:joda-time:$jodaVersion",
    "com.google.firebase:firebase-crashlytics-ktx",
    "com.google.firebase:firebase-analytics-ktx",
    "androidx.viewpager2:viewpager2:$viewpager2Version",
    "com.github.PhilJay:MPAndroidChart:$chartVersion",
    "com.google.dagger:dagger:$dagger2Version"
)


var testImplementationDependencies: List<String> by extra
testImplementationDependencies = listOf(
    "junit:junit:$junitVersion",
    "androidx.room:room-testing:$roomTestingVersion"
)

var kaptDependencies: List<String> by extra
kaptDependencies = listOf(
    "com.google.dagger:dagger-compiler:$dagger2Version",
    "com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion",
    "androidx.room:room-compiler:$roomVersion"
)



