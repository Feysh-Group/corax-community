package com.feysh.corax.config.community.checkers

import com.feysh.corax.config.api.CustomAttributeID
import com.feysh.corax.config.api.Elements
import com.feysh.corax.config.api.AIAnalysisApi
import com.feysh.corax.config.api.AIAnalysisUnit
import com.feysh.corax.config.api.baseimpl.matchSoot
import com.feysh.corax.config.api.PreAnalysisApi
import com.feysh.corax.config.api.PreAnalysisUnit
import com.feysh.corax.config.community.PermissiveCorsChecker
import com.feysh.corax.config.general.checkers.internetControl
import com.feysh.corax.config.general.model.javaee.JavaeeFrameworkConfigs
import soot.tagkit.AnnotationArrayElem
import soot.tagkit.AnnotationStringElem

@Suppress("ClassName")
object `permissive-cors` : AIAnalysisUnit() {

    private val accessControlAllowOrigin = CustomAttributeID<Boolean>("AccessControlAllowOrigin")

    context (AIAnalysisApi)
    override suspend fun config() {
        method(matchSoot("<org.springframework.web.servlet.config.annotation.CorsRegistration: org.springframework.web.servlet.config.annotation.CorsRegistration allowedOrigins(java.lang.String[])>"))
            .modelNoArg {
                check(p0.field(Elements).getString().stringEquals(literal("*")), PermissiveCorsChecker.PermissiveCors)
            }

        listOf(
            matchSoot("<javax.servlet.http.HttpServletResponse: void addHeader(java.lang.String,java.lang.String)>"),
            matchSoot("<javax.servlet.http.HttpServletResponse: void setHeader(java.lang.String,java.lang.String)>"),
            matchSoot("<javax.servlet.http.HttpServletResponseWrapper: void addHeader(java.lang.String,java.lang.String)>"),
            matchSoot("<javax.servlet.http.HttpServletResponseWrapper: void setHeader(java.lang.String,java.lang.String)>"),
        ).forEach { m ->
            method(m).modelNoArg {

                `this`.attr[accessControlAllowOrigin] = `this`.attr[accessControlAllowOrigin].getBoolean() or (
                    p0.getString().toLowerCase().stringEquals(literal("access-control-allow-origin")) and
                        p1.taint.containsAll(taintOf(internetControl))
                )

                // Holds if `header` sets `Access-Control-Allow-Credentials` to `true`. This ensures fair chances of exploitability.
                check(
                    `this`.attr[accessControlAllowOrigin].getBoolean() and
                            p0.getString().toLowerCase().stringEquals(literal("access-control-allow-credentials")) and
                            p1.getString().toLowerCase().stringEquals(literal("true")),
                    PermissiveCorsChecker.PermissiveCors
                )

                check(
                    p0.getString().toLowerCase().stringEquals(literal("access-control-allow-origin")) and
                            p1.getString().stringEquals(literal("*")),
                    PermissiveCorsChecker.PermissiveCors
                )

            }
        }
    }


    object `any-url-request`: PreAnalysisUnit(){
        context (PreAnalysisApi)
        override suspend fun config() {
            atAnyMethod {
                val mapping = visibilityAnnotationTag?.annotations?.filter { it.type in JavaeeFrameworkConfigs.option.REQUEST_MAPPING_ANNOTATION_TYPES } ?: return@atAnyMethod
                for (m in mapping){
                    val valueElement = m.elems.filterIsInstance<AnnotationArrayElem>().filter { element -> element.name == "value" }
                    for (value in valueElement){
                        val anyUrlRequest = value.values.filterIsInstance<AnnotationStringElem>().any { e -> e.value == "*" }
                        if (!anyUrlRequest) continue
                        report(PermissiveCorsChecker.PermissiveCors)
                    }
                }
            }
        }
    }

}

