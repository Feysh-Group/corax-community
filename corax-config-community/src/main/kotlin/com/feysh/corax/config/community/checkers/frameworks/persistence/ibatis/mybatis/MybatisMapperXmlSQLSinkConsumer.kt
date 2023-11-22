package com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.mybatis

import com.feysh.corax.config.api.Elements
import com.feysh.corax.config.api.AIAnalysisApi
import com.feysh.corax.config.api.AIAnalysisUnit
import com.feysh.corax.config.api.MethodConfig
import com.feysh.corax.config.api.baseimpl.matchMethodName
import com.feysh.corax.config.api.utils.typename
import com.feysh.corax.config.general.checkers.GeneralTaintTypes
import com.feysh.corax.config.community.SqliChecker
import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.IbatisUtils
import com.feysh.corax.config.general.checkers.internetControl
import com.feysh.corax.config.general.utils.isCollection
import com.feysh.corax.config.general.utils.isStringType

object MybatisMapperXmlSQLSinkConsumer : AIAnalysisUnit() {


    data class Select(
        val namespace: String, val id: String,
        val parameterType: String?, val resultMap: String?,
        val sqlContent: String,
        var riskParameterNames: List<String> = IbatisUtils.getRiskNames(sqlContent)
    )

    val selects = mutableSetOf<Select>()

    context (AIAnalysisApi)
    override fun config() {
        for (select in selects) {
            if (select.riskParameterNames.isEmpty()) {
                continue
            }
            method(matchMethodName(select.namespace, select.id)).sootDecl.forEach {
                val sootMethod = it.sootMethod
                it.modelNoArg(config = { at = MethodConfig.CheckCall.PrevCallInCaller }) {
                    for (parameterIndex in 0 until sootMethod.parameterCount) {
                        val pt = sootMethod.getParameterType(parameterIndex)
                        when {
                            select.parameterType != null && pt.typename?.endsWith(select.parameterType) == true -> {}
                            select.parameterType == null && pt.isStringType -> {}
                            else -> continue
                        }
                        val p = parameter(parameterIndex)
                        val sink = if (pt.isCollection) p.field(Elements) else p
                        check(
                            sink.taint.containsAll(taintOf(internetControl + GeneralTaintTypes.CONTAINS_SQL_INJECT)),
                            SqliChecker.SqlInjection
                        ){
                            this.args["type"] = "Mybatis Xml Mapper SQL Query"
                        }
                    }

                }
            }
        }
    }
}