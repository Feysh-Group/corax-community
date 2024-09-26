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

@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.feysh.corax.config.general.utils

import com.feysh.corax.config.api.BugMessage.Env
import com.feysh.corax.config.api.IMethodMatch
import com.feysh.corax.config.api.Language
import com.feysh.corax.config.api.baseimpl.RawSignatureMatch
import com.feysh.corax.config.api.baseimpl.SootSignatureMatch
import com.feysh.corax.config.api.baseimpl.matchSimpleSig
import com.feysh.corax.config.api.baseimpl.matchSoot
import com.feysh.corax.config.api.report.Region
import com.feysh.corax.config.api.utils.typename
import com.feysh.corax.config.general.checkers.analysis.LibVersionProvider
import com.feysh.corax.config.general.model.taint.TaintRule
import com.feysh.corax.config.general.rule.IMethodAccessPath
import com.feysh.corax.config.general.rule.IMethodSignature
import com.google.common.base.Optional
import mu.KotlinLogging
import soot.*
import soot.asm.AsmUtil
import java.io.File
import java.io.IOException
import java.net.URI
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.*

// Hint: 自定义配置插件和商业版插件混合使用时, 可以修改定义, 但不可删改声明, 否则会出现商业版插件冲突崩溃。注: 单独使用社区版插件可以任意修改

private val logger = KotlinLogging.logger {}
fun Any.enumerate(e: (Int) -> Unit) {
    when(this){
        is Iterable<*> -> this.forEach { e(it as Int) }
        is Int -> e(this)
        is Long -> e(this.toInt())
    }
}


val primTypesBoxedQuotedString get() = setOf(
    "java.lang.Byte",
    "java.lang.Short",
    "java.lang.Integer",
    "java.lang.Long",
    "java.lang.Float",
    "java.lang.Double",
    "java.lang.Boolean",
    "java.lang.Character",
)

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

inline val Type.isBoxedPrimitives: Boolean
    get() = primTypesBoxed.contains(this)
inline val Type.isPrimitives: Boolean
    get() = this is PrimType
inline val stringType: RefType get() = Scene.v().getRefType("java.lang.String")
inline val charSequenceType: RefType get() = Scene.v().getRefType("java.lang.CharSequence")

inline val Type.isStringType: Boolean
    get() = this.typename.let { name -> name == "java.lang.String" || name == "java.lang.CharSequence" }

inline val Type.isVoidType: Boolean get() = this is VoidType || (this is RefType && this.className.substringAfterLast(".") == "Void")

inline val Type.isByteArray: Boolean
    get() = this == G.v().soot_ByteType().arrayType

inline val Type.isCharArray: Boolean
    get() = this == G.v().soot_CharType().arrayType

inline val Type.isIntArray: Boolean
    get() = this == G.v().soot_IntType().arrayType

inline val Type.isLongArray: Boolean
    get() = this == G.v().soot_LongType().arrayType

inline val Type.isStringArray: Boolean
    get() = this == stringType.arrayType || this == charSequenceType.arrayType

fun Type.isInstanceOf(parents: Collection<Type>): Boolean  {
    if (this is RefLikeType) {
        val hierarchy = Scene.v().orMakeFastHierarchy
        return parents.any { hierarchy.canStoreType(this, it) }
    }
    return false
}

fun Type.isInstanceOf(parent: Type): Boolean  {
    if (this is RefLikeType) {
        val hierarchy = Scene.v().orMakeFastHierarchy
        return hierarchy.canStoreType(this, parent)
    }
    return false
}

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

fun isVoidReturnTypeOf(methodMatch: IMethodMatch) : Boolean {
    return when (methodMatch) {
        is RawSignatureMatch -> {
            methodMatch.name.any { it == SootMethod.constructorName } || methodMatch.returnType?.lowercase() == "void"
        }
        is SootSignatureMatch -> {
            val sm = methodMatch.sm
            sm.name == SootMethod.constructorName || sm.returnType.lowercase() == "void"
        }
        else -> false
    }
}

fun IMethodAccessPath.checkArg(methodMatch: IMethodMatch, sig: String): Boolean {
    if (isVoidReturnTypeOf(methodMatch) && this.arg.lowercase().contains("returnvalue"))
        error("it's not allowed to assign 'ReturnValue' to 'arg' if the return type is 'void'.The sig is:$sig")
    return true
}

fun TaintRule.Summary.checkFromTo(methodMatch: IMethodMatch, sig: String): Boolean {
    if (isVoidReturnTypeOf(methodMatch) && (this.from.lowercase().contains("returnvalue") || this.to.lowercase().contains("returnvalue")))
        error("it's not allowed to assign 'ReturnValue' to 'from' or 'to' if the return type is 'void'.The sig is:$sig")
    return true
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

inline val URI.isFileScheme: Boolean get() = scheme == "file"

fun Env.appendPathEvents(versionCheckResult: LibVersionProvider.VersionCheckResult) {
    val groupedDependencies = versionCheckResult.dependencies.sortedBy { it.toSortString() }
        .groupBy { it.libraryDescriptor.toString() }
    groupedDependencies.forEach { (libraryDescriptor, dependencies) ->
        val condition = versionCheckResult.condition
        val conditionMessage = "$libraryDescriptor ${condition.op.code} ${condition.libraryDescriptor.version}"
        val locationMessage = dependencies.joinToString { it.shortLocation }

        val message = mapOf(
            Language.EN to "Vulnerability library version condition: $conditionMessage, location: $locationMessage",
            Language.ZH to "漏洞库版本判断条件: $conditionMessage , 位置: $locationMessage"
        )

        val fileLoc = dependencies.firstNotNullOfOrNull { dependency ->
            dependency.fileLoc.takeIf { dependency.isFromNormalFile() && it?.second?.valid == true }
        } ?: dependencies.firstNotNullOfOrNull { it.fileLoc }
        if (fileLoc != null) {
            appendPathEvent(
                message = message,
                loc = fileLoc.first,
                region = fileLoc.second?.takeIfValid ?: Region.ERROR,
            )
        }
        val sootLoc = dependencies.firstNotNullOfOrNull { dependency ->
            dependency.sootLoc.takeIf { dependency.isFromNormalFile() && it?.second?.valid == true }
        } ?: dependencies.firstNotNullOfOrNull { it.sootLoc }
        if (sootLoc != null) {
            appendPathEvent(
                message = message,
                loc = sootLoc.first,
                region = sootLoc.second?.takeIfValid,
            )
        }
    }
}

val <E> List<E>.removeAdjacentDuplicates: List<E>
    get() = if (this.isEmpty()) this else (this.zipWithNext().filter { it.first != it.second }
        .map { it.first } + this.last())