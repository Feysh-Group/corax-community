package com.feysh.corax.config.general.checkers.analysis

import kotlinx.serialization.json.*

abstract class JsonExtVisitor {
    open fun visitObject(key: String, value: JsonPrimitive) {}
    open fun visitObject(key: String, value: List<JsonElement>) {}
    open fun visitObject(key: String, value: Map<String, JsonElement>) {}

    open fun visitElement(key: String, value: JsonElement) {
        when (value) {
            is JsonPrimitive -> visitObject(key, value)
            is JsonArray -> visitObject(key, value)
            is JsonObject -> visitObject(key, value)
        }
    }


    open fun visit(j: JsonPrimitive) {}

    open fun visit(j: JsonArray) {}

    open fun visit(j: JsonObject) {
        for ((key, value) in j) {
            visitElement(key, value)
        }
    }


    open fun visitAll(json: String) {
        if (!json.trim().let { it.startsWith('{') && it.endsWith('}') }) return
        when (val jsonElem = jsonFormat.parseToJsonElement(json)) {
            is JsonObject -> visit(jsonElem)
            is JsonPrimitive -> visit(jsonElem)
            is JsonArray -> visit(jsonElem)
        }
    }

    companion object {
        private val jsonFormat = Json {
            useArrayPolymorphism = true
            prettyPrint = true
        }
    }
}