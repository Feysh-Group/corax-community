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

package com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.mybatis

import com.feysh.corax.config.community.checkers.frameworks.xml.BasedXmlHandler
import com.feysh.corax.config.general.utils.PositionalXMLReader
import mu.KotlinLogging
import org.apache.ibatis.builder.MapperBuilderAssistant
import org.apache.ibatis.builder.xml.XMLIncludeTransformer
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver
import org.apache.ibatis.executor.keygen.NoKeyGenerator
import org.apache.ibatis.executor.keygen.SelectKeyGenerator
import org.apache.ibatis.mapping.MappedStatement
import org.apache.ibatis.mapping.ResultSetType
import org.apache.ibatis.mapping.SqlCommandType
import org.apache.ibatis.mapping.StatementType
import org.apache.ibatis.parsing.XNode
import org.apache.ibatis.parsing.XPathParser
import org.apache.ibatis.scripting.LanguageDriver
import org.apache.ibatis.scripting.xmltags.MixedSqlNode
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver
import org.apache.ibatis.scripting.xmltags.XMLScriptBuilder
import org.apache.ibatis.session.Configuration
import java.nio.file.Path
import kotlin.io.path.pathString

class MybatisEntry(
    val resource: Path,
    var namespace: String = "",
    var methodSqlList: MutableList<MyBatisTransform.Statement> = mutableListOf()
) {
    val methodSqlMap: Map<String?, String> get() = methodSqlList.associate { it.id to it.phantomBoundSql.sql }

    override fun equals(other: Any?): Boolean {
        if (other !is MybatisEntry) return false
        return namespace == other.namespace && methodSqlList.size == other.methodSqlList.size
    }

    override fun hashCode(): Int {
        return namespace.hashCode() + methodSqlList.size * 123
    }

    override fun toString(): String {
        return namespace
    }
}

class SimpleScriptBuilder(configuration: Configuration, val context: XNode) : XMLScriptBuilder(configuration, context) {
    fun getNode(): MixedSqlNode? {
        return parseDynamicTags(context)
    }
}

open class MyBatisMapperXmlHandler : BasedXmlHandler() {

    companion object {
        const val NAME = "MyBatisHandler"
        private val logger = KotlinLogging.logger {}
    }

    override val name: String = NAME

    override fun detect(name: String?, publicId: String?, systemId: String?): Boolean {
        if (systemId != null) {
            if (name == "mapper" && (systemId.endsWith("mybatis-3-mapper.dtd"))) {
                return true
            }
        }

        return false
    }

    fun compute(filePath: Path, configuration: Configuration): MybatisEntry? {
        logger.debug("process mybatis mapper file: {}", filePath.toUri())
        return streamToSqls(filePath, configuration)
    }

    fun newBuilderAssistant(configuration: Configuration, context: XNode, resource: String): MapperBuilderAssistant {
        val builderAssistant = MapperBuilderAssistant(configuration, resource)
        val namespace: String? = context.getStringAttribute("namespace")
        if (namespace == null || namespace == "") {
            logger.debug { "Mapper's namespace cannot be empty. resource: $resource" }
            return builderAssistant
        }
        builderAssistant.currentNamespace = namespace
        return builderAssistant
    }

    fun initSqlFragments(resource: Path, configuration: Configuration): Boolean {
        val document = PositionalXMLReader.readXMLUnsafe(resource)
        if (document == null) {
            logger.warn { "Failed to process MyBatis SqlFragments in xml: $resource" }
            return false
        }
        val parser = XPathParser(document, true, configuration.variables, XMLMapperEntityResolver())
        val context = parser.evalNode("/mapper") ?: return false
        val builderAssistant = newBuilderAssistant(configuration, context, resource.pathString)
        // create inside <sql> for inline
        val sqlNodes = parser.evalNodes("/mapper/sql")
        parseSqlStatement(sqlNodes, builderAssistant, configuration)
        return true
    }

    fun streamToSqls(resource: Path, configuration: Configuration): MybatisEntry? {
        val document = PositionalXMLReader.readXMLUnsafe(resource)
        if (document == null) {
            logger.warn { "Failed to process MyBatis mappers in xml: $resource" }
            return null
        }

        val parser = XPathParser(document, true, configuration.variables, XMLMapperEntityResolver())

        val context = parser.evalNode("/mapper") ?: return null

        val mybatisEntry = MybatisEntry(resource = resource, namespace = context.getStringAttribute("namespace"))

        val builderAssistant = newBuilderAssistant(configuration, context, resource.pathString)

        buildCrudSqlMap(resource, mybatisEntry.namespace, context, configuration, builderAssistant, mybatisEntry.methodSqlList)
        return mybatisEntry
    }

    private fun buildCrudSqlMap(
        resource: Path,
        namespace: String,
        context: XNode,
        configuration: Configuration,
        builderAssistant: MapperBuilderAssistant,
        methodSqlList: MutableList<MyBatisTransform.Statement>
    ) {
        val langDriver: LanguageDriver = XMLLanguageDriver()
        val crudList = context.evalNodes("select|insert|update|delete")

        for (it in crudList) {
            val methodName: String = it.getStringAttribute("id") ?: continue
            try {
                // 1. enable include
                try {
                    val includeParser = XMLIncludeTransformer(configuration, builderAssistant)
                    includeParser.applyIncludes(it.node)
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to apply include at method name: $methodName in mybatis mapper file: $resource" }
                    continue
                } catch (e: StackOverflowError) {
                    logger.error { "StackOverflowError: Failed to apply include at method name: $methodName in mybatis mapper file: $resource . Please check and fix the xml recursive include field" }
                    continue
                }

                // 2. follow parseStatementNode to remove all keys
                val selectKeyNodes = it.evalNodes("selectKey")
                parseSelectKeyStatement(selectKeyNodes, methodName, langDriver, configuration, builderAssistant)


                // 3. get rootNode to do some simple calculate
                val simpleScriptBuilder = SimpleScriptBuilder(configuration, it)
                val rootNode: MixedSqlNode = simpleScriptBuilder.getNode()!!

                // 4. convert to source
                val myBatisTransform = createMyBatisTransform()
                val sqlContent = myBatisTransform.applyDynamicContent(resource, configuration, namespace, it, rootNode)
                methodSqlList += sqlContent
            } catch (e: Exception) {
                logger.error(e) { "Failed to parse mybatis mapper id: $namespace#$methodName in $resource" }
            }
        }
    }

    open fun createMyBatisTransform(): MyBatisTransform {
        return MyBatisTransform(SimpleSqlNodeTranslatorFactory)
    }

    private fun parseSqlStatement(
        sqlNodes: MutableList<XNode>,
        builderAssistant: MapperBuilderAssistant,
        configuration: Configuration
    ) {
        sqlNodes.forEach {
            var id = it.getStringAttribute("id") ?: return@forEach
            id = builderAssistant.applyCurrentNamespace(id, false)
            synchronized(configuration) {
                configuration.sqlFragments.putIfAbsent(id, it)
            }
        }
    }

    private fun parseSelectKeyStatement(
        selectKeyNodes: MutableList<XNode>,
        methodName: String,
        langDriver: LanguageDriver,
        configuration: Configuration,
        builderAssistant: MapperBuilderAssistant
    ) {
        selectKeyNodes.forEach { selectNode ->
            val id: String = methodName + SelectKeyGenerator.SELECT_KEY_SUFFIX
            parseSelectKeyNode(id, selectNode, Any::class.java, langDriver, "", configuration, builderAssistant)
        }
        // remove before parser
        for (nodeToHandle in selectKeyNodes) {
            nodeToHandle.parent.node.removeChild(nodeToHandle.node)
        }
    }


    private fun parseSelectKeyNode(
        id: String,
        nodeToHandle: XNode,
        parameterTypeClass: Class<*>,
        langDriver: LanguageDriver,
        databaseId: String,
        configuration: Configuration,
        builderAssistant: MapperBuilderAssistant
    ) {
        val statementType =
            StatementType.valueOf(nodeToHandle.getStringAttribute("statementType", StatementType.PREPARED.toString()))
        val keyProperty = nodeToHandle.getStringAttribute("keyProperty")
        val keyColumn = nodeToHandle.getStringAttribute("keyColumn")
        val executeBefore = "BEFORE" == nodeToHandle.getStringAttribute("order", "AFTER")

        val sqlSource = langDriver.createSqlSource(configuration, nodeToHandle, parameterTypeClass)

        builderAssistant.addMappedStatement(
            id, sqlSource, statementType, SqlCommandType.SELECT,
            null as Int?, null as Int?, null as String?, parameterTypeClass, null as String?, null,
            null as ResultSetType?, false, false, false,
            NoKeyGenerator.INSTANCE, keyProperty, keyColumn, databaseId, langDriver, null
        )

        val idWithNamespace = builderAssistant.applyCurrentNamespace(id, false)
        val keyStatement: MappedStatement = configuration.getMappedStatement(idWithNamespace, false)
        configuration.addKeyGenerator(idWithNamespace, SelectKeyGenerator(keyStatement, executeBefore))
    }


}
