import org.jetbrains.changelog.Changelog

plugins {
    java
    id("maven-publish")
    id("fabric-loom") version "1.11.+"
    kotlin("jvm") version "2.2.20"
    id("io.github.goooler.shadow") version "8.1.7"
    kotlin("plugin.serialization") version "2.2.20"
    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
    id("org.jetbrains.changelog") version "2.+"
}

configurations.implementation.get().extendsFrom(configurations.shadow.get())

allprojects {
    val modId: String by project
    val modName: String by project
    val modVersion: String by project
    val mavenGroup: String by project

    apply(plugin = "fabric-loom")

    base.archivesName.set(modId)
    group = mavenGroup
    version = "$modVersion+${rootProject.libs.versions.minecraft.get()}"

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    loom {
        //serverOnlyMinecraftJar()
    }

    repositories {
        mavenCentral()
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        maven("https://api.modrinth.com/maven")
        maven("https://snapshots-repo.kordex.dev")
        maven("https://releases-repo.kordex.dev")
        maven("https://repo.kordex.dev/snapshots")
        maven("https://mirror-repo.kordex.dev")
    }

    // Declare dependencies
    dependencies {
        // Fabric
        minecraft(rootProject.libs.minecraft)
        mappings(variantOf(rootProject.libs.yarn.mappings) { classifier("v2") })
        modImplementation(rootProject.libs.fabric.loader)

        // Mods
        modImplementation(rootProject.libs.fabric.api)

        // Optional deps
        modRuntimeOnly(rootProject.libs.permission.api)
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
            expand(
                mapOf(
                    "id" to modId,
                    "name" to modName,
                    "version" to modVersion,
                    "fabricLoader" to libs.versions.fabric.loader.get(),
                    "fabricApi" to libs.versions.fabric.api.get(),
                )
            )
        }
    }
}

repositories {
    mavenCentral()
    maven("https://maven.nucleoid.xyz/")
    maven("https://snapshots-repo.kordex.dev")
    maven("https://repo.kord.dev/snapshots")
}

dependencies {
    modImplementation(libs.fabric.kotlin)

    modImplementation(libs.placeholder.api)
    include(libs.placeholder.api)

    modImplementation(libs.permission.api)
    include(libs.permission.api)

    shadow(libs.mcDiscordReserializer)
    shadow(libs.adventure.gson)

    shadow(libs.kord.extensions)
    shadow(libs.emoji)

    shadow(libs.konf.base)
    shadow(libs.konf.toml)

    // Optional deps
    modCompileOnly(rootProject.libs.vanish.api)

    subprojects.forEach {
        implementation(project(":${it.name}", "namedElements"))
        include(project("${it.name}:")) // nest within distribution
    }
}

publishMods {
    file.set(tasks.remapJar.get().archiveFile)
    type.set(STABLE)
    changelog.set(fetchChangelog())

    displayName.set("Blockbot ${version.get()}")
    modLoaders.add("fabric")
    modLoaders.add("quilt")

    val minecraftVersion = rootProject.libs.versions.minecraft.get()
    val curseForgeMinecraftVersion = rootProject.libs.versions.curseforge.minecraft.get()
    curseforge {
        accessToken.set(providers.environmentVariable("CF_API_TOKEN"))
        projectId.set("482904")
        minecraftVersions.add(curseForgeMinecraftVersion)
    }
    modrinth {
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        projectId.set("yKZ9outG")
        minecraftVersions.add(minecraftVersion)
    }
    github {
        accessToken.set(providers.environmentVariable("GITHUB_TOKEN"))
        repository.set(providers.environmentVariable("GITHUB_REPOSITORY").getOrElse("QuiltServerTools/dryrun"))
        commitish.set(providers.environmentVariable("GITHUB_REF_NAME").getOrElse("dryrun"))
    }
}

tasks {
    remapJar {
        dependsOn(shadowJar)
        input.set(shadowJar.get().archiveFile)
    }

    shadowJar {
        from("LICENSE")

        configurations = listOf(
            project.configurations.shadow.get()
        )
        archiveClassifier.set("dev-all")

        minimize {
            exclude(project(":blockbot-api"))
        }

        exclude("kotlin/**", "kotlinx/coroutines/**", "kotlinx/serialization/**", "javax/**", "META-INF")
        exclude("org/checkerframework/**", "org/intellij/**", "org/jetbrains/annotations/**")
        exclude("com/google/gson/**")
        exclude("org/slf4j/**")
        exclude("org/bouncycastle/**")
        exclude("com/codahale/**")

        relocate("com.fasterxml")
        relocate("com.moandjiezana")
        relocate("com.uchuhimo")
        relocate("com.github.zafarkhaja")
        relocate("com.googlecode")
        relocate("com.ibm")
        relocate("com.iwebpp")
        relocate("com.kotlindiscord")
        relocate("com.typesafe")
        relocate("com.vdurmont")
        relocate("javassist")
        relocate("dev.kord")
        relocate("dev.vankka")
        relocate("io.ktor")
        relocate("io.sentry")
        relocate("org.apache.commons")
        relocate("org.eclipse")
        relocate("org.gjt")
        relocate("org.jaxen")
        relocate("org.json")
        relocate("org.koin")
        relocate("org.pf4j")
        relocate("org.relaxng")
        relocate("org.reflections")
        relocate("org.xml")
        relocate("org.xmlpull")
        relocate("org.yaml")
        relocate("org.dom4j")
        relocate("kotlinx.atomicfu")
        relocate("kotlinx.datetime")
        relocate("net.kyori")
        relocate("net.peanuuutz")
    }
}

fun com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.relocate(pattern: String) {
    this.relocate(pattern, "io.github.quiltservertools.blockbotdiscord.libs.$pattern")
}

private fun fetchChangelog(): String {
    val changelog = tasks.getChangelog.get().changelog.get()
    val modVersion: String by project
    return if (changelog.has(modVersion)) {
        changelog.renderItem(
            changelog.get(modVersion).withHeader(false),
            Changelog.OutputType.MARKDOWN
        )
    } else {
        ""
    }
}
