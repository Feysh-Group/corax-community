package com.feysh.corax.config.general.model.javaee

import com.feysh.corax.config.api.*
import com.feysh.corax.config.api.baseimpl.matchSoot
import com.feysh.corax.config.general.checkers.internetSource
import com.feysh.corax.config.general.model.ConfigCenter
import com.feysh.corax.config.general.rule.RuleArgumentParser
import com.feysh.corax.config.general.utils.primTypesBoxedQuotedString
import kotlinx.serialization.Serializable
import soot.ArrayType
import soot.G
import soot.PrimType
import soot.RefLikeType
import soot.RefType
import soot.SootClass
import soot.tagkit.VisibilityParameterAnnotationTag


object JavaeeAnnotationSource : AIAnalysisUnit() {

    @Serializable
    class Options : SAOptions {
        val excludeRequestBodyTypes: Set<String> = setOf(
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

    }

    var option: Options = Options()

    private fun addParamSource(
        dtoClasses: MutableSet<SootClass>,
        method: ISootMethodDecl<Any>,
        i: Int
    ) {

        method.modelNoArg(config = { at = MethodConfig.CheckCall.PrevCallInCallee }) {
            val param = parameter(i)
            val paramType = method.sootMethod.getParameterType(i)
            val sources = RuleArgumentParser.fillingPath(param, paramType)
            val dataType = when (paramType) {
                is ArrayType -> paramType.elementType
                is PrimType -> paramType
                is RefLikeType -> paramType
                else -> return@modelNoArg
            }
            if (ConfigCenter.skipTaintPrimitiveType(dataType)) {
                return@modelNoArg
            }

//            with(method) {
//                val annotations = param.annotationTag
//                val annotationTypes = annotations.map { it.type }
//                if (option.requestBodyAnnotationWhiteList.isNotEmpty() &&
//                    option.requestBodyAnnotationWhiteList.intersect(annotationTypes).isEmpty()){
//                    return@modelNoArg
//                }
//            }

            for (source in sources) {
                val refType = dataType as? RefType
                if (refType != null) {
                    val refTypeQuoted = refType.toQuotedString()
                    val refTypeClazz = refType.sootClass
                    if (refTypeQuoted == "java.lang.String" || refTypeQuoted in primTypesBoxedQuotedString) {
                        source.taint += taintOf(internetSource)
                        continue
                    }

                    if (source is IAccessPathT) {
                        source.taint += taintOf(internetSource)
                        continue
                    }

                    if (refTypeQuoted in option.excludeRequestBodyTypes) {
                        continue
                    }

                    // taint in: taint DTO this
                    source.taint += taintOf(internetSource)

                    // taint in: all the class declaring fields
                    source.subFields.taint += taintOf(internetSource)


                    if (refTypeClazz.isJavaLibraryClass ||
                        option.requestParameterTypePrefixBlackList.any { refTypeClazz.name.startsWith(it) }
                    ) {
                        continue
                    }
                    // Serializable DTO Class
                    // taint out: taint DTO method return value from this
                    dtoClasses.add(refTypeClazz)
                } else {
                    source.taint += taintOf(internetSource)
                }
            }
        }
    }

    context (api@AIAnalysisApi)
    override fun config() {
        val taintParameters = mutableMapOf<ISootMethodDecl<Any>, MutableSet<Int>>()
        eachMethod {
            val visibilityAnnotationTag = visibilityAnnotationTag ?: return@eachMethod
            if (!visibilityAnnotationTag.hasAnnotations()) {
                return@eachMethod
            }
            for (annotation in visibilityAnnotationTag.annotations) {
                when (annotation.type) {
                    in JavaeeFrameworkConfigs.option.REQUEST_MAPPING_ANNOTATION_TYPES -> {
                        for (parameterIndex in 0 until sootMethod.parameterCount) {
                            taintParameters.getOrPut(this) { mutableSetOf() }.add(parameterIndex)
                        }
                    }
                }
            }

            for (paramTag in tags.filterIsInstance<VisibilityParameterAnnotationTag>()) {
                paramTag.visibilityAnnotations.forEachIndexed { parameterIndex, tag ->
                    if (tag != null && tag.annotations.any { it.type in JavaeeFrameworkConfigs.option.REQUEST_PARAM_ANNOTATION_TYPES }) {
                        taintParameters.getOrPut(this) { mutableSetOf() }.add(parameterIndex)
                    }
                }
            }

        }
        afterConfig {
            val dtoClasses = mutableSetOf<SootClass>()
            for ((m, parameters) in taintParameters) {
                for (taintParameter in parameters) {
                    addParamSource(dtoClasses, m, taintParameter)
                }
            }

            for (dto in dtoClasses) {
                for (m in dto.methods) {
                    // auto modeling getter and setter
                    if (m.name.startsWith("set")) {
                        method(matchSoot(m.signature)).modelNoArgSoot {
                            when {
                                m.isStatic || m.isStaticInitializer || m.isSynchronized -> {}
                                m.isJavaLibraryMethod -> {}
                                else -> {
                                    val from = method.sootMethod.parameterTypes.withIndex().flatMap { (i, type) ->
                                        if (ConfigCenter.skipTaintPrimitiveType(type)) {
                                            return@flatMap emptyList()
                                        }
                                        RuleArgumentParser.fillingPath(parameter(i), type)
                                    }
                                    val taintTypes = from.fold(null as ITaintSet?) { acc, argFrom ->
                                        acc?.let { it + argFrom.taint } ?: argFrom.taint
                                    }
                                    if (taintTypes != null) {
                                        `this`.taint += taintTypes
                                    }
                                }
                            }
                        }
                        continue
                    } else if (m.name.startsWith("get")) {
                        if (ConfigCenter.skipTaintPrimitiveType(m.returnType) || m.returnType == G.v()
                                .soot_VoidType()
                        ) {
                            continue
                        }
                        method(matchSoot(m.signature)).modelNoArgSoot {
                            when {
                                m.isStatic || m.isStaticInitializer || m.isSynchronized -> {}
                                m.isJavaLibraryMethod -> {}
                                else -> {
                                    for (to in RuleArgumentParser.fillingPath(`return`, `return`.type)) {
                                        to.taint += `this`.taint
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
