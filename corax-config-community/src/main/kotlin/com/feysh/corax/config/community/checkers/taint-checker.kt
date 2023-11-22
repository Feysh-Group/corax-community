package com.feysh.corax.config.community.checkers

import com.feysh.corax.config.api.*                           
import com.feysh.corax.config.community.*
import com.feysh.corax.config.general.checkers.*
import com.feysh.corax.config.general.model.ConfigCenter
import com.feysh.corax.config.general.model.taint.TaintModelingConfig
import kotlinx.serialization.*


@Suppress("ClassName")
object `taint-checker` : AIAnalysisUnit() {
    @Serializable
    data class CustomSinkDataForCheck(
        val checkTaintTypes: Set<ITaintType>,
        val taintTypeExcludes:  Set<ITaintType> = emptySet(),
        val reportType: CheckType,
        val msgArgs: Map<String, String> = emptyMap(),
        val enable: Boolean = true,
    )

    @Serializable
    class Options : SAOptions {
        val kind2Checker: List<Pair<String, CustomSinkDataForCheck>> = listOf(
            "command-injection" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_COMMAND_INJECT, reportType = CmdiChecker.CommandInjection),
            "command-injection-scala" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_COMMAND_INJECT, reportType = CmdiChecker.CommandInjection),

            "sql-injection" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_SQL_INJECT, reportType = SqliChecker.SqlInjection, msgArgs = mapOf("type" to "SQL Sink")),
            "sql-injection-aws" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_SQL_INJECT, reportType = SqliChecker.SqlInjection, msgArgs = mapOf("type" to "AmazonAws ")),
            "sql-injection-hibernate" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_SQL_INJECT, reportType = SqliChecker.SqlInjection, msgArgs = mapOf("type" to "Hibernate")),
            "sql-injection-android" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_SQL_INJECT, reportType = SqliChecker.SqlInjection, msgArgs = mapOf("type" to "Android")),
            "sql-injection-jdo" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_SQL_INJECT, reportType = SqliChecker.SqlInjection, msgArgs = mapOf("type" to "JDO")),
            "sql-injection-jpa" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_SQL_INJECT, reportType = SqliChecker.SqlInjection, msgArgs = mapOf("type" to "Hibernate")),
            "sql-injection-scala-anorm" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_SQL_INJECT, reportType = SqliChecker.SqlInjection, msgArgs = mapOf("type" to "Scala Anorm")),
            "sql-injection-scala-slick" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_SQL_INJECT, reportType = SqliChecker.SqlInjection, msgArgs = mapOf("type" to "Scala Slick")),
            "sql-injection-spring" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_SQL_INJECT, reportType = SqliChecker.SqlInjection, msgArgs =  mapOf("type" to "Spring")),
            "sql-injection-turbine" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_SQL_INJECT, reportType = SqliChecker.SqlInjection, msgArgs = mapOf("type" to "Turbine")),
            "sql-injection-vertx" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_SQL_INJECT, reportType = SqliChecker.SqlInjection, msgArgs =  mapOf("type" to "Vert.x Sql Client")),
            )
    }

    private var option: Options = Options()

    context(AIAnalysisApi)
    override fun config() {
        val kindsExists = ConfigCenter.taintRulesManager.sinks.allKinds
        val unusedKinds = kindsExists - option.kind2Checker.mapTo(mutableSetOf()) { it.first }
        if (unusedKinds.isNotEmpty()) {
            logger.warn { "The following types of rules have not be used: $unusedKinds" }
        }
        for ((kind, sink) in option.kind2Checker) {
            if (!sink.enable) {
                continue
            }
            TaintModelingConfig.applyJsonExtSinks(kind, ConfigCenter.taintRulesManager.sinks, TaintModelingConfig.SimpleApplySink(sink.checkTaintTypes, sink.taintTypeExcludes, sink.reportType) {
                for ((name, msg) in sink.msgArgs) {
                    args[name] = msg
                }
            })
        }
    }
}