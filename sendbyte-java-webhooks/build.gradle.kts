plugins {
    `java-library`
}

dependencies {
    api(project(":sendbyte-java-core"))
    compileOnly(platform("com.fasterxml.jackson:jackson-bom:2.21.4"))
    compileOnly("com.fasterxml.jackson.core:jackson-annotations")
    compileOnly("com.fasterxml.jackson.core:jackson-databind")
}
