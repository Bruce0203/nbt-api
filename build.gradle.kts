typealias ShadowJar = com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val kotlin_version = "1.8.0"
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("gradle.plugin.com.github.johnrengelman:shadow:7.1.2")
    }
}

plugins {
    kotlin("jvm") version "1.8.0"
    id("maven-publish")

}

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "com.github.johnrengelman.shadow")
    tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        archiveFileName.set("${rootProject.name}.jar")
    }

    repositories {
        mavenCentral()
        maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
        maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
        maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/public/") }
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://libraries.minecraft.net/") }
        maven { url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/") }

    }

    dependencies {
//        api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
        compileOnly("org.spigotmc:spigot-api:1.19.2-R0.1-SNAPSHOT")
        compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
        api("de.tr7zw", "item-nbt-api-plugin", "2.10.0")

    }
    tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        dependsOn(tasks.processResources)
        archiveFileName.set("${rootProject.name}.jar")

        doLast {
            copy {
                val sep = File.separator
                from("${buildDir.absolutePath}${sep}libs$sep${project.name}.jar")
                into("${project.buildDir.absolutePath}${sep}dist")
            }
        }
    }

    tasks {
        processResources {
            repeat(2) {
                filesMatching("**/*.yml") {
                    expand(HashMap(rootProject.properties)
                        .apply { putAll(project.properties) }
                        .apply { put("version", rootProject.version)})
                }
            }
        }
    }

    lateinit var sourcesArtifact: PublishArtifact


    tasks {
        artifacts {
            sourcesArtifact = archives(getByName("shadowJar")) {
                classifier = null
            }
        }
    }

    apply(plugin = "maven-publish")

    publishing {
        val repo = System.getenv("GITHUB_REPOSITORY")
        if (repo === null) return@publishing
        repositories {
            maven {
                url = uri("https://s01.oss.sonatype.org/content/repositories/releases/")
                credentials {

                    username = System.getenv("SONATYPE_USERNAME")
                    password = System.getenv("SONATYPE_PASSWORD")
                }
            }
        }
        publications {
            register<MavenPublication>(project.name) {
                groupId = "io.github.bruce0203"
                artifactId = project.name.toLowerCase()
                version = System.getenv("GITHUB_BUILD_NUMBER")?: project.version.toString()
                artifact(sourcesArtifact)
            }
        }

    }

}


