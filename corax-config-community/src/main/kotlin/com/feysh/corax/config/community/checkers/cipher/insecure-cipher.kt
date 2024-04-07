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

package com.feysh.corax.config.community.checkers.cipher

import com.feysh.corax.config.api.AIAnalysisApi
import com.feysh.corax.config.api.AIAnalysisUnit
import com.feysh.corax.config.api.baseimpl.matchSoot
import com.feysh.corax.config.api.PreAnalysisApi
import com.feysh.corax.config.api.PreAnalysisUnit
import com.feysh.corax.config.community.InsecureCipherChecker
import com.feysh.corax.config.general.model.taint.TaintModelingConfig

@Suppress("ClassName")
object `insecure-cipher` : AIAnalysisUnit() {

    context (AIAnalysisApi)
    override suspend fun config() {
        TaintModelingConfig.applyJsonExtSinksDefault("cipher:transformation") {
            val transformation = it.getString()
            val transformationLower = transformation.toLowerCase()

            check(
                transformationLower.stringEquals("des") or transformationLower.startsWith("des/"),
                InsecureCipherChecker.DesUsage
            )

            check(
                transformationLower.stringEquals("desede") or transformationLower.startsWith("desede/"),
                InsecureCipherChecker.TdesUsage
            )

            check(
                transformationLower.stringEquals("aes") or
                        transformationLower.stringEquals("des") or
                        transformationLower.stringEquals("desede") or
                        transformationLower.startsWith("aes/ecb/") or
                        transformationLower.startsWith("des/ecb/") or
                        transformationLower.startsWith("desede/ecb/"),
                InsecureCipherChecker.EcbMode
            )
        }
    }

}


@Suppress("ClassName")
object `insecure-cipher-api-call` : PreAnalysisUnit() {
    context (PreAnalysisApi)
    override suspend fun config() {
        atInvoke(matchSoot("<com.hazelcast.config.SymmetricEncryptionConfig: void <init>()>")) {
            report(InsecureCipherChecker.HazelcastSymmetricEncryption)
        }
    }
}