package com.feysh.corax.config.tests

import TestClassAnchorPoint
import com.feysh.corax.config.api.IAnalyzerConfigManager
import com.feysh.corax.config.api.utils.ClassCommons
import com.feysh.corax.config.api.validate.AnalyzerConfigValidator
import com.feysh.corax.config.community.AnalyzerConfigRegistry
import com.feysh.corax.config.general.model.ConfigCenter
import mu.KotlinLogging
import org.junit.Before
import org.junit.Test
import soot.MethodOrMethodContext
import soot.PackManager
import soot.Scene
import soot.UnitPatchingChain
import soot.jimple.DynamicInvokeExpr
import soot.jimple.Stmt
import soot.jimple.toolkits.callgraph.Edge
import soot.options.Options
import java.nio.file.Path
import kotlin.io.path.Path


@Suppress("unused")
fun locateBytecode(name: String): Path {
    return Path("../corax-config-tests/src/main/resources/bytecode_samples", name)
}


fun initInvokeExprMethods() {
    val cg = Scene.v().callGraph
    val reachableMethods = Scene.v().reachableMethods
    reachableMethods.update()

    val listener: Iterator<MethodOrMethodContext> = reachableMethods.listener()
    while (listener.hasNext()) {
        val src = listener.next().method()
        if (src.hasActiveBody()) {
            val units: UnitPatchingChain = src.activeBody.units
            for (u in units) {
                val srcUnit: Stmt = u as Stmt
                if (srcUnit.containsInvokeExpr()) {
                    if (srcUnit.invokeExpr is DynamicInvokeExpr) {
                        return
                    }
                    val tgt = srcUnit.invokeExpr.method
                    cg.addEdge(Edge(src, srcUnit, tgt))
                }
            }
        }
    }
}

class ConfigValidate {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val classPath = ClassCommons.locateAllClass(TestClassAnchorPoint::class.java)
    // val classPath = locateBytecode("scala_ssl_disabler.jar")

    @Before
    fun initSoot() {
        Options.v().prepend_classpath()
        Options.v().apply {
            set_soot_classpath(classPath.toString())
            set_process_dir(listOf(classPath.toString()))
            set_src_prec(Options.src_prec_c)
            set_prepend_classpath(true)
//            set_process_dir(listOf("target/test-classes"))
            set_whole_program(true)
            set_no_bodies_for_excluded(true)
            set_include_all(false)
            set_allow_phantom_refs(true)
            set_ignore_resolving_levels(true)

            setPhaseOption("cg.spark", "on")
            // enableReflection
            setPhaseOption("cg", "types-for-invoke:true")
        }
        println(Scene.v().sootClassPath)
        Scene.v().loadNecessaryClasses()
        val applicationClasses = Scene.v().applicationClasses
        val libraryClasses = Scene.v().libraryClasses
        val phantomClasses = Scene.v().phantomClasses
        for (appClass in applicationClasses) {
            if (appClass.isPhantom) {
                continue
            }
            for (method in appClass.methods) {
                if (!method.isConcrete) {
                    continue
                }
                Scene.v().entryPoints.add(method)
            }
        }
        PackManager.v().getPack("wjpp").apply()
        PackManager.v().getPack("cg").apply()

        initInvokeExprMethods()
        Scene.v().orMakeFastHierarchy

        logger.info("applicationClasses: ${applicationClasses.size}. libraryClasses: ${libraryClasses.size}. phantomClasses: ${phantomClasses.size}")
    }

    private fun IAnalyzerConfigManager.testConfig() {
        aiCheckerImpl?.eachLocalVariable {
            name
        }
    }

    // entry for validate and debug
    @Test
    fun validate() {
        with(ConfigCenter.option) {
            ruleDirectories.clear()
            ruleDirectories.add("rules")
            ruleDirectories.add("../corax-config-general/rules")
        }
        println(ConfigCenter.taintRulesManager)
        with(ConfigCenter.taintRulesManager) {
            fun failedText(kind: String, actual: Int): String {
                return "The number is too small ($kind size: $actual) to meet the assertion. Please adjust the limitation here accordingly or ensure that there are no omissions."
            }
            assert(sources.size >= 429){ failedText("sources", sources.size) }
            assert(summaries.size >= 9447){ failedText("summaries", summaries.size) }
            assert(sinks.size >= 30){ failedText("sinks", sinks.size) }
        }
        with(AnalyzerConfigValidator()) {
            with(AnalyzerConfigRegistry.CommunityJavaDefault) {
                configCallBack()
            }
            testConfig()
            validate()
//            assert(this.modelConfig.errors.isEmpty()) { "detected ${this.modelConfig.errors.size} errors, abort" }
        }
    }


}
