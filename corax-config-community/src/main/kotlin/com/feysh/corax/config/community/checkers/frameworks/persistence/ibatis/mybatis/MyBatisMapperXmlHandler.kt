package com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.mybatis

import com.feysh.corax.config.community.checkers.frameworks.xml.BasedXmlHandler
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
import kotlin.io.path.inputStream
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

    fun compute(filePath: Path): MybatisEntry? {
        logger.debug("process mybatis mapper file: {}", filePath.toUri())
        return streamToSqls(filePath)
    }

    fun streamToSqls(resource: Path): MybatisEntry? {
        val configuration = createConfiguration()
        return resource.inputStream().use { inputSource ->
            val parser = XPathParser(inputSource, true, configuration.variables, XMLMapperEntityResolver())

            val context = parser.evalNode("/mapper") ?: return@use null

            val mybatisEntry = MybatisEntry(resource = resource, namespace = context.getStringAttribute("namespace"))

            val builderAssistant = MapperBuilderAssistant(configuration, resource.pathString)

            // create inside <sql> for inline
            val sqlNodes = context.evalNodes("/mapper/sql")
            parseSqlStatement(sqlNodes, builderAssistant, configuration)

            try {
                mybatisEntry.methodSqlList += buildCrudSqlMap(mybatisEntry.namespace, context, configuration, builderAssistant)
            } catch (e: Exception) {
                logger.warn(e) { "parse $sqlNodes. ${e.message} " }
            }
            return@use mybatisEntry
        }
    }

    private fun buildCrudSqlMap(
        namespace: String,
        context: XNode,
        configuration: Configuration,
        builderAssistant: MapperBuilderAssistant,
    ): List<MyBatisTransform.Statement> {
        val sqlList: MutableList<MyBatisTransform.Statement> = mutableListOf()
        val langDriver: LanguageDriver = XMLLanguageDriver()
        val crudList = context.evalNodes("select|insert|update|delete")

        for (it in crudList) {
            val methodName: String = it.getStringAttribute("id") ?: continue

            // 1. enable include
            try {
                val includeParser = XMLIncludeTransformer(configuration, builderAssistant)
                includeParser.applyIncludes(it.node)
            } catch (e: Exception) {
                logger.warn(e) { e.message }
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
            val sqlContent = myBatisTransform.applyDynamicContent(configuration, namespace, it, rootNode)
            sqlList += sqlContent
        }
        return sqlList
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
            configuration.sqlFragments[id] = it
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

    private fun createConfiguration(): Configuration {
        val configuration = Configuration()
        configuration.defaultResultSetType = ResultSetType.SCROLL_INSENSITIVE
        configuration.isShrinkWhitespacesInSql = true
        return configuration
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
