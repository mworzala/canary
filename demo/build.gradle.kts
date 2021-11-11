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
    testImplementation(rootProject)

    val minestomVariant = rootProject.property("minestom.variant") as String
    val minestomVersion = rootProject.property("minestom.version") as String
    implementation("com.github.$minestomVariant:Minestom:$minestomVersion")

    val junitVersion = rootProject.property("junit.version") as String
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
