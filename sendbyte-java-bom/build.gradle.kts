plugins {
    `java-platform`
}

dependencies {
    constraints {
        api(project(":sendbyte-java-core"))
        api(project(":sendbyte-java-email"))
        api(project(":sendbyte-java-marketing"))
        api(project(":sendbyte-java-webhooks"))
        api(project(":sendbyte-java-admin"))
        api(project(":sendbyte-java-all"))
    }
}
