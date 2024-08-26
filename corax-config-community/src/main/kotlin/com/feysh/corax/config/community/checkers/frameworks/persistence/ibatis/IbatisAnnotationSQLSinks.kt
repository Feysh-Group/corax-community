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

package com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis

import com.feysh.corax.config.api.*
import com.feysh.corax.config.api.utils.typename
import com.feysh.corax.config.general.checkers.GeneralTaintTypes
import com.feysh.corax.config.community.SqliChecker
import com.feysh.corax.config.general.checkers.internetControl
import com.feysh.corax.config.general.model.ConfigCenter
import soot.tagkit.AnnotationArrayElem
import soot.tagkit.AnnotationStringElem
import soot.tagkit.AnnotationTag

// TODO: need refactor
object IbatisAnnotationSQLSinks : AIAnalysisUnit() {

    context (AIAnalysisApi)
    override suspend fun config() {
        eachMethod {
            val visibilityAnnotationTag = visibilityAnnotationTag ?: return@eachMethod
            if (!visibilityAnnotationTag.hasAnnotations()) {
                return@eachMethod
            }
            for (annotation in visibilityAnnotationTag.annotations) {
                when (annotation.type) {
                    "Lorg/apache/ibatis/annotations/Select;" -> {
                        val selects = annotation.elems.filterIsInstance<AnnotationArrayElem>().flatMap { it.values }
                            .filterIsInstance<AnnotationStringElem>().map { it.value }

                        val riskNames = selects.flatMap { IbatisUtils.getRiskNames(it) }.toSet()
                        if (riskNames.isEmpty()) continue

                        with(preAnalysis) {
                            selects.forEach {
                                report(SqliChecker.MybatisSqlInjectionSinkHint, this@eachMethod.sootMethod) {
                                    this.args["numSinks"] = riskNames.size
                                    this.args["boundSql"] = it
                                }
                            }
                        }

                        modelNoArg(config = { at = MethodConfig.CheckCall.PrevCallInCaller }) {
                            for (parameterIndex in 0 until sootMethod.parameterCount) {
                                val p = parameter(parameterIndex)
                                val annotations = p.visibilityAnnotationTag?.annotations ?: continue
                                val params = annotations.filterIsInstance<AnnotationTag>()
                                    .filter { it.type == "Lorg/apache/ibatis/annotations/Param;" }.flatMap { it.elems }
                                    .filterIsInstance<AnnotationStringElem>().map { it.value }.toSet()

                                val matched = params.intersect(riskNames)
                                if (matched.isNotEmpty()) {
                                    val sinParam = if (matched.size == 1) "${matched.first()}" else "$matched"
                                    val sinkType = p.type.typename?.removeSuffix("java.lang.")
                                    val sinks = if (ConfigCenter.isCollectionClassType(p.type) || ConfigCenter.isOptionalClassType(p.type)) listOf(p.field(Elements), p) else listOf(p)
                                    for (sink in sinks) {
                                        check(
                                            sink.taint.containsAll(taintOf(internetControl + GeneralTaintTypes.CONTAINS_SQL_INJECT)),
                                            SqliChecker.SqlInjection
                                        ) {
                                            this.args["type"] = "ibatis annotations Select"
                                            appendPathEvent(
                                                message = mapOf(
                                                    Language.EN to "In the MyBatis Mapper Interface, there is a controllable dynamic concatenation of the $sinkType type parameter `$sinParam` that is vulnerable to external malicious control.",
                                                    Language.ZH to "MyBatis Mapper Interface 中存在外部恶意控制的动态拼接参数: `$sinParam`, 参数类型为: $sinkType"
                                                ),
                                                loc = this@eachMethod.sootMethod
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}