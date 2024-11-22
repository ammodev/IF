plugins {
    id("com.if.common")
}

dependencies {
    api(project(":adventure-support"))

    compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")
}

description = "abstraction"