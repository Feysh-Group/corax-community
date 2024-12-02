package com.feysh.corax.config.community.checkers

import com.feysh.corax.cache.AnalysisCache
import com.feysh.corax.cache.analysis.SootMethodExtend
import com.feysh.corax.config.api.*
import com.feysh.corax.config.api.baseimpl.SootParameter
import com.feysh.corax.config.api.baseimpl.matchSimpleSig
import com.feysh.corax.config.api.utils.possibleTypes
import com.feysh.corax.config.api.utils.typename
import com.feysh.corax.config.community.PathTraversalChecker
import com.feysh.corax.config.general.checkers.GeneralTaintTypes
import com.feysh.corax.config.general.checkers.untrusted
import com.feysh.corax.config.general.model.ConfigCenter
import com.feysh.corax.config.general.model.javaee.JavaeeFrameworkConfigs
import com.feysh.corax.config.general.rule.RuleArgumentParser
import com.feysh.corax.config.general.utils.isInstanceOf
import com.feysh.corax.config.general.utils.methodMatch
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.type.ClassOrInterfaceType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import soot.RefType
import soot.Scene
import kotlin.jvm.optionals.getOrNull

/**
 * String source = getParameter("filename") // upload source
 * File f = new File(DIR, source);          // path-injection
 * stream = getInputStream(f)               // path-to-read
 * IOUtils.copy(inputStream, response.getOutputStream()); // ArbitraryFileLeak
 */

@Suppress("ClassName")
object `arbitrary-file-access` : AIAnalysisUnit() {

    @Serializable
    class Options : SAOptions {
        @SerialName("file_path_class_types")
        val filePathClassTypes = listOf(
            "java.io.File",
            "java.nio.file.Path",
            "java.net.URI",
            "java.net.URL",
        )
    }

    var options: Options = Options()

    context(AIAnalysisApi)
    override suspend fun config() {
        val contentWriteFlowRules = ConfigCenter.methodMultiAccessPathDataBase.getRulesByGroupKinds("content-write:flow")

        for (sink in contentWriteFlowRules) {
            method(sink.methodMatch).sootDecl.forEach { decl ->

                decl.modelNoArg {
                    val toArg = RuleArgumentParser.parseArg2AccessPaths(sink.args[0], shouldFillingPath = true)
                    val fromArg = RuleArgumentParser.parseArg2AccessPaths(sink.args[1], shouldFillingPath = true)

                    for (to in toArg) {
                        for (from in fromArg) {
                            val fromTaintCheck = if (from is SootParameter && from.type.toString() in options.filePathClassTypes) {
                                untrusted + GeneralTaintTypes.CONTAINS_PATH_TRAVERSAL
                            } else {
                                untrusted + GeneralTaintTypes.CONTAINS_PATH_TRAVERSAL + GeneralTaintTypes.FileStreamData
                            }
                            check(to.taint.containsAll(taintOf(GeneralTaintTypes.SERVLET_OUTPUT_STREAM)) and
                                    from.taint.containsAll(taintOf(fromTaintCheck)),
                                PathTraversalChecker.ArbitraryFileLeak
                            )
// FIXME
//                          check(
//                              to.taint.containsAll(taintOf(untrusted + GeneralTaintTypes.CONTAINS_PATH_TRAVERSAL)) and
//                                  from.taint.containsAll(taintOf(untrusted)),
//                              PathTraversalChecker.ArbitraryFileReceive
//                          )
                        }
                    }
                }
            }
        }

        /**
         * public ResponseEntity<byte[]> downloadExcel(String resourceLocation) throws Exception
         */

        val method2entityInvokeExpr = with(preAnalysis) {
            atInvoke(matchSimpleSig("javax.ws.rs.core.Response\$ResponseBuilder: * entity(*,**)")) {
                invokeExpr?.let {
                    container to it
                }
            }
        }.await().filterNotNull().groupBy { it.first }.mapValues { entry -> entry.value.map { it.second } }

        val inputStream: RefType? = Scene.v().getRefTypeUnsafe("java.io.InputStream")
        eachMethod {
            val visibilityAnnotationTag = visibilityAnnotationTag ?: return@eachMethod
            if (!visibilityAnnotationTag.hasAnnotations()) {
                return@eachMethod
            }
            for (annotation in visibilityAnnotationTag.annotations) {
                when (annotation.type) {
                    in JavaeeFrameworkConfigs.option.REQUEST_MAPPING_ANNOTATION_TYPES -> {
                        when (sootMethod.returnType.typename) {
                            null -> {}
                            "org.springframework.http.ResponseEntity",
                            "org.springframework.http.HttpEntity"-> {
                                checkResponseEntity()
                            }
                            "org.springframework.core.io.Resource",
                            "org.springframework.core.io.FileSystemResource",
                            "org.springframework.core.io.AbstractResource" -> {
                                checkResource()
                            }
                            "javax.ws.rs.core.Response" -> { // FIXME: 启发式类型判断，可能漏报
                                val entityInvokes = method2entityInvokeExpr[sootMethod]
                                if (entityInvokes != null && inputStream != null && entityInvokes.any { expr ->
                                    expr.getArg(0).possibleTypes.any { it.isInstanceOf(inputStream) } }
                                ) {
                                    checkResource()
                                }
                            }
                            // TODO:
                        }
                    }
                }
            }
        }
    }

    context(AIAnalysisApi, ISootMethodDecl<Any>)
    private fun checkResponseEntity() {
        val smExt = AnalysisCache.G.sootHost2decl(sootMethod) as SootMethodExtend?
        val smDecl = smExt?.decl as? MethodDeclaration?
        val returnType = smDecl?.type as? ClassOrInterfaceType?
        val returnTypeTypeArguments = returnType?.typeArguments?.getOrNull()
        if (returnTypeTypeArguments != null && returnTypeTypeArguments.none {
            it.toString() == "byte[]" || it.toString().endsWith("Resource")
        }) {
            return
        }

        checkResource()
    }



    context(AIAnalysisApi, ISootMethodDecl<Any>)
    private fun checkResource() {
        modelNoArg(config = { at = MethodConfig.CheckCall.PostCallInCallee }) {
            check(
                `return`.taint.containsAll(taintOf(untrusted + GeneralTaintTypes.CONTAINS_PATH_TRAVERSAL)),
                PathTraversalChecker.ArbitraryFileLeak
            )
        }
    }
}