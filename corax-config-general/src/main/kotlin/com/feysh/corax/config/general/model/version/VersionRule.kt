package com.feysh.corax.config.general.model.version

import com.feysh.corax.commons.compareToNullable
import com.feysh.corax.config.general.rule.GroupedVersionConditionsManager
import com.feysh.corax.config.general.rule.IVersionConditionsGrouped
import kotlinx.serialization.*
import kotlinx.serialization.EncodeDefault.Mode
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import java.nio.file.Path

object VersionRule {

    @Serializable
    data class MavenRepositoryLibraryCoordinate(val groupId: String? = null, val artifactId: String) :
        Comparable<MavenRepositoryLibraryCoordinate> {

        override fun compareTo(other: MavenRepositoryLibraryCoordinate): Int {
            this.groupId.compareToNullable(other.groupId).takeIf { it != 0 }?.let { return it }
            this.artifactId.compareTo(other.artifactId).takeIf { it != 0 }?.let { return it }
            return 0
        }

        override fun toString(): String {
            return if (groupId != null) "$groupId:$artifactId" else artifactId
        }
    }

    @Serializable
    data class VersionCondition(val op: CompareOp, val version: String) {
        enum class Mode {
            MayOrUnknown, May, Must
        }

        enum class CompareOp(val code: String, val check: (test: Int) -> Boolean) {
            LT("<", { it < 0 }), LE("<=", { it <= 0 }), EQ("==", { it == 0 }), GE(">=", { it >= 0 }), GT(">", { it > 0 })
        }

        override fun toString(): String {
            return "${op.code} $version"
        }
    }

    @Serializable
    data class MavenRepositoryLibraryDescriptor(val coordinate: MavenRepositoryLibraryCoordinate, val version: String) :
        Comparable<MavenRepositoryLibraryDescriptor> {

        @Transient
        val m2: DefaultArtifactVersion = DefaultArtifactVersion(version)

        override fun compareTo(other: MavenRepositoryLibraryDescriptor): Int {
            this.coordinate.groupId.compareToNullable(other.coordinate.groupId).takeIf { it != 0 }?.let { return it }
            this.coordinate.artifactId.compareTo(other.coordinate.artifactId).takeIf { it != 0 }?.let { return it }
            return compareToVersion(other)
        }

        fun compareToVersion(other: MavenRepositoryLibraryDescriptor): Int {
            return this.m2.compareTo(other.m2)
        }

        fun compareToVersion(other: DefaultArtifactVersion): Int {
            return this.m2.compareTo(other)
        }

        override fun toString(): String {
            return if (coordinate.groupId != null) "${coordinate.groupId}:${coordinate.artifactId}:$version" else "${coordinate.artifactId}:$version"
        }
    }

    @Serializable
    @OptIn(ExperimentalSerializationApi::class)
    @Suppress("SpellCheckingInspection")
    data class VersionConditions(
        override val key: String,
        val bugMessage: String? = null,
        val libraryCoordinate: MavenRepositoryLibraryCoordinate,
        val compareMode: VersionCondition.Mode,
        @EncodeDefault(mode=Mode.ALWAYS)
        val lowerbound: VersionCondition? = null,
        @EncodeDefault(mode=Mode.ALWAYS)
        val upperbound: VersionCondition? = null
    ): IVersionConditionsGrouped {

        override fun toString(): String {
            return "rule key: \"${key}\": version of $libraryCoordinate can satisfy ${
                listOfNotNull(lowerbound, upperbound).joinToString(" && ") { "($it)" }
            }"
        }
    }

    data class VersionRuleManager(
        val riskVersionConditionsGrouped: GroupedVersionConditionsManager<VersionConditions>,
        val customVersionConditionsGrouped: GroupedVersionConditionsManager<VersionConditions>
    ) {

        override fun toString(): String {
            return "riskVersionConditions: ${riskVersionConditionsGrouped.size}, customVersionConditions: ${customVersionConditionsGrouped.size}"
        }

        companion object {

            fun loadJson(
                riskVersionJsonFiles: List<Path>,
                customVersionJsonFiles: List<Path>
            ): VersionRuleManager {
                val riskVersions: GroupedVersionConditionsManager<VersionConditions> = GroupedVersionConditionsManager.load(riskVersionJsonFiles, serializer())
                val customVersions: GroupedVersionConditionsManager<VersionConditions> = GroupedVersionConditionsManager.load(customVersionJsonFiles, serializer())
                return VersionRuleManager(riskVersions, customVersions)
            }
        }
    }
}