// Apparently this check does not understand how variables work.
@file:Suppress("GradlePackageUpdate")

plugins {
    java
}

group = "com.mattworzala"
version = project.property("canary.version") as String

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://repo.spongepowered.org/maven")
}

dependencies {
    annotationProcessor(project(":codegen"))


    val minestomVariant = project.property("minestom.variant") as String
    val minestomVersion = project.property("minestom.version") as String
    implementation("com.github.$minestomVariant:Minestom:$minestomVersion")

    val junitVersion = project.property("junit.version") as String
    implementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    implementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion") //todo should depend on platform engine instead?
    val junitPlatformVersion = project.property("junit.platform.version") as String
    implementation("org.junit.platform:junit-platform-launcher:$junitPlatformVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    val mockitoVersion = project.property("mockito.version") as String
    testImplementation("org.mockito:mockito-core:${mockitoVersion}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    val doSafetyChecks = project.findProperty("canary.safety") as? String != "none"
    if (doSafetyChecks) {
        // Run the safety checker only on the main module
        getByName<JavaCompile>("compileJava") {
            options.compilerArgs.add("-Xplugin:CanarySafetyChecks")
        }
    }

    test {
        useJUnitPlatform {
            excludeEngines("canary-test-engine")
        }

        testLogging.showExceptions = true
    }
}
