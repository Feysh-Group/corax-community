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

@file:Suppress("MemberVisibilityCanBePrivate", "NON_FINAL_MEMBER_IN_OBJECT")

package com.feysh.corax.config.general.model.taint

import com.feysh.corax.config.api.*
import com.feysh.corax.config.general.checkers.GeneralTaintTypes
import com.feysh.corax.config.general.checkers.analysis.JsonExtVisitor
import com.feysh.corax.config.general.checkers.analysis.LibVersionProvider
import com.feysh.corax.config.general.checkers.fileIoSource
import com.feysh.corax.config.general.checkers.internetSource
import com.feysh.corax.config.general.checkers.userInputSource
import com.feysh.corax.config.general.model.ConfigCenter
import com.feysh.corax.config.general.model.processor.*
import com.feysh.corax.config.general.rule.*
import com.feysh.corax.config.general.utils.appendPathEvents
import com.feysh.corax.config.general.utils.methodMatch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.util.*

object TaintModelingConfig : AIAnalysisUnit() {

    init {
        IPropagate.register(TaintPropagate)
        IPropagate.register(TaintSanitizerPropagate)
        IPropagate.register(ValuePropagate)
        IPropagate.register(StrFragmentPropagate)
    }

    @Serializable
    class Options : SAOptions {

        val sourceKindToAppendTaintTypesMap: Map<String, Set<ITaintType>> = mapOf(
            "remote" to internetSource,
            "fileIo" to fileIoSource,
            "userInput" to userInputSource,

            "content-provider" to userInputSource,
            "android-widget" to userInputSource,
            "android-external-storage-dir" to setOf(GeneralTaintTypes.EXTERNAL_STORAGE),
            "device" to setOf(GeneralTaintTypes.CONTAINS_SENSITIVE_DATA),
            "sensitive" to setOf(GeneralTaintTypes.CONTAINS_SENSITIVE_DATA),
            "zip-entry" to (internetSource - GeneralTaintTypes.CONTAINS_PATH_TRAVERSAL) + setOf(GeneralTaintTypes.ZIP_ENTRY_NAME),
            "upload" to setOf(GeneralTaintTypes.FILE_UPLOAD_SOURCE),
            "servlet-output-stream" to setOf(GeneralTaintTypes.SERVLET_OUTPUT_STREAM),
        )

        val sanitizerTaintTypesMap: Map<String, Set<ITaintType>> = mapOf(
            "ctrl" to setOf(GeneralTaintTypes.Untrusted),
            "crlf" to setOf(GeneralTaintTypes.CONTAINS_CRLF),
            "path-traversal" to setOf(GeneralTaintTypes.CONTAINS_PATH_TRAVERSAL, GeneralTaintTypes.ZIP_ENTRY_NAME),
            "unlimited-file-extension" to setOf(GeneralTaintTypes.UNLIMITED_FILE_EXTENSION),
            "sql" to setOf(GeneralTaintTypes.CONTAINS_SQL_INJECT),
            "xss" to setOf(GeneralTaintTypes.CONTAINS_XSS_INJECT),
            "xpath" to setOf(GeneralTaintTypes.CONTAINS_XPATH_INJECT),
            "cmd" to setOf(GeneralTaintTypes.CONTAINS_COMMAND_INJECT),
            "ldap" to setOf(GeneralTaintTypes.CONTAINS_LDAP_INJECT),
            "ognl" to setOf(GeneralTaintTypes.CONTAINS_OGNL_INJECT),
            "redirection" to setOf(GeneralTaintTypes.CONTAINS_REDIRECTION_INJECT),
            "sensitive" to setOf(GeneralTaintTypes.CONTAINS_SENSITIVE_DATA),
        )
    }

    var option: Options = Options()

    context (AIAnalysisApi)
    open fun applyMethodAccessPathConfig(methodAndAcp: IMethodAccessPath, versionCheckResult: LibVersionProvider.VersionCheckResult? = null, apply: IApplySourceSink) {
        if (methodAndAcp is ISelectable && !methodAndAcp.enable)
            return
        if (methodAndAcp.arg.isEmpty()) {
            logger.error { "accessPath of $methodAndAcp is empty" }
            return
        }
        val match = methodAndAcp.methodMatch
        val decl = method(match)
        decl.sootDecl.forEach { methodDecl ->
            methodDecl.modelNoArg {
                try {
                    val accessPaths =
                        RuleArgumentParser.parseArg2AccessPaths(methodAndAcp.arg, shouldFillingPath = true)
                    for (acp in accessPaths) {
                        apply.visitAccessPath(acp, methodAndAcp, versionCheckResult)
                    }
                } catch (e: Exception) {
                    error.error("${e.message ?: e.toString()} in $methodAndAcp")
                }
            }
        }
    }


    fun interface IApplySourceSink {
        context (AIAnalysisApi, ISootMethodDecl.CheckBuilder<Any>)
        fun visitAccessPath(acp: ILocalT<*>, methodAndAcp: IMethodAccessPath, versionCheckResult: LibVersionProvider.VersionCheckResult?)
    }

     open class SimpleApplySink(
        val check: Collection<ITaintType>,
        val excludes: Collection<ITaintType>,
        val report: CheckType,
        val checkExpr: context (AIAnalysisApi, ISootMethodDecl.CheckBuilder<Any>)(e: IBoolExpr) -> IBoolExpr = { it },
        val env: BugMessage.Env.(rule: IMethodAccessPath) -> Unit = { }
    ) : IApplySourceSink {
        context (api@AIAnalysisApi, bdr@ISootMethodDecl.CheckBuilder<Any>)
        override fun visitAccessPath(acp: ILocalT<*>, methodAndAcp: IMethodAccessPath, versionCheckResult: LibVersionProvider.VersionCheckResult?) {
            val vulnerableBoolExpr = if (excludes.isEmpty()) {
                acp.taint.containsAll(taintOf(check))
            } else {
                acp.taint.containsAll(taintOf(check)) and (!acp.taint.containsAll(taintOf(excludes)))
            }
            val envArgs = mutableMapOf<String, String>()
            object : JsonExtVisitor() {
                override fun visitObject(key: String, value: Map<String, JsonElement>) {
                    when (key) {
                        "@env" -> {
                            envArgs.putAll(value.mapValues { it.value.toString() })
                        }
                    }
                }
            }.visitAll(methodAndAcp.ext)
            val env = this.env
            val checkBoolExpr = checkExpr(this@api, this@bdr, vulnerableBoolExpr)
            check(checkBoolExpr, report, env = {
                args.putAll(envArgs)
                env(methodAndAcp)
                versionCheckResult?.let { appendPathEvents(it) }
            })
        }
    }


    context (AIAnalysisApi)
    fun applySourceRule(sourceRule: TaintRule.Source, append: Set<ITaintType>) {
        if (!sourceRule.enable)
            return
        applyMethodAccessPathConfig(sourceRule) { acp, _, _ ->
            acp.taint += taintOf(append) // must plusAssign
        }
    }

    context(AIAnalysisApi)
    private fun applySummaryRule(summary: TaintRule.Summary) {
        if (!summary.enable)
            return
        val match = summary.methodMatch

        val operators = summary.propagate.split(",").mapTo(LinkedHashSet()) { it.trim() }
        method(match).modelNoArgSoot {
            for (operator in operators) {
                try {
                    val fns = IPropagate[operator] ?: continue
                    for (fn in fns) {
                        fn.interpretation(to = summary.to, from = summary.from)
                    }
                } catch (e: Exception) {
                    error.error("${e.message ?: e.toString()} in $summary")
                }
            }
        }
    }


    context (AIAnalysisApi)
    override suspend fun config() {
        val taintRulesManager = ConfigCenter.taintRulesManager
        for ((kind, taintTypes) in option.sourceKindToAppendTaintTypesMap.mapKeys { it.key.lowercase(Locale.getDefault()) }) {
            val sources = taintRulesManager.sources.getRulesByGroupKinds(kind)
            for (sourceRule in sources) {
                applySourceRule(sourceRule, taintTypes)
            }
        }

        for (summary in taintRulesManager.summaries.rules) {
            applySummaryRule(summary)
        }
    }


    context (AIAnalysisApi)
    fun applyJsonExtSinks(kind: String,
                          ruleManager: GroupedMethodsManager<out IMethodAccessPathGrouped>,
                          apply: IApplySourceSink
    ) {
        val sinkRules = ruleManager.getRulesByGroupKinds(kind)
        val info = "${this.javaClass.simpleName}: ${sinkRules.size} rules defined in JSON have been found based on kinds: $kind"
        if (sinkRules.isEmpty()) {
            logger.warn { info }
        } else {
            logger.debug { info }
        }
        for (sinkRule in sinkRules) {
            val checkResult = LibVersionProvider.getVersionCheckResult(sinkRule.ext)
            if (checkResult?.predicateResult == false) {
                continue
            }
            applyMethodAccessPathConfig(sinkRule, checkResult, apply)
        }
    }

    context (AIAnalysisApi)
    fun applyJsonExtSinksDefault(
        kind: String,
        rules: GroupedMethodsManager<out IMethodAccessPathGrouped> = ConfigCenter.methodAccessPathDataBase,
        visit: context(AIAnalysisApi, ISootMethodDecl.CheckBuilder<Any>) (acp: ILocalT<*>, versionCheckResult: LibVersionProvider.VersionCheckResult?) -> Unit
    ) {
        applyJsonExtSinks(kind, rules, object : IApplySourceSink {
            context(api@AIAnalysisApi, bdr@ISootMethodDecl.CheckBuilder<Any>)
            override fun visitAccessPath(acp: ILocalT<*>, methodAndAcp: IMethodAccessPath, versionCheckResult: LibVersionProvider.VersionCheckResult?) {
                visit(this@api, this@bdr, acp, versionCheckResult)
            }
        })
    }
}
