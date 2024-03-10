plugins {
    kotlin("jvm") version "1.9.20"
}

group = "pt.isel.pc"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("org.slf4j:slf4j-simple:1.8.0-beta4")
    implementation("io.github.microutils:kotlin-logging:1.12.5")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}