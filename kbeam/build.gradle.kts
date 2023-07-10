plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.spotless)
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

spotless {
    kotlin {
        ktfmt()
    }
}
