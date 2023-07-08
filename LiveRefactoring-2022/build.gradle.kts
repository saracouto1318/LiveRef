plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.10.0"
}

group = "com.example"
version = "1.0"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.2.3")
    pluginName.set("LiveRef")
    type.set("IC") // Target IDE Platform
    plugins.set(listOf("com.intellij.java"))
}

dependencies {
    implementation("com.google.firebase:firebase-admin:6.2.0")
    implementation(group = "org.slf4j", name ="slf4j-api", version = "1.7.2")
    implementation(group = "ch.qos.logback", name ="logback-classic", version = "1.0.9")
    implementation(group = "ch.qos.logback", name ="logback-core", version = "1.0.9")
    implementation("org.antlr:antlr4-intellij-adaptor:0.1")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    patchPluginXml {
        sinceBuild.set("213")
        untilBuild.set("223.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}