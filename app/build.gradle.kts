import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("com.android.application")
}

val buildDate = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))
    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

android {
    namespace = "com.dudu.run"
    compileSdk = 35
    buildToolsVersion = "35.0.0"

    defaultConfig {
        applicationId = "com.dudu.run"
        minSdk = 23
        targetSdk = 30
        manifestPlaceholders["tencentMapKey"] = providers.gradleProperty("tencentMapKey").orElse("").get()
        versionCode = 66
        versionName = "2.1.36-at"
        testInstrumentationRunner = "com.dudu.run.test.MaterialUiProbe"
        buildConfigField("String", "BUILD_DATE", "\"" + buildDate + "\"")

        multiDexEnabled = true
    }

    buildFeatures {
        buildConfig = true
    }

    androidResources {
        additionalParameters += listOf("--stable-ids", file("legacy-resource-ids.txt").absolutePath)
    }

    signingConfigs {
        getByName("debug") {
            providers.gradleProperty("localDebugKeystore").orNull?.let { storeFile = file(it) }
        }
    }
    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packaging {
        jniLibs.useLegacyPackaging = true
        resources.excludes += setOf(
            "META-INF/DEPENDENCIES",
            "META-INF/LICENSE*",
            "META-INF/NOTICE*"
        )
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(files("libs/relocated-legacy-runtime.jar"))
    implementation("com.google.android.material:material:1.14.0")
}

