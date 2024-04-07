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

package com.feysh.corax.config.community.checkers

import com.feysh.corax.config.api.*
import com.feysh.corax.config.api.PreAnalysisApi
import com.feysh.corax.config.api.PreAnalysisUnit
import com.feysh.corax.config.community.WeakSslChecker
import com.feysh.corax.config.general.model.ConfigCenter
import com.feysh.corax.config.general.model.taint.TaintModelingConfig
import com.feysh.corax.config.general.utils.methodMatch
import kotlinx.serialization.Serializable
import java.util.*

@Suppress("ClassName")
object `weak-ssl` {
    @Serializable
    class Options : SAOptions {
        val riskAlgorithm = listOf("SSl", "SSLv2", "SSLv3", "TLS", "TLSv1", "TLSv1.1")
    }

    private var options: Options = Options()

    object `default-http-client` : PreAnalysisUnit() {
        context (PreAnalysisApi)
        override suspend fun config() {
            val avoidMethods = ConfigCenter.methodAccessPathDataBase.getRulesByGroupKinds("weak-ssl:default-http-client")
            for (avoidMethod in avoidMethods) {
                atInvoke(avoidMethod.methodMatch) {
                    report(WeakSslChecker.DefaultHttpClient)
                }
            }
        }
    }

    object SSLContext : AIAnalysisUnit() {

        context (AIAnalysisApi)
        override suspend fun config() {
            TaintModelingConfig.applyJsonExtSinksDefault("weak-ssl:algorithm") {
                val isRisk = options.riskAlgorithm.fold(literal(false)) { acc, algorithm ->
                    acc or it.getString().toLowerCase().stringEquals(algorithm.lowercase(Locale.getDefault()))
                }
                check(isRisk, WeakSslChecker.SslContext)
            }
        }
    }
}