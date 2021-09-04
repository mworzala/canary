plugins {
    java
    id("com.mattworzala.canary")
}

group = "com.mattworzala.canary"
version = rootProject.version

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://repo.spongepowered.org/maven")
}

dependencies {
    val minestomVariant: String by rootProject
    val minestomVersion: String by rootProject
    implementation("com.github.$minestomVariant:Minestom:$minestomVersion")

    val junitVersion: String by rootProject
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    testImplementation(rootProject)
}

tasks {
    test {
        useJUnitPlatform()
    }

    canary {

    }
}
