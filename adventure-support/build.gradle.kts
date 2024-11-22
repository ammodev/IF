plugins {
    id("com.if.common")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly(libs.adventure.api)
}

description = "adventure-support"