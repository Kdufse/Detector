// 项目设置文件
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        
        // 自定义仓库
        maven(url = "https://jitpack.io")
        maven(url = "https://maven.google.com")
        maven(url = "https://plugins.gradle.org/m2/")
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    }
    
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                // Android Gradle Plugin
                "com.android.application",
                "com.android.library" -> useModule("com.android.tools.build:gradle:8.1.0")
                
                // Kotlin
                "org.jetbrains.kotlin.android" -> useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
                "org.jetbrains.kotlin.jvm" -> useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
                "org.jetbrains.kotlin.kapt" -> useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
                "org.jetbrains.kotlin.plugin.serialization" -> useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
                
                // Hilt
                "dagger.hilt.android.plugin" -> useModule("com.google.dagger:hilt-android-gradle-plugin:2.48")
                
                // Google Services
                "com.google.gms.google-services" -> useModule("com.google.gms:google-services:4.3.15")
                
                // Firebase Crashlytics
                "com.google.firebase.crashlytics" -> useModule("com.google.firebase:firebase-crashlytics-gradle:2.9.9")
                
                // Ktlint
                "org.jlleitschuh.gradle.ktlint" -> useModule("org.jlleitschuh.gradle:ktlint-gradle:11.5.1")
                
                // Detekt
                "io.gitlab.arturbosch.detekt" -> useModule("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.1")
                
                // Spotless
                "com.diffplug.spotless" -> useModule("com.diffplug.spotless:spotless-plugin-gradle:6.20.0")
                
                // Ben-Manes Versions
                "com.github.ben-manes.versions" -> useModule("com.github.ben-manes:gradle-versions-plugin:0.48.0")
            }
        }
    }
}

// 功能预览
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

// 依赖版本管理
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        
        // 第三方仓库
        maven(url = "https://jitpack.io")
        maven(url = "https://maven.google.com")
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
        maven(url = "https://storage.googleapis.com/snapshots.gradle.org")
        
        // 公司内部仓库（如果有）
        // maven {
        //     url = uri("https://company.artifactory.com/artifactory/gradle-release")
        //     credentials {
        //         username = System.getenv("ARTIFACTORY_USERNAME")
        //         password = System.getenv("ARTIFACTORY_PASSWORD")
        //     }
        // }
    }
    
    // 版本目录（Gradle 7.0+ 特性）
    versionCatalogs {
        create("libs") {
            // Kotlin
            version("kotlin", "1.9.0")
            version("coroutines", "1.7.3")
            
            library("kotlin-stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").versionRef("kotlin")
            library("kotlin-reflect", "org.jetbrains.kotlin", "kotlin-reflect").versionRef("kotlin")
            library("coroutines-android", "org.jetbrains.kotlinx", "kotlinx-coroutines-android").versionRef("coroutines")
            library("coroutines-core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").versionRef("coroutines")
            library("coroutines-test", "org.jetbrains.kotlinx", "kotlinx-coroutines-test").versionRef("coroutines")
            
            // AndroidX Core
            version("androidx-core", "1.12.0")
            version("androidx-appcompat", "1.6.1")
            version("androidx-material", "1.10.0")
            
            library("androidx-core-ktx", "androidx.core", "core-ktx").versionRef("androidx-core")
            library("androidx-appcompat", "androidx.appcompat", "appcompat").versionRef("androidx-appcompat")
            library("androidx-material", "com.google.android.material", "material").versionRef("androidx-material")
            library("androidx-constraintlayout", "androidx.constraintlayout", "constraintlayout").version("2.1.4")
            library("androidx-recyclerview", "androidx.recyclerview", "recyclerview").version("1.3.1")
            library("androidx-swiperefreshlayout", "androidx.swiperefreshlayout", "swiperefreshlayout").version("1.1.0")
            library("androidx-viewpager2", "androidx.viewpager2", "viewpager2").version("1.0.0")
            
            // AndroidX Lifecycle
            version("androidx-lifecycle", "2.6.2")
            library("androidx-lifecycle-viewmodel-ktx", "androidx.lifecycle", "lifecycle-viewmodel-ktx").versionRef("androidx-lifecycle")
            library("androidx-lifecycle-livedata-ktx", "androidx.lifecycle", "lifecycle-livedata-ktx").versionRef("androidx-lifecycle")
            library("androidx-lifecycle-runtime-ktx", "androidx.lifecycle", "lifecycle-runtime-ktx").versionRef("androidx-lifecycle")
            library("androidx-lifecycle-common", "androidx.lifecycle", "lifecycle-common-java8").versionRef("androidx-lifecycle")
            
            // AndroidX Navigation
            version("androidx-navigation", "2.7.4")
            library("androidx-navigation-fragment-ktx", "androidx.navigation", "navigation-fragment-ktx").versionRef("androidx-navigation")
            library("androidx-navigation-ui-ktx", "androidx.navigation", "navigation-ui-ktx").versionRef("androidx-navigation")
            
            // Dagger Hilt
            version("hilt", "2.48")
            library("hilt-android", "com.google.dagger", "hilt-android").versionRef("hilt")
            library("hilt-compiler", "com.google.dagger", "hilt-compiler").versionRef("hilt")
            library("hilt-navigation", "androidx.hilt", "hilt-navigation-fragment").version("1.0.0")
            
            // Networking
            version("retrofit", "2.9.0")
            version("okhttp", "5.0.0-alpha.11")
            
            library("retrofit", "com.squareup.retrofit2", "retrofit").versionRef("retrofit")
            library("retrofit-gson", "com.squareup.retrofit2", "converter-gson").versionRef("retrofit")
            library("retrofit-moshi", "com.squareup.retrofit2", "converter-moshi").versionRef("retrofit")
            library("okhttp", "com.squareup.okhttp3", "okhttp").versionRef("okhttp")
            library("okhttp-logging", "com.squareup.okhttp3", "logging-interceptor").versionRef("okhttp")
            
            // Google Play Services
            library("play-services-safetynet", "com.google.android.gms", "play-services-safetynet").version("18.0.1")
            library("play-integrity", "com.google.android.play", "integrity").version("1.1.0")
            
            // Testing
            version("junit", "4.13.2")
            version("androidx-test", "1.5.0")
            version("espresso", "3.5.1")
            
            library("junit", "junit", "junit").versionRef("junit")
            library("androidx-test-core", "androidx.test", "core").version("1.5.0")
            library("androidx-test-junit", "androidx.test.ext", "junit").version("1.1.5")
            library("androidx-test-runner", "androidx.test", "runner").version("1.5.2")
            library("androidx-test-rules", "androidx.test", "rules").version("1.5.0")
            library("espresso-core", "androidx.test.espresso", "espresso-core").versionRef("espresso")
            library("espresso-contrib", "androidx.test.espresso", "espresso-contrib").versionRef("espresso")
            library("espresso-intents", "androidx.test.espresso", "espresso-intents").versionRef("espresso")
            
            // Mocking
            library("mockito-core", "org.mockito", "mockito-core").version("5.5.0")
            library("mockito-android", "org.mockito", "mockito-android").version("5.5.0")
            library("mockito-kotlin", "org.mockito.kotlin", "mockito-kotlin").version("5.1.0")
            library("mockk", "io.mockk", "mockk").version("1.13.8")
            library("mockk-android", "io.mockk", "mockk-android").version("1.13.8")
            
            // Utils
            library("timber", "com.jakewharton.timber", "timber").version("5.0.1")
            library("gson", "com.google.code.gson", "gson").version("2.10.1")
            library("moshi", "com.squareup.moshi", "moshi").version("1.15.0")
            library("moshi-kotlin", "com.squareup.moshi", "moshi-kotlin-codegen").version("1.15.0")
            
            // Image Loading
            library("glide", "com.github.bumptech.glide", "glide").version("4.16.0")
            library("glide-compiler", "com.github.bumptech.glide", "compiler").version("4.16.0")
            library("coil", "io.coil-kt", "coil").version("2.4.0")
            
            // Security
            library("security-crypto", "androidx.security", "security-crypto").version("1.1.0-alpha06")
            
            // Debugging
            library("leakcanary", "com.squareup.leakcanary", "leakcanary-android").version("2.12")
            
            // Lottie Animations
            library("lottie", "com.airbnb.android", "lottie").version("6.1.0")
            
            // Chucker - Network Inspector
            library("chucker", "com.github.chuckerteam.chucker", "library").version("4.0.0")
            library("chucker-noop", "com.github.chuckerteam.chucker", "library-no-op").version("4.0.0")
        }
        
        // 插件版本目录
        create("pluginLibs") {
            // 插件版本定义
            version("android-gradle", "8.1.0")
            version("kotlin-gradle", "1.9.0")
            version("hilt-gradle", "2.48")
            version("google-services", "4.3.15")
            version("firebase-crashlytics", "2.9.9")
            version("ktlint-gradle", "11.5.1")
            version("detekt-gradle", "1.23.1")
            version("spotless-gradle", "6.20.0")
            version("versions-gradle", "0.48.0")
        }
    }
}

// 项目包含
include(":app")

// 可选：包含其他模块
// include(":core")
// include(":data")
// include(":domain")
// include(":features:home")
// include(":features:settings")
// include(":libraries:network")
// include(":libraries:design")

// 项目名称
rootProject.name = "RootDetector"

// 为每个模块设置项目目录（如果使用非标准结构）
project(":app").projectDir = file("app")
// project(":core").projectDir = file("core")
// project(":data").projectDir = file("data")
// project(":domain").projectDir = file("domain")