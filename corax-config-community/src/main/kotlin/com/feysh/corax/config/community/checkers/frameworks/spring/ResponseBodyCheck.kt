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


package com.feysh.corax.config.community.checkers.frameworks.spring

import com.feysh.corax.config.api.*
import com.feysh.corax.config.api.utils.typename
import com.feysh.corax.config.general.checkers.GeneralTaintTypes
import com.feysh.corax.config.general.model.javaee.JavaeeFrameworkConfigs
import com.feysh.corax.config.community.SensitiveDataExposeChecker
import com.feysh.corax.config.community.XssChecker
import kotlinx.serialization.Serializable
import soot.tagkit.AnnotationArrayElem
import soot.tagkit.AnnotationStringElem
import java.util.regex.Pattern

object ResponseBodyCheck : AIAnalysisUnit() {
    @Serializable
    class Options : SAOptions {
        val isXssVulnerableContentType = listOf(
                "(?i)text/(html|xml|xsl|rdf|vtt|cache-manifest).*",
                "(?i)application/(.*\\+)?xml.*",
                "(?i)application/octet-stream",
                "(?i)cache-manifest.*",
                "(?i)image/svg\\+xml.*",
            )
    }

    private var options: Options = Options()

    context (AIAnalysisApi)
    override suspend fun config() {

        val isXssVulnerableContentType = options.isXssVulnerableContentType.map{ Pattern.compile(it, Pattern.CASE_INSENSITIVE) }

        eachMethod {
            val visibilityAnnotationTag = visibilityAnnotationTag ?: return@eachMethod
            if (!visibilityAnnotationTag.hasAnnotations()) {
                return@eachMethod
            }
            for (annotation in visibilityAnnotationTag.annotations) {
                when (annotation.type) {
                    in JavaeeFrameworkConfigs.option.REQUEST_MAPPING_ANNOTATION_TYPES -> {
                        when (sootMethod.returnType.typename) {
                            "java.lang.String" -> {
                                /*
                                *   @RequestMapping("/vuln")
                                *   @ResponseBody
                                *   public static String vuln(String xss) {
                                *       return xss;
                                *   }
                                * */
                                val produces = annotation.elems.filterIsInstance<AnnotationArrayElem>().firstOrNull{ it.name == "produces" }?.values?.firstOrNull() as? AnnotationStringElem

                                if (produces == null || isXssVulnerableContentType.any { it.matcher(produces.value).matches() }) {
                                    this.modelNoArg(config = {
                                        at = MethodConfig.CheckCall.PostCallInCallee
                                    }) {
                                        check(
                                            `return`.taint.containsAll(taintOf(GeneralTaintTypes.CONTAINS_XSS_INJECT)),
                                            XssChecker.XssSpringResponseBody
                                        )
                                    }
                                }

                                /*
                                * @RequestMapping("/no-proxy")
                                * public static String noProxy(HttpServletRequest request) { return request.getRemoteAddr();}
                                * */
                                this.modelNoArg(config = {
                                    at = MethodConfig.CheckCall.PostCallInCallee
                                }) {
                                    check(
                                        `return`.taint.containsAll(taintOf(GeneralTaintTypes.CONTAINS_SENSITIVE_DATA)),
                                        SensitiveDataExposeChecker.SensitiveDataExposure
                                    )
                                }
                            }

                        }
                        break
                    }
                }
            }

        }
    }
}
