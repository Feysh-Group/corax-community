@file:Suppress("ClassName")

package com.feysh.corax.config.general.model

import com.feysh.corax.config.api.AIAnalysisApi
import com.feysh.corax.config.api.AIAnalysisUnit
import com.feysh.corax.config.api.SAOptions
import com.feysh.corax.config.api.baseimpl.matchSoot
import com.feysh.corax.config.general.checkers.GeneralTaintTypes
import com.feysh.corax.config.general.rule.RuleArgumentParser
import com.feysh.corax.config.general.utils.isInstanceOf
import com.feysh.corax.config.general.utils.methodMatch
import kotlinx.serialization.Serializable
import soot.RefType
import soot.Scene

object `stream-summaries`: AIAnalysisUnit() {


    @Serializable
    class Options : SAOptions {
        val streamClasses = listOf(
            listOf(
                "java.io.Writer",
                "java.io.OutputStream"
            ),
            listOf(
                "java.io.Reader",
                "java.io.InputStream",
            )
        )
    }

    var option: Options = Options()

    context(AIAnalysisApi)
    override suspend fun config() {
        for (streamClass in option.streamClasses) {
            val streamClasses = streamClass.mapNotNull { Scene.v().getRefTypeUnsafe(it) }
            for (c in streamClasses) {
                autoGenerateConstructorSummary(c, streamClasses)
            }
        }

        val fileReadSources = ConfigCenter.taintRulesManager.sinks.getRulesByGroupKinds("path-to-read")
        for (fileReadSource in fileReadSources) {
            val readContentSource = fileReadSource.ext.substringAfter("source:", missingDelimiterValue = "")
            if (readContentSource.isBlank()) {
                continue
            }
            method(fileReadSource.methodMatch).sootDecl.forEach {
                it.modelNoArg {
                    val sources = RuleArgumentParser.parseArg2AccessPaths(readContentSource, shouldFillingPath = true)
                    sources.forEach { source ->
                        source.taint += taintOf(GeneralTaintTypes.FileStreamData)
                    }
                }
            }
        }
    }

    context(AIAnalysisApi)
    private fun autoGenerateConstructorSummary(refType: RefType, streamClasses: List<RefType>) {
        val allSubclassOfIncluding = Scene.v().activeHierarchy.getSubclassesOfIncluding(refType.sootClass)
        for (sub in allSubclassOfIncluding) {
            for (sootMethod in sub.methods) {
                if (!sootMethod.isConstructor) {
                    continue
                }

                val taintFromParamIndexes = sootMethod.parameterTypes.withIndex()
                    .filter { it.value.isInstanceOf(streamClasses) }
                    .map { it.index }

                if (taintFromParamIndexes.isNotEmpty()) {
                    method(matchSoot(sootMethod.signature))
                        .modelNoArg {
                            for (from in taintFromParamIndexes) {
                                `this`.taint += parameter(from).taint
                            }
                        }
                }
            }
        }
    }
}