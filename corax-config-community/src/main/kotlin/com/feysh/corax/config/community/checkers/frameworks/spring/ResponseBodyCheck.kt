
package com.feysh.corax.config.community.checkers.frameworks.spring

import com.feysh.corax.config.api.*
import com.feysh.corax.config.general.checkers.GeneralTaintTypes
import com.feysh.corax.config.general.model.javaee.JavaeeFrameworkConfigs
import com.feysh.corax.config.community.SensitiveDataExposeChecker
import com.feysh.corax.config.community.XssChecker

object ResponseBodyCheck : AIAnalysisUnit() {


    context (AIAnalysisApi)
    override fun config() {

        eachMethod {
            val visibilityAnnotationTag = visibilityAnnotationTag ?: return@eachMethod
            if (!visibilityAnnotationTag.hasAnnotations()) {
                return@eachMethod
            }
            for (annotation in visibilityAnnotationTag.annotations) {
                when (annotation.type) {
                    in JavaeeFrameworkConfigs.option.REQUEST_MAPPING_ANNOTATION_TYPES -> {
                        when (sootMethod.returnType.toQuotedString()) {
                            "java.lang.String" -> {
                                /*
                                *   @RequestMapping("/vuln")
                                *   @ResponseBody
                                *   public static String vuln(String xss) {
                                *       return xss;
                                *   }
                                * */
                                this.modelNoArg(config = {
                                    at = MethodConfig.CheckCall.PostCallInCallee
                                }) {
                                    check(
                                        `return`.taint.containsAll(taintOf(GeneralTaintTypes.CONTAINS_XSS_INJECT)),
                                        XssChecker.XssSpringResponseBody
                                    )
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
