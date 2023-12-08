package com.feysh.corax.config.community.checkers.hardcode

import com.feysh.corax.config.api.*
import com.feysh.corax.config.general.model.ConfigCenter
import com.feysh.corax.config.general.model.processor.IPropagate
import com.feysh.corax.config.general.model.taint.TaintModelingConfig
import kotlinx.serialization.Serializable


abstract class HardCodeBase : AIAnalysisUnit() {
    @Serializable
    data class SinkDataForCheck(
        val reportType: CheckType,
        val enable: Boolean = true,
    )

    init {
        IPropagate.register(ConstantPropagate)
    }

    context (AIAnalysisApi)
    fun applyRules(kind2Checker: Map<String, SinkDataForCheck>) {
        for ((kind, sink) in kind2Checker) {
            if (!sink.enable) {
                continue
            }

            TaintModelingConfig.applyJsonExtSinks(kind, ConfigCenter.methodAccessPathDataBase, object : TaintModelingConfig.IApplySourceSink {
                context(AIAnalysisApi, ISootMethodDecl.CheckBuilder<Any>)
                override fun visitAccessPath(acp: ILocalT<*>) {
                    check(ConstantPropagate.isConst(acp), sink.reportType)
                }
            })
        }
    }
}