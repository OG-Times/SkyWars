plugins {
    id("java")
}

group = "fun.ogtimes.skywars"
version = rootProject.version

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("net.md-5:bungeecord-api:26.1-R0.1-SNAPSHOT")
}