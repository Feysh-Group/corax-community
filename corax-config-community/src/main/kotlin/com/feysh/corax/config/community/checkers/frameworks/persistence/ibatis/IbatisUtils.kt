package com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis

object IbatisUtils {

    private val riskParameterNameRegex = "\\$\\{\\s*([^{}]+)\\s*}".toRegex()
    fun getRiskNames(sqlContent: String) =
        riskParameterNameRegex.findAll(sqlContent).mapNotNull { it.groupValues.getOrNull(1) }.toList()

}