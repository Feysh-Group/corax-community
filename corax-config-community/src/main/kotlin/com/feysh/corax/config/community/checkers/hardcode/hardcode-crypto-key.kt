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

import com.feysh.corax.config.api.AIAnalysisApi
import com.feysh.corax.config.api.SAOptions
import com.feysh.corax.config.community.HardcodeKeyChecker
import kotlinx.serialization.Serializable

// tracking a hard-coded crypto key in a call to a sensitive Java crypto API which may compromise security
// has tests
@Suppress("ClassName")
object `hardcode-crypto-key` : HardCodeBase() {

    @Serializable
    class Options : SAOptions {
        val kind2Checker: Map<String, SinkDataForCheck> = mapOf(
            "hardcode:crypto-key" to SinkDataForCheck(HardcodeKeyChecker.HardCodeKey),
        )
    }


    private var option: Options = Options()
    context (AIAnalysisApi)
    override suspend fun config() {
        applyRules(option.kind2Checker)
    }

}