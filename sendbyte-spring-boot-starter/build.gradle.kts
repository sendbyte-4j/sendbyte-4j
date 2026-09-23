import org.gradle.api.tasks.compile.JavaCompile

plugins {
    `java-library`
}

dependencies {
    api(project(":sendbyte-java-core"))
    compileOnly("org.springframework.boot:spring-boot-autoconfigure:3.4.4")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:3.4.4")
    compileOnly(project(":sendbyte-java-admin"))
    compileOnly(project(":sendbyte-java-email"))
    compileOnly(project(":sendbyte-java-marketing"))
    compileOnly(project(":sendbyte-java-webhooks"))
    testImplementation(project(":sendbyte-java-admin"))
    testImplementation(project(":sendbyte-java-email"))
    testImplementation(project(":sendbyte-java-marketing"))
    testImplementation(project(":sendbyte-java-webhooks"))
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("org.springframework.boot:spring-boot-autoconfigure:3.4.4")
    testImplementation("org.springframework.boot:spring-boot-test:3.4.4")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
    options.compilerArgs.add("-Xlint:-processing")
}
