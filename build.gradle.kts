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

    base.archivesName.set("${modId}-mc${rootProject.libs.versions.minecraft.get()}")
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
        //mappings(variantOf(rootProject.libs.yarn.mappings) { classifier("v2") })
        mappings("net.fabricmc:yarn:1.17.1+build.14:v2")
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
}

dependencies {
    modImplementation(libs.fabric.kotlin)

    modImplementation(include("eu.pb4", "placeholder-api", "1.0.0-rc2-1.17"))

    //implementation("com.kotlindiscord.kord.extensions:kord-extensions:1.4.1-20210613.173344-25")
    //implementation("com.kotlindiscord.kordex.ext.common:ext-common:1.0.0-SNAPSHOT")
    implementation(libs.kord.extensions)

    implementation("com.uchuhimo:konf:0.23.0")
    implementation("com.uchuhimo:konf-toml:0.23.0")

    subprojects.forEach {
        implementation(project(":${it.name}"))
    }
}

tasks.compileKotlin {
    kotlinOptions {
        jvmTarget = "16"
    }
}
