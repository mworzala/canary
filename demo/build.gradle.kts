import java.util.Properties

plugins {
    java
    id("com.mattworzala.canary")
}

val rootProperties = Properties()
file("${project.rootDir.parent}/gradle.properties").inputStream().use { rootProperties.load(it) }

group = "com.mattworzala.canary"
version = rootProperties.getProperty("canary.version")

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://repo.spongepowered.org/maven")
}

dependencies {
    val minestomVariant = rootProperties.getProperty("minestom.variant")
    val minestomVersion = rootProperties.getProperty("minestom.version")
    implementation("com.github.$minestomVariant:Minestom:$minestomVersion")

//    testImplementation("com.mattworzala:canary:1.1")

    val junitVersion = rootProperties.getProperty("junit.version")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks {
    test {
        useJUnitPlatform()
    }

    canary {

    }
}
