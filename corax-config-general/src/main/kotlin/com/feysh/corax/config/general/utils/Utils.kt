@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.feysh.corax.config.general.utils

import com.feysh.corax.config.api.IMethodMatch
import com.feysh.corax.config.api.baseimpl.matchSimpleSig
import com.feysh.corax.config.api.baseimpl.matchSoot
import com.feysh.corax.config.api.utils.typename
import com.feysh.corax.config.general.rule.IMethodSignature
import com.google.common.base.Optional
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.w3c.dom.Document
import org.xml.sax.InputSource
import soot.*
import soot.asm.AsmUtil
import java.io.CharArrayReader
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.io.path.inputStream

// Hint: 自定义配置插件和商业版插件混合使用时, 可以删改定义, 不可删改声明, 否则会出现商业版插件冲突崩溃。注: 单独使用社区版插件可以任意修改

private val logger = KotlinLogging.logger {}
fun Any.enumerate(e: (Int) -> Unit) {
    when(this){
        is Iterable<*> -> this.forEach { e(it as Int) }
        is Int -> e(this)
        is Long -> e(this.toInt())
    }
}


val collectionClasses = mutableListOf(
    "java.util.List",
    "java.util.Set",
    "java.lang.Iterable",
    "java.util.Collection",
    "java.util.Enumeration",
    "java.util.ArrayList",
    "java.util.Optional"
)
val Type.isCollection: Boolean
    get() = run {
        if (this is ArrayType)
            return@run true
        @Suppress("DuplicatedCode")
        if (this is RefType){
            val hierarchy = Scene.v().orMakeFastHierarchy
            val scene = Scene.v()
            return@run collectionClasses.any { hierarchy.canStoreType(this@isCollection, scene.getOrAddRefType(it)) }
        }
        return@run false
    }

val mapClasses = mutableListOf(
    "java.util.Map",
    "java.util.HashMap",
    "java.util.LinkedHashMap",
    "java.util.TreeMap",
)
val Type.isMap: Boolean
    get() = run {
        if (this is RefType) {
            val hierarchy = Scene.v().orMakeFastHierarchy
            val scene = Scene.v()
            return@run mapClasses.any { hierarchy.canStoreType(this@isMap, scene.getOrAddRefType(it)) }
        }
        return@run false
    }


val primTypes
    get() = setOf(
        ByteType.v(),
        ShortType.v(),
        IntType.v(),
        LongType.v(),
        FloatType.v(),
        DoubleType.v(),
        BooleanType.v(),
        CharType.v()
    )

val primTypesBoxed get() = primTypes.mapTo(mutableSetOf()) { it.boxedType() }

val primTypesBoxedQuotedString get() = primTypesBoxed.mapTo(mutableSetOf()) { it.typename!! }

val RefType.isBoxedPrimitives: Boolean
    get() = primTypesBoxed.contains(this)

val stringType: RefType get() = Scene.v().getRefType("java.lang.String")
val Type.isStringType: Boolean
    get() = this == stringType

val Type.isByteArray: Boolean
    get() = this == G.v().soot_ByteType().arrayType

val Type.isCharArray: Boolean
    get() = this == G.v().soot_CharType().arrayType

val Type.isIntArray: Boolean
    get() = this == G.v().soot_IntType().arrayType

val Type.isLongArray: Boolean
    get() = this == G.v().soot_LongType().arrayType

val Type.isStringArray: Boolean
    get() = this == stringType.arrayType


object Utils {

    val project by lazy {
        Properties().also {
            Thread.currentThread().contextClassLoader.getResourceAsStream("project.properties")?.use { stream ->
                it.load(stream)
            } ?: error("project.properties doesn't exists")
        }
    }

    val isWindows = System.getProperty("os.name").lowercase(Locale.getDefault()).contains("windows")
    val javaExecutableFilePath: String?
        get() {
            // Initialize variables
            val javaExecutablePath: String
            val javaExecutableFile: File?

            // Get Java home directory
            val javaHome = System.getProperty("java.home")

            if (javaHome.isNullOrEmpty()) {
                // Java home directory is not found
                return null
            }


            // Get Java executable file path
            if (isWindows) {
                // Windows operating system
                javaExecutablePath = "$javaHome\\bin\\java.exe"
                javaExecutableFile = File(javaExecutablePath)
                if (!javaExecutableFile.exists()) {
                    // Unable to locate Java executable file
                    return null
                }
            } else {
                // Unix-like operating systems
                javaExecutablePath = "$javaHome/bin/java"
                javaExecutableFile = File(javaExecutablePath)
                if (!javaExecutableFile.exists()) {
                    // Unable to locate Java executable file
                    return null
                }
            }

            return javaExecutablePath
        }

}


val IMethodSignature.methodMatch: IMethodMatch
    get() {
        return if (this.signature.startsWith("<") && this.signature.endsWith(">")) {
            matchSoot(this.signature)
        } else {
            matchSimpleSig(this.signature)
        }
    }


fun walkFiles(jsonDirs: List<Path>, filter: (file: Path) -> Boolean): List<Path> {
    val files: MutableList<Path> = mutableListOf()
    for (jsonDir in jsonDirs) {
        Files.walkFileTree(jsonDir, object : SimpleFileVisitor<Path>() {
            @Throws(IOException::class)
            override fun visitFile(filePath: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (filter(filePath)) {
                    files.add(filePath)
                }
                return FileVisitResult.CONTINUE
            }

            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                return visitFile(dir, attrs)
            }
        })
    }
    return files
}

suspend fun parseXmlSafe(xmlFile: Path): Document? {
    try {
        xmlFile.inputStream().use { stream ->
            val dbFactory = DocumentBuilderFactory.newInstance()
            with(dbFactory) {
                setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
                setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
                setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")
                isValidating = false
                isExpandEntityReferences = false
            }
            val dBuilder = dbFactory.newDocumentBuilder()
            dBuilder.setEntityResolver { _, _ -> InputSource(CharArrayReader(CharArray(0))) }
            val doc = withContext(Dispatchers.IO) {
                try {
                    dBuilder.parse(stream)
                } catch (e: Exception) {
                    logger.warn { "failed to parse xml file $xmlFile, e: ${e.message}" }
                    null
                }
            } ?: return null
            doc.documentElement.normalize()
            return doc
        }

    } catch (e: Exception) {
        logger.warn { "failed to open stream of file $xmlFile. e: ${e.message}" }
        return null
    }

}

/**
 * Converts type descriptor in bytecode to Soot type descriptor.
 * For example:
 *
 *  * `[I` to `int[]`.
 *  * `[[I` to `int[][]`.
 *  * `Ljava/lang/Object;` to `java.lang.Object`.
 *  * `[Ljava/lang/Object;` to `java.lang.Object[]`.
 *
 */
fun classTypeToSootTypeDesc(ty: String): String = AsmUtil.toJimpleDesc(ty, Optional.fromNullable(null)).first().typename!!