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

import com.feysh.corax.config.api.Elements
import com.feysh.corax.config.api.AIAnalysisApi
import com.feysh.corax.config.api.AIAnalysisUnit
import com.feysh.corax.config.api.MethodConfig
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
                        modelNoArg(config = { at = MethodConfig.CheckCall.PrevCallInCaller }) {
                            for (parameterIndex in 0 until sootMethod.parameterCount) {
                                val p = parameter(parameterIndex)
                                val annotations = p.visibilityAnnotationTag?.annotations ?: continue
                                val params = annotations.filterIsInstance<AnnotationTag>()
                                    .filter { it.type == "Lorg/apache/ibatis/annotations/Param;" }.flatMap { it.elems }
                                    .filterIsInstance<AnnotationStringElem>().map { it.value }.toSet()

                                if (params.intersect(riskNames).isNotEmpty()) {
                                    val sink = if (ConfigCenter.isCollectionClassType(p.type) || ConfigCenter.isOptionalClassType(p.type)) p.field(Elements) else p
                                    check(
                                        sink.taint.containsAll(taintOf(internetControl + GeneralTaintTypes.CONTAINS_SQL_INJECT)),
                                        SqliChecker.SqlInjection
                                    ){
                                        this.args["type"] = "ibatis annotations Select"
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