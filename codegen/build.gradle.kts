plugins {
    java
    id("net.kyori.blossom") version "1.2.0"
}

group = "com.mattworzala.canary"
version = "1.0"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://repo.spongepowered.org/maven")
}

dependencies {
    val autoserviceVersion = rootProject.property("autoservice.version") as String
    implementation("com.google.auto.service:auto-service:${autoserviceVersion}")
    annotationProcessor("com.google.auto.service:auto-service:${autoserviceVersion}")

    val minestomVariant = rootProject.property("minestom.variant") as String
    val minestomVersion = rootProject.property("minestom.version") as String
    implementation("com.github.$minestomVariant:Minestom:$minestomVersion")

    val javapoetVersion = rootProject.property("javapoet.version") as String
    implementation("com.squareup:javapoet:${javapoetVersion}")

    //todo why the main source set dependency on JUnit?
    val junitVersion = rootProject.property("junit.version") as String
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks {
    test {
        useJUnitPlatform()

        testLogging.showExceptions = true
    }

    blossom {
        replaceToken("\$CODEGEN_VERSION$", project.version)
    }
}