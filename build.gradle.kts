plugins {
    java
    id("io.github.goooler.shadow") version "8.1.7"
}

group = "Itzjatinog.ogduels"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.citizensnpcs.co/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    // Compiled against the latest 1.21.x API surface to support 1.21 up through recent releases seamlessly
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    
    compileOnly("net.citizensnpcs:citizens-main:2.0.35-SNAPSHOT") {
        exclude(group = "*", module = "*")
    }
    compileOnly("me.clip:placeholderapi:2.11.6")
}

tasks.withType<JavaCompile> {
    options.release.set(21)
}

tasks.shadowJar {
    archiveClassifier.set("") // Keeps output name clean without appending '-all'
}