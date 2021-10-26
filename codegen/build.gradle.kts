plugins {
    java
}

group = "com.mattworzala.canary"
version = "1.0"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://repo.spongepowered.org/maven")
}

dependencies {
    annotationProcessor("com.google.auto.service:auto-service:1.0")
    implementation("com.google.auto.service:auto-service:1.0")

    val minestomVariant: String by rootProject
    val minestomVersion: String by rootProject
    implementation("com.github.$minestomVariant:Minestom:$minestomVersion")

    implementation("com.squareup:javapoet:1.13.0")

    val junitVersion: String by rootProject
    implementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    implementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks {
    test {
        useJUnitPlatform()

        testLogging.showExceptions = true
    }
}