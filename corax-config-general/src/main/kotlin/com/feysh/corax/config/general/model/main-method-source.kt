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

@file:Suppress("ClassName")

package com.feysh.corax.config.general.model

import com.feysh.corax.config.api.Elements
import com.feysh.corax.config.api.AIAnalysisApi
import com.feysh.corax.config.api.AIAnalysisUnit
import com.feysh.corax.config.api.MethodConfig
import com.feysh.corax.config.general.checkers.userInputSource


object `main-method-source` : AIAnalysisUnit() {
    context (AIAnalysisApi)
    override suspend fun config() {
        eachMethod {
            if (sootMethod.subSignature != "void main(java.lang.String[])") {
                return@eachMethod
            }
            this.modelNoArg(config = { at = MethodConfig.CheckCall.PrevCallInCallee}) {
                p0.field(Elements).taint += taintOf(userInputSource)
            }
        }
    }
}