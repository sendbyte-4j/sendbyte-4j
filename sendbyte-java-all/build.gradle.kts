plugins {
    `java-library`
}

dependencies {
    api(project(":sendbyte-java-email"))
    api(project(":sendbyte-java-marketing"))
    api(project(":sendbyte-java-webhooks"))
    api(project(":sendbyte-java-admin"))
}
