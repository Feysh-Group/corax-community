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

import com.feysh.corax.config.api.CheckerUnit
import com.feysh.corax.config.api.SAOptions
import com.feysh.corax.config.api.utils.ClassCommons
import com.feysh.corax.config.general.model.taint.TaintRule
import com.feysh.corax.config.general.rule.GroupedMethodsManager
import com.feysh.corax.config.general.rule.MethodAccessPath
import com.feysh.corax.config.general.utils.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import mu.KotlinLogging
import soot.*
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.name
import kotlin.io.path.pathString

@Suppress("MemberVisibilityCanBePrivate")
object ConfigCenter : CheckerUnit() {

    const val configDirectoryIdentifier = "\${analysis-config-dir}"

    var analysisConfigPathRelativizePlugin = ClassCommons.locateAllClass(ConfigCenter::class.java).resolve("../../../").pathString

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

    val methodAccessPathDataBase: GroupedMethodsManager<MethodAccessPath> by lazy {
        val sourcesJsonFiles = walkFiles(getConfigDirectories()){ file -> file.name.endsWith("access-path.json") }
        GroupedMethodsManager.load(sourcesJsonFiles, serializer = serializer())
    }

    fun isEnableTaintFlowType(type: Type): Boolean {
        if (!option.taintPrimTypeValue && (type.isPrimitives || type.isBoxedPrimitives)) return false
        if (type.isVoidType) return false
        return true
    }


    private val logger = KotlinLogging.logger {}
}