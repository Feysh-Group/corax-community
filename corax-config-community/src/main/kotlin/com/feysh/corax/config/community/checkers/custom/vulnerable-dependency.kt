package com.feysh.corax.config.community.checkers.custom

import com.feysh.corax.config.api.AIAnalysisApi
import com.feysh.corax.config.api.AIAnalysisUnit
import com.feysh.corax.config.community.CustomizeChecker
import com.feysh.corax.config.general.checkers.analysis.LibVersionProvider
import com.feysh.corax.config.general.model.ConfigCenter.versionRuleManager

@Suppress("ClassName")
object `vulnerable-dependency` : AIAnalysisUnit() {
    context(AIAnalysisApi)
    override suspend fun config() {
        val versionConditionKeys = versionRuleManager.customVersionConditionsGrouped.rules.map { it.key }
        with(preAnalysis) {
            versionConditionKeys.forEach { key ->
                LibVersionProvider.customVersionCondCheck(key)?.let { result ->
                    if (!result.predicateResult) return@forEach
                    result.dependencies.forEach libFor@{ dependency ->
                        val fileLoc = dependency.fileLoc
                        val bugMessage = (result.condition.bugMessage?.let { "$it . " } ?: "") + "${result.condition}"
                        if (fileLoc != null) {
                            report(CustomizeChecker.VulnerableDependency, fileLoc.first, fileLoc.second) {
                                args["bugMessage"] = bugMessage
                            }
                        }
                        val sootLoc = dependency.sootLoc
                        if (sootLoc != null) {
                            report(CustomizeChecker.VulnerableDependency, sootLoc.first, sootLoc.second) {
                                args["bugMessage"] = bugMessage
                            }
                        }
                    }
                }
            }
        }
    }
}