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

package com.feysh.corax.config.general.model.javaee

import com.feysh.corax.config.api.*
import com.feysh.corax.config.api.baseimpl.matchSoot
import com.feysh.corax.config.api.utils.superClasses
import com.feysh.corax.config.general.checkers.GeneralTaintTypes
import com.feysh.corax.config.general.checkers.internetSource
import com.feysh.corax.config.general.common.collect.Maps
import com.feysh.corax.config.general.common.collect.MultiMap
import com.feysh.corax.config.general.model.ConfigCenter
import com.feysh.corax.config.general.model.type.HandlerTypeVisitorInTaint
import com.feysh.corax.config.general.model.type.TypeHandler
import kotlinx.serialization.Serializable
import soot.*
import soot.tagkit.VisibilityParameterAnnotationTag


internal enum class WebFramework {
    SPRING, JAVAX_WS_RS
}

object JavaeeAnnotationSource : AIAnalysisUnit() {

    @Serializable
    class Options : SAOptions {
        val excludeModelingJavaWebModelClassTypes: Set<String> = setOf(
            "javax.servlet.ServletRequest",
            "javax.servlet.ServletRequestWrapper",
            "javax.servlet.http.HttpServletRequest",
            "javax.servlet.http.HttpServletRequestWrapper",
            "javax.servlet.ServletResponse",
            "javax.servlet.ServletResponseWrapper",
            "javax.servlet.http.HttpServletResponse",
            "javax.servlet.http.HttpServletResponseWrapper",
            "org.springframework.web.multipart.MultipartFile",
            "org.springframework.http.ResponseEntity",
            "org.springframework.http.HttpEntity",
            "org.springframework.web.servlet.mvc.support.RedirectAttributes"
        )

//        val requestBodyAnnotationWhiteList = setOf(
//            "org.springframework.web.bind.annotation.RequestBody"
//        )

        val requestParameterTypePrefixBlackList = setOf(
            "java.", "javax."
        )
        val sourceKindByParamType = mapOf(
            "java.io.InputStream" to listOf(GeneralTaintTypes.FILE_UPLOAD_SOURCE),
            "com.sun.jersey.core.header.FormDataContentDisposition" to listOf(GeneralTaintTypes.FILE_UPLOAD_SOURCE),
        )
    }

    var option: Options = Options()

    context (api@PreAnalysisApi)
    private fun parseWebControllerMappingMethods(): PreAnalysisApi.Result<Pair<WebFramework, SootMethod>> {
        return atAnyMethod(config = { incrementalAnalyze = false; ignoreProjectConfigProcessFilter = true }) {
            val visibilityAnnotationTag = visibilityAnnotationTag
            if (visibilityAnnotationTag != null) {
                if (!visibilityAnnotationTag.hasAnnotations()) {
                    return@atAnyMethod null
                }

                for (annotation in visibilityAnnotationTag.annotations) {
                    when (annotation.type) {
                        in JavaeeFrameworkConfigs.option.REQUEST_MAPPING_ANNOTATION_TYPES_SPRING -> {
                            return@atAnyMethod WebFramework.SPRING to sootMethod
                        }

                        in JavaeeFrameworkConfigs.option.REQUEST_MAPPING_ANNOTATION_JAVAX_WS_RS -> {
                            return@atAnyMethod WebFramework.JAVAX_WS_RS to sootMethod
                        }
                    }
                }
            }

            val tags = sootMethod.tags
            for (paramTag in tags.filterIsInstance<VisibilityParameterAnnotationTag>()) {
                for (paramAnnotation in paramTag.visibilityAnnotations) {
                    if (paramAnnotation == null) continue
                    if (paramAnnotation.annotations.any { it.type in JavaeeFrameworkConfigs.option.REQUEST_PARAM_ANNOTATION_TYPES_SPRING }) {
                        return@atAnyMethod WebFramework.SPRING to sootMethod
                    }
                    if (paramAnnotation.annotations.any { it.type in JavaeeFrameworkConfigs.option.REQUEST_PARAM_ANNOTATION_TYPES_JAVAX_WS_RS }) {
                        return@atAnyMethod WebFramework.JAVAX_WS_RS to sootMethod
                    }
                }
            }
            return@atAnyMethod null
        }.nonNull()
    }

    fun isWebModelClassType(t: TypeHandler.OtherClassType): Boolean {
        // the black list
        if (t.type.className in option.excludeModelingJavaWebModelClassTypes) {
            return false
        }
        return true
    }


    context (AIAnalysisApi)
    @Suppress("UNUSED_PARAMETER")
    private fun taintWebRequestMappingHandlerParameters(
        framework: WebFramework,
        requestMappingHandler: SootMethod,
        sourceKindByParamType: MultiMap<Type, ITaintType>
    ) {
        val hTypes = TypeHandler.getHandlerType(requestMappingHandler)
        for ((i, hType) in hTypes) {
            method(matchSoot(requestMappingHandler.signature))
                .modelNoArg(config = { at = MethodConfig.CheckCall.PrevCallInCallee }) {
                    val p = parameter(i)
                    val visitor = object : HandlerTypeVisitorInTaint(this) {

                        var recursiveTaintCount: Int = 0

                        override fun process(accessPath: ILocalT<*>, paramType: Type) {
                            if (!ConfigCenter.isEnableTaintFlowType(paramType)) {
                                return
                            }
                            // (TAINT IN): add taint source kinds
                            val kinds = sourceKindByParamType.get(paramType)
                            accessPath.taint += taintOf(internetSource + kinds)
                        }

                        override fun visit(v: ILocalT<*>, t: TypeHandler.PrimitiveType) {
                            if (!ConfigCenter.option.taintPrimTypeValue) {
                                return
                            }
                            super.visit(v, t)
                        }

                        override fun visit(v: ILocalT<*>, t: TypeHandler.BoxedPrimitiveType) {
                            if (!ConfigCenter.option.taintPrimTypeValue) {
                                return
                            }
                            return super.visit(v, t)
                        }

                        override fun visit(v: ILocalT<*>, t: TypeHandler.OtherClassType) {
                            if (!isWebModelClassType(t)) {
                                return
                            }
                            // taint object instance base
                            process(v, t.type)
                            // taint all the class declaring fields by subFields
                            process(v.subFields, t.type)
                            // taint all the class declaring fields
                            try {
                                val modelingClasses = t.type.sootClass.superClasses.filter { !it.isJavaLibraryClass }
                                val modelingFields = modelingClasses.flatMap { it.fields }
                                for (field in modelingFields) {
                                    if (++recursiveTaintCount < 100) {
                                        TypeHandler.getHandlerType(field.type).visit(v.field(field), this)
                                    }
                                }
                            } catch (e: Exception) {
                                logger.warn(e) { "Failed to taint subfields" }
                            }
                        }
                    }
                    hType.visit(p, visitor)
                }
        }
    }


    context (bdr@ISootMethodDecl.CheckBuilder<Any>)
    private fun taintModelClassSetterMethods() {
        val m = method.sootMethod
        if (m.isStatic || m.isStaticInitializer || m.isSynchronized) {
            return
        }
        if (m.isJavaLibraryMethod) {
            return
        }

        val hTypes = TypeHandler.getHandlerType(m)
        for ((i, hType) in hTypes) {
            val taintFrom = HandlerTypeVisitorInTaint.TaintFrom(this@bdr).getExpr(parameter(i), hType)
            if (taintFrom != null) {
                `this`.taint += taintFrom
            }
        }
    }

    context (bdr@ISootMethodDecl.CheckBuilder<Any>)
    private fun taintModelClassGetterMethods() {
        val m = method.sootMethod
        if (!ConfigCenter.isEnableTaintFlowType(m.returnType)) {
            return
        }
        if (m.isStatic || m.isStaticInitializer || m.isSynchronized) {
            return
        }
        if (m.isJavaLibraryMethod) {
            return
        }

        val returnHType = TypeHandler.getHandlerType(`return`.type)
        val taintOut = HandlerTypeVisitorInTaint.TaintOut(this@bdr).get(`return`, returnHType)

        for (out in taintOut) {
            out.taint += `this`.taint
        }
    }


    // taint propagate: auto modeling getter and setter
    context (AIAnalysisApi)
    private fun taintModelClassMemberMethods(javaWebModelClassType: RefType) {
        val modelClass = javaWebModelClassType.sootClass
        for (m in modelClass.methods) {
            if (m.name.startsWith("set") && m.name.length > 3) {
                // taint in
                method(matchSoot(m.signature)).modelNoArgSoot {
                    taintModelClassSetterMethods()
                }
                continue
            } else if (m.name.startsWith("get") && m.name.length > 3 && m.parameterCount == 0) {
                // taint out
                method(matchSoot(m.signature)).modelNoArgSoot {
                    taintModelClassGetterMethods()
                }
            }
        }
    }

    context (AIAnalysisApi)
    override suspend fun config() {

        // parse controllers from web framework and configure taint source for parameters of request mapping methods
        val requestMappingHandlers = with(preAnalysis) { parseWebControllerMappingMethods() }

        val sourceKindByParamType = Maps.newMultiMap<Type, ITaintType>()
        for ((type, sourceKind) in option.sourceKindByParamType) {
            Scene.v().getRefTypeUnsafe(type)?.let {
                sourceKindByParamType.putAll(it, sourceKind)
            }
        }
        for ((framework, requestMappingHandler) in requestMappingHandlers.await()) {
            taintWebRequestMappingHandlerParameters(framework, requestMappingHandler, sourceKindByParamType)
        }


        val visitor = object : TypeHandler.Visitor<Unit, TypeHandler.OtherClassType?> {
            override fun visitDefault(v: Unit, t: TypeHandler.HType) = null
            override fun visit(v: Unit,t: TypeHandler.OtherClassType): TypeHandler.OtherClassType? {
                if (isWebModelClassType(t)) {
                    return t
                }
                return null
            }
        }

        val javaWebModelClassTypesAtMappingMethodParam: Set<RefType> =
            requestMappingHandlers.await().flatMapTo(mutableSetOf()) { (_, requestMappingHandler) ->
                val hTypes = TypeHandler.getHandlerType(requestMappingHandler)
                hTypes.mapNotNull { indexedValue ->
                    indexedValue.value.visit(Unit, visitor)?.type
                }
            }

        // (TAINT IN/OUT): add taint propagate between setter and getter
        for (javaWebModelClass in javaWebModelClassTypesAtMappingMethodParam) {
            taintModelClassMemberMethods(javaWebModelClass)
        }
    }
}
