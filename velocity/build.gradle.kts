plugins {
    id("java")
}

group = "fun.ogtimes.skywars"
version = "1.0.0"

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    implementation("org.yaml:snakeyaml:2.5")
}