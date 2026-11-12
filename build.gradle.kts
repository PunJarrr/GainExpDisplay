import xyz.jpenilla.runpaper.task.RunServer

plugins {
    java
    id("com.gradleup.shadow") version "9.6.1"
    // run-paper 3.1.0 needs Gradle 9.7+ (see gradle/wrapper/gradle-wrapper.properties)
    id("xyz.jpenilla.run-paper") version "3.1.0"
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    // Compile against the OLDEST and SMALLEST API we support: Spigot 1.17.1. Everything that compiles
    // here also exists on Paper, Purpur and Folia, and on every newer version. Paper-only features
    // (its built-in Adventure text) are reached at runtime in NativeMessenger instead.
    compileOnly("org.spigotmc:spigot-api:1.17.1-R0.1-SNAPSHOT")

    // MiniMessage and the Adventure text library, bundled into the jar and renamed (see shadowJar)
    // so they never clash with the copy Paper ships. Adventure 4.x still runs on Java 8+.
    implementation("net.kyori:adventure-text-minimessage:4.26.1")
    implementation("net.kyori:adventure-text-serializer-gson:4.26.1") {
        exclude(group = "com.google.code.gson") // every server already has Gson
    }
    implementation("net.kyori:adventure-text-serializer-legacy:4.26.1")

    testImplementation("org.spigotmc:spigot-api:1.17.1-R0.1-SNAPSHOT")
    testImplementation("net.kyori:adventure-text-serializer-plain:4.26.1")
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

tasks {
    compileJava {
        // Minecraft 1.17 servers can run on Java 16, so the jar must too.
        options.release = 16
        options.encoding = "UTF-8"
    }

    test {
        useJUnitPlatform()
    }

    processResources {
        val props = mapOf("version" to project.version, "description" to project.description)
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
        from(rootProject.file("LICENSE"))
    }

    jar {
        // The plain jar is not usable on a server (MiniMessage is missing); only shadowJar is published.
        archiveClassifier = "plain"
    }

    shadowJar {
        archiveClassifier = ""
        relocate("net.kyori", "dev.punjarrr.gainExpDisplay.libs.kyori")
        exclude("META-INF/maven/**", "META-INF/versions/*/module-info.class", "module-info.class")
        mergeServiceFiles()
    }

    assemble {
        dependsOn(shadowJar)
    }
}
