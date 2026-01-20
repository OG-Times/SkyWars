plugins {
    id("java")
}

group = "fun.ogtimes.skywars"
version = rootProject.version

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("net.md-5:bungeecord-api:1.21-R0.5-SNAPSHOT")
}