import org.gradle.api.JavaVersion.VERSION_17
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties
import org.jetbrains.kotlin.incremental.createDirectory

group = "com.feysh.corax"
val pluginModules : String by project
val publishingModules : String by project
val kotlinVersion: String by project
val coroutinesVersion: String by project
val immutableCollectionsVersion: String by project
val pf4jVersion: String by project
val sootVersion: String by rootProject
val cpgVersion: String by rootProject
val junit4Version: String by project
val junit4PlatformVersion: String by project
val commonsLangVersion: String by rootProject
val kotlinLoggingVersion: String? by rootProject
val kotlinSerializationVersion: String by rootProject
val kamlVersion: String by rootProject
val guavaVersion: String by rootProject
val semVer: String? by project
val configDir by extra { file("${layout.buildDirectory.get()}/analysis-config") }
val pluginDir by extra { file("${layout.buildDirectory.get()}/analysis-config/plugins") }
version = semVer ?: "2.8"

plugins {
    `java-library`
    kotlin("jvm") version "1.9.10"
    kotlin("kapt") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.10"
    `maven-publish`
    // Gradle Properties Plugin - read more: https://github.com/stevesaliman/gradle-properties-plugin
    id("net.saliman.properties") version "1.5.2"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

buildscript {
    repositories {
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.9.10")
    }
}

apply(plugin = "org.jetbrains.kotlin.plugin.serialization")


configure<JavaPluginExtension> {
    sourceCompatibility = VERSION_17
    targetCompatibility = VERSION_17
}

apply(from = "build.local.env.gradle.kts")

allprojects {
    group = rootProject.group
    version = rootProject.version

    apply {
        plugin("maven-publish")
        plugin("kotlin")
    }

    tasks {
        withType<JavaCompile> {
            sourceCompatibility = "17"
            targetCompatibility = "17"
            options.encoding = "UTF-8"
            options.compilerArgs = options.compilerArgs + "-Xlint:all"
            options.isIncremental = true
        }
        withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "17"
                freeCompilerArgs = freeCompilerArgs + listOf(
                    "-Xallow-result-return-type",
                    "-Xsam-conversions=class",
                    "-Xcontext-receivers"
                )
                allWarningsAsErrors = false
                incremental = true
            }
        }
        compileTestKotlin {
            kotlinOptions {
                freeCompilerArgs = freeCompilerArgs + listOf(
                    "-Xallow-result-return-type",
                    "-Xsam-conversions=class",
                    "-Xcontext-receivers"
                )
                allWarningsAsErrors = false
                incremental = true
            }
        }
        withType<Test> {
            // set heap size for the test JVM(s)
            minHeapSize = "128m"
            maxHeapSize = "3072m"

            jvmArgs = listOf("-XX:MaxHeapSize=3072m")

            useJUnitPlatform {
                excludeTags = setOf("slow", "IntegrationTest")
            }

            addTestListener(object : TestListener {
                override fun beforeSuite(suite: TestDescriptor) {}
                override fun beforeTest(testDescriptor: TestDescriptor) {}
                override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
                    println("[$testDescriptor.classDisplayName] [$testDescriptor.displayName]: $result.resultType, length - ${(result.endTime - result.startTime) / 1000.0} sec")
                }

                override fun afterSuite(testDescriptor: TestDescriptor, result: TestResult) {
                    if (testDescriptor.parent == null) { // will match the outermost suite
                        println("Test summary: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} successes, ${result.failedTestCount} failures, ${result.skippedTestCount} skipped)")
                    }
                }
            })
//            testLogging {
//                events = setOf(TestLogEvent.STARTED,
//                    TestLogEvent.FAILED,
//                    TestLogEvent.SKIPPED,
//                    TestLogEvent.STANDARD_ERROR,
//            //                        TestLogEvent.STANDARD_OUT,
//            //                        TestLogEvent.PASSED
//                )
//                exceptionFormat = TestExceptionFormat.FULL
//                showExceptions = true
//                showCauses = true
//                showStackTraces = true
//                info.events = debug.events
//                info.exceptionFormat = debug.exceptionFormat
//            }
        }
        withType<Javadoc> {
            isFailOnError=false
            options.encoding = "UTF-8"
        }
    }

    repositories {
        mavenLocal()
        maven("https://maven.aliyun.com/repository/central")
        mavenCentral()
        maven("https://s01.oss.sonatype.org/content/repositories/orgunittestbotsoot-1004/")
        maven("https://plugins.gradle.org/m2")
        maven("https://www.jetbrains.com/intellij-repository/releases")
        maven("https://cache-redirector.jetbrains.com/maven-central")
    }

    dependencies {
        testImplementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib-jdk8", version = kotlinVersion)
        testImplementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib", version = kotlinVersion)
        testImplementation(group = "org.jetbrains.kotlin", name = "kotlin-reflect", version = kotlinVersion)
        testImplementation(
            group = "org.jetbrains.kotlinx",
            name = "kotlinx-collections-immutable-jvm",
            version = immutableCollectionsVersion
        )
    }
}

//subprojects {
//    group = rootProject.group
//    version = rootProject.version
//
//    publishing {
//        publications {
//            create<MavenPublication>("jar") {
//                from(components["java"])
//                groupId = "com.feysh.corax"
//                artifactId = project.name
//            }
//        }
//    }
//}

dependencies {
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-gradle-plugin", version = kotlinVersion)
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-allopen", version = kotlinVersion)
}


configure(
    pluginModules.split(",").map { project(":$it") }
) {
    tasks.withType<KotlinCompile> {
        //kotlinOptions.languageVersion = "1.8"
        kotlinOptions.freeCompilerArgs += listOf(
            "-XXLanguage:+ReferencesToSyntheticJavaProperties",
        )
    }

    // if the variable definitions are put here they are resolved for each subproject
    val pluginId: String by project
    val pluginProvider: String by project
    val pluginDependencies: String by project

    apply {
        plugin("net.saliman.properties")
        plugin("com.github.johnrengelman.shadow")
        plugin("org.jetbrains.kotlin.plugin.serialization")
        plugin("org.jetbrains.kotlin.kapt")
    }


    dependencies{
        compileOnly(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-core-jvm", version = kotlinSerializationVersion)
        compileOnly(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-core", version = kotlinSerializationVersion)
        compileOnly(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = coroutinesVersion)
        compileOnly(group = "org.jetbrains.kotlin", name = "kotlin-stdlib-jdk8", version = kotlinVersion)
        compileOnly(group = "org.jetbrains.kotlin", name = "kotlin-stdlib", version = kotlinVersion)
        compileOnly(group = "org.jetbrains.kotlin", name = "kotlin-reflect", version = kotlinVersion)
        compileOnly(group = "org.jetbrains.kotlin", name = "kotlin-bom", version = kotlinVersion)
        compileOnly(group = "org.jetbrains.kotlinx", name = "kotlinx-collections-immutable-jvm", version = immutableCollectionsVersion)

        compileOnly(sootVersion) { exclude(group="com.google.guava", module="guava") }
        compileOnly(group = "de.fraunhofer.aisec", name = "cpg-core", version = cpgVersion) { exclude(group = "org.apache.logging.log4j") }
        compileOnly(group = "de.fraunhofer.aisec", name = "cpg-language-java", version = cpgVersion) { exclude(group = "org.apache.logging.log4j") }

        compileOnly(group = "com.charleskorn.kaml", name = "kaml", version = kamlVersion)
        compileOnly(group = "io.github.microutils", name = "kotlin-logging", version = kotlinLoggingVersion)

        compileOnly(group = "org.pf4j", name = "pf4j", version = pf4jVersion)
        kapt(group = "org.pf4j",  name = "pf4j", version = pf4jVersion)

        testImplementation(sootVersion)
        testImplementation(group = "de.fraunhofer.aisec", name = "cpg-core", version = cpgVersion) { exclude(group = "org.apache.logging.log4j") }
        testImplementation(group = "de.fraunhofer.aisec", name = "cpg-language-java", version = cpgVersion) { exclude(group = "org.apache.logging.log4j") }

        testImplementation(group = "org.pf4j", name = "pf4j", version = pf4jVersion)
        testImplementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = coroutinesVersion)
        testImplementation(group = "io.github.microutils", name = "kotlin-logging", version = kotlinLoggingVersion)
        testImplementation(group = "org.apache.commons", name = "commons-lang3", version = commonsLangVersion)
        testImplementation(group = "com.google.guava", name = "guava", version = guavaVersion)
        testImplementation(group = "junit", name = "junit", version = junit4Version)
        testImplementation(group = "org.junit.platform", name = "junit-platform-console-standalone", version = junit4PlatformVersion)
    }

    // for the jar task we have to set the plugin properties, so they can be written to the manifest
    tasks.named<Jar>("jar") {
        manifest {
            attributes["Plugin-Id"] = pluginId
            attributes["Plugin-Version"] = archiveVersion
            attributes["Plugin-Provider"] = pluginProvider
            if (pluginDependencies.isNotEmpty()) {
                attributes["Plugin-Dependencies"] = pluginDependencies
            }
        }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    val pluginZip = tasks.register<Zip>("plugin") {
        archiveBaseName.set("${pluginId}-plugin")

        // first taking the classes generated by the jar task
        into("classes") {
            with(tasks.named<Jar>("jar").get())
        }
        // and then we also need to include any libraries that are needed by the plugin
        dependsOn(configurations.runtimeClasspath)
        into("lib") {
            from({
                configurations.runtimeClasspath.get()
                    .filter { it.name.endsWith("jar") }
                    .filter{ !it.name.startsWith("kotlin-stdlib") }
                    .filter{ !it.name.startsWith("kotlinx-serialization-core-jvm") }
                    .filter{ !it.name.startsWith("kotlinx-serialization-json-jvm") }
            })
        }
        archiveExtension.set("zip")
        isZip64 = true
    }



    tasks.register<Copy>("copyRules") {
        dependsOn(pluginZip)
        val fromConfigsDir = projectDir.resolve("rules")
        val toConfigDir = configDir.resolve("rules")
        if (fromConfigsDir.exists()) {
            from(fromConfigsDir)
            into(toConfigDir)
            doFirst {
                println("copy $fromConfigsDir to $toConfigDir")
            }
        }
    }


// the assemblePlugin will copy the zip file into the common plugins directory
    tasks.register<Copy>("copyPlugin") {
        dependsOn("copyRules")
        dependsOn(pluginZip)
        from(pluginZip)
        into(pluginDir)
        val archiveFileName = pluginZip.get().archiveFileName.get()
        val folder = archiveFileName.substringBeforeLast(pluginZip.get().archiveExtension.get()).dropLast(1)
        val extract = "$pluginDir/${folder}"
        doFirst {
            println("delete $extract")
            delete(extract)
            delete("$configDir/default-config.yml")
            delete("$configDir/default-config.normalize.yml")
        }
    }

    tasks.register("createProperties") {
        dependsOn("processResources")
        dependsOn("processTestResources")
        doLast {
            val file = File("${layout.buildDirectory.get()}/resources/test/project.properties")
            file.parentFile.takeIf { !it.exists() }?.createDirectory()
            val engineJar: File? by rootProject.extra
            file.bufferedWriter().use { writer ->
                val properties = Properties()
                properties["engineJar"] = engineJar?.path ?: ""
                properties.store(writer, null)
            }
        }
    }

    tasks {
        "testClasses" {
            dependsOn(named("createProperties"))
        }

        "classes" {
            dependsOn(named("createProperties"))
        }

        "build" {
            dependsOn(named("createProperties"))
            dependsOn(named("plugin"))
            dependsOn(named("copyPlugin"))
        }
    }
}

configure(
    publishingModules.split(",").map { project(":$it") }
) {
    publishing {
        publications {
            create<MavenPublication>("library") {
                from(components["java"])
            }
        }
    }
}
