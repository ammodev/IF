plugins {
    id("com.if.common")
    id("com.if.paperweight")
}

dependencies {
    api(project(":nms:abstraction"))

    paperweight.paperDevBundle("1.20.2-R0.1-SNAPSHOT")
}

description = "1_20_2"