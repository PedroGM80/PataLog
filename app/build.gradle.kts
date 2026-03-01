plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    id("org.jetbrains.compose") version "1.5.11"
}

group = "com.patalog"
version = "0.5.0"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Compose Desktop
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")
    
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // SQLite
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")
    
    // Testing
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

compose.desktop {
    application {
        mainClass = "com.patalog.MainKt"
        
        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
            
            packageName = "PataLog"
            packageVersion = "0.5.0"
            description = "Asistente de transcripcion para consultas veterinarias con IA local"
            copyright = "© 2025 TeckelSoft"
            vendor = "TeckelSoft"
            
            windows {
                menuGroup = "PataLog"
                shortcut = true
                dirChooser = true
                perUserInstall = true
                upgradeUuid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
                iconFile.set(project.file("src/main/resources/icon.ico"))
            }
            
            macOS {
                bundleID = "com.teckelsoft.patalog"
                iconFile.set(project.file("src/main/resources/icon.icns"))
            }
            
            linux {
                packageName = "patalog"
                debMaintainer = "info@teckelsoft.com"
                menuGroup = "Office"
                iconFile.set(project.file("src/main/resources/icon.png"))
            }
            
            // Incluir el backend empaquetado
            appResourcesRootDir.set(project.layout.projectDirectory.dir("../dist"))
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

// Tarea para empaquetar todo
tasks.register("packageAll") {
    group = "distribution"
    description = "Empaqueta backend con PyInstaller y luego la app con jpackage"
    
    doFirst {
        println("=== Empaquetando PataLog ===")
        println("1. Ejecuta primero: cd ../backend && python -m PyInstaller --onedir src/main.py -n patalog-backend")
        println("2. Copia dist/patalog-backend a ../dist/")
        println("3. Luego ejecuta: ./gradlew packageMsi (Windows) o packageDmg (Mac)")
    }
}
