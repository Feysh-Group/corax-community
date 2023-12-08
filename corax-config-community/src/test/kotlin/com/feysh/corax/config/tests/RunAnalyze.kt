package com.feysh.corax.config.tests

import com.feysh.corax.config.general.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.management.ManagementFactory


internal fun testSamplesByYmlConfig() { // 推荐的
    runAnalyzeAndTest(listOf("--config", "default-config.yml@build\\analysis-config"))
}

internal fun testSamplesByConfigName() { // 可能废弃
    runAnalyzeAndTest(listOf("--config", "feysh.community.java@build\\analysis-config"))
}

internal fun runAnalyzeAndTest(args: List<String>) = runBlocking {
    val engineJar: String? = Utils.project.getProperty("engineJar")
    println("Used engine: $engineJar")
    if (engineJar.isNullOrEmpty()) {
        println("skipped")
        return@runBlocking
    }

    val testTemplate = mutableListOf(
        Utils.javaExecutableFilePath!!,
        "-jar", engineJar,
        "--verbosity", "info",
        "--output", "build/opt",
        "--make-scorecard",
        "--enable-data-flow",
        "true",
        "--target",
        "java",
        "--auto-app-classes",
        "./corax-config-tests",
        "--result-type", "sarif",
        "--result-type",
        "plist",
    ).also {
        it.addAll(1, ManagementFactory.getRuntimeMXBean().inputArguments)
    }

    var pb = ProcessBuilder()
    val dir = File(".").absoluteFile
    println("Cmd directory: $dir")
    pb = pb.directory(dir)
    pb = pb.command(testTemplate + args)
    println(pb.command().joinToString(" "))
    pb = pb.redirectErrorStream(true)
    withContext(Dispatchers.IO) {
        val process = pb.start()
        launch {
            process.inputStream.copyTo(System.out)
        }
        val code = process.waitFor()
        check(code == 0) { "exit code: $code"  }
    }
}


fun main() { /* <- 点击左侧的蓝色箭头开始执行, 执行前先 gradle build 一次 */
    testSamplesByYmlConfig()
}