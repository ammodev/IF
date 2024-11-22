plugins {
    id("com.if.common")
}

dependencies {
    api(project(":inventory-view:iv-abstraction"))

    compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")
}

description = "iv-interface"