package com.feysh.corax.config.community.checkers

import com.feysh.corax.config.api.PreAnalysisApi
import com.feysh.corax.config.api.PreAnalysisUnit
import com.feysh.corax.config.community.CsrfChecker
import soot.tagkit.AnnotationArrayElem
import soot.tagkit.AnnotationElem
import soot.tagkit.AnnotationEnumElem

@Suppress("ClassName")
object csrf : PreAnalysisUnit() {
    context (PreAnalysisApi)
    override suspend fun config() {
        atAnyInvoke {
            /*
             * security.and().csrf().disable(); // $CWE-352
             * invokevirtual Method org/springframework/security/config/annotation/web/builders/HttpSecurity.csrf:()Lorg/springframework/security/config/annotation/web/configurers/CsrfConfigurer;
             * invokevirtual Method org/springframework/security/config/annotation/web/configurers/CsrfConfigurer.disable:()Lorg/springframework/security/config/annotation/web/HttpSecurityBuilder;
             */
            if (callee.declaringClass.name == "org.springframework.security.config.annotation.web.configurers.CsrfConfigurer" && callee.name == "disable") {
                report(CsrfChecker.SpringCsrfProtectionDisabled)
            }
        }

        atAnyMethod {
            // If the method is not annotated with `@RequestMapping`, there is no vulnerability.
            val annotationTag =
                visibilityAnnotationTag?.annotations?.firstOrNull { it.type == "Lorg/springframework/web/bind/annotation/RequestMapping;" }
                    ?: return@atAnyMethod

            // If the `@RequestMapping` annotation is used without the `method` annotation attribute,
            // there is a vulnerability.
            val methodElement = annotationTag.elems.firstOrNull { it.name == "method" } as? AnnotationArrayElem
            if (methodElement == null || isVulnerable(methodElement.values)) {
                report(CsrfChecker.SpringCsrfUnrestrictedRequestMapping)
            }
        }
    }

    private fun isVulnerable(values: ArrayList<AnnotationElem>): Boolean {
        // If the `@RequestMapping` annotation is used with the `method` annotation attribute equal to `{}`,
        // there is a vulnerability.
        if (values.isEmpty())
            return true

        // If the `@RequestMapping` annotation is used with the `method` annotation attribute but contains a mix of
        // unprotected and protected HTTP request methods, there is a vulnerability.

        // There cannot be a mix if there is no more than one element.
        if (values.size <= 1)
            return false

        // Return `true` as soon as we find at least one unprotected and at least one protected HTTP request method.
        var atLeastOneUnprotected = false
        var atLeastOneProtected = false
        for (elementValue in values) {
            if (elementValue is AnnotationEnumElem) {
                if (elementValue.constantName in setOf("GET", "HEAD", "TRACE", "OPTIONS"))
                    atLeastOneUnprotected = true
                else
                    atLeastOneProtected = true
            }
            if (atLeastOneUnprotected && atLeastOneProtected) {
                return true
            }
        }

        return false
    }
}
