import org.gradle.api.tasks.compile.JavaCompile

plugins {
    `java-library`
}

dependencies {
    api(project(":sendbyte-java-core"))
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}
