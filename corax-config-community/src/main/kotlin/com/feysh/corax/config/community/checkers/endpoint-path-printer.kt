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

package com.feysh.corax.config.community.checkers

import com.feysh.corax.config.api.PreAnalysisApi
import com.feysh.corax.config.api.PreAnalysisUnit
import com.feysh.corax.config.api.utils.typename
import com.feysh.corax.config.general.model.javaee.JavaeeAnnotationSource
import com.feysh.corax.config.general.model.javaee.JavaeeFrameworkConfigs
import com.feysh.corax.config.general.model.type.TypeHandler
import com.feysh.corax.config.general.utils.classTypeToSootTypeDesc
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import org.apache.commons.io.FileUtils
import soot.RefType
import soot.SootMethod
import soot.Type
import soot.tagkit.AnnotationArrayElem
import soot.tagkit.AnnotationStringElem
import soot.tagkit.VisibilityAnnotationTag
import kotlin.io.path.outputStream

@Suppress("ClassName")
object `endpoint-path-printer` : PreAnalysisUnit() {

    private val jsonFormat = Json {
        useArrayPolymorphism = true
        prettyPrint = true
    }

    data class UsefulMethodInfo(val sootMethod: SootMethod, val beans: List<Pair<Int, Type>>, val webMappingPaths: Map<String, List<String>>) {

        @Serializable
        data class View(val method: String,
                        @SerialName("parameter_beans")
                        val beans: List<Pair<Int, String?>>,
                        @SerialName("mapping")
                        val webMappingPaths: Map<String, List<String>>)

        val view get() = View(sootMethod.signature, beans.map { it.first to it.second.typename }, webMappingPaths)
    }

    context (PreAnalysisApi)
    @OptIn(ExperimentalSerializationApi::class)
    private fun dump(info: List<UsefulMethodInfo>) {
        val out = outputPath.resolve("checker/endpoint-path").also { FileUtils.forceMkdir(it.toFile()) }
        out.resolve("web-request-mapping-paths.txt").outputStream().use { outputStream ->
            jsonFormat.encodeToStream(info.map { it.view }, outputStream)
        }
    }

    private val VisibilityAnnotationTag.mappingPaths: List<Pair<String, String>> get() {
        return annotations.mapNotNull { annotation ->
            when (annotation.type) {
                in JavaeeFrameworkConfigs.option.REQUEST_MAPPING_ANNOTATION_TYPES -> {
                    val path = (annotation.elems.filterIsInstance<AnnotationArrayElem>()
                        .firstOrNull { it.name == "value" }?.values?.firstOrNull() as? AnnotationStringElem)?.value
                        ?.removePrefix("/")?.removeSuffix("/") ?: return@mapNotNull null
                    classTypeToSootTypeDesc(annotation.type) to path
                }
                "Ljavax/jws/WebMethod;" -> null
                "Ljavax/ws/rs/Path;" -> null
                else -> null
            }
        }
    }

    context (PreAnalysisApi)
    override suspend fun config() {

        val paths = atAnyMethod {
            val methodMappingPaths = visibilityAnnotationTag?.mappingPaths ?: emptyList()
            val declaringClassMappingPaths = (sootMethod.declaringClass.getTag(VisibilityAnnotationTag.NAME) as VisibilityAnnotationTag?)?.mappingPaths ?: emptyList()

            if (methodMappingPaths.isEmpty() && declaringClassMappingPaths.isEmpty()) {
                return@atAnyMethod null
            }

            val mappingPaths = declaringClassMappingPaths.ifEmpty { listOf("" to "") }.flatMap { (classWebMethod, declaringClassMappingPath) ->
                methodMappingPaths.ifEmpty { listOf("" to "") }.map { (methodWebMethod, methodMappingPath) ->
                    var joinPath = "$declaringClassMappingPath/$methodMappingPath"
                        .removePrefix("/")
                        .removeSuffix("/")
                    joinPath = "/$joinPath"
                    if (methodMappingPath.isEmpty()) {
                        classWebMethod to joinPath
                    } else {
                        methodWebMethod to joinPath
                    }
                }
            }.groupBy({ it.first }) { it.second }
            val beans: List<Pair<Int, Type>> = sootMethod.parameterTypes.withIndex().mapNotNull { (index, ty) ->
                val classType = ty as? RefType ?: return@mapNotNull null
                val hType = TypeHandler.getHandlerType(classType)
                if (hType !is TypeHandler.OtherClassType || !JavaeeAnnotationSource.isWebModelClassType(hType)) {
                    return@mapNotNull null
                }
                index to classType // bean type
            }
            UsefulMethodInfo(sootMethod, beans, mappingPaths)
        }

        runInScene {
            dump(paths.nonNull().await())
        }
    }
}
