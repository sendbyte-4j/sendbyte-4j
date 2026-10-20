import java.security.MessageDigest
import java.util.zip.ZipFile
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.quality.PmdExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    base
    id("com.diffplug.spotless") version "8.10.2"
    id("com.github.jk1.dependency-license-report") version "3.1.4"
    id("com.vanniktech.maven.publish") version "0.37.0" apply false
}

val stableProductProjects = listOf(
    ":sendbyte-java-email",
    ":sendbyte-java-marketing",
    ":sendbyte-java-webhooks",
    ":sendbyte-java-admin",
)

val publishedProjects = listOf(
    ":sendbyte-java-core",
    *stableProductProjects.toTypedArray(),
    ":sendbyte-java-all",
    ":sendbyte-java-bom",
    ":sendbyte-spring-boot-starter",
    ":sendbyte-quarkus-runtime",
    ":sendbyte-quarkus-deployment",
    ":sendbyte-quarkus",
)

allprojects {
    group = "io.github.sendbyte-4j.sendbyte"
    version = "0.1.0"

    repositories {
        mavenCentral()
    }

    configurations.configureEach {
        resolutionStrategy.failOnVersionConflict()
    }

    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
}

tasks.register("verifyReleaseVersion") {
    group = "verification"
    description = "Verifies that the manually selected release version matches this checkout."

    doLast {
        val selectedVersion = providers.gradleProperty("releaseVersion").orNull
            ?: error("Set -PreleaseVersion to the version selected for this release.")
        check(selectedVersion == project.version.toString()) {
            "Selected release version $selectedVersion does not match project version ${project.version}."
        }
        check(!selectedVersion.endsWith("-SNAPSHOT")) {
            "Snapshot versions cannot be released."
        }
    }
}

subprojects {
    pluginManager.apply("maven-publish")

    if (path in publishedProjects) {
        pluginManager.apply("com.vanniktech.maven.publish")
    }

    plugins.withType<JavaPlugin> {
        pluginManager.apply("pmd")

        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion.set(JavaLanguageVersion.of(17))
        }

        extensions.configure<PmdExtension> {
            toolVersion = "7.24.0"
            isConsoleOutput = true
            rulesMinimumPriority.set(3)
            ruleSets = emptyList()
            ruleSetFiles = files(rootProject.file("config/pmd/ruleset.xml"))
        }

        tasks.withType<JavaCompile>().configureEach {
            options.release.set(11)
            options.encoding = "UTF-8"
            options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))
        }

        tasks.withType<Javadoc>().configureEach {
            // Validate documentation syntax now; completeness remains a release gate.
            (options as StandardJavadocDocletOptions)
                .addStringOption("Xdoclint:all,-missing", "-quiet")
        }

        dependencies {
            "testImplementation"(platform("org.junit:junit-bom:5.14.4"))
            "testImplementation"("org.junit.jupiter:junit-jupiter")
            "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
        }

        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
            val testJava = providers.gradleProperty("testJavaVersion").orElse("17")
            val toolchains = project.extensions.getByType<JavaToolchainService>()
            javaLauncher.set(toolchains.launcherFor {
                languageVersion.set(testJava.map(JavaLanguageVersion::of))
            })
        }

    }

    if (path in publishedProjects) {
        extensions.configure<MavenPublishBaseExtension> {
            publishToMavenCentral(automaticRelease = true)
            if (providers.gradleProperty("signingInMemoryKey").isPresent) {
                signAllPublications()
            }
            pom {
                name.set("SendByte Java SDK - ${project.name}")
                description.set("${project.name} module for the SendByte Java SDK.")
                url.set("https://github.com/sendbyte-4j/sendbyte-4j")
                developers {
                    developer {
                        id.set("masterscode")
                        name.set("Emmanuel Ogbinaka")
                        url.set("https://github.com/masterscode")
                    }
                    developer {
                        id.set("fabian-emmanuel")
                        name.set("Fabian Emmanuel")
                        url.set("https://github.com/fabian-emmanuel")
                    }
                }
                scm {
                    url.set("https://github.com/sendbyte-4j/sendbyte-4j")
                    connection.set("scm:git:https://github.com/sendbyte-4j/sendbyte-4j.git")
                    developerConnection.set("scm:git:ssh://git@github.com/sendbyte-4j/sendbyte-4j.git")
                }
            }
        }
    }

    extensions.configure<PublishingExtension> {
        repositories {
            maven {
                name = "consumerTest"
                url = rootProject.layout.buildDirectory.dir("consumer-repository").get().asFile.toURI()
            }
        }
    }
}

spotless {
    java {
        target("**/src/**/*.java")
        googleJavaFormat("1.28.0").aosp()
        removeUnusedImports()
        forbidWildcardImports()
    }

    format("repositoryFiles") {
        target(
            "*.gradle.kts",
            "*.md",
            ".gitignore",
            ".github/**/*.yml",
            "config/**/*.json",
            "config/**/*.xml",
            "**/build.gradle.kts",
            "consumer-tests/**/*.xml",
        )
        trimTrailingWhitespace()
        endWithNewline()
    }
}

licenseReport {
    allowedLicensesFile = layout.projectDirectory.file("config/allowed-licenses.json")
}

val cleanConsumerRepository by tasks.registering(Delete::class) {
    delete(layout.buildDirectory.dir("consumer-repository"))
}

val publishConsumerRepository by tasks.registering {
    group = "verification"
    description = "Publishes every SDK module to an isolated local repository."
}

gradle.projectsEvaluated {
    val publicationTasks = publishedProjects.map { projectPath ->
        project(projectPath).tasks.named("publishAllPublicationsToConsumerTestRepository")
    }

    publicationTasks.forEach { publicationTask ->
        publicationTask.configure {
            mustRunAfter(cleanConsumerRepository)
        }
    }

    publishConsumerRepository.configure {
        dependsOn(cleanConsumerRepository)
        dependsOn(publicationTasks)
    }
}

val consumerRepositoryUri = layout.buildDirectory.dir("consumer-repository").map { directory ->
    directory.asFile.toURI().toString()
}

val verifyGradleConsumer by tasks.registering(Exec::class) {
    group = "verification"
    description = "Compiles selective, aggregate, and BOM usage in a clean Gradle fixture."
    dependsOn(publishConsumerRepository)

    commandLine(
        "./gradlew",
        "-p",
        "consumer-tests/gradle",
        "clean",
        "check",
        "--no-daemon",
        "-PsdkRepository=${consumerRepositoryUri.get()}",
        "-PsdkVersion=${project.version}",
    )
}

val verifyMavenConsumer by tasks.registering(Exec::class) {
    group = "verification"
    description = "Compiles selective, aggregate, and BOM usage in a clean Maven fixture."
    dependsOn(publishConsumerRepository)

    commandLine(
        "mvn",
        "--batch-mode",
        "--no-transfer-progress",
        "-f",
        "consumer-tests/maven/pom.xml",
        "verify",
        "-Dsdk.repository=${consumerRepositoryUri.get()}",
        "-Dsdk.version=${project.version}",
    )
}

val verifyConsumerFixtures by tasks.registering {
    group = "verification"
    description = "Verifies Maven and Gradle consumers against locally published artifacts."
    dependsOn(verifyGradleConsumer, verifyMavenConsumer)
}

val productionSecretPatterns = mapOf(
    "SendByte live API key" to Regex("""sk_live_[A-Za-z0-9_\-]+"""),
    "SendByte webhook signing secret" to Regex("""whsec_(?!test)[A-Za-z0-9_\-]+"""),
)

fun assertNoProductionSecrets(label: String, content: String) {
    productionSecretPatterns.forEach { (description, pattern) ->
        val match = pattern.find(content)
        check(match == null) {
            "$label contains a possible $description. Replace it with a documented placeholder before committing."
        }
    }
}

val secretScanFiles = fileTree(layout.projectDirectory) {
    include(
        "*.gradle.kts",
        "*.md",
        ".gitignore",
        "config/**/*.json",
        "config/**/*.xml",
        "consumer-tests/**/*.java",
        "consumer-tests/**/*.kt",
        "consumer-tests/**/*.kts",
        "consumer-tests/**/*.xml",
        "sendbyte-*/src/**/*.java",
        "sendbyte-*/src/**/*.json",
        "sendbyte-*/src/**/*.properties",
        "sendbyte-*/src/**/*.yaml",
        "sendbyte-*/src/**/*.yml",
        "spec/**/*.yaml",
    )
    exclude(
        "**/.gradle/**",
        "**/build/**",
        "**/target/**",
    )
}

val verifyNoProductionSecrets by tasks.registering {
    group = "verification"
    description = "Scans source, docs, fixtures, and configuration for production-looking secrets."
    inputs.files(secretScanFiles)

    doLast {
        secretScanFiles.files.forEach { file ->
            assertNoProductionSecrets(file.relativeTo(rootDir).path, file.readText())
        }
    }
}

val verifyPublishedArtifactContents by tasks.registering {
    group = "verification"
    description = "Checks locally published JARs for duplicate entries, test classes, build metadata, and secrets."
    dependsOn(publishConsumerRepository)

    doLast {
        val repository = layout.buildDirectory.dir("consumer-repository").get().asFile
        val jars = repository.walkTopDown().filter { file -> file.isFile && file.extension == "jar" }
        jars.forEach { jar ->
            ZipFile(jar).use { zip ->
                val names = mutableSetOf<String>()
                val duplicates = mutableSetOf<String>()
                zip.entries().asSequence().forEach { entry ->
                    if (!names.add(entry.name)) {
                        duplicates.add(entry.name)
                    }
                    check(!entry.name.endsWith("Test.class")) {
                        "${jar.relativeTo(repository).path} contains test class ${entry.name}"
                    }
                    check(!entry.name.startsWith("build/") && !entry.name.startsWith("tmp/")) {
                        "${jar.relativeTo(repository).path} contains local build metadata ${entry.name}"
                    }
                    check(entry.name != ".DS_Store") {
                        "${jar.relativeTo(repository).path} contains macOS metadata"
                    }
                    if (!entry.isDirectory && isTextArtifactEntry(entry.name)) {
                        val content = zip.getInputStream(entry).bufferedReader().use { it.readText() }
                        assertNoProductionSecrets("${jar.name}!/${entry.name}", content)
                    }
                }
                check(duplicates.isEmpty()) {
                    "${jar.relativeTo(repository).path} contains duplicate entries: $duplicates"
                }
            }
        }
    }
}

fun isTextArtifactEntry(name: String): Boolean =
    name.endsWith(".java") ||
        name.endsWith(".json") ||
        name.endsWith(".properties") ||
        name.endsWith(".yaml") ||
        name.endsWith(".yml") ||
        name.endsWith(".xml") ||
        name.endsWith(".md") ||
        name.endsWith(".txt")

val verifyStableModuleSets by tasks.registering {
    group = "verification"
    description = "Prevents unapproved modules from entering the stable aggregate or BOM."

    doLast {
        val aggregateProjects = project(":sendbyte-java-all")
            .configurations.getByName("api")
            .dependencies
            .filterIsInstance<org.gradle.api.artifacts.ProjectDependency>()
            .map { dependency -> dependency.path }
            .toSet()
        check(aggregateProjects == stableProductProjects.toSet()) {
            "Stable aggregate mismatch: expected $stableProductProjects, found $aggregateProjects"
        }

        val bomProjectNames = project(":sendbyte-java-bom")
            .configurations.getByName("api")
            .dependencyConstraints
            .map { constraint -> constraint.name }
            .toSet()
        val expectedBomProjectNames = publishedProjects
            .filterNot { projectPath -> projectPath == ":sendbyte-java-bom" }
            .map { projectPath -> projectPath.removePrefix(":") }
            .toSet()
        check(bomProjectNames == expectedBomProjectNames) {
            "Stable BOM mismatch: expected $expectedBomProjectNames, found $bomProjectNames"
        }
    }
}

val openApiSnapshot = layout.projectDirectory.file("spec/openapi.yaml")
val openApiUpstreamUrl = providers.gradleProperty("openApiUpstreamUrl")
    .orElse(providers.environmentVariable("OPENAPI_UPSTREAM_URL"))
val expectedOpenApiSha256 = "de37a5be7fee93eebbec0955f5f3ceff7aed856ab2f3da61fd1dadc3c28815c9"

fun ByteArray.sha256(): String = MessageDigest.getInstance("SHA-256")
    .digest(this)
    .joinToString("") { "%02x".format(it) }

val verifyOpenApiSnapshot by tasks.registering {
    group = "verification"
    description = "Verifies that the reviewed OpenAPI snapshot was not edited silently."
    inputs.file(openApiSnapshot)

    doLast {
        val actual = openApiSnapshot.asFile.readBytes().sha256()
        check(actual == expectedOpenApiSha256) {
            "spec/openapi.yaml has changed (expected $expectedOpenApiSha256, found $actual). " +
                "Review the upstream diff and update the snapshot, audit, and checksum together."
        }
    }
}

// Framework integrations compile to Java 17 bytecode, so a Java 11 JVM cannot load their test
// classes and the Java 11 leg of the CI matrix fails with "Could not execute test class". Run
// their tests on Java 17 or newer, still honouring a newer matrix JVM. Core and product modules
// keep honouring -PtestJavaVersion unchanged.
val java17TestProjects = listOf(
    ":sendbyte-quarkus",
    ":sendbyte-quarkus-runtime",
    ":sendbyte-quarkus-deployment",
    ":sendbyte-spring-boot-starter",
)

gradle.projectsEvaluated {
    java17TestProjects.forEach { projectPath ->
        val moduleProject = project(projectPath)
        moduleProject.tasks.withType<Test>().configureEach {
            val toolchains = moduleProject.extensions.getByType<JavaToolchainService>()
            val requested = moduleProject.providers.gradleProperty("testJavaVersion").orElse("17")
            javaLauncher.set(
                toolchains.launcherFor {
                    languageVersion.set(
                        requested.map { value ->
                            val matrix = JavaLanguageVersion.of(value)
                            if (matrix.asInt() >= 17) matrix else JavaLanguageVersion.of(17)
                        }
                    )
                }
            )
        }
    }
}

// Guards the invariant that broke the Java 11 leg: a module's tests must never run on a JVM older
// than the bytecode the module targets. Read from each module's compile release, so a new
// framework module is covered without being listed above.
val verifyTestJvmBaselines by tasks.registering {
    group = "verification"
    description = "Fails when a module's tests would run on a JVM older than its bytecode target."

    doLast {
        project.allprojects
            .filter { moduleProject -> moduleProject.plugins.hasPlugin("java") }
            .forEach { moduleProject ->
                val release =
                    moduleProject.tasks
                        .named("compileJava", JavaCompile::class.java)
                        .get()
                        .options
                        .release
                        .getOrElse(11)
                val launcher =
                    moduleProject.tasks.withType(Test::class.java).first().javaLauncher.get()
                val version = launcher.metadata.languageVersion.asInt()
                check(version >= release) {
                    "${moduleProject.path} compiles to Java $release but its tests would run on Java $version"
                }
            }
    }
}

// Hermetic gates. These run on every edit/verify cycle: no network, no external
// toolchain beyond the JDK, and no local publication.
tasks.check {
    dependsOn(
        verifyOpenApiSnapshot,
        "checkLicense",
        verifyNoProductionSecrets,
        verifyStableModuleSets,
        verifyTestJvmBaselines,
    )
}

// Release gates. Publishing every module and then compiling real Gradle and Maven
// consumers needs `mvn` on PATH plus Maven Central access, so it is deliberately kept
// out of `check` and run once per revision instead of on every matrix leg.
val verifyRelease by tasks.registering {
    group = "verification"
    description = "Runs every gate: check plus consumer fixtures and published artifact checks."
    dependsOn(
        tasks.check,
        verifyConsumerFixtures,
        verifyPublishedArtifactContents,
    )
}

tasks.register("checkOpenApiDrift") {
    group = "verification"
    description = "Compares the reviewed OpenAPI snapshot with its configured public upstream."

    val upstreamUrl = openApiUpstreamUrl.orNull?.trim()
    onlyIf {
        if (upstreamUrl.isNullOrEmpty()) {
            logger.lifecycle(
                "Skipping OpenAPI drift check: set OPENAPI_UPSTREAM_URL or " +
                    "-PopenApiUpstreamUrl to the canonical contract URL."
            )
            false
        } else {
            true
        }
    }

    doLast {
        val connection = java.net.URI(upstreamUrl!!).toURL().openConnection()
        connection.connectTimeout = 10_000
        connection.readTimeout = 30_000
        val upstreamSha256 = connection.getInputStream().use { it.readBytes().sha256() }
        val snapshotSha256 = openApiSnapshot.asFile.readBytes().sha256()

        check(upstreamSha256 == snapshotSha256) {
            "The public OpenAPI contract differs from spec/openapi.yaml " +
                "(upstream $upstreamSha256, snapshot $snapshotSha256). Review the upstream " +
                "diff and update the snapshot, checksum, fixtures, and implementation together."
        }
    }
}
