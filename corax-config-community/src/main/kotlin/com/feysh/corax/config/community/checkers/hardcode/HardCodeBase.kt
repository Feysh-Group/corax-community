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
import com.feysh.corax.config.general.model.ConfigCenter
import com.feysh.corax.config.general.model.processor.IPropagate
import com.feysh.corax.config.general.model.taint.TaintModelingConfig
import kotlinx.serialization.Serializable


abstract class HardCodeBase : AIAnalysisUnit() {
    @Serializable
    data class SinkDataForCheck(
        val reportType: CheckType,
        val enable: Boolean = true,
    )

    init {
        IPropagate.register(ConstantPropagate)
    }

    context (AIAnalysisApi)
    fun applyRules(kind2Checker: Map<String, SinkDataForCheck>) {
        for ((kind, sink) in kind2Checker) {
            if (!sink.enable) {
                continue
            }

            TaintModelingConfig.applyJsonExtSinks(kind, ConfigCenter.methodAccessPathDataBase) { acp, _, _ ->
                check(
                    ConstantPropagate.isConst(acp),
                    sink.reportType
                )
            }
        }
    }
}