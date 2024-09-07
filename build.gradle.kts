
plugins {
    kotlin("jvm") version "2.0.20"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    kotlin("plugin.serialization") version "1.9.10"
}

group = "me.pog5"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    /* Cloud */
    implementation("org.incendo:cloud-paper:2.0.0-beta.9")
    implementation("org.incendo:cloud-minecraft-extras:2.0.0-beta.9")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.2")

    // PostgreSQL
    implementation("org.postgresql:postgresql:42.6.0") // Or latest version
    implementation("org.jetbrains.exposed:exposed-core:0.42.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.42.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.42.1")
    implementation("org.jetbrains.exposed:exposed-json:0.42.1")

    // SQLite
    implementation("org.xerial:sqlite-jdbc:3.46.1.0") // Or latest version
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}

tasks.runServer {
    minecraftVersion("1.21.1")
}