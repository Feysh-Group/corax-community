@file:Suppress("VulnerableLibrariesLocal")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val commonsLangVersion: String by rootProject
val kamlVersion: String by rootProject
val log4j2Version: String by rootProject
val caffeineVersion: String by rootProject
val mybatisApacheVersion: String by rootProject
val javaparserVersion: String by rootProject
val kotlinSerializationVersion: String by rootProject

plugins {
    kotlin("kapt")
}

tasks.withType<KotlinCompile> {
    //kotlinOptions.languageVersion = "1.8"
    kotlinOptions.freeCompilerArgs += listOf(
        "-XXLanguage:+ReferencesToSyntheticJavaProperties",
    )
}

evaluationDependsOn(":corax-config-tests")
tasks.named<JavaCompile>("compileTestJava") {
    dependsOn(tasks.getByPath(":corax-config-tests:classes"))
}

dependencies {
    compileOnly(project(":corax-config-api"))
    compileOnly(project(":corax-config-general"))

    implementation(group = "org.mybatis", name = "mybatis", version = mybatisApacheVersion)
    implementation(group = "com.github.ben-manes.caffeine", name = "caffeine", version = caffeineVersion)
    implementation(group = "org.apache.commons", name = "commons-lang3", version = commonsLangVersion)
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version = kotlinSerializationVersion)
    implementation(group = "com.github.javaparser", name = "javaparser-core", version = javaparserVersion)

    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("javax:javaee-api:7.0")
    implementation("org.springframework:spring-web:4.3.30.RELEASE")
    implementation("org.springframework:spring-webmvc:4.3.30.RELEASE")


    testImplementation(project(":corax-config-api"))
    testImplementation(project(":corax-config-general"))
    testImplementation(group = "com.charleskorn.kaml", name = "kaml", version = kamlVersion)
    testImplementation(group = "org.apache.logging.log4j", name = "log4j-slf4j-impl", version = log4j2Version)
}


sourceSets {
    main {
        java {
            srcDirs("src/main/java", "src/main/kotlin")
        }
        kotlin {
            srcDirs("src/main/java", "src/main/kotlin")
        }
    }
}
