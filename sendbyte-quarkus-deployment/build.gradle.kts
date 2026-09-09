import org.gradle.api.tasks.compile.JavaCompile

plugins {
    `java-library`
}

dependencies {
    implementation(project(":sendbyte-quarkus-runtime"))
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}
