import java.net.URI
import java.security.MessageDigest
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService

plugins {
    base
}

allprojects {
    group = "africa.sendbyte"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion.set(JavaLanguageVersion.of(17))
            withSourcesJar()
            withJavadocJar()
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
}

val openApiSnapshot = layout.projectDirectory.file("spec/openapi.yaml")
val expectedOpenApiSha256 = "01c89ddb9d553a9738b02a4c76162a8435c1f5ae0482a384aba4b68d94155b72"

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

tasks.check {
    dependsOn(verifyOpenApiSnapshot)
}

tasks.register("checkOpenApiDrift") {
    group = "verification"
    description = "Checks whether SendByte's published OpenAPI contract differs from the reviewed snapshot."

    doLast {
        val upstream = URI("https://docs.sendbyte.africa/api-reference/openapi.yaml")
            .toURL()
            .openStream()
            .use { it.readBytes() }
        val upstreamSha256 = upstream.sha256()
        check(upstreamSha256 == expectedOpenApiSha256) {
            "The upstream OpenAPI contract changed (reviewed $expectedOpenApiSha256, upstream $upstreamSha256). " +
                "Download and review it; do not overwrite the snapshot automatically."
        }
    }
}
