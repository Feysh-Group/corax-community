package com.feysh.corax.config.general.rule

import com.feysh.corax.config.general.model.taint.TaintRule
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.nio.file.Path
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.io.path.absolute
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
            val ret = mutableListOf<T>()
            jsonFormat.parseToJsonElement(text).jsonArray.forEach { e ->
                if (e is JsonObject){
                    ret.add(jsonFormat.decodeFromString(serializer, e.toString()))
                }
            }
            return ret
        }
        private fun <T> decode(file: Path, serializer: KSerializer<T>): List<T> {
            try {
                file.reader(Charsets.UTF_8).use {
                    return@decode decode(it.readText(), serializer)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                error("invalid parse json file $file")
            }
        }


        fun <T> load(files: List<Path>, serializer: KSerializer<T>): RuleManager<T> {
            val methods = ArrayList<T>(files.size * 100)
            files.sortedBy { it.absolute().normalize() }.forEach { file ->
                methods +=  decode(file, serializer)
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

open class GroupedMethodsManager<T: IMethodGrouped>(methods: List<T>) : RuleManager<T>(methods){

    private val methodsGroup: LinkedHashMap<String, MutableList<T>> =
        LinkedHashMap<String, MutableList<T>>().also {
            for (gMethod in methods) {
                for (group in gMethod.group.split(",")) {
                    it.getOrPut(group.trim()) { mutableListOf() }.add(gMethod)
                }
            }
        }

    val allKinds = methodsGroup.keys

    fun getRulesByGroupKinds(kind: String): List<T> = methodsGroup.filter { it.key.lowercase(Locale.getDefault()) == kind }.values.flatten()

    fun getRulesByGroupKinds(kinds: List<String>): Map<String, List<T>> = kinds.associateWith { getRulesByGroupKinds(it) }

    companion object {
        fun <T: IMethodGrouped> load(files: List<Path>, serializer: KSerializer<T>): GroupedMethodsManager<T> {
            val methods = RuleManager.load(files, serializer)
            return GroupedMethodsManager(methods.rules)
        }
    }
}