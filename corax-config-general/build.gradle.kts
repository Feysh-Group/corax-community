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