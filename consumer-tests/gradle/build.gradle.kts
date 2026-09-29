plugins {
    java
}

val sdkRepository = providers.gradleProperty("sdkRepository")
val sdkVersion = providers.gradleProperty("sdkVersion")

repositories {
    maven {
        url = uri(sdkRepository.get())
    }
    mavenCentral()
}

val aggregate by configurations.creating

dependencies {
    implementation(platform("io.github.sendbyte-4j.sendbyte:sendbyte-java-bom:${sdkVersion.get()}"))
    implementation("io.github.sendbyte-4j.sendbyte:sendbyte-java-email")
    aggregate("io.github.sendbyte-4j.sendbyte:sendbyte-java-all:${sdkVersion.get()}")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(11)
}

tasks.register("verifyDependencyBoundaries") {
    val selectiveArtifacts = providers.provider {
        val configuration = configurations.runtimeClasspath.get()

        configuration.resolvedConfiguration.resolvedArtifacts
            .map { artifact -> "${artifact.moduleVersion.id.group}:${artifact.name}" }
            .toSet()
    }
    val aggregateArtifacts = providers.provider {
        aggregate.resolvedConfiguration.resolvedArtifacts
            .map { artifact -> "${artifact.moduleVersion.id.group}:${artifact.name}" }
            .toSet()
    }

    doLast {
        val selective = selectiveArtifacts.get()
        val unrelated = selective.filter { coordinate ->
            coordinate.contains("marketing") ||
                coordinate.contains("webhooks") ||
                coordinate.contains("admin") ||
                coordinate.contains("spring") ||
                coordinate.contains("quarkus")
        }
        check(unrelated.isEmpty()) {
            "Email-only consumer resolved unrelated modules: $unrelated"
        }

        val expectedAggregate = setOf(
            "io.github.sendbyte-4j.sendbyte:sendbyte-java-all",
            "io.github.sendbyte-4j.sendbyte:sendbyte-java-email",
            "io.github.sendbyte-4j.sendbyte:sendbyte-java-marketing",
            "io.github.sendbyte-4j.sendbyte:sendbyte-java-webhooks",
            "io.github.sendbyte-4j.sendbyte:sendbyte-java-admin",
            "io.github.sendbyte-4j.sendbyte:sendbyte-java-core",
        )
        check(aggregateArtifacts.get().containsAll(expectedAggregate)) {
            "Aggregate consumer did not resolve every stable product module"
        }
    }
}

tasks.check {
    dependsOn("verifyDependencyBoundaries")
}
