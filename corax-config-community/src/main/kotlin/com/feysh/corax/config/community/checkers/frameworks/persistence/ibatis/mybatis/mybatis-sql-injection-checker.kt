@file:Suppress("ClassName")

package com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.mybatis

import com.feysh.corax.config.api.*
import com.feysh.corax.config.api.baseimpl.matchSoot
import com.feysh.corax.config.api.utils.superClasses
import com.feysh.corax.config.api.utils.superInterfaces
import com.feysh.corax.config.api.utils.typename
import com.feysh.corax.config.community.SqliChecker
import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.IbatisParamNameResolver
import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.builder.xml.MybatisConfiguration
import com.feysh.corax.config.general.checkers.GeneralTaintTypes
import com.feysh.corax.config.general.checkers.internetControl
import mu.KotlinLogging
import org.apache.ibatis.scripting.xmltags.DynamicContext
import soot.Scene
import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.type.TypeAliasRegistry
import com.feysh.corax.config.community.checkers.frameworks.xml.XmlParser
import com.feysh.corax.config.general.model.ConfigCenter
import kotlinx.serialization.Serializable
import soot.SootMethod
import soot.Type
import java.nio.file.Path


/**
 * @author NotifyBiBi
 */
private class TraverseExpr(
    val statement: MyBatisTransform.Statement,
    val paramNameResolver: IbatisParamNameResolver,
    val typeAliasRegistry: TypeAliasRegistry
){

    companion object {
        private val logger = KotlinLogging.logger {}

        fun isStringType(name: String?) = name == "java.lang.String" || name == "java.lang.Character"
        fun isStringType(type: Type?) = isStringType(type?.typename)

        fun canCauseSqlInject(typename: String?): String? = typename?.takeIf { isStringType(it) }
        fun canCauseSqlInject(type: Type?): Type? = type?.takeIf { isStringType(it.typename) }
    }

    val method = paramNameResolver.method
    data class AccessPath(val value: ILocalT<*>, val type: Type)

    context (builder@ISootMethodDecl.CheckBuilder<Any>)
    fun traverseExpr(value: Any?): List<AccessPath>? {
        return when (value) {
            is ExpressionEvaluator.MethodParameter -> {
                val parameterTypeInStatement = statement.parameterType
                check(method.parameterCount >= 1)
                if (method.parameterCount != 1) { // multi parameters
                    val index = paramNameResolver.getParamByName(value.name) ?: return null
                    val parameterTypeActual = canCauseSqlInject(method.getParameterType(index)) ?: return null
                    listOf(AccessPath(parameter(index), parameterTypeActual))
                } else { // single parameter
                    val aliasClass = if (parameterTypeInStatement != null) typeAliasRegistry.resolveAlias(parameterTypeInStatement) else null
                    val parameterTypeActual = method.getParameterType(0)
                    if (aliasClass != null && aliasClass != parameterTypeActual.typename) {
                        logger.warn { "type mismatch: xml at $aliasClass, ${method.signature} at parameter0" }
                    }
//                    val parameterTypeActualName = parameterTypeActual.typename ?: return null
                    if (isStringType(parameterTypeActual) || ConfigCenter.isMapClassType(parameterTypeActual) || ConfigCenter.isCollectionClassType(parameterTypeActual)){
                        listOf(AccessPath(parameter(0), parameterTypeActual))
                    } else if (aliasClass == null && parameterTypeInStatement != null) { // custom entity
                        val parameterType = Scene.v().getOrAddRefType(parameterTypeInStatement)
                        val fieldDeclareType = parameterType.sootClass.getFieldByNameUnsafe(value.name)?.type ?: Scene.v().objectType
                        listOf(AccessPath(parameter(0).field(declaringClass = parameterType.className, value.name), fieldDeclareType)) // TODO: it should be actual type
                    } else {
                        null
                    }
                }
            }

            is ExpressionEvaluator.ForEachItem -> {
                val collection = traverseExpr(value.collection) ?: return null
                collection
            }

            is ExpressionEvaluator.AccessPath -> {
                val bases = traverseExpr(value.base) ?: return null
                val baseIsForEach = value.base is ExpressionEvaluator.ForEachItem
                val traversedACP = value.accessPath.fold(bases) { acc, fieldName ->
                    acc.flatMap { base ->
                        val unknownType = Scene.v().objectType // TODO: it should be actual type
                        if (ConfigCenter.isMapClassType(base.type)) {
                            when (fieldName) {
                                "key" -> listOf(AccessPath(base.value.field(MapKeys), type = unknownType))
                                "value" -> listOf(AccessPath(base.value.field(MapValues), type = unknownType))
                                else -> emptyList()
                            }
                        } else if (ConfigCenter.isCollectionClassType(base.type)) {
                            when {
                                baseIsForEach ->  listOf(AccessPath(base.value.field(Elements).field(declaringClass = null, fieldName = fieldName), unknownType) )
                                else -> emptyList()
                            }
                        } else {
                            if (baseIsForEach) {
                                listOf(AccessPath(base.value.field(Elements).field(declaringClass = null, fieldName = fieldName), unknownType))
                            } else {
                                if (base.type is soot.RefType && base.type != unknownType) {
                                    val fieldDeclareType = base.type.sootClass.getFieldByNameUnsafe(fieldName)?.type ?: unknownType
                                    listOf(AccessPath(base.value.field(declaringClass = base.type.className, fieldName = fieldName), fieldDeclareType))
                                } else {
                                    listOf(AccessPath(base.value.field(declaringClass = null, fieldName = fieldName), unknownType))
                                }
                            }
                        }
                    }
                }
                traversedACP
            }

            else -> {
                logger.warn { "unsupported value: $value" }
                null
            }
        }
    }
}


object `mybatis-sql-injection-checker` : AIAnalysisUnit() {
    @Serializable
    class Options : SAOptions {
        val reportSqlInjectionSinksAtMapperInterfaceMethod: Boolean = true
    }

    private var option: Options = Options()


    private val logger = KotlinLogging.logger {}

    data class MybatisParseResult(
        val mybatisEntries: PreAnalysisApi.Result<MybatisEntry>,
        val mybatisConfiguration: PreAnalysisApi.Result<MybatisConfiguration>,
    )

    private fun checkMybatisSqlInjection(mybatisEntry: MybatisEntry, visit: (at: SootMethod, statement: MyBatisTransform.Statement) -> Unit) {
        for (statement in mybatisEntry.methodSqlList) {
            val id = statement.id ?: continue
            val sqlInjectParameters = statement.translator.sqlInjectParameters
            if (sqlInjectParameters.isEmpty()) {
                continue
            }
            val mapperClass = Scene.v().getSootClassUnsafe(statement.namespace, false) ?: continue
            val mapperMethods = (mapperClass.superClasses + mapperClass.superInterfaces).flatMap { it.methods.filter { m -> m.name == id } }.toList()
            for (method in mapperMethods) {
                visit(method, statement)
            }
        }
    }

    context (builder@ISootMethodDecl.CheckBuilder<Any>)
    private fun checkSink(statement: MyBatisTransform.Statement, from: ExpressionEvaluator.Value, sink: ILocalT<*>) {
        check(
            sink.taint.containsAll(taintOf(internetControl + GeneralTaintTypes.CONTAINS_SQL_INJECT)),
            SqliChecker.SqlInjection
        ) {
            this.args["type"] = "Mybatis Xml Mapper SQL Query"
            this.args["msg"] = "The injection point: `$from` at sql: `${statement.phantomBoundSql.sql}`"
        }
    }

    context (builder@ISootMethodDecl.CheckBuilder<Any>)
    private fun checkMybatisStatement(statement: MyBatisTransform.Statement, typeAliasRegistry: TypeAliasRegistry) {
        val method = this@builder.method.sootMethod
        if (method.parameterCount <= 0) {
            return
        }
        val paramNameResolver = IbatisParamNameResolver(method)
        val sqlInjectParameters = statement.translator.sqlInjectParameters
        for (bindingValue in sqlInjectParameters) {
            if ((bindingValue is ExpressionEvaluator.MethodParameter) && bindingValue.name == DynamicContext.PARAMETER_OBJECT_KEY) {
                if (method.parameterCount == 1) {
                    val pt = method.getParameterType(0)
                    if (TraverseExpr.isStringType(pt.typename)){
                        checkSink(statement, bindingValue, p0)
                    }
                }
                continue
            }

            if (bindingValue is ExpressionEvaluator.Value) {
                val sinks = TraverseExpr(statement, paramNameResolver, typeAliasRegistry = typeAliasRegistry)
                    .traverseExpr(bindingValue)
                if (sinks != null) {
                    for (sink in sinks) {
                        checkSink(statement, bindingValue, sink.value)
                    }
                }
            } else {
                logger.warn { "unsupported bindingValue: $bindingValue" }
            }
        }
    }

    context (PreAnalysisApi)
    private fun parseMybatisMapperAndConfig(): MybatisParseResult {
        val mybatisEntries = atAnySourceFile(extension = "xml") {
            XmlParser.parseMybatisMapper(path)
        }

        val mybatisConfiguration = atAnySourceFile(extension = "xml") {
            val configuration = XmlParser.parseMybatisConfiguration(Scene.v(), path) ?: return@atAnySourceFile null
            configuration
        }.nonNull()

        return MybatisParseResult(mybatisEntries.nonNull(), mybatisConfiguration)
    }

    context (PreAnalysisApi)
    private fun reportOnlyMybatisSqlInjectionSinkInMapperXml(entry: MybatisEntry) {
        checkMybatisSqlInjection(entry) { method, statement ->
            // Just make a hint for security engineer! :)
            report(SqliChecker.MybatisSqlInjectionSinkHint, method) {
                this.args["numSinks"] = statement.translator.sqlInjectParameters.size
                this.args["boundSql"] = statement.phantomBoundSql.sql
            }
        }
    }

    context (AIAnalysisApi)
    override suspend fun config() {
        val mybatisInfo = with(preAnalysis) { parseMybatisMapperAndConfig() }
        val mybatisEntries = mybatisInfo.mybatisEntries.await().toSet()

        val typeAliasRegistry = TypeAliasRegistry()
        val configurationFiles: MutableSet<Path> = mutableSetOf()
        for (config in mybatisInfo.mybatisConfiguration.await()) {
            typeAliasRegistry.meet(config.typeAliasRegistry)
            configurationFiles.add(config.resource)
        }

        for ((_, entries) in mybatisEntries.groupBy { it.namespace }) {
            for (entry in entries) {

                if (option.reportSqlInjectionSinksAtMapperInterfaceMethod) {
                    with(preAnalysis) {
                        reportOnlyMybatisSqlInjectionSinkInMapperXml(entry)
                    }
                }

                checkMybatisSqlInjection(entry) { method: SootMethod, statement ->
                    toDecl(method) dependsOn toDecl(entry.resource) // for incremental analysis
                    listOf(toDecl(method)) dependsOn configurationFiles.mapTo(mutableSetOf()) { toDecl(it) } // for incremental analysis

                    method(matchSoot(method.signature)).sootDecl.forEach {
                        it.modelNoArg(config = { at = MethodConfig.CheckCall.PrevCallInCaller }) {
                            checkMybatisStatement(statement, typeAliasRegistry)
                        }
                    }
                }
            }
        }
    }
}