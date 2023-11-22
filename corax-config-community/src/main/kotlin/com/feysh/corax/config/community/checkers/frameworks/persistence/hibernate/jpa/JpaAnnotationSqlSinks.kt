package com.feysh.corax.config.community.checkers.frameworks.persistence.hibernate.jpa

import com.feysh.corax.config.api.Elements
import com.feysh.corax.config.api.AIAnalysisApi
import com.feysh.corax.config.api.AIAnalysisUnit
import com.feysh.corax.config.api.MethodConfig
import com.feysh.corax.config.general.checkers.GeneralTaintTypes
import com.feysh.corax.config.community.SqliChecker
import com.feysh.corax.config.general.checkers.internetControl
import com.feysh.corax.config.general.utils.isCollection

object JpaAnnotationSqlSinks : AIAnalysisUnit() {

    context (AIAnalysisApi)
    override fun config() {
        eachMethod {
            val visibilityAnnotationTag = visibilityAnnotationTag ?: return@eachMethod
            if (!visibilityAnnotationTag.hasAnnotations()) {
                return@eachMethod
            }
            for (annotation in visibilityAnnotationTag.annotations) {
                when (annotation.type) {
                    // JpaRepository
                    "Lorg/springframework/data/jpa/repository/Query;" -> {
//                        val selects = annotation.elems.filterIsInstance<AnnotationStringElem>().map { it.value }
                        modelNoArg(config = { at = MethodConfig.CheckCall.PrevCallInCaller }) {
                            for (parameterIndex in 0 until sootMethod.parameterCount) {
                                val p = parameter(parameterIndex)
                                val sink = if (p.type.isCollection) p.field(Elements) else p
                                check(
                                    sink.taint.containsAll(taintOf(internetControl + GeneralTaintTypes.CONTAINS_SQL_INJECT)),
                                    SqliChecker.SqlInjection
                                ){
                                    this.args["type"] = "Springframework Jpa Query"
                                }
                            }
                        }
                    }
                }
            }
        }
    }



}