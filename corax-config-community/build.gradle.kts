@file:Suppress("VulnerableLibrariesLocal")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val commonsLangVersion: String by rootProject
val testModules: String by rootProject
val kamlVersion: String by rootProject
val log4j2Version: String by rootProject
val caffeineVersion: String by rootProject
val mavenArtifactVersion: String by rootProject

plugins {
    kotlin("kapt")
}

tasks.withType<KotlinCompile> {
    //kotlinOptions.languageVersion = "1.8"
    kotlinOptions.freeCompilerArgs += listOf(
        "-XXLanguage:+ReferencesToSyntheticJavaProperties",
    )
}

dependencies {
    compileOnly(project(":corax-config-api"))
    compileOnly(project(":corax-config-general"))


    implementation(group = "com.github.ben-manes.caffeine", name = "caffeine", version = caffeineVersion)
    implementation(group = "org.apache.commons", name = "commons-lang3", version = commonsLangVersion)
    implementation(group = "org.apache.maven", name = "maven-artifact", version = mavenArtifactVersion)
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("javax:javaee-api:7.0")
    implementation("org.mybatis:mybatis:3.4.5")
    implementation("org.apache.wicket:wicket:1.4.15")
    implementation("commons-lang:commons-lang:2.6")
    implementation("commons-io:commons-io:2.6")
    implementation("org.springframework:spring-web:4.3.30.RELEASE")
    implementation("org.springframework:spring-webmvc:4.3.30.RELEASE")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")


    testImplementation(project(":corax-config-api"))
    testImplementation(project(":corax-config-general"))
    testImplementation(group = "com.charleskorn.kaml", name = "kaml", version = kamlVersion)
    testImplementation(group = "org.apache.logging.log4j", name = "log4j-slf4j-impl", version = log4j2Version)

    testModules.split(",").forEach {
        testImplementation(project(":$it"))
    }
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
