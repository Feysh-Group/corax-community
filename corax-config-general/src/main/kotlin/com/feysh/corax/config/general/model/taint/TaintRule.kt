@file:Suppress("EnumEntryName", "SpellCheckingInspection")

package com.feysh.corax.config.general.model.taint


import com.feysh.corax.config.general.rule.*
import kotlinx.serialization.*
import mu.KotlinLogging
import java.nio.file.*


object TaintRule {


    @Serializable
    data class Source(
        override val enable: Boolean = true,
        @Required @SerialName("kind") override val group: String,
        @Required override val signature: String,
        @Required override val subtypes: Boolean,
        @Required override val arg: String,
        override val provenance: String,
        override val ext: String
    ) : IMethodGrouped, IMethodAccessPath, ISelectable

    @Serializable
    data class Summary(
        override val enable: Boolean = true,
        @Required override val signature: String,
        @Required override val subtypes: Boolean,
        @Required val to: String,
        @Required val propagate: String,
        @Required val from: String,
        override val provenance: String,
        override val ext: String,
    ) : IMethodSignature, ISelectable

    @Serializable
    data class Sink(
        override val enable: Boolean = true,
        @Required @SerialName("kind") override val group: String,
        @Required override val signature: String,
        @Required override val subtypes: Boolean,
        override val arg: String,
        override val provenance: String,
        override val ext: String
    ) : IMethodAccessPathGrouped, ISelectable

    open class SummaryManager(methods: List<Summary>) : RuleManager<Summary>(methods) {
        companion object {
            fun load(files: List<Path>, serializer: KSerializer<Summary>): SummaryManager {
                val methods = RuleManager.load(files, serializer)
                return SummaryManager(methods.rules)
            }
        }
    }


    data class TaintRulesManager(
        val sources: GroupedMethodsManager<Source>,
        val summaries: SummaryManager,
        val sinks: GroupedMethodsManager<Sink>
    ) {


        override fun toString(): String {
            return "sources: ${sources.size} summaries: ${summaries.size} sinks: ${sinks.size}"
        }


        fun dump(sourcesJsonFile: Path, summariesJsonFile: Path, sinksJsonFile: Path) {
            sources.dump(sourcesJsonFile, serializer())
            summaries.dump(summariesJsonFile, serializer())
            sinks.dump(sinksJsonFile, serializer())
            logger.info { "dump: $this" }
        }

        companion object {

            private val logger = KotlinLogging.logger {}

            fun loadJsons(
                sourcesJsonFiles: List<Path>, summariesJsonFiles: List<Path>, sinksJsonFiles: List<Path>
            ): TaintRulesManager {
                val sources: GroupedMethodsManager<Source> = GroupedMethodsManager.load(sourcesJsonFiles, serializer())
                val summaries: SummaryManager = SummaryManager.load(summariesJsonFiles, serializer())
                val sinks: GroupedMethodsManager<Sink> = GroupedMethodsManager.load(sinksJsonFiles, serializer())
                return TaintRulesManager(sources, summaries, sinks)
            }
        }
    }

}
