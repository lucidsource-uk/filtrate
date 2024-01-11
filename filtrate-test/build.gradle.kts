plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("kapt") version "1.9.21"
}

repositories {
    mavenCentral()
}

dependencies {
    kapt(project(":filtrate-apt"))

    implementation(project(":filtrate-api"))
    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.2.3")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}