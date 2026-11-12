plugins {
    // Lets Gradle download missing JDKs (17 for 1.17-1.20.4 test servers, 21 for 1.20.5-1.21.x, 25 for 26.x).
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "GainExpDisplay"
