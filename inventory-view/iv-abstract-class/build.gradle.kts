plugins {
    id("com.if.common")
}

dependencies {
    api(project(":inventory-view:iv-abstraction"))

    compileOnly("org.spigotmc:spigot-api:1.20.3-R0.1-SNAPSHOT")
}

description = "iv-abstract-class"