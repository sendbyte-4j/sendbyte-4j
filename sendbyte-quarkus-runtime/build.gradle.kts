import org.gradle.api.tasks.compile.JavaCompile

plugins {
    `java-library`
}

dependencies {
    api(project(":sendbyte-java-core"))
    compileOnly(project(":sendbyte-java-admin"))
    compileOnly(project(":sendbyte-java-email"))
    compileOnly(project(":sendbyte-java-marketing"))
    compileOnly(project(":sendbyte-java-webhooks"))
    testImplementation(project(":sendbyte-java-admin"))
    testImplementation(project(":sendbyte-java-email"))
    testImplementation(project(":sendbyte-java-marketing"))
    testImplementation(project(":sendbyte-java-webhooks"))
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}
