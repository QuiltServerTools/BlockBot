pluginManagement {
    repositories {
        mavenCentral()
        maven(url = "https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        gradlePluginPortal()
    }
}

rootProject.name = "blockbot-discord"
include("blockbot-api")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}
