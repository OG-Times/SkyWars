plugins {
    id("java")
}

group = "fun.ogtimes.skywars"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:26.0.2")

    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("org.spigotmc:spigot-api:1.21.11-R0.2-SNAPSHOT")
    compileOnly("com.zaxxer:HikariCP:7.0.2")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("me.filoghost.holographicdisplays:holographicdisplays-api:3.0.0")
}