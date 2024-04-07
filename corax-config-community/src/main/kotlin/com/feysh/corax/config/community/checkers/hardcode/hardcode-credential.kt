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

package com.feysh.corax.config.community.checkers.hardcode

import com.feysh.corax.config.api.AIAnalysisApi
import com.feysh.corax.config.api.SAOptions
import com.feysh.corax.config.community.HardcodeCredentialChecker
import kotlinx.serialization.Serializable

// tracking a hard-coded credential in a call to a sensitive Java API which may compromise security
// has tests


@Suppress("ClassName")
object `hardcode-credential` : HardCodeBase() {

    context (AIAnalysisApi)
    private fun checkPasswordFieldAssignedHardcodedValue() {
        // TODO
    }


    context (AIAnalysisApi)
    private fun hardcodedCredentialComparison() {
        // TODO
    }


    context (AIAnalysisApi)
    private fun hardcodedCredentialsSourceCall() {
        // TODO
    }

    @Serializable
    class Options : SAOptions {
        val kind2Checker: Map<String, SinkDataForCheck> = mapOf(
            "hardcode:credential:username" to SinkDataForCheck(HardcodeCredentialChecker.HardCodeUserName),
            "hardcode:credential:password" to SinkDataForCheck(HardcodeCredentialChecker.HardCodePassword),
            "hardcode:credential:other" to SinkDataForCheck(HardcodeCredentialChecker.HardCodeOthers),
        )
    }


    private var option: Options = Options()



    context (AIAnalysisApi)
    override suspend fun config() {
        applyRules(option.kind2Checker)
        hardcodedCredentialsSourceCall()
        hardcodedCredentialComparison()
        checkPasswordFieldAssignedHardcodedValue()
    }
}