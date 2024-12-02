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

package com.feysh.corax.config.general.model

import com.feysh.corax.cache.AnalysisCache
import com.feysh.corax.config.api.CheckerUnit
import com.feysh.corax.config.api.SAOptions
import com.feysh.corax.config.api.utils.ClassCommons
import com.feysh.corax.config.api.utils.activeBodyOrNull
import com.feysh.corax.config.api.utils.typename
import com.feysh.corax.config.api.utils.visibilityAnnotationTag
import com.feysh.corax.config.general.model.taint.TaintRule
import com.feysh.corax.config.general.model.version.VersionRule
import com.feysh.corax.config.general.rule.GroupedMethodsManager
import com.feysh.corax.config.general.rule.MethodAccessPath
import com.feysh.corax.config.general.rule.MultiMethodAccessPath
import com.feysh.corax.config.general.rule.RuleManager.Companion.jsonFormat
import com.feysh.corax.config.general.utils.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.serializer
import mu.KotlinLogging
import soot.*
import soot.tagkit.AnnotationTag
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.inputStream
import kotlin.io.path.name
import kotlin.io.path.pathString

@Suppress("MemberVisibilityCanBePrivate")
object ConfigCenter : CheckerUnit() {

    const val configDirectoryIdentifier = "\${analysis-config-dir}"

    var analysisConfigPathRelativizePlugin = ClassCommons.locateAllClass(ConfigCenter::class.java).let {
        if (it.name.endsWith(".jar")) it.parent else it
    }.resolve("../../../").pathString

    @Serializable
    class Options : SAOptions {
        val ruleDirectories: MutableList<String> = mutableListOf(
            "${configDirectoryIdentifier}/rules"
        )
        val taintPrimTypeValue: Boolean = true

        val taintToStringMethod: Boolean = true

        val optionalClasses = mutableListOf(
            "java.util.Optional"
        )

        val collectionClasses = mutableListOf(
            "java.util.List",
            "java.util.Set",
            "java.lang.Iterable",
            "java.util.Collection",
            "java.util.Enumeration",
            "java.util.ArrayList",
        )

        val multiValueMapTypes = mutableListOf(
            "org.springframework.util.MultiValueMap",
            "javax.ws.rs.core.MultiValueMap",
        )

        val mapClasses = mutableListOf(
            "java.util.Map",
            "java.util.HashMap",
            "java.util.LinkedHashMap",
            "java.util.TreeMap",
            "org.springframework.util.MultiValueMap",
            "javax.ws.rs.core.MultiValueMap",
        )


        val junitSrcAnySubPath: Set<String> = setOf("src/test")

        val junitPackages = setOf("org.junit")

        val mockPackages = setOf("org.mockito")
    }

    var option: Options = Options()

    fun isCollectionClassType(type: Type): Boolean {
        if (type is ArrayType)
            return true
        val scene = Scene.v()
        return type.isInstanceOf(option.collectionClasses.map { scene.getOrAddRefType(it) })
    }

    fun isMultiValueMapClassType(type: Type): Boolean {
        val scene = Scene.v()
        return type.isInstanceOf(option.multiValueMapTypes.map { scene.getOrAddRefType(it) })
    }

    fun isMapClassType(type: Type): Boolean {
        val scene = Scene.v()
        return type.isInstanceOf(option.mapClasses.map { scene.getOrAddRefType(it) })
    }

    fun isOptionalClassType(type: Type): Boolean {
        val scene = Scene.v()
        return type.isInstanceOf(option.optionalClasses.map { scene.getOrAddRefType(it) })
    }

    fun pathTranslate(paths: List<String>): List<Path> {
        return paths.map { Paths.get(it.replace(configDirectoryIdentifier, analysisConfigPathRelativizePlugin)) }
    }

    fun getConfigDirectories() = pathTranslate(option.ruleDirectories)

    val taintRulesManager by lazy {
        val jsonDirs = getConfigDirectories()
        val sourcesJsonFiles = walkFiles(jsonDirs){ file -> file.name.endsWith("sources.json") }
        val summariesJsonFiles = walkFiles(jsonDirs){ file -> file.name.endsWith("summaries.json") }
        val sinksJsonFiles = walkFiles(jsonDirs){ file -> file.name.endsWith("sinks.json") }

        logger.info { "sourcesJsonFiles: $sourcesJsonFiles" }
        logger.info { "summariesJsonFiles: $summariesJsonFiles" }
        logger.info { "sinksJsonFiles: $sinksJsonFiles" }
        TaintRule.TaintRulesManager.loadJsons(sourcesJsonFiles, summariesJsonFiles, sinksJsonFiles)
    }

    val versionRuleManager by lazy {
        val jsonDirs = getConfigDirectories()
        val riskVersionJsonFiles = walkFiles(jsonDirs) { file -> file.name.endsWith("versions.json") && !file.name.startsWith("custom") }
        val customVersionJsonFiles = walkFiles(jsonDirs) { file -> file.name.endsWith("versions.json") && file.name.startsWith("custom") }
        logger.info { "riskVersionsJsonFiles: $riskVersionJsonFiles" }
        logger.info { "customVersionsJsonFiles: $customVersionJsonFiles" }
        VersionRule.VersionRuleManager.loadJson(riskVersionJsonFiles, customVersionJsonFiles)
    }

    val methodAccessPathDataBase: GroupedMethodsManager<MethodAccessPath> by lazy {
        val jsonFiles = walkFiles(getConfigDirectories()){ file -> file.name.endsWith(".access-path.json") }
        GroupedMethodsManager.load(jsonFiles, serializer = serializer())
    }

    val methodMultiAccessPathDataBase: GroupedMethodsManager<MultiMethodAccessPath> by lazy {
        val jsonFiles = walkFiles(getConfigDirectories()){ file -> file.name.endsWith(".multi-access-path.json") }
        GroupedMethodsManager.load(jsonFiles, serializer = serializer())
    }

    fun isEnableTaintFlowType(type: Type): Boolean {
        if (!option.taintPrimTypeValue && (type.isPrimitives || type.isBoxedPrimitives)) return false
        if (type.isVoidType) return false
        return true
    }

    fun isJunitTestClass(declaringClass: SootClass): Boolean {
        val src = AnalysisCache.G.class2SourceFile(declaringClass)?.pathString?.replace('\\', '/')
        if (src != null && option.junitSrcAnySubPath.any { src.contains(it.replace('\\', '/')) }) {
            return true
        }

        val junitPackages = option.junitPackages
        val anyMethodAnnotatedJunitType = declaringClass.methods.firstOrNull { method ->
            method.annotationTypeMatchFirstOrNull{ type -> junitPackages.any { type.contains(it) } } != null
        }
        if (anyMethodAnnotatedJunitType != null)
            return true

        return false
    }

    fun hasAnyMockInBody(sootMethod: SootMethod): Boolean {
        val body = sootMethod.activeBodyOrNull ?: return false
        val mockPackages = option.mockPackages
        return body.locals.any { local ->
            val type = local.type.typename ?: return@any false
            mockPackages.any { type.contains(it) }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun <T> loadConfigs(serializer: KSerializer<T>, filter: (file: Path) -> Boolean): Map<Path, T> {
        val matchedFiles =
            walkFiles(getConfigDirectories(), filter)

        return matchedFiles.associateWith { path ->
            path.inputStream().use {
                jsonFormat.decodeFromStream(serializer, it)
            }
        }
    }

    private val logger = KotlinLogging.logger {}
}

fun SootMethod.annotationTypeMatchFirstOrNull(predicate: (type: String) -> Boolean): AnnotationTag? {
    val visibilityAnnotationTag = visibilityAnnotationTag ?: return null
    return visibilityAnnotationTag.annotations?.firstOrNull { predicate(classTypeToSootTypeDesc(it.type)) }
}