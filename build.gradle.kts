plugins {
    kotlin("jvm") version "2.1.10"
    id("com.gradle.plugin-publish") version "1.3.1"
}

group = "kr.lanthanide"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
}

gradlePlugin {
    website = "https://github.com/lantice3720/mindugradle"
    vcsUrl = "https://github.com/lantice3720/mindugradle"
    plugins {
        create("mindugradle") {
            id = "kr.lanthanide.mindugradle"
            implementationClass = "kr.lanthanide.mindugradle.MinduGradle"
            displayName = "MinduGradle"
            description = "Gradle plugin for Mindustry mod development"
            tags.set(listOf("mod", "mindustry"))
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}