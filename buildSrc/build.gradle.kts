plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("com.gradleup.shadow:shadow-gradle-plugin:8.3.5")
    implementation("io.papermc.paperweight:paperweight-userdev:1.7.4")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
}
