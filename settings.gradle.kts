pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/google") {
            content { includeGroupByRegex("com\\.android.*"); includeGroup("com.google.testing.platform") }
        }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("https://maven.aliyun.com/repository/google") {
            content {
                includeGroupByRegex("androidx\\..*")
                includeGroupByRegex("com\\.android.*")
                includeGroup("com.google.android.material")
            }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "SportAssistant"
include(":app")
