plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.serialization") version "2.3.10"
    kotlin("plugin.compose") version "2.3.10"
    id("org.jetbrains.compose") version "1.9.3"
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    jacoco
}

group = "com.patalog"
version = "1.0.0"

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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")

    // SQLite
    implementation("org.xerial:sqlite-jdbc:3.51.2.0")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

compose.desktop {
    application {
        mainClass = "com.patalog.MainKt"

        nativeDistributions {
            val os = System.getProperty("os.name").lowercase()
            val formats = when {
                os.contains("win") -> listOf(
                    org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                    org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe
                )
                os.contains("mac") -> listOf(
                    org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg
                )
                else -> listOf(
                    org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
                )
            }
            targetFormats(*formats.toTypedArray())

            packageName = "PataLog"
            packageVersion = "1.0.0"
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
    finalizedBy("jacocoTestReport")
}

kotlin {
    jvmToolchain(17)
}

// Configuración de Detekt
detekt {
    toolVersion = "1.23.8"
    config.setFrom(files("${rootDir}/detekt.yml"))
    parallel = true
    ignoreFailures = true
}

// Configuración de ktlint
ktlint {
    ignoreFailures.set(true)
}

// Configuración de JaCoCo
jacoco {
    toolVersion = "0.8.10"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required.set(true)
        csv.required.set(true)
        html.required.set(true)
    }
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
