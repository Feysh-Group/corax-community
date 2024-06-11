/*
 *  CoraxJava - a Java Static Analysis Framework
 *  Copyright (C) 2024.  Feysh-Tech Group
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
@file:Suppress("VulnerableLibrariesLocal")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val apacheCommonsLang3Version: String by rootProject
val kamlVersion: String by rootProject
val log4j2Version: String by rootProject
val caffeineVersion: String by rootProject
val mybatisApacheVersion: String by rootProject
val javaparserVersion: String by rootProject
val kotlinSerializationVersion: String by rootProject
val guavaVersion: String by rootProject
val apacheCommonsTextVersion: String by rootProject

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
    implementation(group = "org.apache.commons", name = "commons-lang3", version = apacheCommonsLang3Version)
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version = kotlinSerializationVersion)
    implementation(group = "com.github.javaparser", name = "javaparser-core", version = javaparserVersion)
    implementation(group = "com.google.guava", name = "guava", version = guavaVersion)
    implementation(group = "org.apache.commons", name = "commons-text", version = apacheCommonsTextVersion)

    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("javax:javaee-api:7.0")
    implementation("org.springframework:spring-web:4.3.30.RELEASE")
    implementation("org.springframework:spring-webmvc:4.3.30.RELEASE")


    testImplementation(project(":corax-config-api"))
    testImplementation(project(":corax-config-general"))
    testImplementation(group = "com.charleskorn.kaml", name = "kaml", version = kamlVersion)
    testImplementation(group = "org.apache.logging.log4j", name = "log4j-core", version = log4j2Version)
    testImplementation(group = "org.apache.logging.log4j", name = "log4j-api", version = log4j2Version)
    testImplementation(group = "org.apache.logging.log4j", name = "log4j-slf4j2-impl", version = log4j2Version)
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
