plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("kapt") version "1.9.21"
}

repositories {
    mavenCentral()
}

dependencies {
    kapt("com.google.auto.service:auto-service:1.1.1")

    implementation(project(":filtrate-api"))
    implementation("com.google.auto.service:auto-service:1.1.1")
    implementation("com.squareup:javapoet:1.13.0")
    implementation("jakarta.validation:jakarta.validation-api:${findProperty("jakarta_validation_version")}")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}