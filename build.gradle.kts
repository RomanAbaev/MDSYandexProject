// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    apply(from = "gradle/scripts/global.gradle.kts")
    apply(from = "gradle/scripts/plugins.gradle.kts")
    apply(from = "gradle/scripts/dependencies.gradle.kts")


    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
    dependencies {
        val pluginDependencies: List<String> by project.extra
        pluginDependencies.forEach { plugin ->
            classpath(plugin)
        }
    }
}

allprojects {
    apply(from = "$rootDir/gradle/scripts/global.gradle.kts")
    apply(from = "$rootDir/gradle/scripts/plugins.gradle.kts")
    apply(from = "$rootDir/gradle/scripts/dependencies.gradle.kts")

    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}