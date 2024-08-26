package com.feysh.corax.config.tests

import com.feysh.corax.config.api.AIAnalysisUnit
import com.feysh.corax.config.api.CheckerUnit
import com.feysh.corax.config.api.validate.AIAnalysisValidator
import com.feysh.corax.config.community.AnalyzerConfigRegistry
import com.feysh.corax.config.general.model.ConfigCenter
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.junit.Before
import org.junit.Test
import soot.*
import soot.jimple.DynamicInvokeExpr
import soot.jimple.Stmt
import soot.jimple.toolkits.callgraph.Edge
import soot.options.Options
import soot.util.Chain
import java.io.File
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

    private val testClasses = "../corax-config-tests/normal/build/classes/java/main"

    @Before
    fun initSoot() {
        val applicationClasses = getAppClasses(testClasses)
        val libraryClasses = Scene.v().libraryClasses
        val phantomClasses = Scene.v().phantomClasses
        check(applicationClasses.size > 1)
        getAndAddSootMethods(applicationClasses)
        PackManager.v().getPack("wjpp").apply()
        PackManager.v().getPack("cg").apply()

        initInvokeExprMethods()
        Scene.v().orMakeFastHierarchy

        logger.info("applicationClasses: ${applicationClasses.size}. libraryClasses: ${libraryClasses.size}. phantomClasses: ${phantomClasses.size}")
    }

    fun getAndAddSootMethods(applicationClasses: Chain<SootClass>, checkConcrete: Boolean = true): LinkedHashSet<SootMethod> {
        val sootMethods = linkedSetOf<SootMethod>()
        for (appClass in applicationClasses) {
            if (appClass.isPhantom) {
                continue
            }
            for (method in appClass.methods) {
                if (checkConcrete && !method.isConcrete) {
                    continue
                }
                sootMethods.add(method)
                Scene.v().entryPoints.add(method)
            }
        }
        return sootMethods
    }

    fun getAppClasses(classPath: String): Chain<SootClass> {
        Options.v().prepend_classpath()
        Options.v().apply {
//            set_soot_classpath(classPath.toString())
            check(File(classPath).exists()) {
                "需要手动 gradle build 一次 corax-config-tests 模块，或者更改IDEA的Build+Execution+Deployment>Build+Tools>Gradle>using 更改为 gradle"
            }
            set_process_dir(listOf(classPath))
            set_src_prec(Options.src_prec_only_class)
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
            setPhaseOption("jb.sils", "enabled:false")
        }
        println(Scene.v().sootClassPath)
        Scene.v().loadNecessaryClasses()
        return Scene.v().applicationClasses
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
            sources.validate()
            summaries.validate()
            sinks.validate()
            assert(sources.size >= 429){ failedText("sources", sources.size) }
            assert(summaries.size >= 9447){ failedText("summaries", summaries.size) }
            assert(sinks.size >= 30){ failedText("sinks", sinks.size) }
        }
        ConfigCenter.methodAccessPathDataBase.validate()
        val aiCheckerImpl = AIAnalysisValidator()
        runBlocking {
            val allUnits: Set<CheckerUnit> = AnalyzerConfigRegistry.CommunityJavaDefault().units
            allUnits.filterIsInstance<AIAnalysisUnit>()
                .forEach {
                    launch {
                        CheckerUnit.processUnit(aiCheckerImpl, it) { it.config() }
                    }
                }
        }
        aiCheckerImpl.validate()
    }


}
