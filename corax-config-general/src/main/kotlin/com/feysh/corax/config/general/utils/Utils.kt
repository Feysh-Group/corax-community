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

import com.feysh.corax.config.api.IMethodMatch
import com.feysh.corax.config.api.baseimpl.matchSimpleSig
import com.feysh.corax.config.api.baseimpl.matchSoot
import com.feysh.corax.config.api.utils.typename
import com.feysh.corax.config.general.rule.IMethodSignature
import com.google.common.base.Optional
import mu.KotlinLogging
import org.xml.sax.*
import soot.*
import soot.asm.AsmUtil
import java.io.File
import java.io.IOException
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