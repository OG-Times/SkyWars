import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.withType

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.1"
    id("maven-publish")
}

group = "fun.ogtimes.skywars"
version = "1.0.0"

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()

        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/groups/public/")
        maven("https://repo.viaversion.com")
        maven("https://repo.extendedclip.com/releases/")
        maven("https://repo.lunarclient.dev")
        maven("https://jitpack.io")
        maven("https://repo.codemc.io/repository/maven-public/")
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "com.gradleup.shadow")
    apply(plugin = "maven-publish")

    dependencies {
        implementation("org.jetbrains:annotations:26.0.2-1")
        compileOnly("org.projectlombok:lombok:1.18.42")
        annotationProcessor("org.projectlombok:lombok:1.18.42")
    }

    tasks.withType<ShadowJar>().configureEach {
        archiveClassifier.set("")
        archiveFileName.set("${rootProject.name}-${project.name}-${rootProject.version}.jar")

        relocate("org.jspecify", "${project.group}.shaded.jspecify")
        relocate("org.bouncycastle", "${project.group}.shaded.bouncycastle")
        relocate("io.nats", "${project.group}.shaded.nats")
        relocate("com.google", "${project.group}.shaded.google")

        finalizedBy("publishShadowPublicationToMavenLocal")
    }

    publishing {
        publications {
            create<MavenPublication>("shadow") {
                groupId = project.group.toString()
                artifactId = "apple-${project.name}"
                version = project.version.toString()
                artifact(tasks.named<ShadowJar>("shadowJar"))
            }
        }
        repositories {
            mavenLocal()
        }
    }
}