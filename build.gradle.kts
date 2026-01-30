// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.0" apply false
    id("com.android.library") version "8.1.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("com.google.gms.google-services") version "4.3.15" apply false
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    
    dependencies {
        // 注意：这些版本需要根据实际项目调整
        classpath("com.android.tools.build:gradle:8.1.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.48")
        classpath("com.google.gms:google-services:4.3.15")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.9")
        
        // 代码质量检查工具
        classpath("org.jlleitschuh.gradle:ktlint-gradle:11.5.1")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://maven.google.com") }
        
        // 如果需要其他仓库，可以在这里添加
    }
    
    // 应用代码质量检查
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    
    // 配置ktlint
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        debug.set(true)
        verbose.set(true)
        android.set(true)
        outputToConsole.set(true)
        outputColorName.set("RED")
        ignoreFailures.set(false)
        enableExperimentalRules.set(true)
        
        filter {
            exclude { element ->
                val path = element.file.toString()
                path.contains("generated/") || path.contains("build/")
            }
        }
    }
}

tasks {
    register("clean", Delete::class) {
        delete(rootProject.buildDir)
    }
    
    // 自定义任务：代码质量检查
    register("codeQualityCheck") {
        dependsOn("ktlintCheck", "detekt")
        group = "verification"
        description = "Run all code quality checks"
    }
}

// 扩展函数：检查任务是否存在
fun Project.hasTask(taskName: String): Boolean {
    return tasks.findByName(taskName) != null
}

// 配置子项目
subprojects {
    // 为所有子项目添加通用配置
    afterEvaluate {
        // 如果是Android项目
        if (plugins.hasPlugin("com.android.application") || plugins.hasPlugin("com.android.library")) {
            configure<com.android.build.gradle.BaseExtension> {
                // 通用编译选项
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
                
                // 如果是应用模块，配置构建类型
                if (plugins.hasPlugin("com.android.application")) {
                    buildTypes {
                        getByName("debug") {
                            isDebuggable = true
                            isMinifyEnabled = false
                            isShrinkResources = false
                            proguardFiles(
                                getDefaultProguardFile("proguard-android-optimize.txt"),
                                "proguard-rules.pro"
                            )
                        }
                        
                        getByName("release") {
                            isDebuggable = false
                            isMinifyEnabled = true
                            isShrinkResources = true
                            proguardFiles(
                                getDefaultProguardFile("proguard-android-optimize.txt"),
                                "proguard-rules.pro"
                            )
                        }
                    }
                }
            }
        }
        
        // 配置Kotlin选项
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                jvmTarget = "17"
                freeCompilerArgs = listOf(
                    "-Xopt-in=kotlin.RequiresOptIn",
                    "-Xjvm-default=all",
                )
            }
        }
        
        // 配置Java编译选项
        tasks.withType<JavaCompile> {
            options.encoding = "UTF-8"
            options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
        }
        
        // 配置测试任务
        tasks.withType<Test> {
            useJUnitPlatform()
            testLogging {
                events("passed", "skipped", "failed")
                showStandardStreams = true
            }
            maxHeapSize = "4g"
            systemProperty("junit.jupiter.execution.parallel.enabled", "true")
        }
    }
}

// 定义扩展属性
val Project.android: com.android.build.gradle.BaseExtension
    get() = extensions.findByName("android") as? com.android.build.gradle.BaseExtension
        ?: error("Project '$name' is not an Android project")

// 依赖版本管理（推荐使用Version Catalogs，这里是兼容方案）
object Versions {
    const val compileSdk = 34
    const val minSdk = 21
    const val targetSdk = 34
    const val buildTools = "34.0.0"
    
    // Kotlin
    const val kotlin = "1.9.0"
    const val coroutines = "1.7.3"
    
    // AndroidX
    const val coreKtx = "1.12.0"
    const val appcompat = "1.6.1"
    const val material = "1.10.0"
    const val constraintLayout = "2.1.4"
    const val lifecycle = "2.6.2"
    const val navigation = "2.7.4"
    const val room = "2.6.0"
    const val paging = "3.2.1"
    const val workManager = "2.8.1"
    
    // Google
    const val playServices = "18.2.0"
    const val playIntegrity = "1.1.0"
    const val safetyNet = "18.0.1"
    const val gson = "2.10.1"
    
    // Dagger Hilt
    const val hilt = "2.48"
    const val hiltNavigation = "1.0.0"
    
    // Testing
    const val junit = "4.13.2"
    const val junitExt = "1.1.5"
    const val espresso = "3.5.1"
    const val mockito = "5.5.0"
    const val mockitoKotlin = "5.1.0"
    
    // Networking
    const val retrofit = "2.9.0"
    const val okhttp = "5.0.0-alpha.11"
    const val moshi = "1.15.0"
    
    // Image Loading
    const val coil = "2.4.0"
    const val glide = "4.16.0"
    
    // Logging
    const val timber = "5.0.1"
    const val slf4j = "2.0.9"
    
    // Security
    const val securityCrypto = "1.1.0-alpha06"
    
    // Other
    const val lottie = "6.1.0"
    const val leakCanary = "2.12"
}

// 任务：生成版本报告
tasks.register("dependencyReport") {
    doLast {
        val outputFile = File("$buildDir/dependency-report.txt")
        outputFile.parentFile.mkdirs()
        
        val dependencies = mutableMapOf<String, MutableSet<String>>()
        
        allprojects.forEach { project ->
            project.configurations.forEach { config ->
                config.dependencies.forEach { dependency ->
                    val key = "${dependency.group}:${dependency.name}:${dependency.version}"
                    dependencies.getOrPut(key) { mutableSetOf() }.add("${project.name}:${config.name}")
                }
            }
        }
        
        outputFile.bufferedWriter().use { writer ->
            writer.write("=== 项目依赖报告 ===\n")
            writer.write("生成时间: ${java.time.LocalDateTime.now()}\n")
            writer.write("项目总数: ${allprojects.size}\n\n")
            
            dependencies.toSortedMap().forEach { (dependency, usages) ->
                writer.write("$dependency\n")
                usages.sorted().forEach { usage ->
                    writer.write("  - $usage\n")
                }
                writer.write("\n")
            }
        }
        
        println("依赖报告已生成: ${outputFile.absolutePath}")
    }
}

// 任务：检查依赖更新
tasks.register("checkDependencyUpdates") {
    doLast {
        println("检查依赖更新...")
        // 这里可以集成依赖更新检查工具，如 ben-manes/gradle-versions-plugin
        // 需要添加相应的插件: id("com.github.ben-manes.versions") version "0.48.0"
    }
}