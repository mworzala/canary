plugins {
    java
}

group = "com.mattworzala"
val canaryVersion: String by project
version = canaryVersion

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://repo.spongepowered.org/maven")
}

dependencies {
    val minestomVariant: String by project
    val minestomVersion: String by project
    implementation("com.github.$minestomVariant:Minestom:$minestomVersion")

    val junitVersion: String by project
    implementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    implementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    val junitPlatformVersion: String by project
    implementation("org.junit.platform:junit-platform-launcher:$junitPlatformVersion")
}

tasks {
    test {
        useJUnitPlatform()
    }
}
