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
    implementation(platform("africa.sendbyte:sendbyte-java-bom:${sdkVersion.get()}"))
    implementation("africa.sendbyte:sendbyte-java-email")
    aggregate("africa.sendbyte:sendbyte-java-all:${sdkVersion.get()}")
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
            "africa.sendbyte:sendbyte-java-all",
            "africa.sendbyte:sendbyte-java-email",
            "africa.sendbyte:sendbyte-java-marketing",
            "africa.sendbyte:sendbyte-java-webhooks",
            "africa.sendbyte:sendbyte-java-admin",
            "africa.sendbyte:sendbyte-java-core",
        )
        check(aggregateArtifacts.get().containsAll(expectedAggregate)) {
            "Aggregate consumer did not resolve every stable product module"
        }
    }
}

tasks.check {
    dependsOn("verifyDependencyBoundaries")
}
