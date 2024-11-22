plugins {
    id("com.if.common")
    id("com.if.paperweight")
}

dependencies {
    api(project(":nms:abstraction"))

    paperweight.paperDevBundle("1.21.3-R0.1-SNAPSHOT")
}

description = "1_21_2-3"