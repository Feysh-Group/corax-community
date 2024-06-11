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
package com.feysh.corax.config.general.checkers.analysis

import com.feysh.corax.config.api.PreAnalysisApi
import com.feysh.corax.config.api.PreAnalysisUnit
import com.feysh.corax.config.api.SAOptions
import com.feysh.corax.config.general.common.collect.Maps
import com.feysh.corax.config.general.common.collect.Sets
import com.feysh.corax.config.general.utils.MavenParser
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.*
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.apache.maven.model.Model
import soot.Scene
import soot.SootField
import soot.SootMethod
import soot.jimple.*
import soot.tagkit.ConstantValueTag
import java.io.IOException
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.regex.Pattern
import kotlin.collections.Map
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.io.path.pathString

@Serializable
data class MavenRepositoryLibraryDescriptor(val groupId: String, val artifactId: String, val version: String): Comparable<MavenRepositoryLibraryDescriptor> {

    @Transient
    val m2: DefaultArtifactVersion = DefaultArtifactVersion(version)

    override fun compareTo(other: MavenRepositoryLibraryDescriptor): Int {
        this.groupId.compareTo(other.groupId).takeIf { it != 0 }?.let { return it }
        this.artifactId.compareTo(other.artifactId).takeIf { it != 0 }?.let { return it }
        return this.m2.compareTo(other.m2)
    }

    override fun toString(): String {
        return "$groupId:$artifactId:$version"
    }
}

@Serializable
data class ExistsDependency(val location: String, val libraryDescriptor: MavenRepositoryLibraryDescriptor) {
    override fun toString(): String {
        return "$libraryDescriptor(at $location)"
    }
}

@Serializable
data class LibVersionInVersionClassField(val groupId: String, val artifactId: String, val className: String, val methodName: String, val fieldName: String)

object LibVersionProvider : PreAnalysisUnit() {

    private val logger = KotlinLogging.logger {}

    private val libraryDescriptors: ConcurrentMap<String, MutableSet<ExistsDependency>> = ConcurrentHashMap()

    private val xmlPathAndModelMap: ConcurrentMap<Path, Model?> = ConcurrentHashMap()

    private val allProperties = Maps.newMultiMap<String, String>(Collections.synchronizedMap(Maps.newHybridMap())) {
        Collections.synchronizedSet(Sets.newHybridSet())
    }

    fun findFieldAssignedStringConstantValue(sm: SootMethod): Map<SootField, Constant>? {
        val body = if (sm.hasActiveBody()) sm.activeBody else return null
        val r = mutableMapOf<SootField, Constant>()
        for (u in body.units) {
            if (u is AssignStmt) {
                val rightStr = u.rightOp as? StringConstant ?: return null
                if (u.containsFieldRef() && u.fieldRef is FieldRef && u.leftOpBox == u.fieldRefBox) {
                    val ifr = u.fieldRef
                    if (ifr is StaticFieldRef) {
                        if (ifr.field.declaringClass == sm.declaringClass) {
                            r[ifr.field] = rightStr
                        }
                    } else if (ifr is InstanceFieldRef) {
                        if (ifr.base.equivTo(body.thisLocal)) {
                            r[ifr.field] = rightStr
                        }
                    }
                }
            }
        }
        return r
    }
    fun findFieldAssignedStringConstantValue(className: String, methodName: String): Map<SootField, Constant>? {
        val sc = Scene.v().getSootClassUnsafe(className, false) ?: return null
        val r = mutableMapOf<SootField, Constant>()
        for (f in sc.fields) {
            f.tags.filterIsInstance<ConstantValueTag>().firstOrNull()?.constant?.let {
                r[f] = it
            }
        }
        val sm: SootMethod = sc.getMethodByNameUnsafe(methodName) ?: return r
        return findFieldAssignedStringConstantValue(sm)?.let { it + r }
    }


    fun getFieldAssignedStringConstantValue(className: String, methodName: String, filedName: String): Constant? {
        return findFieldAssignedStringConstantValue(className = className, methodName = methodName)?.mapKeys { it.key.name.lowercase() }?.get(filedName.lowercase())
    }


    @Serializable
    data class VersionConditions(
        val compareMode: Mode,
        val op: CompareOp,
        val libraryDescriptor: MavenRepositoryLibraryDescriptor
    ) {
        enum class Mode {
            MayOrUnknown, May, Must
        }

        enum class CompareOp(val code: String, val check: (test: Int) -> Boolean) {
            LT("<", { it < 0 }), LE("<=", { it <= 0 }), EQ("==", { it == 0 }), GE(">=", { it >= 0 }), GT(">", { it > 0 })
        }
    }


    @Serializable
    class Options : SAOptions {
        val libVersionInVersionClassFields = listOf(
            LibVersionInVersionClassField(
                groupId = "org.apache.poi", artifactId = "poi-ooxml",
                className = "org.apache.poi.Version",  methodName = SootMethod.staticInitializerName, fieldName = "VERSION_STRING" // org.apache.poi:poi
            )
        )

        val versionConditions = mapOf(
            "risk-fastjson" to VersionConditions(
                VersionConditions.Mode.Must,
                VersionConditions.CompareOp.LT,
                MavenRepositoryLibraryDescriptor("com.alibaba", "fastjson", "1.2.83")
            ),
            "risk-jackson" to VersionConditions(
                VersionConditions.Mode.Must,
                VersionConditions.CompareOp.LT,
                MavenRepositoryLibraryDescriptor("com.fasterxml.jackson.core", "jackson-databind", "2.13.4.2")
            ),
            "risk-log4j-injection" to VersionConditions(
                VersionConditions.Mode.Must, // // 不要设置为 MayOrUnknown issue: corax-java-config#70 问题2
                VersionConditions.CompareOp.LT,
                MavenRepositoryLibraryDescriptor("org.apache.logging.log4j", "log4j-core", "2.16.0")
            ),
            "risk-org.apache.poi-ooxml-CVE-2019-12415" to VersionConditions(
                VersionConditions.Mode.May,
                VersionConditions.CompareOp.LT,
                MavenRepositoryLibraryDescriptor("org.apache.poi", "poi-ooxml", "4.1.1")
            ),
            "risk-org.apache.poi-ooxml-CVE-2014-3529" to VersionConditions(
                VersionConditions.Mode.May,
                VersionConditions.CompareOp.LT,
                MavenRepositoryLibraryDescriptor("org.apache.poi", "poi-ooxml", "3.10.1")
            )
        )
    }

    private var option: Options = Options()

    // logic and
    fun isEnable(conditionString: String): Boolean {
        if (conditionString.isEmpty())
            return true

        var result = true
        object : JsonExtVisitor() {
            override fun visitObject(key: String, value: JsonPrimitive) {
                if (!result) return
                when (key) {
                    "@active:condition:version" -> {
                        if (versionCondCheck(value.jsonPrimitive.content) == false) {
                            result = false
                        }
                    }
                }
            }
        }.visitAll(json = conditionString)

        return result
    }

    private val parseConditionCache = mutableMapOf<String, Any>()

    fun versionCondCheck(condName: String): Boolean? {
        val versionCondition = option.versionConditions[condName] ?: let {
            logger.error { "condition name `$condName` is not exists." }; return null
        }
        val cmpResults = compareTo(versionCondition.libraryDescriptor)
        val checkRes = cmpResults.mapValues { versionCondition.op.check(it.value) }
        val boolRes = checkRes.values
        val mode = versionCondition.compareMode
        return if (cmpResults.isEmpty()) {
            mode == VersionConditions.Mode.MayOrUnknown
        } else {
            val anyFalse = boolRes.any { !it }
            val anyTrue = boolRes.any { it }
            if (mode == VersionConditions.Mode.Must) {
                !anyFalse
            } else {
                anyTrue
            }
        }.also { result ->
            if (parseConditionCache.put(condName, result) == null) {
                logger.info {
                    val versionInfoMsg = "Condition evaluate result: cond: \"$condName\" = $result with ${versionCondition.compareMode} compare mode."
                    val versionDetailedMsg = if (cmpResults.isEmpty()) "" else {
                        "\n" + checkRes.toList().joinToString(postfix = "\n", separator = "\n") {
                            "\t${it.first} ${versionCondition.op.code} ${versionCondition.libraryDescriptor.version} = ${it.second}"
                        }
                    }
                    versionInfoMsg + versionDetailedMsg
                }
            }
        }
    }


    private fun parsePomProperties(virtualFile: Path): MavenRepositoryLibraryDescriptor? {
        val properties = Properties()
        try {
            virtualFile.inputStream().use { properties.load(it) }
        } catch (e: IOException) {
            return null
        }
        val groupId = properties.getProperty("groupId")
        val artifactId = properties.getProperty("artifactId")
        val version = properties.getProperty("version")
        return if (groupId != null && artifactId != null && version != null) MavenRepositoryLibraryDescriptor(
            groupId,
            artifactId,
            version
        ) else null
    }

    fun getLibraryDescriptor(groupId: String, artifactId: String): Set<ExistsDependency> {
        return libraryDescriptors["$groupId.$artifactId"] ?: emptySet()
    }

    fun compareTo(other: MavenRepositoryLibraryDescriptor) =
        getLibraryDescriptor(other.groupId, other.artifactId).associateWith { it.libraryDescriptor.compareTo(other) }


    fun add(descriptor: ExistsDependency) =
        libraryDescriptors.getOrPut("${descriptor.libraryDescriptor.groupId}.${descriptor.libraryDescriptor.artifactId}") {
            Collections.synchronizedSet(LinkedHashSet())
        }.add(descriptor)

    context (PreAnalysisApi)
    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun config() {
        assert(option.versionConditions.none { it.key.contains(":") })
        for (it in option.libVersionInVersionClassFields) {
            val version = getFieldAssignedStringConstantValue(className = it.className, methodName = it.methodName, filedName = it.fieldName) ?: continue
            val versionString = (version as? StringConstant)?.value ?: continue
            add(ExistsDependency(it.toString(), MavenRepositoryLibraryDescriptor(groupId = it.groupId, artifactId = it.artifactId, version = versionString)))
        }

        val parser = atAnySourceFile(filename = "pom.properties", config = { incrementalAnalyze = false; ignoreProjectConfigProcessFilter = true }) {
            val descriptor = parsePomProperties(path) ?: return@atAnySourceFile
            add(ExistsDependency(path.toUri().toString(), descriptor))
        }

        val parser2 = atAnySourceFile(filename = "pom.xml", config = { incrementalAnalyze = false; ignoreProjectConfigProcessFilter = true }) {
            // skip xml contains in jar lib
            if (path.pathString.contains("META-INF/maven/")) {
                return@atAnySourceFile
            }
            val model = MavenParser(path).parse() ?: return@atAnySourceFile
            xmlPathAndModelMap[path] = model
            mergeProperties(model)
        }

        runInScene {
            parser.await()
            parser2.await()
            collectExistsDependency()
            val out = outputPath.resolve("project-env").also { FileUtils.forceMkdir(it.toFile()) }
            out.resolve("versions.txt").outputStream().use { outputStream ->
                jsonFormat.encodeToStream(libraryDescriptors.toMap(), outputStream)
            }
        }
    }

    private fun collectExistsDependency() {
        for (entry in xmlPathAndModelMap.entries) {
            val (xmlPath, model) = entry
            generateMavenRepositoryLibraryDescriptors(model).forEach {
                add(ExistsDependency(xmlPath.toUri().toString(), it))
            }
        }
    }

    private fun getTrueVersion(version: String): Set<String> {
        val matcher = Pattern.compile("\\$ *\\{(.*?)}").matcher(version)
        if (matcher.find()) {
            val versionDesc = matcher.group(1).trim()
            return allProperties.get(versionDesc).toSet()
        }
        return setOf(version)
    }

    private fun generateMavenRepositoryLibraryDescriptors(model: Model?): MutableList<MavenRepositoryLibraryDescriptor> {
        val mavenRepositoryLibraryDescriptors = mutableListOf<MavenRepositoryLibraryDescriptor>()
        model?.dependencies?.forEach { dependency->
            val version = dependency.version ?: return@forEach
            val trueVersion = getTrueVersion(version)
            trueVersion.forEach {
                val descriptor = MavenRepositoryLibraryDescriptor(dependency.groupId, dependency.artifactId, it)
                mavenRepositoryLibraryDescriptors.add(descriptor)
            }
        }
        return mavenRepositoryLibraryDescriptors
    }

    private fun mergeProperties(model: Model?) {
        model?.properties?.entries?.forEach {
            val key = (it.key as? String)?.trim() ?: return@forEach
            val value = (it.value as? String)?.trim() ?: return@forEach
            allProperties.put(key, value)
        }
    }

    private val jsonFormat = Json {
        useArrayPolymorphism = true
        prettyPrint = true
    }
}