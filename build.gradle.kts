plugins {
    java
    id("maven-publish")
    id("fabric-loom") version "0.8-SNAPSHOT"
    kotlin("jvm") version "1.5.20"
}

allprojects {
    val modId: String by project
    val modName: String by project
    val modVersion: String by project
    val mavenGroup: String by project

    apply(plugin = "fabric-loom")

    base.archivesName.set(modId)
    group = mavenGroup
    version = modVersion

    java {
        sourceCompatibility = JavaVersion.VERSION_16
        targetCompatibility = JavaVersion.VERSION_16
    }

    // Declare dependencies
    dependencies {
        // Fabric
        minecraft(rootProject.libs.minecraft)
        mappings(variantOf(rootProject.libs.yarn.mappings) { classifier("v2") })
        modImplementation(rootProject.libs.fabric.loader)

        // Mods
        modImplementation(rootProject.libs.fabric.api)
    }

    // Produce a sources distribution
    java {
        withSourcesJar()
    }

    // Add the licence to all distributions
    tasks.withType<AbstractArchiveTask> {
        from(file("LICENSE.txt"))
    }

    // Process any resources
    tasks.processResources {
        inputs.property("id", modId)
        inputs.property("name", modName)
        inputs.property("version", modVersion)

        // fabric.mod.json
        filesMatching("fabric.mod.json") {
            expand(mapOf("id" to modId, "name" to modName, "version" to modVersion))
        }
    }

    // Add any additional repositories
    repositories {
        mavenCentral()
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
    }
}

repositories {
    maven("https://maven.nucleoid.xyz/")
    maven("https://maven.kotlindiscord.com/repository/maven-public/")
    mavenLocal()
}

dependencies {
    modImplementation(libs.fabric.kotlin)

    modImplementation(libs.placeholder.api)
    include(libs.placeholder.api)

    modImplementation(libs.mcDiscordReserializer)
    include(libs.mcDiscordReserializer)

    implementation(libs.kord.extensions)
    implementation(libs.emoji)

    implementation(libs.konf.base)
    implementation(libs.konf.toml)

    subprojects.forEach {
        implementation(project(":${it.name}"))
        include(project("${it.name}:")) // nest within distribution
    }
}

tasks.compileKotlin {
    kotlinOptions {
        jvmTarget = "16"
    }
}
