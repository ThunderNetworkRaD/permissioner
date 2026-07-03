plugins {
    kotlin("multiplatform") version "2.4.0"
    id("maven-publish")
}

group = "org.thundernetwork"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {

        }
    }
}