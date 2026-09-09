plugins {
    `java-library`
}

dependencies {
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.21.4"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
}

tasks.test {
    jvmArgs("--add-modules", "jdk.httpserver")
}
