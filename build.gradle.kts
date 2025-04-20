plugins {
    kotlin("jvm") version "2.1.10"
    `java-gradle-plugin`
    `maven-publish`
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
    plugins {
        create("mindugradle") {
            id = "kr.lanthanide.mindugradle"
            implementationClass = "kr.lanthanide.mindugradle.MinduGradle"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}