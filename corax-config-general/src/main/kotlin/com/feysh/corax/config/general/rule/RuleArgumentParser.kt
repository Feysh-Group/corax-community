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

package com.feysh.corax.config.general.rule

import com.feysh.corax.config.api.*
import com.feysh.corax.config.general.model.type.TypeHandler
import mu.KotlinLogging
import soot.G
import soot.Type
import java.util.*


object RuleArgumentParser {
    private val argRangeRegex = "(?<start>-?(\\d+)|global|this)..(?<end>-?(\\d+)|this)".toRegex()

    context (ISootMethodDecl.CheckBuilder<*>)
    private fun addAccessPath(
        p: ILocalT<Any>,
        type: Type,
        acpStr: String,
        shouldFillingPath: Boolean
    ): List<ILocalT<Any>> {
        val acp = acpStr.split(".").filter { it.isNotEmpty() }
        if (acp.isEmpty()) {
            if (!shouldFillingPath) {
                return listOf(p)
            }
            return when (val hType = TypeHandler.getHandlerType(type)) {
                is TypeHandler.BoxedPrimitiveType -> listOf(p)
                is TypeHandler.CollectionType -> listOf(p.field(Elements))
                is TypeHandler.MapType -> if (hType.isMultiValueMap) listOf(
                    p.field(MapKeys),
                    p.field(MapValues).field(Elements)
                ) else listOf(p.field(MapKeys), p.field(MapValues))

                is TypeHandler.OptionalType -> listOf(p.field(Elements))
                is TypeHandler.OtherClassType -> listOf(p)
                is TypeHandler.PrimitiveType -> listOf(p)
                is TypeHandler.StringType -> listOf(p)
                is TypeHandler.UnknownType -> listOf(p)
            }
        }

        var cur = listOf(p)
        val accessPathFragment = acp.toMutableList()
        while (accessPathFragment.isNotEmpty()) {
            val fragment = accessPathFragment.removeFirst()
            cur = cur.map {
                when (fragment) {
                    "Element", "Elements" -> it.field(Elements)
                    "ArrayElement", "ArrayElements" -> it.field(Elements)
                    "MapKey", "MapKeys" -> it.field(MapKeys)
                    "MapValue", "MapValues" -> it.field(MapValues)
                    // TODO .Parameter[0] not support yet
                    else -> {
                        return emptyList()
                    }
                }
            }
        }
        return cur
    }

    private fun arg2index(argument: MatchGroup?): Int? {
        val arg = argument?.value
        return arg2index(arg)
    }

    private fun arg2index(arg: String?): Int? {
        return when {
            arg?.lowercase(Locale.getDefault()) == "this" -> return -1
            arg?.lowercase(Locale.getDefault()) == "global" -> return -2
            arg?.toIntOrNull() != null -> return arg.toIntOrNull()
            else -> null
        }
    }

    private fun isRange(args: String): IntRange? {
        arg2index(args)?.let { return it..it }
        val argument = argRangeRegex.matchEntire(args)
        if (argument != null) {
            val start = arg2index(argument.groups["start"]) ?: error("invalid syntax $args for $argRangeRegex")
            val end = arg2index(argument.groups["end"]) ?: error("invalid syntax $args for $argRangeRegex")
            return start..end
        }
        return null
    }

    fun parseRange(range: String): List<Int>? {
        isRange(range)?.let { return it.toList() }
        range.split(",").takeIf { it.isNotEmpty() }?.let {
            it.fold(emptyList<Int>()) { acc, arg ->
                val x = isRange(arg)?.toList() ?: return@let null
                acc + x
            }
        }?.let { return it }
        return null
    }

    context (ISootMethodDecl.CheckBuilder<Any>)
    fun parseArg2AccessPaths(argumentsStr: String, shouldFillingPath: Boolean): List<ILocalT<Any>> {
        val arguments = if (argumentsStr.startsWith("Parameter[")) {
            "Argument[" + argumentsStr.substringAfter("[")
        } else {
            argumentsStr
        }
        kotlin.run {
            val method = method
            when {
                arguments.startsWith("ReturnValue") -> {
                    if (`return`.type == G.v().soot_VoidType()) {
                        method.error.warning("yml: $method: void return type can't be access.")
                    }
                    val acp = arguments.substringAfter(".", missingDelimiterValue = "")
                    return addAccessPath(`return`, `return`.type, acp, shouldFillingPath)
                }

                arguments.startsWith("Argument[") -> {
                    val (range, acp) = arguments.substringAfter("Argument[").let {
                        it.substringBefore("]", missingDelimiterValue = "") to it.substringAfter(
                            "]",
                            missingDelimiterValue = ""
                        )
                    }
                    if (range.isEmpty())
                        return@run null
                    val args = parseRange(range) ?: return@run null
                    val errorIndex = mutableListOf<Int>()
                    val argLocals = args.mapNotNull { index ->
                        method.argumentCnt.takeIf { index >= it }?.let {
                            errorIndex.add(index)
                            return@mapNotNull null
                        }
                        parameter(index)
                    }
                    while (errorIndex.isNotEmpty()) {
                        if (method.sootMethod.signature.startsWith("<java.util.Map: java.util.Map of(")) {
                            break
                        }
                        method.error.warning("yml: $method (argument count: ${method.argumentCnt}): arguments${errorIndex} out of index range.")
                        break
                    }

                    if (acp.isNotEmpty() || shouldFillingPath) {
                        return argLocals.flatMap { argument ->
                            addAccessPath(argument, argument.type, acp, shouldFillingPath)
                        }
                    } else {
                        return argLocals
                    }
                }

                arguments.equals("global", ignoreCase = true) -> {
                    val acp = arguments.substringAfter(".", missingDelimiterValue = "")
                    return addAccessPath(global, G.v().soot_VoidType(), acp, shouldFillingPath)
                }

                else -> {
                    null
                }
            }
        }
        method.error.warning("yml: invalid syntax: $arguments")
        return emptyList()
    }

    fun getArgumentListFromString(argumentsStr: String): List<Int> {
        val arguments = if (argumentsStr.startsWith("Parameter[")) {
            "Argument[" + argumentsStr.substringAfter("[")
        } else {
            argumentsStr
        }

        kotlin.run {
            when {
                arguments.startsWith("Argument[") -> {
                    val (range, acp) = arguments.substringAfter("Argument[").let {
                        it.substringBefore("]", missingDelimiterValue = "") to it.substringAfter(
                            "]",
                            missingDelimiterValue = ""
                        )
                    }
                    if (range.isEmpty())
                        return@run null
                    return parseRange(range) ?: return@run
                }

                else -> {}
            }
        }
        logger.warn("yml: invalid syntax: $arguments")
        return emptyList()
    }

    fun parseParameters(str: String) = str.let {
        if (it.isEmpty()) null else {
            val ps = it.removeSurrounding(prefix = "(", suffix = ")")
            if (ps.isEmpty()) emptyList()
            else ps.split(",")
        }
    }

    private val logger = KotlinLogging.logger { }

}