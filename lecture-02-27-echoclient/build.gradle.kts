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
    testImplementation("org.slf4j:slf4j-simple:1.8.0-beta4")
    implementation("org.slf4j:slf4j-simple:1.8.0-beta4")

    implementation("io.github.microutils:kotlin-logging:1.12.5")
    implementation(project(":utils"))

}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "pt.isel.pc.client.EchoClientKt"
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

