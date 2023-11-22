@file:Suppress("MemberVisibilityCanBePrivate", "NON_FINAL_MEMBER_IN_OBJECT")

package com.feysh.corax.config.general.model.taint

import com.feysh.corax.config.api.*
import com.feysh.corax.config.general.checkers.GeneralTaintTypes
import com.feysh.corax.config.general.checkers.fileIoSource
import com.feysh.corax.config.general.checkers.internetSource
import com.feysh.corax.config.general.checkers.userInputSource
import com.feysh.corax.config.general.model.ConfigCenter
import com.feysh.corax.config.general.model.processor.*
import com.feysh.corax.config.general.rule.*
import com.feysh.corax.config.general.utils.methodMatch
import kotlinx.serialization.Serializable
import java.util.*
import kotlin.collections.LinkedHashSet

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
        )

        val sanitizerTaintTypesMap: Map<String, Set<ITaintType>> = mapOf(
            "crlf" to setOf(GeneralTaintTypes.CONTAINS_CRLF),
            "path-traversal" to setOf(GeneralTaintTypes.CONTAINS_PATH_TRAVERSAL),
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
    open fun applyMethodAccessPathConfig(methodAndAcp: IMethodAccessPath, apply: IApplySourceSink) {
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
                        apply.visitAccessPath(acp)
                    }
                } catch (e: Exception) {
                    error.error("${e.message ?: e.toString()} in $methodAndAcp")
                }
            }
        }
    }


    context (AIAnalysisApi)
    open fun applyMethodAccessPathConfig(accessPaths: Collection<IMethodAccessPath>, apply: IApplySourceSink) {
        for (accessPath in accessPaths) {
            applyMethodAccessPathConfig(accessPath, apply)
        }
    }

    interface IApplySourceSink {
        context (AIAnalysisApi, ISootMethodDecl.CheckBuilder<Any>)
        fun visitAccessPath(acp: ILocalT<*>)
    }

     open class SimpleApplySink(
        val check: Collection<ITaintType>,
        val excludes: Collection<ITaintType>,
        val report: CheckType,
        val env: BugMessage.Env.() -> Unit = { }
    ) : IApplySourceSink {
        context (AIAnalysisApi, ISootMethodDecl.CheckBuilder<Any>)
        override fun visitAccessPath(acp: ILocalT<*>) {
            val vulnerableBoolExpr = if (excludes.isEmpty()) {
                acp.taint.containsAll(taintOf(check))
            } else {
                acp.taint.containsAll(taintOf(check)) and (!acp.taint.containsAll(taintOf(excludes)))
            }
            check(vulnerableBoolExpr, report, env)
        }
    }


    context (AIAnalysisApi)
    fun applySourceRule(sourceRule: TaintRule.Source, append: Set<ITaintType>) {
        if (!sourceRule.enable)
            return
        applyMethodAccessPathConfig(sourceRule, object : TaintModelingConfig.IApplySourceSink {
            context(AIAnalysisApi, ISootMethodDecl.CheckBuilder<Any>)
            override fun visitAccessPath(acp: ILocalT<*>) {
                acp.taint += taintOf(append) // must plusAssign
            }
        })
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
    override fun config() {
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
                          apply: IApplySourceSink,
                          filter: (rule: IMethodSignature) -> Boolean = { true }
    ) {
        val sinkRules = ruleManager.getRulesByGroupKinds(kind)
        val info = "${this.javaClass.simpleName}: ${sinkRules.size} rules defined in JSON have been found based on kinds: $kind"
        if (sinkRules.isEmpty()) {
            logger.warn { info }
        } else {
            logger.debug { info }
        }
        for (sinkRule in sinkRules) {
            if (!filter(sinkRule)) continue
            applyMethodAccessPathConfig(sinkRule, apply)
        }
    }

    context (AIAnalysisApi)
    fun applyJsonExtSinksDefault(
        kind: String,
        visit: context(AIAnalysisApi, ISootMethodDecl.CheckBuilder<Any>) (acp: ILocalT<*>) -> Unit
    ) {
        applyJsonExtSinks(kind, ConfigCenter.methodAccessPathDataBase,
            object : IApplySourceSink {
                context(api@AIAnalysisApi, builder@ISootMethodDecl.CheckBuilder<Any>)
                override fun visitAccessPath(acp: ILocalT<*>) {
                    visit(this@api, this@builder, acp)
                }
            })
    }
}
