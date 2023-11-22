
val kotlinLoggingVersion: String? by rootProject
val kamlVersion: String by rootProject
val commonsLangVersion: String by rootProject
val guavaVersion: String by rootProject

java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    compileOnly(project(":corax-config-api"))
    compileOnly(group = "org.apache.commons", name = "commons-lang3", version = commonsLangVersion)
    implementation(group = "com.google.guava", name = "guava", version = guavaVersion)

    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("javax:javaee-api:7.0")
    implementation("org.mybatis:mybatis:3.4.5")
    implementation("org.apache.wicket:wicket:1.4.15")
    implementation("commons-lang:commons-lang:2.6")
    implementation("commons-io:commons-io:2.6")
    implementation("org.springframework:spring-web:4.3.30.RELEASE")
    implementation("org.springframework:spring-webmvc:4.3.30.RELEASE")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
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