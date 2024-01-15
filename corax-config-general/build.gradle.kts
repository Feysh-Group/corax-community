
val kotlinLoggingVersion: String? by rootProject
val kamlVersion: String by rootProject
val commonsLangVersion: String by rootProject
val guavaVersion: String by rootProject
val kotlinSerializationVersion: String by rootProject
val mavenArtifactVersion: String by rootProject
val mybatisApacheVersion: String by rootProject

java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    compileOnly(project(":corax-config-api"))
    compileOnly(group = "org.apache.commons", name = "commons-lang3", version = commonsLangVersion)
    implementation(group = "com.google.guava", name = "guava", version = guavaVersion)
    implementation(group = "org.apache.maven", name = "maven-artifact", version = mavenArtifactVersion)
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version = kotlinSerializationVersion)
    implementation(group = "org.mybatis", name = "mybatis", version = mybatisApacheVersion)

    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("javax:javaee-api:7.0")
    implementation("org.springframework:spring-web:4.3.30.RELEASE")
    implementation("org.springframework:spring-webmvc:4.3.30.RELEASE")
}

sourceSets {
    main{
        java{
            srcDirs("src/main/java", "src/main/kotlin")
        }
        kotlin{
            srcDirs("src/main/java", "src/main/kotlin")
        }
    }
}