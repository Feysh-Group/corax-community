package com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis

import com.feysh.corax.config.api.Elements
import com.feysh.corax.config.api.AIAnalysisApi
import com.feysh.corax.config.api.AIAnalysisUnit
import com.feysh.corax.config.api.MethodConfig
import com.feysh.corax.config.general.checkers.GeneralTaintTypes
import com.feysh.corax.config.community.SqliChecker
import com.feysh.corax.config.general.checkers.internetControl
import com.feysh.corax.config.general.model.ConfigCenter
import soot.tagkit.AnnotationArrayElem
import soot.tagkit.AnnotationStringElem
import soot.tagkit.AnnotationTag

// TODO: need refactor
object IbatisAnnotationSQLSinks : AIAnalysisUnit() {

    context (AIAnalysisApi)
    override suspend fun config() {
        eachMethod {
            val visibilityAnnotationTag = visibilityAnnotationTag ?: return@eachMethod
            if (!visibilityAnnotationTag.hasAnnotations()) {
                return@eachMethod
            }
            for (annotation in visibilityAnnotationTag.annotations) {
                when (annotation.type) {
                    "Lorg/apache/ibatis/annotations/Select;" -> {
                        val selects = annotation.elems.filterIsInstance<AnnotationArrayElem>().flatMap { it.values }
                            .filterIsInstance<AnnotationStringElem>().map { it.value }

                        val riskNames = selects.flatMap { IbatisUtils.getRiskNames(it) }.toSet()
                        if (riskNames.isEmpty()) continue
                        modelNoArg(config = { at = MethodConfig.CheckCall.PrevCallInCaller }) {
                            for (parameterIndex in 0 until sootMethod.parameterCount) {
                                val p = parameter(parameterIndex)
                                val annotations = p.visibilityAnnotationTag?.annotations ?: continue
                                val params = annotations.filterIsInstance<AnnotationTag>()
                                    .filter { it.type == "Lorg/apache/ibatis/annotations/Param;" }.flatMap { it.elems }
                                    .filterIsInstance<AnnotationStringElem>().map { it.value }.toSet()

                                if (params.intersect(riskNames).isNotEmpty()) {
                                    val sink = if (ConfigCenter.isCollectionClassType(p.type) || ConfigCenter.isOptionalClassType(p.type)) p.field(Elements) else p
                                    check(
                                        sink.taint.containsAll(taintOf(internetControl + GeneralTaintTypes.CONTAINS_SQL_INJECT)),
                                        SqliChecker.SqlInjection
                                    ){
                                        this.args["type"] = "ibatis annotations Select"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}