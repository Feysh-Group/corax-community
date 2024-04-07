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

import com.feysh.corax.commons.delegateField
import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.builder.WSqlSourceBuilder
import org.apache.ibatis.mapping.BoundSql
import org.apache.ibatis.mapping.SqlSource
import org.apache.ibatis.parsing.GenericTokenParser
import org.apache.ibatis.parsing.TokenHandler
import org.apache.ibatis.parsing.XNode
import org.apache.ibatis.scripting.xmltags.*
import org.apache.ibatis.session.Configuration

/**
 * @author NotifyBiBi
 */
class WTrimSqlNode(
    val contents: SqlNode,
    val prefix: String?,
    val suffix: String?,
    val prefixesToOverride: List<String>?,
    val suffixesToOverride: List<String>?,
    val configuration: Configuration,
) : TrimSqlNode(configuration, contents, prefix, prefixesToOverride, suffix, suffixesToOverride) {


    override fun apply(context: DynamicContext): Boolean {
        val filteredDynamicContext = FilteredDynamicContext(context)
        val result = contents.apply(filteredDynamicContext)
        filteredDynamicContext.applyAll()
        return false
    }


    inner class FilteredDynamicContext(val delegate: DynamicContext) :
        DynamicContext(configuration, null) {
        var prefixApplied = false
        var suffixApplied = false
        var sqlBuffer: StringBuilder

        init {
            sqlBuffer = StringBuilder()
        }

        fun applyAll() {
            sqlBuffer = StringBuilder(sqlBuffer.toString().trim { it <= ' ' })
            val trimmedUppercaseSql = sqlBuffer.toString().uppercase()
            if (trimmedUppercaseSql.isNotEmpty()) {
                applyPrefix(sqlBuffer, trimmedUppercaseSql)
                applySuffix(sqlBuffer, trimmedUppercaseSql)
            }
            delegate.appendSql(sqlBuffer.toString())
        }

        override fun getBindings(): Map<String, Any> {
            return delegate.bindings
        }

        override fun bind(name: String, value: Any) {
            delegate.bind(name, value)
        }

        override fun getUniqueNumber(): Int {
            return delegate.uniqueNumber
        }

        override fun appendSql(sql: String?) {
            if (sql == null) return
            sqlBuffer.append(sql)
        }

        override fun getSql(): String {
            return delegate.sql
        }

        fun applyPrefix(sql: StringBuilder, trimmedUppercaseSql: String) {
            if (!prefixApplied) {
                prefixApplied = true
                val prefixesToOverride = prefixesToOverride
                if (prefixesToOverride != null) {
                    for (toRemove in prefixesToOverride) {
                        if (trimmedUppercaseSql.startsWith(toRemove)) {
                            sql.delete(0, toRemove.trim { it <= ' ' }.length)
                            break
                        }
                    }
                }
                if (prefix != null) {
                    sql.insert(0, " ")
                    sql.insert(0, prefix)
                }
            }
        }

        fun applySuffix(sql: StringBuilder, trimmedUppercaseSql: String) {
            if (!suffixApplied) {
                suffixApplied = true
                val suffixesToOverride = suffixesToOverride
                if (suffixesToOverride != null) {
                    for (toRemove in suffixesToOverride) {
                        if (trimmedUppercaseSql.endsWith(toRemove) || trimmedUppercaseSql.endsWith(toRemove.trim { it <= ' ' })) {
                            val start = sql.length - toRemove.trim { it <= ' ' }.length
                            val end = sql.length
                            sql.delete(start, end)
                            break
                        }
                    }
                }
                if (suffix != null) {
                    sql.append(" ")
                    sql.append(suffix)
                }
            }
        }
    }
}


/**
 * @author NotifyBiBi
 */
interface ExpressionEvaluator {
    interface Value
    data class MethodParameter(val name: String) : Value {
        override fun toString(): String = "para_$name"
    }

    data class ForEachItem(val collection: Any) : Value {
        override fun toString(): String = "item($collection)"
    }

    data class AccessPath(val base: Any, val accessPath: List<String>) : Value {
        override fun toString(): String = "$base.${accessPath.joinToString(separator = ".")}"
    }

    fun getValue(expr: String, dynamicContext: DynamicContext): Any? {
        val accessPath = expr.split(".")
        val first = accessPath.first()
        val bindings = dynamicContext.bindings
        val parent = bindings[first] ?: (MethodParameter(first).also {
            dynamicContext.bind(first, it)
        })
        return if (accessPath.size == 1) {
            parent
        } else {
            AccessPath(parent, accessPath.drop(1))
        }
    }

    fun evaluateIterable(expr: String, dynamicContext: DynamicContext): Collection<Any> {
        val value = getValue(expr, dynamicContext) ?: return emptyList()
        return listOf(ForEachItem(value))
    }
}

object SimpleExpressionEvaluator : ExpressionEvaluator

/**
 * @author NotifyBiBi
 */
open class WForEachSqlNode(
    val evaluator: ExpressionEvaluator,
    val contents: SqlNode,
    val collectionExpression: String?,
    val open: String?,
    val close: String?,
    val separator: String?,
    val item: String?,
    val index: String?,
    val configuration: Configuration
) : ForEachSqlNode(configuration, contents, collectionExpression, index, item, open, close, separator) {

    fun itemizeItem(item: String?, i: Int): String {
        return ForEachSqlNode.ITEM_PREFIX + item + "_" + i
    }

    private fun applyIndex(context: DynamicContext, o: Any, i: Int) {
        index?.let {
            context.bind(it, o)
            context.bind(itemizeItem(it, i), o)
        }
    }

    fun applyItem(context: DynamicContext, o: Any, i: Int) {
        item?.let {
            context.bind(it, o)
            context.bind(itemizeItem(it, i), o)
        }
    }

    fun applyOpen(context: DynamicContext) {
        if (open != null) {
            context.appendSql(open)
        }
    }

    fun applyClose(context: DynamicContext) {
        if (close != null) {
            context.appendSql(close)
        }
    }


    override fun apply(context: DynamicContext): Boolean {
        val collectionExpression = collectionExpression ?: return false
        var contextMutable = context
        val separator = separator
        var first = true
        val iterable = evaluator.evaluateIterable(collectionExpression, context)
        applyOpen(contextMutable)
        for ((i, o) in iterable.withIndex()) {
            val oldContext = contextMutable
            if (first || separator == null) {
                contextMutable = PrefixedContext(contextMutable, "")
            } else {
                contextMutable = PrefixedContext(contextMutable, separator)
            }
            val uniqueNumber = contextMutable.uniqueNumber
            // Issue #709
//            if (o is Map.Entry<*, *>) {
//                val (key, value) = o as Map.Entry<Any, Any>
//                applyIndex(context, key, uniqueNumber)
//                applyItem(context, value, uniqueNumber)
//            } else {
            applyIndex(contextMutable, i, uniqueNumber)
            applyItem(contextMutable, o, uniqueNumber)
//            }
            contents.apply(FilteredDynamicContext(configuration, contextMutable, index, item, uniqueNumber))
            if (first) {
                first = !contextMutable.isPrefixApplied
            }
            contextMutable = oldContext
        }
        applyClose(contextMutable)
        contextMutable.bindings.remove(item)
        contextMutable.bindings.remove(index)
        return false
    }


    private inner class FilteredDynamicContext(
        configuration: Configuration?,
        private val delegate: DynamicContext,
        private val itemIndex: String?,
        private val item: String?,
        private val index: Int
    ) : DynamicContext(configuration, null) {
        override fun getBindings(): Map<String, Any> {
            return delegate.bindings
        }

        override fun bind(name: String, value: Any) {
            delegate.bind(name, value)
        }

        override fun getSql(): String {
            return delegate.sql
        }

        override fun appendSql(sql: String?) {
            val parser = GenericTokenParser("#{", "}") { content: String ->
                var newContent = content.replaceFirst(
                    "^\\s*$item(?![^.,:\\s])".toRegex(), itemizeItem(item, index)
                )
                if (itemIndex != null && newContent == content) {
                    newContent = content.replaceFirst(
                        "^\\s*$itemIndex(?![^.,:\\s])".toRegex(), itemizeItem(itemIndex, index)
                    )
                }
                "#{$newContent}"
            }
            delegate.appendSql(parser.parse(sql))
        }

        override fun getUniqueNumber(): Int {
            return delegate.uniqueNumber
        }
    }


    private inner class PrefixedContext(private val delegate: DynamicContext, private val prefix: String) :
        DynamicContext(configuration, null) {
        var isPrefixApplied = false
            private set

        override fun getBindings(): Map<String, Any> {
            return delegate.bindings
        }

        override fun bind(name: String, value: Any) {
            delegate.bind(name, value)
        }

        override fun appendSql(sql: String?) {
            if (!isPrefixApplied && sql != null && sql.trim { it <= ' ' }.isNotEmpty()) {
                delegate.appendSql(prefix)
                isPrefixApplied = true
            }
            delegate.appendSql(sql)
        }

        override fun getSql(): String {
            return delegate.sql
        }

        override fun getUniqueNumber(): Int {
            return delegate.uniqueNumber
        }
    }

}

/**
 * @author NotifyBiBi
 */
open class StatementTranslator(val expressionEvaluator: ExpressionEvaluator = SimpleExpressionEvaluator) {

    val sqlInjectParameters: LinkedHashSet<Any> = LinkedHashSet()

    open fun handleSqlInjection(content: String, context: DynamicContext): String {
        val parameter = expressionEvaluator.getValue(expr = DynamicContext.PARAMETER_OBJECT_KEY, context)
        if (parameter == null) {
            context.bindings["value"] = null
        } else {
            context.bindings["value"] = parameter
        }
        val value = expressionEvaluator.getValue(expr = content, context)
        if (value != null) {
            sqlInjectParameters += value
        }
        return "\${${value}}"
    }

    open fun createForEachSqlNode(
        contents: SqlNode,
        collectionExpression: String?,
        open: String?,
        close: String?,
        separator: String?,
        item: String?,
        index: String?,
        configuration: Configuration
    ): WForEachSqlNode = WForEachSqlNode(
        evaluator = expressionEvaluator,
        contents = contents,
        collectionExpression = collectionExpression,
        open = open,
        close = close,
        separator = separator,
        item = item,
        index = index,
        configuration = configuration,
    )

    open fun visit(node: MixedSqlNode): SqlNode {
        val contents: List<SqlNode> by node.delegateField()
        return MixedSqlNode(contents.map { translate(it) })
    }

    open fun visit(node: StaticTextSqlNode): SqlNode {
        return node
    }

    // ${}
    open fun visit(node: TextSqlNode): SqlNode {
        return object : SqlNode {
            val text: String by node.delegateField()
            fun createParser(handler: TokenHandler): GenericTokenParser {
                return GenericTokenParser("\${", "}", handler)
            }

            override fun apply(context: DynamicContext): Boolean {
                val parser: GenericTokenParser = createParser { content -> handleSqlInjection(content, context) }
                context.appendSql(parser.parse(text))
                return true
            }
        }
    }

    open fun visit(node: ForEachSqlNode): SqlNode {
        val contents: SqlNode by node.delegateField()
        val collectionExpression: String? by node.delegateField()
        val open: String? by node.delegateField()
        val close: String? by node.delegateField()
        val separator: String? by node.delegateField()
        val item: String? by node.delegateField()
        val index: String? by node.delegateField()
        val configuration: Configuration by node.delegateField()

        val contentsNew = translate(contents)
        return createForEachSqlNode(
            contents = contentsNew,
            collectionExpression = collectionExpression,
            open = open,
            close = close,
            separator = separator,
            item = item,
            index = index,
            configuration = configuration
        )
    }

    open fun visit(node: IfSqlNode): SqlNode {
        return object : SqlNode {
            val contents: SqlNode by node.delegateField()
            val contentsT by lazy { translate(contents) }
            override fun apply(context: DynamicContext): Boolean {
                contentsT.apply(context)
                return false
            }
        }
    }

    open fun visit(node: VarDeclSqlNode): SqlNode {
        return object : SqlNode {
            val name: String by node.delegateField()
            val expression: String by node.delegateField()
            override fun apply(context: DynamicContext): Boolean {
                context.bind(name, expression)
                return false
            }
        }
    }

    open fun visit(node: TrimSqlNode): SqlNode {
        val clz = TrimSqlNode::class.java
        val contents: SqlNode by node.delegateField(clz)
        val prefix: String? by node.delegateField(clz)
        val suffix: String? by node.delegateField(clz)
        val prefixesToOverride: List<String>? by node.delegateField(clz)
        val suffixesToOverride: List<String>? by node.delegateField(clz)
        val configuration: Configuration by node.delegateField(clz)
        return WTrimSqlNode(
            contents = translate(contents),
            prefix = prefix,
            suffix = suffix,
            prefixesToOverride = prefixesToOverride,
            suffixesToOverride = suffixesToOverride,
            configuration = configuration
        )
    }

    open fun visit(node: ChooseSqlNode): SqlNode {
        return object : SqlNode {
            val defaultSqlNode: SqlNode? by node.delegateField()
            val ifSqlNodes: List<SqlNode> by node.delegateField()
            val defaultSqlNodeT by lazy { defaultSqlNode?.let { translate(it) } }
            val ifSqlNodesT by lazy { ifSqlNodes.map { translate(it) } }
            override fun apply(context: DynamicContext): Boolean {
                ifSqlNodesT.forEach { sqlNode -> sqlNode.apply(context) }
                defaultSqlNodeT?.apply(context)
                return false
            }
        }
    }

    fun translate(node: SqlNode): SqlNode {
        val delegate = when (node) {
            is StaticTextSqlNode -> visit(node)

            is MixedSqlNode -> visit(node)

            is TextSqlNode -> visit(node) // ${}

            is ForEachSqlNode -> visit(node)

            is IfSqlNode -> visit(node)

            is VarDeclSqlNode -> visit(node)

            is TrimSqlNode -> visit(node)

            is ChooseSqlNode -> visit(node)

            else -> {
                TODO("not support this type: ${node.javaClass}")
            }
        }

        return delegate
    }
}

interface SqlNodeTranslatorFactory {
    fun createStatementTranslator(
        configuration: Configuration,
        namespace: String,
        dynamicContext: DynamicContext,
        xNode: XNode,
        node: SqlNode
    ): StatementTranslator
}

object SimpleSqlNodeTranslatorFactory : SqlNodeTranslatorFactory {
    override fun createStatementTranslator(
        configuration: Configuration,
        namespace: String,
        dynamicContext: DynamicContext,
        xNode: XNode,
        node: SqlNode
    ): StatementTranslator = StatementTranslator()
}

open class MyBatisTransform(private val factory: SqlNodeTranslatorFactory) {

    class Statement(
        val namespace: String,
        val xNode: XNode,
        val sqlNode: SqlNode,
        val flatSqlNode: SqlNode,
        val dynamicContext: DynamicContext,
        val translator: StatementTranslator,
        val sqlSource: SqlSource
    ) {

        fun boundSql(params: List<Any>): BoundSql = sqlSource.getBoundSql(params)

        val phantomBoundSql = boundSql(emptyList())

        val id: String? = xNode.getStringAttribute("id")
        val databaseId: String? = xNode.getStringAttribute("databaseId")
        val parameterType: String? = xNode.getStringAttribute("parameterType")
        val parameterMap: String? = xNode.getStringAttribute("parameterMap")
        val resultType: String? = xNode.getStringAttribute("resultType")
        val resultMap: String? = xNode.getStringAttribute("resultMap")
        val resultSetType: String? = xNode.getStringAttribute("resultSetType")

        val method: String = "$namespace.$id"

        override fun toString(): String {
            return "val xNode: ${xNode},\n\nval sqlNode: ${sqlNode},\n\nval flatSqlNode: ${flatSqlNode},\n\nval sql: ${phantomBoundSql.sql}\n"
        }
    }


    fun applyDynamicContent(configuration: Configuration, namespace: String, xNode: XNode, node: SqlNode): Statement {
        val dynamicContext = DynamicContext(configuration, null)
        val translator = factory.createStatementTranslator(configuration, namespace, dynamicContext, xNode, node)
        val flatSqlNode = translator.translate(node)
        flatSqlNode.apply(dynamicContext)

        val sql = dynamicContext.sql
        val sqlSourceParser = WSqlSourceBuilder(configuration)
        val sqlSource = sqlSourceParser.parse(sql, Any::class.java, dynamicContext.bindings.mapValues { Any() })

        return Statement(namespace, xNode, node, flatSqlNode, dynamicContext, translator, sqlSource)
    }
}