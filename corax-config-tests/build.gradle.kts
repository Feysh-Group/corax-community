
plugins {
    id("com.github.johnrengelman.shadow")
}

repositories {
    google()
}

java {
    withSourcesJar()
}

tasks.withType<Javadoc> {
    isFailOnError = false
    options.encoding = "UTF-8"
    enabled = false
}
