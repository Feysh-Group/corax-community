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

package com.feysh.corax.config.general.model.processor

import com.feysh.corax.config.api.*
import com.feysh.corax.config.api.baseimpl.SootParameter
import com.feysh.corax.config.general.model.taint.TaintModelingConfig
import com.feysh.corax.config.general.rule.RuleArgumentParser
import com.feysh.corax.config.general.utils.isStringType
import mu.KotlinLogging
import java.util.*
import kotlin.collections.LinkedHashSet

interface IPropagate {
    val name: String
    context (ISootMethodDecl.CheckBuilder<Any>)
    fun interpretation(to: String, from: String)

    companion object {
        private val operators = mutableMapOf<String, LinkedHashSet<IPropagate>>()

        fun register(propagator: IPropagate) {
            operators.getOrPut(propagator.name){ LinkedHashSet() }.add(propagator)
        }

        operator fun get(name: String): LinkedHashSet<IPropagate>? = operators[name]
    }

}

object ValuePropagate : IPropagate {
    override val name: String = "value"
    context (ISootMethodDecl.CheckBuilder<Any>)
    override fun interpretation(to: String, from: String) {
        val acpTo = RuleArgumentParser.parseArg2AccessPaths(to, shouldFillingPath = true)
        if (from.lowercase(Locale.getDefault()) == "empty") {
            for (toAcp in acpTo) {
                toAcp.value = anyOf()
            }
        } else {
            val acpFrom = RuleArgumentParser.parseArg2AccessPaths(from, shouldFillingPath = true)

            val pointsTo =
                acpFrom.fold(null as ILocalValue<Any>?) { acc, p -> acc?.anyOr(p.value) ?: p.value } ?: return

            for (toAcp in acpTo) {
                toAcp.value = toAcp.value anyOr pointsTo
            }
        }
    }

}

object TaintPropagate : IPropagate {
    override val name: String = "taint"
    context (ISootMethodDecl.CheckBuilder<Any>)
    override fun interpretation(to: String, from: String) {
        val acpTo = RuleArgumentParser.parseArg2AccessPaths(to, shouldFillingPath = true)
        if (from.lowercase() == "empty") {
            for (toAcp in acpTo) {
                toAcp.taint = emptyTaint
            }
        } else {
            val acpFrom = RuleArgumentParser.parseArg2AccessPaths(from, shouldFillingPath = true)

            val fromTaintSet =
                acpFrom.fold(null as ITaintSet?) { acc, p -> acc?.plus(p.taint) ?: p.taint } ?: return

            for (toAcp in acpTo) {
                toAcp.taint += fromTaintSet
            }
        }
    }
}

object TaintSanitizerPropagate : IPropagate {
    override val name: String = "sanitizer"
    private val logger = KotlinLogging.logger {}
    context (ISootMethodDecl.CheckBuilder<Any>)
    override fun interpretation(to: String, from: String) {
        val acpTo = RuleArgumentParser.parseArg2AccessPaths(to, shouldFillingPath = true)
        val sanitizerMap = TaintModelingConfig.option.sanitizerTaintTypesMap.mapKeys { it.key.lowercase(Locale.getDefault()) }

        val kind = sanitizerMap[from.lowercase(Locale.getDefault())]
        if (kind == null) {
            logger.error { "sanitizer kind of $from is not register in TaintModelingConfig.option.sanitizerTaintTypesMap" }
            return
        }
        for (toAcp in acpTo) {
            toAcp.taint = toAcp.taint - taintOf(kind)
        }
    }
}


object StrFragmentPropagate : IPropagate {
    override val name: String = "str-fragment"
    private val strFragment = CustomAttributeID<String>("str-fragment")

    context (ISootMethodDecl.CheckBuilder<Any>)
    private fun getAttr(l: ILocalT<*>): ILocalValue<String> {
        return if (l is SootParameter<*> && l.type.isStringType) {
            @Suppress("unchecked_cast")
            l.value as ILocalValue<String> anyOr l.attr[strFragment].value
        } else {
            l.attr[strFragment].value
        }
    }

    context (ISootMethodDecl.CheckBuilder<Any>)
    private fun getFromFragments(froms: List<ILocalT<Any>>) = froms.fold(null as ILocalValue<String>?) { acc, p -> acc?.anyOr(getAttr(p)) ?: getAttr(p) }

    context (ISootMethodDecl.CheckBuilder<Any>)
    override fun interpretation(to: String, from: String) {
        val acpTo = RuleArgumentParser.parseArg2AccessPaths(to, shouldFillingPath = true)
        val acpFrom = RuleArgumentParser.parseArg2AccessPaths(from, shouldFillingPath = true)

        val fromStrFragment = getFromFragments(acpFrom) ?: return

        for (toAcp in acpTo) {
            toAcp.attr[strFragment].value = toAcp.attr[strFragment].value anyOr fromStrFragment
        }
    }

}