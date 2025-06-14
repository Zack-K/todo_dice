import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    id("org.jetbrains.compose") version "1.5.11"
}

group = "com.diceapp"
version = "1.0.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
        withJava()
    }
    
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                
                // ViewModel対応
                implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")
                
                // JSON処理（設定保存用）
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
                
                // テスト用
                implementation("junit:junit:4.13.2")
                implementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.21")
            }
        }
        
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("junit:junit:4.13.2")
                implementation("org.mockito:mockito-core:5.8.0")
                implementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.diceapp.MainKt"
        
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "DiceApp"
            packageVersion = "1.0.0"
            
            description = "多機能ダイス・TODO・ランダム選択アプリ"
            copyright = "© 2024 DiceApp"
            vendor = "DiceApp Team"
        }
    }
}

// コンソールアプリ実行用タスク
tasks.register<JavaExec>("runConsole") {
    group = "application"
    description = "Run DiceApp in console mode"
    val jvmTarget = kotlin.jvm()
    classpath = jvmTarget.compilations.getByName("main").output.allOutputs + 
                jvmTarget.compilations.getByName("main").runtimeDependencyFiles
    mainClass.set("com.diceapp.ConsoleAppKt")
}