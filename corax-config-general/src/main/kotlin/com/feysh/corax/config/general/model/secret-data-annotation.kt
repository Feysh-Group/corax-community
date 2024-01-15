package com.feysh.corax.config.general.model

import com.feysh.corax.config.api.*
import com.feysh.corax.config.general.checkers.GeneralTaintTypes
import kotlinx.serialization.Serializable
import soot.RefLikeType
import soot.SootField
import java.util.*

@Suppress("ClassName")
object `secret-data-annotation` : AIAnalysisUnit() {


    @Serializable
    class Options : SAOptions {
        @Suppress("SpellCheckingInspection")
        val sensitiveNameFilter: List<String> = listOf(
            "pass",
            "passport",
            "passw0rd",
            "passwd",
            "secret",
            "privatekey",
            "password",
        )

        val fieldNameExcludes: Set<String> = setOf(
            "passHandle"
        )
    }

    var option: Options = Options()

    context (AIAnalysisApi)
    private fun fieldSource() {

        val matcher: List<(field: SootField, fieldName: String) -> Boolean> = option.sensitiveNameFilter.map { name ->
            if (name.startsWith("regex:")) {
                val regex = Regex(name, RegexOption.IGNORE_CASE)
                return@map { _, fieldName ->
                    regex.matches(fieldName)
                }
            } else {
                val nameLowerCase = name.lowercase(Locale.getDefault())
                return@map { _, fieldName ->
                    fieldName.contains(nameLowerCase)
                }
            }
        }

        eachField {
            val fieldType = sootField.type
            if (sootField.isStatic || sootField.isFinal || fieldType !is RefLikeType) {
                return@eachField
            }

            val fieldName = sootField.name.lowercase(Locale.getDefault())
            val matches = matcher.any { it.invoke(sootField, fieldName) }
            if (!matches) {
                return@eachField
            }

            if (option.fieldNameExcludes.any { fieldName in it }) {
                return@eachField
            }

            atGet {
                val accessPaths = if (ConfigCenter.isCollectionClassType(fieldType) || ConfigCenter.isOptionalClassType(fieldType)) {
                    listOf(field.field(Elements))
                } else if (ConfigCenter.isMapClassType(fieldType)) {
                    listOf(field.field(MapValues))
                } else {
                    listOf(field)
                }
                for (acp in accessPaths) {
                    acp.taint += taintOf(GeneralTaintTypes.UNENCRYPTED_DATA, GeneralTaintTypes.CONTAINS_SENSITIVE_DATA)
                }
            }
        }
    }


    context (AIAnalysisApi)
    override suspend fun config() {
        fieldSource()
        // TODO
    }
}