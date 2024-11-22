plugins {
    `java-library`
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        content {
            includeGroup("org.bukkit")
            includeGroup("org.spigotmc")
        }
    }
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
    maven("https://libraries.minecraft.net/")
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnly("org.jetbrains:annotations:25.0.0")
    testCompileOnly("org.jetbrains:annotations:25.0.0")
}

group = "com.github.stefvanschie.inventoryframework"
version = "0.11.0"

java {
    withSourcesJar()
    withJavadocJar()

    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}
