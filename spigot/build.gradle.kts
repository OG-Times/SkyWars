plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.1"
}

group = "fun.ogtimes.skywars"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation("org.jetbrains:annotations:26.0.2-1")
    implementation("net.kyori:adventure-api:4.26.1")
    implementation("net.kyori:adventure-platform-bukkit:4.4.1")
    implementation("com.zaxxer:HikariCP:7.0.2")

    compileOnly("me.clip:placeholderapi:2.12.1")
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("me.filoghost.holographicdisplays:holographicdisplays-api:3.0.5")
}

tasks.shadowJar {
    minimize()
    relocate("net.kyori", "fun.ogtimes.skywars.libs.kyori")
}