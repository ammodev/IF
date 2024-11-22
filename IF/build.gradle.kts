plugins {
    id("com.if.common")
    id("com.if.publish")
    id("com.if.shadow")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.3-R0.1-SNAPSHOT")

    api(project(":inventory-view:iv-abstraction"))
    api(project(":inventory-view:iv-abstract-class"))
    api(project(":inventory-view:iv-interface"))
    compileOnly(libs.adventure.api)
    compileOnly(libs.authlib)

    // NMS Versions
    api(project(":nms:abstraction"))
    api(project(":nms:1_20_0"))
    api(project(":nms:1_20_1"))
    api(project(":nms:1_20_2"))
    api(project(":nms:1_20_3-4"))
    api(project(":nms:1_20_6"))
    api(project(":nms:1_21_0"))
    api(project(":nms:1_21_1"))
    api(project(":nms:1_21_2-3"))

    // Test
    testImplementation(libs.jupiter.engine)
}

description = "IF"