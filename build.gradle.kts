plugins {
    java
    id("maven-publish")
    id("fabric-loom") version "0.10.+"
    kotlin("jvm") version "1.6.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    kotlin("plugin.serialization") version "1.5.21"
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
    version = "$modVersion${getVersionMetadata()}"

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    repositories {
        mavenCentral()
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        maven("https://api.modrinth.com/maven")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
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
        modImplementation(rootProject.libs.styled.chat)
        modRuntime(rootProject.libs.permission.api)
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
    maven("https://maven.kotlindiscord.com/repository/maven-public/")
}

dependencies {
    modImplementation(libs.fabric.kotlin)

    modImplementation(libs.placeholder.api)
    include(libs.placeholder.api)

    shadow(libs.mcDiscordReserializer)
    modImplementation(libs.adventure.fabric)
    include(libs.adventure.fabric)

    shadow(libs.kord.extensions)
    shadow(libs.emoji)

    shadow(libs.konf.base)
    shadow(libs.konf.toml)

    subprojects.forEach {
        implementation(project(":${it.name}", "namedElements"))
        include(project("${it.name}:")) // nest within distribution
    }
}

tasks {
    remapJar {
        dependsOn(shadowJar)
        input.set(shadowJar.get().archiveFile)
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }

    shadowJar {
        from("LICENSE")

        configurations = listOf(
            project.configurations.shadow.get()
        )
        archiveClassifier.set("dev-all")

        exclude("kotlin/**", "kotlinx/coroutines/**", "kotlinx/serialization/**", "javax/**", "META-INF")
        exclude("org/checkerframework/**", "org/intellij/**", "org/jetbrains/annotations/**")
        exclude("com/google/gson/**")
        exclude("net/kyori/**")
        exclude("org/slf4j/**")
        exclude("dev/kord/voice/**")
        exclude("org/bouncycastle/**")
        exclude("com/codahale/**")

        val relocPath = "io.github.quiltservertools.blockbotdiscord.libs."
        relocate("com.fasterxml", relocPath + "com.fasterxml")
        relocate("com.moandjiezana", relocPath + "com.moandjiezana")
        relocate("com.uchuhimo", relocPath + "com.uchuhimo")
        relocate("com.googlecode", relocPath + "com.googlecode")
        relocate("com.kotlindiscord", relocPath + "com.kotlindiscord")
        relocate("com.sun", relocPath + "com.sun")
        relocate("com.typesafe", relocPath + "com.typesafe")
        relocate("com.vdurmont", relocPath + "com.vdurmont")
        relocate("com.ibm.icu", relocPath + "com.ibm.icu")
        relocate("javassist", relocPath + "javassist")
        relocate("dev.kord", relocPath + "dev.kord")
        relocate("io.ktor", relocPath + "io.ktor")
        relocate("io.sentry", relocPath + "io.sentry")
        relocate("org.apache.commons", relocPath + "org.apache.commons")
        relocate("org.eclipse", relocPath + "org.eclipse")
        relocate("org.gjt", relocPath + "org.gjt")
        relocate("org.jaxen", relocPath + "org.jaxen")
        relocate("org.json", relocPath + "org.json")
        relocate("org.koin", relocPath + "org.koin")
        relocate("org.relaxng", relocPath + "org.relaxng")
        relocate("org.xml", relocPath + "org.xml")
        relocate("org.xmlpull", relocPath + "org.xmlpull")
        relocate("org.yaml", relocPath + "org.yaml")
        relocate("org.dom4j", relocPath + "org.dom4j")
        relocate("kotlinx.atomicfu", relocPath + "kotlinx.atomicfu")
        relocate("kotlinx.datetime", relocPath + "kotlinx.datetime")
        relocate("org.reflections", relocPath + "org.reflections")
    }
}

fun getVersionMetadata(): String {
    val buildId = System.getenv("GITHUB_RUN_NUMBER")

    // CI builds only
    if (buildId != null) {
        return "+build.$buildId"
    }

    // No tracking information could be found about the build
    return ""
}
