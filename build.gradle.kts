import xyz.jpenilla.runpaper.task.RunServer
import xyz.jpenilla.runtask.service.DownloadsAPIService
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption

plugins {
    java
    id("com.gradleup.shadow") version "9.6.1"
    // run-paper 3.1.0 needs Gradle 9.7+ (see gradle/wrapper/gradle-wrapper.properties)
    id("xyz.jpenilla.run-paper") version "3.1.0"
    // Uploads to Modrinth, and to Hangar (see "Publishing" below)
    id("me.modmuss50.mod-publish-plugin") version "2.2.1"
    id("io.papermc.hangar-publish-plugin") version "0.1.4"
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
        from(rootProject.file("LICENSE"), rootProject.file("credits.txt"))
    }

    jar {
        // The plain jar doesn't work on a server (MiniMessage is missing), so keep it out of build/libs.
        // Only the shadow jar is tested and published.
        archiveClassifier = "plain"
        destinationDirectory = layout.buildDirectory.dir("plain-jar")
    }

    shadowJar {
        archiveClassifier = ""
        relocate("net.kyori", "dev.punjarrr.gainExpDisplay.libs.kyori")
        exclude("META-INF/maven/**", "META-INF/versions/*/module-info.class", "module-info.class")
        // Merge Adventure's service files instead of dropping duplicates
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        mergeServiceFiles()
        filesNotMatching("META-INF/services/**") { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
    }

    assemble {
        dependsOn(shadowJar)
    }
}

// ---------------------------------------------------------------------------
// Publishing: the Publish GitHub workflow runs these when a release is published.
//   MODRINTH_TOKEN and HANGAR_API_TOKEN come from repository secrets; CHANGELOG is the release text.
//   Without MODRINTH_TOKEN, `./gradlew publishMods` only checks the settings (dry run).
// ---------------------------------------------------------------------------
val mcVersions = providers.gradleProperty("mc_versions").get().split(',').map { it.trim() }.filter { it.isNotEmpty() }
val releaseChangelog = providers.environmentVariable("CHANGELOG")
    .orElse("See https://github.com/PunJarrr/GainExpDisplay/blob/master/CHANGELOG.md")

publishMods {
    val modrinthToken = providers.environmentVariable("MODRINTH_TOKEN")
    file = tasks.shadowJar.flatMap { it.archiveFile }
    displayName = "GainExpDisplay ${project.version}"
    version = project.version.toString()
    changelog = releaseChangelog
    type = STABLE
    modLoaders.addAll("paper", "purpur", "folia", "spigot")
    dryRun = modrinthToken.map { false }.orElse(true)

    modrinth {
        projectId = providers.gradleProperty("modrinth_id")
        accessToken = modrinthToken
        minecraftVersions.addAll(mcVersions)
    }
}

hangarPublish {
    publications.register("plugin") {
        version = project.version.toString()
        id = providers.gradleProperty("hangar_id").map { it.substringAfter('/') }
        channel = "Release"
        changelog = releaseChangelog
        apiKey = providers.environmentVariable("HANGAR_API_TOKEN")
        platforms {
            paper {
                jar = tasks.shadowJar.flatMap { it.archiveFile }
                platformVersions = mcVersions
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Test servers: one Gradle task per server we test on. Each runs in run/<platform>-<version>/ with the
// Java version that Minecraft version needs (Gradle downloads missing JDKs).
//
//   ./gradlew runServer                -> Paper 26.3 (newest)
//   ./gradlew runPaper_1_17_1          -> Paper 1.17.1 (oldest)
//   ./gradlew runSpigot_1_21_11        -> Spigot 1.21.11 (built with BuildTools the first time, slow)
//   ./gradlew -q printTestServers      -> every test server, used by scripts/smoke-test.sh
//
// The servers accept the Minecraft EULA (https://aka.ms/MinecraftEULA) in their run/ folders.
// ---------------------------------------------------------------------------
data class TestServer(val platform: String, val minecraftVersion: String) {
    val id = "$platform-$minecraftVersion"
    val taskName = "run" + platform.replaceFirstChar(Char::uppercase) + "_" + minecraftVersion.replace('.', '_')
    val javaVersion = when {
        minecraftVersion.startsWith("26.") -> 25
        minecraftVersion.split('.').let { it[1].toInt() > 20 || (it[1] == "20" && (it.getOrNull(2)?.toInt() ?: 0) >= 5) } -> 21
        else -> 17
    }
}

val testServers = listOf(
    "paper" to listOf("1.17.1", "1.18.2", "1.19.4", "1.20.4", "1.20.6", "1.21.1", "1.21.4", "1.21.11", "26.1.2", "26.2", "26.3"),
    "purpur" to listOf("1.17.1", "26.3"),
    "folia" to listOf("1.19.4", "1.21.11", "26.3"),
    "spigot" to listOf("1.17.1", "1.21.11", "26.3"),
).flatMap { (platform, versions) -> versions.map { TestServer(platform, it) } }

/** Downloads a file once; delete it to download again. */
abstract class DownloadFile : DefaultTask() {
    @get:Input abstract val url: Property<String>
    @get:OutputFile abstract val destination: RegularFileProperty

    init {
        // Checked by file, not by Gradle's history, so a jar restored from the CI cache is used as is
        onlyIf("the file isn't downloaded yet") { !destination.get().asFile.exists() }
    }

    @TaskAction
    fun download() {
        val target = destination.get().asFile.toPath()
        Files.createDirectories(target.parent)
        val partial = target.resolveSibling(target.fileName.toString() + ".part")
        URI(url.get()).toURL().openStream().use { Files.copy(it, partial, StandardCopyOption.REPLACE_EXISTING) }
        Files.move(partial, target, StandardCopyOption.REPLACE_EXISTING)
    }
}

val serverCache = layout.projectDirectory.dir("run/_servers")
val pluginJar = tasks.shadowJar.flatMap { it.archiveFile }

val downloadBuildTools = tasks.register<DownloadFile>("downloadBuildTools") {
    url = "https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar"
    destination = serverCache.file("BuildTools.jar")
}

tasks.register("printTestServers") {
    group = "run paper"
    description = "Lists every test server (platform-version), for scripts/smoke-test.sh and CI."
    val ids = testServers.map { it.id }
    doLast { ids.forEach(::println) }
}

testServers.forEach { server ->
    val serverJava = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(server.javaVersion) }

    // Purpur and Spigot aren't downloaded by run-paper, so prepare their server jar first
    val serverJarFile: Provider<RegularFile>? = when (server.platform) {
        "purpur" -> tasks.register<DownloadFile>("downloadPurpur_${server.minecraftVersion.replace('.', '_')}") {
            url = "https://api.purpurmc.org/v2/purpur/${server.minecraftVersion}/latest/download"
            destination = serverCache.file("purpur-${server.minecraftVersion}.jar")
        }.flatMap { it.destination }
        "spigot" -> {
            val outDir = serverCache.dir("spigot-${server.minecraftVersion}")
            tasks.register<JavaExec>("buildSpigot_${server.minecraftVersion.replace('.', '_')}") {
                group = "run paper"
                description = "Builds Spigot ${server.minecraftVersion} with BuildTools (takes a few minutes)."
                val buildTools = downloadBuildTools.flatMap { it.destination }
                inputs.file(buildTools)
                val spigotJar = outDir.file("spigot-${server.minecraftVersion}.jar")
                outputs.file(spigotJar)
                // Built once; a jar restored from the CI cache is used as is. Delete it to build again.
                onlyIf("Spigot isn't built yet") { !spigotJar.asFile.exists() }
                classpath(buildTools)
                javaLauncher = serverJava
                workingDir = serverCache.dir("buildtools").asFile
                args("--rev", server.minecraftVersion, "--output-dir", outDir.asFile.absolutePath, "--disable-java-check")
                doFirst { workingDir.mkdirs() }
            }.map { outDir.file("spigot-${server.minecraftVersion}.jar") }
        }
        else -> null
    }

    val configure: RunServer.() -> Unit = {
        group = "run paper"
        description = "Runs a ${server.platform} ${server.minecraftVersion} test server with the plugin."
        runDirectory = layout.projectDirectory.dir("run/${server.id}")
        javaLauncher = serverJava
        jvmArgs("-Xms1G", "-Xmx2G", "-Dcom.mojang.eula.agree=true")
        // Old builds otherwise pause 20 seconds with "this build is outdated"
        systemProperty("IReallyKnowWhatIAmDoingISwear", "true")
        when (server.platform) {
            "paper" -> minecraftVersion(server.minecraftVersion)
            "folia" -> {
                downloadsApiService = DownloadsAPIService.folia(project)
                minecraftVersion(server.minecraftVersion)
            }
            else -> {
                version = server.minecraftVersion
                serverJar(serverJarFile!!)
                // Spigot doesn't understand Paper's -add-plugin option; copy the jar into plugins/ instead
                if (server.platform == "spigot") legacyPluginLoading()
            }
        }
        args("nogui")
    }

    if (server.id == "paper-26.3") {
        // The default run-paper task: the newest Paper, already wired to our plugin jar
        tasks.named<RunServer>("runServer") { configure() }
    }
    tasks.register<RunServer>(server.taskName) {
        configure()
        pluginJars(pluginJar)
    }
}
