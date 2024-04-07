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

package com.feysh.corax.config.community.checkers.hardcode

import com.feysh.corax.config.api.*
import com.feysh.corax.config.general.model.processor.IPropagate
import com.feysh.corax.config.general.rule.RuleArgumentParser
import kotlinx.serialization.Serializable

object ConstantPropagate : AIAnalysisUnit(), IPropagate {
    override val name: String = "taint"

    private val isConstant = CustomAttributeID<Boolean>("isConstant")

    @Serializable
    class Options : SAOptions {
        val enableConstantPropagate: Boolean = true
    }

    var options: Options = Options()
    context (IMethodDecl.CheckBuilder<Any>)
    fun isConst(p: ILocalT<*>): IBoolExpr {
        return p.isConstant.or(p.attr[isConstant].getBoolean())
    }

    context (ISootMethodDecl.CheckBuilder<Any>)
    override fun interpretation(to: String, from: String) {
        if (!options.enableConstantPropagate) return
        if (from.lowercase() == "empty") {
            return
        }
        val acpTo = RuleArgumentParser.parseArg2AccessPaths(to, shouldFillingPath = true)
        val acpFrom = RuleArgumentParser.parseArg2AccessPaths(from, shouldFillingPath = true)
        val fromIsConst =
            acpFrom.fold(null as IBoolExpr?) { acc, p -> acc?.or(isConst(p)) ?: isConst(p) } ?: return
        for (toAcp in acpTo) {
            toAcp.attr[isConstant] = fromIsConst
        }
    }

    context(AIAnalysisApi) override suspend fun config() { }

}