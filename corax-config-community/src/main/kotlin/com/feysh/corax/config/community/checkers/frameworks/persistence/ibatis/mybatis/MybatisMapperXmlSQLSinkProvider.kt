package com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.mybatis

import com.feysh.corax.config.api.PreAnalysisApi
import com.feysh.corax.config.api.PreAnalysisUnit
import com.feysh.corax.config.general.utils.parseXmlSafe

object MybatisMapperXmlSQLSinkProvider: PreAnalysisUnit() {
    context (PreAnalysisApi)
    override fun config() {
        atAnySourceFile(extension = "xml") {
            val doc = parseXmlSafe(path) ?: return@atAnySourceFile
            val namespace =
                doc.documentElement.attributes.getNamedItem("namespace")?.nodeValue ?: return@atAnySourceFile

            val selectNodes = doc.getElementsByTagName("select")
            for (i in 0 until selectNodes.length) {
                val selectNode = selectNodes.item(i)
                val selectId = selectNode.attributes.getNamedItem("id")?.nodeValue ?: return@atAnySourceFile
                val parameterType = selectNode.attributes.getNamedItem("parameterType")?.nodeValue
                val resultMap = selectNode.attributes.getNamedItem("resultMap")?.nodeValue
                val select = MybatisMapperXmlSQLSinkConsumer.Select(
                    namespace = namespace, id = selectId,
                    parameterType = parameterType,
                    resultMap = resultMap,
                    sqlContent = selectNode.textContent
                )
                synchronized(MybatisMapperXmlSQLSinkConsumer) {
                    MybatisMapperXmlSQLSinkConsumer.selects.add(select)
                }
            }
        }
    }
}