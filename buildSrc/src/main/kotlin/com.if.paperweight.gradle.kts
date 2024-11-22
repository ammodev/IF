plugins {
    id("io.papermc.paperweight.userdev")
}

paperweight.reobfArtifactConfiguration =
    io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION

tasks.named("assemble") {
    dependsOn(tasks.reobfJar)
}