package com.feysh.corax.config.community.checkers

import com.feysh.corax.config.api.AIAnalysisApi
import com.feysh.corax.config.api.AIAnalysisUnit
import com.feysh.corax.config.community.XssChecker
import com.feysh.corax.config.general.checkers.GeneralTaintTypes
import com.feysh.corax.config.general.checkers.internetControl
import com.feysh.corax.config.general.model.ConfigCenter
import com.feysh.corax.config.general.rule.RuleArgumentParser
import com.feysh.corax.config.general.utils.methodMatch

@Suppress("ClassName")
object `xss-servlet` : AIAnalysisUnit() {
    context(AIAnalysisApi)
    override suspend fun config() {
        val contentWriteFlowRules =
            ConfigCenter.methodMultiAccessPathDataBase.getRulesByGroupKinds("content-write:flow")

        for (sink in contentWriteFlowRules) {
            method(sink.methodMatch).sootDecl.forEach { decl ->
                decl.modelNoArg {
                    val toArg = RuleArgumentParser.parseArg2AccessPaths(sink.args[0], shouldFillingPath = true)
                    val fromArg = RuleArgumentParser.parseArg2AccessPaths(sink.args[1], shouldFillingPath = true)

                    for (to in toArg) {
                        for (from in fromArg) {
                            check(to.taint.containsAll(taintOf(GeneralTaintTypes.SERVLET_OUTPUT_STREAM)) and
                                        from.taint.containsAll(taintOf(internetControl + GeneralTaintTypes.CONTAINS_XSS_INJECT)),
                                XssChecker.XssInjection
                            ) {
                                args["type"] = "Servlet Sink"
                            }
                        }
                    }
                }
            }
        }
    }

}