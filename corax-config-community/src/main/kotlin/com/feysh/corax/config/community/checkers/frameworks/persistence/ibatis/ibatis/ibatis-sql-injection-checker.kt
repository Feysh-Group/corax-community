package com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.ibatis

import com.feysh.corax.commons.linesMatch
import com.feysh.corax.config.api.PreAnalysisApi
import com.feysh.corax.config.api.PreAnalysisUnit
import com.feysh.corax.config.api.SAOptions
import com.feysh.corax.config.api.report.Region
import com.feysh.corax.config.community.SqliChecker
import com.feysh.corax.config.general.utils.PositionalXMLReader
import com.feysh.corax.config.general.utils.XmlVisitor
import com.feysh.corax.config.general.utils.region
import kotlinx.serialization.Serializable
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.util.regex.Pattern

@Suppress("ClassName")
object `ibatis-sql-injection-checker` : PreAnalysisUnit() {

    @Serializable
    class Options : SAOptions {
        val reportSqlInjectionSinksAtIbatisSqlMap: Boolean = true
        val checkPreContentMaxLines: Int = 10
    }

    private var option: Options = Options()

    context(PreAnalysisApi)
    override suspend fun config() {
        if (!option.reportSqlInjectionSinksAtIbatisSqlMap) {
            return
        }
        val pattern = Pattern.compile(".*\\$([a-zA-Z0-9.\\[\\]_]+)\\$.*", Pattern.MULTILINE)

        atAnySourceFile(extension = "xml", config = { incrementalAnalyze = false; ignoreProjectConfigProcessFilter = true }) {
            if (path.linesMatch(option.checkPreContentMaxLines) { it.contains("sqlMap") } == null) {
                return@atAnySourceFile
            }
            val doc = PositionalXMLReader.readXMLUnsafe(path) ?: return@atAnySourceFile
            val sqlMapElements: NodeList = doc.getElementsByTagName("sqlMap") ?: return@atAnySourceFile
            if (sqlMapElements.length != 1) return@atAnySourceFile

            object : XmlVisitor() {
                val sinks = mutableSetOf<String>()
                override fun visitAttrNameAndValue(node: Node, name: String, value: String) {
                    if (node.nodeType == Node.ELEMENT_NODE) {
                        val matcher = pattern.matcher(value.trimIndent())
                        while (matcher.find()) {
                            sinks.add(matcher.group(1))
                        }
                        if (sinks.isEmpty()) return
                        report(SqliChecker.IbatisSqlInjectionSinkHint, node.region ?: Region.ERROR) {
                            args["sink"] = sinks.joinToString(", ")
                        }
                        sinks.clear()
                    }
                }
            }.visitElements(doc)
        }
    }
}