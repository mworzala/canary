import java.util.Properties

plugins {
    java
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.15.0"
    id("net.kyori.blossom") version "1.2.0"
}

// Load properties from root project
val rootProperties = Properties()
file("${project.rootDir.parent}/gradle.properties").inputStream().use { rootProperties.load(it) }

group = "com.mattworzala.canary"
version = rootProperties.getProperty("canary.gradle.version")

repositories {
    mavenCentral()
}

dependencies {
    val junitVersion = rootProperties.getProperty("junit.version")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks {
    test {
        useJUnitPlatform()
    }

    blossom {
        replaceToken("\$CANARY_GRADLE_VERSION$", rootProperties.getProperty("canary.gradle.version"))
        replaceToken("\$CANARY_VERSION$", rootProperties.getProperty("canary.version"))
    }
}

gradlePlugin {
    plugins {
        create("canary") {
            id = "com.mattworzala.canary"
            implementationClass = "com.mattworzala.canary.gradle.CanaryPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/mworzala/canary"
    vcsUrl = "https://github.com/mworzala/canary.git"

    description = "Setup plugin for the Canary testing framework"

    (plugins) {
        "canary" {
            displayName = "Canary Gradle Plugin"
            tags = listOf("minecraft", "minestom", "junit", "test", "e2e")
            version = project.version as String
        }
    }

    mavenCoordinates {
        groupId = "com.mattworzala"
        artifactId = "canary"
        version = project.version as String
    }
}
