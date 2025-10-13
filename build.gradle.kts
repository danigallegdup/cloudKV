plugins {
    `java-library`
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
}

allprojects {
    group = "io.dani.cloudkv"
    version = "0.1.0"
    repositories { mavenCentral() }
}

subprojects {
    apply(plugin = "java-library")
    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
