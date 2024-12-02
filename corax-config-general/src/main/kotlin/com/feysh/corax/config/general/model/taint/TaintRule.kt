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

@file:Suppress("EnumEntryName", "SpellCheckingInspection")

package com.feysh.corax.config.general.model.taint


import com.feysh.corax.config.general.rule.*
import com.feysh.corax.config.general.utils.checkFromTo
import com.feysh.corax.config.general.utils.methodMatch
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
    ) : IMethodSignature, ISelectable, IRuleExt

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
        fun validate() {
            rules.forEach {
                val methodMatch = it.methodMatch
                it.checkFromTo(methodMatch, it.signature)
            }
        }

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
