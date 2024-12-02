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

package com.feysh.corax.config.general.rule

import com.feysh.corax.config.general.model.taint.TaintRule
import com.feysh.corax.config.general.utils.checkArg
import com.feysh.corax.config.general.utils.methodMatch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.nio.file.Path
import java.util.*
import kotlin.collections.LinkedHashMap
import kotlin.io.path.name
import kotlin.io.path.outputStream
import kotlin.io.path.reader


open class RuleManager<T> (val rules: List<T>){

    open val size: Int get() = rules.size
    companion object {

        val jsonFormat = Json {
            useArrayPolymorphism = true
            prettyPrint = false
            serializersModule = SerializersModule {
                polymorphic(IMethodSignature::class) {
                    subclass(TaintRule.Source::class)
                    subclass(TaintRule.Summary::class)
                    subclass(TaintRule.Sink::class)
                }
            }
        }

        private fun <T> decode(text: String, serializer: KSerializer<T>): List<T> {
            return jsonFormat.parseToJsonElement(text).jsonArray.mapNotNull { e ->
                if (e is JsonObject) {
                    jsonFormat.decodeFromJsonElement(serializer, e)
                } else { // comment string
                    null
                }
            }
        }

        private fun <T> decode(file: Path, serializer: KSerializer<T>): List<T> {
            try {
                file.reader(Charsets.UTF_8).use {
                    return@decode decode(it.readText(), serializer)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                error("Failed to parse json file $file")
            }
        }


        fun <T> load(files: List<Path>, serializer: KSerializer<T>): RuleManager<T> {
            val methods = ArrayList<T>(files.size * 100)
            files.sortedBy { it.name }.forEach { file ->
                methods += decode(file, serializer)
            }
            return RuleManager(methods)
        }

    }

    fun dump(out: Path, serializer: KSerializer<T>){
        val jsonRules = rules.mapTo(linkedSetOf()) { jsonFormat.encodeToString(serializer, it) }
        out.outputStream().writer(Charsets.UTF_8).use {
            it.write(jsonRules.joinToString(separator = ",\n", prefix = "[", postfix = "]"))
        }
    }
}

open class GroupedVersionConditionsManager<T : IVersionConditionsGrouped>(versionConditionsList: List<T>) :
    RuleManager<T>(versionConditionsList) {

    companion object {
        fun <T: IVersionConditionsGrouped> load(files: List<Path>, serializer: KSerializer<T>): GroupedVersionConditionsManager<T> {
            val methods = RuleManager.load(files, serializer)
            return GroupedVersionConditionsManager(methods.rules)
        }
    }
}

open class GroupedMethodsManager<T: IMethodGrouped>(methods: List<T>) : RuleManager<T>(methods){

    private val methodsGroup: LinkedHashMap<String, MutableList<T>> =
        LinkedHashMap<String, MutableList<T>>().also {
            for (gMethod in methods) {
                for (group in gMethod.group.split(",")) {
                    it.getOrPut(group.trim().lowercase(Locale.getDefault())) { mutableListOf() }.add(gMethod)
                }
            }
        }

    val allKinds = methodsGroup.keys

    fun getRulesByGroupKinds(kind: String): List<T> {
        val lowerKey = kind.lowercase(Locale.getDefault())
        return methodsGroup.filter { it.key == lowerKey }.values.flatten()
    }

    fun getRulesByGroupKinds(kinds: List<String>): Map<String, List<T>> = kinds.associateWith { getRulesByGroupKinds(it) }

    fun validate() {
        rules.forEach {
            val methodMatch = it.methodMatch
            if (it is IMethodAccessPath) it.checkArg(methodMatch, it.signature)
        }
    }

    companion object {
        fun <T: IMethodGrouped> load(files: List<Path>, serializer: KSerializer<T>): GroupedMethodsManager<T> {
            val methods = RuleManager.load(files, serializer)
            return GroupedMethodsManager(methods.rules)
        }
    }
}