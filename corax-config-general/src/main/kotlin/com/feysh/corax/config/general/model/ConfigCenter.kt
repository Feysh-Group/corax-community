package com.feysh.corax.config.general.model

import com.feysh.corax.config.api.CheckerUnit
import com.feysh.corax.config.api.SAOptions
import com.feysh.corax.config.api.utils.ClassCommons
import com.feysh.corax.config.general.model.taint.TaintRule
import com.feysh.corax.config.general.rule.GroupedMethodsManager
import com.feysh.corax.config.general.rule.MethodAccessPath
import com.feysh.corax.config.general.utils.walkFiles
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import mu.KotlinLogging
import soot.PrimType
import soot.Type
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.name
import kotlin.io.path.pathString

@Suppress("MemberVisibilityCanBePrivate")
object ConfigCenter : CheckerUnit() {

    const val configDirectoryIdentifier = "\${analysis-config-dir}"

    var analysisConfigPathRelativizePlugin = ClassCommons.locateAllClass(ConfigCenter::class.java).resolve("../../../").pathString

    @Serializable
    class Options : SAOptions {
         val ruleDirectories: MutableList<String> = mutableListOf(
            "${configDirectoryIdentifier}/rules"
        )
        val taintPrimTypeValue: Boolean = true
        val taintToStringMethod: Boolean = true
    }

    var option: Options = Options()



    fun pathTranslate(paths: List<String>): List<Path> {
        return paths.map { Paths.get(it.replace(configDirectoryIdentifier, analysisConfigPathRelativizePlugin)) }
    }

    fun getConfigDirectories() = pathTranslate(option.ruleDirectories)

    val taintRulesManager by lazy {
        val jsonDirs = getConfigDirectories()
        val sourcesJsonFiles = walkFiles(jsonDirs){ file -> file.name.endsWith("sources.json") }
        val summariesJsonFiles = walkFiles(jsonDirs){ file -> file.name.endsWith("summaries.json") }
        val sinksJsonFiles = walkFiles(jsonDirs){ file -> file.name.endsWith("sinks.json") }

        logger.info { "sourcesJsonFiles: $sourcesJsonFiles" }
        logger.info { "summariesJsonFiles: $summariesJsonFiles" }
        logger.info { "sinksJsonFiles: $sinksJsonFiles" }
        TaintRule.TaintRulesManager.loadJsons(sourcesJsonFiles, summariesJsonFiles, sinksJsonFiles)
    }

    val methodAccessPathDataBase: GroupedMethodsManager<MethodAccessPath> by lazy {
        val sourcesJsonFiles = walkFiles(getConfigDirectories()){ file -> file.name.endsWith("access-path.json") }
        GroupedMethodsManager.load(sourcesJsonFiles, serializer = serializer())
    }

    fun skipTaintPrimitiveType(type: Type) = !option.taintPrimTypeValue && type is PrimType


    private val logger = KotlinLogging.logger {}
}