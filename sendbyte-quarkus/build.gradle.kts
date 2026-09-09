import org.gradle.api.tasks.compile.JavaCompile

plugins {
    `java-library`
}

dependencies {
    api(project(":sendbyte-quarkus-runtime"))
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}
