package com.feysh.corax.config.general.checkers.analysis

import com.feysh.corax.config.api.PreAnalysisApi
import com.feysh.corax.config.api.PreAnalysisUnit
import com.feysh.corax.config.api.SAOptions
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mu.KotlinLogging
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import java.io.IOException
import java.nio.file.Path
import java.util.*
import kotlin.io.path.inputStream
import kotlin.io.path.name

@Serializable
data class MavenRepositoryLibraryDescriptor(val groupId: String, val artifactId: String, val version: String): Comparable<MavenRepositoryLibraryDescriptor> {

    @Transient
    val m2: DefaultArtifactVersion = DefaultArtifactVersion(version)

    override fun compareTo(other: MavenRepositoryLibraryDescriptor): Int {
        this.groupId.compareTo(other.groupId).takeIf { it != 0 }?.let { return it }
        this.artifactId.compareTo(other.artifactId).takeIf { it != 0 }?.let { return it }
        return this.m2.compareTo(other.m2)
    }
}


object LibVersionProvider : PreAnalysisUnit() {

    private val logger = KotlinLogging.logger {}

    private val libraryDescriptors: MutableMap<String, MutableSet<MavenRepositoryLibraryDescriptor>> = mutableMapOf()


    @Serializable
    data class VersionConditions(
        val compareMode: Mode,
        val op: CompareOp,
        val version: MavenRepositoryLibraryDescriptor
    ) {
        enum class Mode {
            MayOrUnknown, May, Must
        }

        enum class CompareOp(val check: (test: Int) -> Boolean) {
            LT({ it < 0 }), LE({ it <= 0 }), EQ({ it == 0 }), GE({ it >= 0 }), GT({ it > 0 })
        }
    }


    @Serializable
    class Options : SAOptions {
        val versionConditions = mapOf(
            "risk-fastjson" to VersionConditions(
                VersionConditions.Mode.May,
                VersionConditions.CompareOp.LT,
                MavenRepositoryLibraryDescriptor("com.alibaba", "fastjson", "1.2.83")
            )
        )
    }

    private var option: Options = Options()

    // logic and
    fun isEnable(conditionString: String): Boolean {
        if (conditionString.isEmpty()) return true
        val condResults = parseCondition(conditionString)
        return condResults.all { it != false }
    }


    private fun versionCondCheck(condName: String): Boolean? {
        val versionCondition = option.versionConditions[condName] ?: let {
            logger.error { "condition name `$condName` is not exists." }; return null
        }
        val cmpResults = compareTo(versionCondition.version)
        val checkRes = cmpResults.map { versionCondition.op.check(it.value) }
        val mode = versionCondition.compareMode
        return if (cmpResults.isEmpty()) {
            mode == VersionConditions.Mode.MayOrUnknown
        } else {
            val anyFalse = checkRes.any { !it }
            val anyTrue = checkRes.any { it }
            if (mode == VersionConditions.Mode.Must) {
                return !anyFalse
            } else {
                return anyTrue
            }
        }
    }


    private val condRegex = Regex("@active:condition:(?<kind>[\\w-]+):(?<cond>[\\w-]+)")
    private fun parseCondition(conditionString: String): Sequence<Boolean?> {
        return condRegex.findAll(conditionString).map { match ->
            val kind = match.groups["kind"]?.value ?: return@map null
            val cond = match.groups["cond"]?.value ?: return@map null
            when (kind) {
                "version" -> versionCondCheck(cond)
                else -> {
                    logger.error { "condition kind `$kind` is not exists." }
                    null
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

    fun getLibraryDescriptor(groupId: String, artifactId: String): Set<MavenRepositoryLibraryDescriptor> {
        return libraryDescriptors["$groupId.$artifactId"] ?: emptySet()
    }

    fun compareTo(other: MavenRepositoryLibraryDescriptor) =
        getLibraryDescriptor(other.groupId, other.artifactId).associateWith { it.compareTo(other) }

    context (PreAnalysisApi)
    override suspend fun config() {
        atAnySourceFile(extension = "properties") {
            if (path.name != "pom.properties") {
                return@atAnySourceFile
            }
            val descriptor = parsePomProperties(path) ?: return@atAnySourceFile
            synchronized(libraryDescriptors) {
                libraryDescriptors.getOrPut("${descriptor.groupId}.${descriptor.artifactId}") { mutableSetOf() }
                    .add(descriptor)
            }
        }
    }
}