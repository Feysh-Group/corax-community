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

import com.feysh.corax.cache.AnalysisCache
import com.feysh.corax.config.api.CheckerUnit
import com.feysh.corax.config.api.Language
import com.feysh.corax.config.api.PreAnalysisApi
import com.feysh.corax.config.api.report.Region
import com.feysh.corax.config.api.utils.superClasses
import com.feysh.corax.config.api.utils.superInterfaces
import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.builder.xml.MybatisConfiguration
import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.builder.xml.XMLConfigBuilder
import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.type.TypeAliasRegistry
import com.feysh.corax.config.community.checkers.frameworks.xml.XmlParser
import com.feysh.corax.config.general.utils.region
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import mu.KotlinLogging
import org.apache.ibatis.mapping.BoundSql
import org.w3c.dom.Node
import soot.Scene
import soot.SootMethod
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

class MybatisParser {

    object Dummy: CheckerUnit()

    fun parse(api: PreAnalysisApi): CompletableFuture<Result> {
        val mybatisParseResult =
            AnalysisCache.G.analysisCache.get(MybatisParser::class) {
                GlobalScope.future {
                    MybatisParser().analyze(api)
                }
            }
        return mybatisParseResult as CompletableFuture<Result>
    }

    data class Result(
        val mybatisInfo: MybatisParseResult,
        val mybatisEntries: Set<MybatisEntry>,
        val typeAliasRegistry: TypeAliasRegistry,
        val configurationFiles: Set<Path>
    ) {

        data class MybatisSqlStmt(
            val namespace: String,
            val entry: MybatisEntry,
            val method: SootMethod,
            val statement: MyBatisTransform.Statement,
            val xmlNode: Node,
            val sqlInjectParameters: Set<Any>,
            val phantomBoundSql: BoundSql,
            val xmlResource: Path
        )

        interface Visitor {
            context(Result)
            fun MybatisSqlStmt.visit()
        }

        context(Result)
        private fun Visitor.visit(m: MybatisSqlStmt) = m.visit()


        fun mappingMethod(mybatisEntry: MybatisEntry, visit: (at: SootMethod, statement: MyBatisTransform.Statement) -> Unit) {
            for (statement in mybatisEntry.methodSqlList) {
                val mapperClass = Scene.v().getSootClassUnsafe(statement.namespace, false)
                if (mapperClass == null) {
                    logger.warn{ "Can't find mybatis mapper class of namespace ${statement.namespace}. check: ${statement.resource}" }
                    continue
                }
                val id = statement.id
                if (id == null) {
                    logger.warn{ "Mybatis statement.id could not be null. check: ${statement.resource}" }
                    continue
                }
                val mapperMethods = (mapperClass.superClasses + mapperClass.superInterfaces).flatMap { it.methods.filter { m -> m.name == id } }.toList()
                for (method in mapperMethods) {
                    visit(method, statement)
                }
            }
        }

        fun visit(v: Visitor) {
            for ((namespace, entries) in mybatisEntries.groupBy { it.namespace }) {
                for (entry in entries) {
                    mappingMethod(entry) { method: SootMethod, statement ->
                        val xmlNode = statement.xNode.node
                        val sqlInjectParameters = statement.translator.sqlInjectParameters
                        val phantomBoundSql = statement.phantomBoundSql
                        val xmlResource = statement.resource
                        val data = MybatisSqlStmt(
                            namespace = namespace,
                            entry = entry,
                            method = method,
                            statement = statement,
                            xmlNode = xmlNode,
                            sqlInjectParameters = sqlInjectParameters,
                            phantomBoundSql = phantomBoundSql,
                            xmlResource = xmlResource
                        )
                        v.visit(data)
                    }
                }
            }
        }
    }

    data class MybatisParseResult(
        val mybatisEntries: PreAnalysisApi.Result<MybatisEntry>,
        val mybatisConfiguration: PreAnalysisApi.Result<MybatisConfiguration>,
    )

    context (PreAnalysisApi, CheckerUnit)
    private suspend fun parseMybatisMapperAndConfig(): MybatisParseResult {
        val mybatisConfiguration = atAnySourceFile(extension = "xml", config = { incrementalAnalyze = false; ignoreProjectConfigProcessFilter = true }) {
            val configuration = XmlParser.parseMybatisConfiguration(Scene.v(), path) ?: return@atAnySourceFile null
            configuration
        }.nonNull()

        val myBatisSqlFragments = atAnySourceFile(extension = "xml", config = { incrementalAnalyze = false; ignoreProjectConfigProcessFilter = true }) {
            val config = XMLConfigBuilder.createConfiguration()
            if (XmlParser.parseMyBatisSqlFragments(path, config)) {
                path to config
            } else {
                null
            }
        }.nonNull()

        val configurationMerge = myBatisSqlFragments.await().fold(XMLConfigBuilder.createConfiguration()) { acc, element ->
            acc.also { it.sqlFragments.putAll(element.second.sqlFragments) }
        }

        val mybatisEntries = atAnySourceFile(extension = "xml", config = { incrementalAnalyze = false; ignoreProjectConfigProcessFilter = true }) {
            val configuration = XMLConfigBuilder.createConfiguration().also { it.sqlFragments.putAll(configurationMerge.sqlFragments) }
            XmlParser.parseMybatisMapper(path, configuration)
        }

        return MybatisParseResult(mybatisEntries.nonNull(), mybatisConfiguration)
    }

    suspend fun analyze(api: PreAnalysisApi): Result {
        val mybatisInfo = with(api) { with(Dummy) { parseMybatisMapperAndConfig() } }
        val mybatisEntries = mybatisInfo.mybatisEntries.await().toSet()

        val typeAliasRegistry = TypeAliasRegistry()
        val configurationFiles: MutableSet<Path> = mutableSetOf()
        for (config in mybatisInfo.mybatisConfiguration.await()) {
            typeAliasRegistry.meet(config.typeAliasRegistry)
            configurationFiles.add(config.resource)
        }
        return Result(
            mybatisInfo = mybatisInfo,
            mybatisEntries = mybatisEntries,
            typeAliasRegistry = typeAliasRegistry,
            configurationFiles = configurationFiles
        )
    }

    companion object {

        private val logger = KotlinLogging.logger {}

    }
}