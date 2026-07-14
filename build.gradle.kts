plugins {
    java
}

group = "dev.spawnhelper"
version = "1.0.0"
description = "SpawnHelper - configurable spawn world protection"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.74-stable")
}

tasks {
    processResources {
        val props = mapOf("version" to version, "apiVersion" to "26.1.1")
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    jar {
        archiveFileName.set("SpawnHelper-${version}.jar")
    }

    compileJava {
        options.encoding = "UTF-8"
        options.release.set(25)

    }
}
