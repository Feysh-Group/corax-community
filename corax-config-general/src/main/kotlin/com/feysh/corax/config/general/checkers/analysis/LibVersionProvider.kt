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
import com.feysh.corax.config.api.report.Region
import com.feysh.corax.config.general.checkers.analysis.LibVersionProvider.SootFieldWithRegion
import com.feysh.corax.config.general.common.collect.Maps
import com.feysh.corax.config.general.common.collect.Sets
import com.feysh.corax.config.general.model.ConfigCenter.versionRuleManager
import com.feysh.corax.config.general.model.version.VersionRule.MavenRepositoryLibraryCoordinate
import com.feysh.corax.config.general.model.version.VersionRule.MavenRepositoryLibraryDescriptor
import com.feysh.corax.config.general.model.version.VersionRule.VersionCondition
import com.feysh.corax.config.general.model.version.VersionRule.VersionConditions
import com.feysh.corax.config.general.utils.MavenParser
import com.feysh.corax.config.general.utils.isFileScheme
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.*
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.apache.maven.model.Dependency
import org.apache.maven.model.InputLocation
import org.apache.maven.model.Model
import soot.Scene
import soot.SootField
import soot.SootMethod
import soot.jimple.*
import soot.tagkit.ConstantValueTag
import soot.tagkit.Host
import java.io.IOException
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.regex.Pattern
import kotlin.io.path.inputStream
import kotlin.io.path.name
import kotlin.io.path.outputStream
import kotlin.io.path.pathString

abstract class ExistsDependency {
    abstract val location: String
    open val shortLocation: String
        get() = location
    abstract val libraryDescriptor: MavenRepositoryLibraryDescriptor
    abstract val sootLoc: Pair<Host, Region>?
    abstract val fileLoc: Pair<Path, Region>?
    abstract val region: Region?
    override fun toString(): String {
        return "$libraryDescriptor(at $location)"
    }
    fun toConsoleString(): String {
        return "$libraryDescriptor(at $location:${region?.startLine}:${region?.startColumn})"
    }
    fun toSortString(): String {
        return "$libraryDescriptor(at $shortLocation:${region?.startLine}:${region?.startColumn})"
    }
    @Serializable
    data class SerializeClass(val location: String, val libraryDescriptor: MavenRepositoryLibraryDescriptor)
    val fileView: SerializeClass get() = SerializeClass(location, libraryDescriptor)

    abstract fun isFromNormalFile(): Boolean
}

val InputLocation.region: Region get() {
    return Region(startLine = lineNumber, startColumn = columnNumber, endLine = -1, endColumn = -1)
}

data class DependencyFromMavenPom(
    val pom: Path,
    override val shortLocation: String,
    val dependency: Dependency,
    override val libraryDescriptor: MavenRepositoryLibraryDescriptor
): ExistsDependency() {
    val uri = pom.toUri()
    override val region: Region get() = dependency.getLocation("version")?.region ?: Region.ERROR
    override val location: String get() = uri.toString()
    override val sootLoc: Pair<Host, Region>? get() = null
    override val fileLoc: Pair<Path, Region> get() = pom to region
    override fun toString(): String = super.toConsoleString()
    override fun isFromNormalFile(): Boolean = uri.isFileScheme
}

data class DependencyFromPomProperties(
    val properties: Path,
    override val libraryDescriptor: MavenRepositoryLibraryDescriptor
): ExistsDependency() {
    val uri = properties.toUri()
    override val region: Region get() = Region(0, 0, 0, 0)
    override val location: String get() = uri.toString()
    override val sootLoc: Pair<Host, Region>? get() = null
    override val fileLoc: Pair<Path, Region> get() = properties to region
    override fun toString(): String = super.toConsoleString()
    override fun isFromNormalFile(): Boolean = uri.isFileScheme
}

data class DependencyFromJarName(
    val jar: Path,
    val versionsFile: Path,
    override val shortLocation: String,
    override val libraryDescriptor: MavenRepositoryLibraryDescriptor
): ExistsDependency() {
    val uri = jar.toUri()
    override val region: Region get() = Region(1, 1, 1, 1)
    override val location: String get() = uri.toString()
    override val sootLoc: Pair<Host, Region>? get() = null
    override val fileLoc: Pair<Path, Region> get() = versionsFile to region
    override fun toString(): String = super.toString()
    override fun isFromNormalFile(): Boolean = uri.isFileScheme
}

data class DependencyFromClassField(
    val classField: LibVersionInVersionClassField,
    val sootFieldWithRegion: SootFieldWithRegion,
    override val libraryDescriptor: MavenRepositoryLibraryDescriptor
): ExistsDependency() {
    override val region: Region get() = sootFieldWithRegion.region
    override val location: String get() = sootFieldWithRegion.sootField.signature
    override val sootLoc: Pair<Host, Region> get() = sootFieldWithRegion.sootField to region
    override val fileLoc: Pair<Path, Region>? get() = null
    override fun toString(): String = super.toConsoleString()
    override fun isFromNormalFile(): Boolean = true
}

@Serializable
data class LibVersionInVersionClassField(val groupId: String, val artifactId: String, val className: String, val methodName: String, val fieldName: String) {

    private fun findFieldAssignedStringConstantValue(sm: SootMethod): Map<SootFieldWithRegion, Constant>? {
        val body = if (sm.hasActiveBody()) sm.activeBody else return null
        val r = mutableMapOf<SootFieldWithRegion, Constant>()
        for (u in body.units) {
            if (u is AssignStmt) {
                val rightStr = u.rightOp as? StringConstant ?: return null
                if (u.containsFieldRef() && u.fieldRef is FieldRef && u.leftOpBox == u.fieldRefBox) {
                    val ifr = u.fieldRef
                    val fieldRegion = Region(u) ?: Region.ERROR
                    if (ifr is StaticFieldRef) {
                        if (ifr.field.declaringClass == sm.declaringClass) {
                            val sootFieldWithRegion = SootFieldWithRegion(ifr.field, fieldRegion)
                            r[sootFieldWithRegion] = rightStr
                        }
                    } else if (ifr is InstanceFieldRef) {
                        if (ifr.base.equivTo(body.thisLocal)) {
                            val sootFieldWithRegion = SootFieldWithRegion(ifr.field, fieldRegion)
                            r[sootFieldWithRegion] = rightStr
                        }
                    }
                }
            }
        }
        return r
    }

    private fun findFieldAssignedStringConstantValue(className: String, methodName: String): Map<SootFieldWithRegion, Constant>? {
        val sc = Scene.v().getSootClassUnsafe(className, false) ?: return null
        val r = mutableMapOf<SootFieldWithRegion, Constant>()
        for (f in sc.fields) {
            f.tags.filterIsInstance<ConstantValueTag>().firstOrNull()?.constant?.let {
                val sootFieldWithRegion = SootFieldWithRegion(f, Region.ERROR)
                r[sootFieldWithRegion] = it
            }
        }
        val sm: SootMethod = sc.getMethodByNameUnsafe(methodName) ?: return r
        return findFieldAssignedStringConstantValue(sm)?.let { it + r }
    }


    fun getFieldAssignedStringConstantValue(className: String, methodName: String, filedName: String): Map<SootFieldWithRegion, Constant> {
        val found = findFieldAssignedStringConstantValue(className = className, methodName = methodName) ?: return emptyMap()
        val lowercaseName = filedName.lowercase()
        return found.filter { it.key.sootField.name.lowercase() == lowercaseName }
    }
}

object LibVersionProvider : PreAnalysisUnit() {

    private val logger = KotlinLogging.logger {}

    private val libraryDescriptors: ConcurrentMap<String, MutableSet<ExistsDependency>> = ConcurrentHashMap()

    private val xmlPathAndModelMap: ConcurrentMap<Pair<Path, String>, Model?> = ConcurrentHashMap()

    private val allProperties = Maps.newMultiMap<String, String>(Collections.synchronizedMap(Maps.newHybridMap())) {
        Collections.synchronizedSet(Sets.newHybridSet())
    }

    @Serializable
    class Options : SAOptions {
        val libVersionInVersionClassFields = listOf(
            LibVersionInVersionClassField(
                groupId = "org.apache.poi", artifactId = "poi-ooxml",
                className = "org.apache.poi.Version",  methodName = SootMethod.staticInitializerName, fieldName = "VERSION_STRING" // org.apache.poi:poi
            )
        )

        @Transient
        val riskVersionConditions = versionRuleManager.riskVersionConditionsGrouped.rules.associateBy { it.key }

        @Transient
        val customVersionConditions = versionRuleManager.customVersionConditionsGrouped.rules.associateBy { it.key }

        val jarFilePattern =
            "^(?<artifactId>[a-zA-Z0-9_\\-\\.]+)-(?<version>[0-9]+(?:\\.[0-9]+)*(?:-|\\.[a-zA-Z0-9_\\-\\.]+)?)\\.jar\$"
    }

    private var option: Options = Options()

    // logic and
    fun getVersionCheckResult(conditionString: String): VersionCheckResult? {
        if (conditionString.isEmpty())
            return null

        var versionCheckResult: VersionCheckResult? = null
        object : JsonExtVisitor() {
            override fun visitObject(key: String, value: JsonPrimitive) {
                if (versionCheckResult != null) return
                when (key) {
                    "@active:condition:version" -> {
                        val versionCondCheck = riskVersionCondCheck(value.jsonPrimitive.content)
                        if (versionCondCheck != null) {
                            versionCheckResult = versionCondCheck
                        }
                    }
                }
            }
        }.visitAll(json = conditionString)

        return versionCheckResult
    }

    private val parseConditionCache = mutableMapOf<String, Boolean>()

    data class VersionCheckResult(
        val condition: VersionConditions,
        val predicateResult: Boolean,
        val dependencies: Set<ExistsDependency>,
    )

    fun riskVersionCondCheck(condName: String): VersionCheckResult? {
        val versionCondition = option.riskVersionConditions[condName] ?: let {
            logger.error { "risk condition name `$condName` is not exists." }; return null
        }
        return versionCondCheck(condName, versionCondition)
    }

    fun customVersionCondCheck(condName: String): VersionCheckResult? {
        val versionCondition = option.customVersionConditions[condName] ?: let {
            logger.error { "custom condition name `$condName` is not exists." }; return null
        }
        return versionCondCheck(condName, versionCondition)
    }

    fun versionCondCheck(condName: String?, versionCondition: VersionConditions): VersionCheckResult {
        val cmpResults = compareTo(versionCondition)
        val mode = versionCondition.compareMode
        return if (cmpResults.isEmpty()) {
            VersionCheckResult(versionCondition, mode == VersionCondition.Mode.MayOrUnknown, emptySet())
        } else {
            val anyFalse = cmpResults.filter { !it.value }.mapTo(mutableSetOf()) { it.key }
            val anyTrue = cmpResults.filter { it.value }.mapTo(mutableSetOf()) { it.key }
            if (mode == VersionCondition.Mode.Must) {
                VersionCheckResult(versionCondition, anyFalse.isEmpty(), anyTrue)
            } else {
                VersionCheckResult(versionCondition, anyTrue.isNotEmpty(), anyTrue)
            }
        }.also { r ->
            if (condName != null && parseConditionCache.put(condName, r.predicateResult) == null) {
                logger.info {
                    val versionInfoMsg = "Condition evaluate result: $versionCondition = ${r.predicateResult}."
                    val versionDetailedMsg = if (cmpResults.isEmpty()) "" else {
                        "\n" + cmpResults.toList().joinToString(postfix = "\n", separator = "\n") {
                            "\t${it.first} is ${if (it.second) "" else "not "}satisfied"
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
            MavenRepositoryLibraryCoordinate(groupId, artifactId),
            version
        ) else null
    }

    fun getLibraryDescriptor(groupId: String?, artifactId: String): Set<ExistsDependency> {
        if (groupId != null) {
            return libraryDescriptors["$groupId.$artifactId"] ?: libraryDescriptors[artifactId] ?: emptySet()
        }
        return libraryDescriptors[artifactId] ?: emptySet()
    }

    fun compareTo(other: VersionConditions) =
        getLibraryDescriptor(other.libraryCoordinate.groupId, other.libraryCoordinate.artifactId).associateWith {
            val l = other.lowerbound?.let { b -> b.op.check(it.libraryDescriptor.compareToVersion(DefaultArtifactVersion(b.version))) }
            val r = other.upperbound?.let { b -> b.op.check(it.libraryDescriptor.compareToVersion(DefaultArtifactVersion(b.version))) }
            (l ?: true) && (r ?: true)
        }


    fun add(descriptor: ExistsDependency) {
        val groupId = descriptor.libraryDescriptor.coordinate.groupId
        val artifactId = descriptor.libraryDescriptor.coordinate.artifactId
        libraryDescriptors.getOrPut(artifactId) {
            Collections.synchronizedSet(LinkedHashSet())
        }.add(descriptor)
        if (groupId != null) {
            libraryDescriptors.getOrPut("$groupId.$artifactId") {
                Collections.synchronizedSet(LinkedHashSet())
            }.add(descriptor)
        }
    }

    private fun heuristicAddLibrary() {
    }

    class SootFieldWithRegion(val sootField: SootField, val region: Region)

    context (PreAnalysisApi)
    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun config() {
        val out = outputPath.resolve("project-env").also { FileUtils.forceMkdir(it.toFile()) }
        val versionsFile = out.resolve("versions.json")
        assert(option.riskVersionConditions.none { it.key.contains(":") })
        for (it in option.libVersionInVersionClassFields) {
            val infos = it.getFieldAssignedStringConstantValue(className = it.className, methodName = it.methodName, filedName = it.fieldName)
            if (infos.isEmpty()) continue
            for (info in infos) {
                val constant: Constant = info.value
                val versionString = (constant as? StringConstant)?.value ?: continue
                add(DependencyFromClassField(it, info.key, MavenRepositoryLibraryDescriptor(coordinate = MavenRepositoryLibraryCoordinate(it.groupId, it.artifactId), version = versionString)))
            }
        }

        val parser = atAnySourceFile(filename = "pom.properties", config = { incrementalAnalyze = false; ignoreProjectConfigProcessFilter = true }) {
            val descriptor = parsePomProperties(path) ?: return@atAnySourceFile
            add(DependencyFromPomProperties(path, descriptor))
        }

        val parser2 = atAnySourceFile(filename = "pom.xml", config = { incrementalAnalyze = false; ignoreProjectConfigProcessFilter = true }) {
            // skip xml contains in jar lib
            if (path.pathString.contains("META-INF/maven/")) {
                return@atAnySourceFile
            }
            val model = MavenParser(path).parse() ?: return@atAnySourceFile
            xmlPathAndModelMap[path to relativePath.relativePath] = model
            mergeProperties(model)
        }

        val regex = option.jarFilePattern.toRegex()
        val collectJarLibs = atAnySourceFile(extension = "jar") {
            val matchResult = regex.matchEntire(path.name)
            if (matchResult == null) {
                logger.debug { "${path.name} can't match the lib pattern while collecting jar libs." }
                return@atAnySourceFile
            }
            val artifactId = matchResult.groups["artifactId"]?.value ?: return@atAnySourceFile
            val version = matchResult.groups["version"]?.value ?: return@atAnySourceFile
            val libraryDescriptor = MavenRepositoryLibraryDescriptor(coordinate = MavenRepositoryLibraryCoordinate(artifactId = artifactId), version = version)
            add(DependencyFromJarName(jar = path, versionsFile = versionsFile, shortLocation = relativePath.relativePath, libraryDescriptor))
        }

        runInScene {
            parser.await()
            parser2.await()
            collectJarLibs.await()
            heuristicAddLibrary()
            collectExistsDependency()
            versionsFile.outputStream().use { outputStream ->
                val map = libraryDescriptors.values.flatten().toSet().groupBy {
                    val groupId = it.libraryDescriptor.coordinate.groupId
                    val artifactId = it.libraryDescriptor.coordinate.artifactId
                    if (groupId != null) "$groupId.$artifactId" else artifactId
                }.mapValues { entry -> entry.value.map { it.fileView } }
                jsonFormat.encodeToStream(map, outputStream)
            }
        }
    }

    private fun collectExistsDependency() {
        for (entry in xmlPathAndModelMap.entries) {
            val (xmlPath, model) = entry
            generateMavenRepositoryLibraryDescriptors(xmlPath.first, xmlPath.second, model)
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

    private fun generateMavenRepositoryLibraryDescriptors(xmlPath: Path, xmlRelativePath: String, model: Model?): MutableList<MavenRepositoryLibraryDescriptor> {
        val mavenRepositoryLibraryDescriptors = mutableListOf<MavenRepositoryLibraryDescriptor>()
        model?.dependencies?.forEach { dependency->
            val version = dependency.version ?: return@forEach
            val trueVersion = getTrueVersion(version)
            trueVersion.forEach {
                val descriptor = MavenRepositoryLibraryDescriptor(coordinate = MavenRepositoryLibraryCoordinate(dependency.groupId, dependency.artifactId), it)
                add(DependencyFromMavenPom(xmlPath, shortLocation = xmlRelativePath, dependency = dependency, descriptor))
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