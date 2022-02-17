plugins {
    kotlin("multiplatform") version "1.6.10"
    id("maven-publish")
}

group = "page.lappe"
version = "0.1"

repositories {
    mavenCentral()
}

kotlin {
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }
    sourceSets {
        val nativeMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
            }
        }
        val nativeTest by getting
    }
}



publishing {
    publications {
        create<MavenPublication>("kotlin-nativ-std") {

        }
    }

    repositories {
        maven {
            name = "github"
            url = uri("https://maven.pkg.github.com/HanLap/kotlin-nativ-std")
        }
    }
}