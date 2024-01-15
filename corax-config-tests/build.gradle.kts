
plugins {
    id("com.github.johnrengelman.shadow")
}

repositories {
    google()
}

tasks.withType<JavaCompile>() {
    options.isWarnings = false
}

java {
    withSourcesJar()
}

tasks.withType<Javadoc> {
    isFailOnError = false
    options.encoding = "UTF-8"
    enabled = false
}
