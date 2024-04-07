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
import com.feysh.corax.config.api.baseimpl.matchConstructor
import com.feysh.corax.config.api.baseimpl.matchSimpleSig
import com.feysh.corax.config.api.baseimpl.matchStaticMethod
import com.feysh.corax.config.api.utils.superClasses
import com.feysh.corax.config.api.utils.superInterfaces
import com.feysh.corax.config.community.PredictRandomChecker
import kotlinx.serialization.Serializable
import java.util.*

@Suppress("ClassName")
object `predict-random` : PreAnalysisUnit() {
    @Serializable
    class Options : SAOptions {
        val secureRandomClass: Set<String> = setOf("java.security.SecureRandom")
    }

    private var option: Options = Options()
    
    context (PreAnalysisApi)
    private fun IInvokeCheckPoint.checkAndReport(bugType: CheckType = PredictRandomChecker.PredictRandom) {
        val calleeClass = callee.declaringClass
        if (option.secureRandomClass.any { calleeClass.isInstanceOf(it) == true })
            return
        report(bugType)
    }

    context (PreAnalysisApi)
    override suspend fun config() {
        listOf(
            matchConstructor<Random>(::Random),
            matchConstructor<Random, Long>(::Random),
            matchStaticMethod(java.lang.Math::random),
            matchStaticMethod(java.util.concurrent.ThreadLocalRandom::current),
            matchSimpleSig("org.apache.commons.lang.math.JVMRandom: * <init>(**)"),
            matchSimpleSig("org.apache.commons.lang.math.JVMRandom: * nextLong(**)"),

            matchSimpleSig("org.apache.commons.lang3.math.JVMRandom: * <init>(**)"),
            matchSimpleSig("org.apache.commons.lang3.math.JVMRandom: * nextLong(**)")
        ).forEach {
            atInvoke(it) { checkAndReport() }
        }

        listOf(
            matchSimpleSig("org.apache.commons.lang.RandomStringUtils: * random(**)"),
            matchSimpleSig("org.apache.commons.lang.RandomStringUtils: * randomAscii(**)"),
            matchSimpleSig("org.apache.commons.lang.RandomStringUtils: * randomAlphabetic(**)"),
            matchSimpleSig("org.apache.commons.lang.RandomStringUtils: * randomAlphanumeric(**)"),
            matchSimpleSig("org.apache.commons.lang.RandomStringUtils: * randomGraph(**)"),
            matchSimpleSig("org.apache.commons.lang.RandomStringUtils: * randomNumeric(**)"),
            matchSimpleSig("org.apache.commons.lang.RandomStringUtils: * randomPrint(**)"),

            matchSimpleSig("org.apache.commons.lang3.RandomStringUtils: * random(**)"),
            matchSimpleSig("org.apache.commons.lang3.RandomStringUtils: * randomAscii(**)"),
            matchSimpleSig("org.apache.commons.lang3.RandomStringUtils: * randomAlphabetic(**)"),
            matchSimpleSig("org.apache.commons.lang3.RandomStringUtils: * randomAlphanumeric(**)"),
            matchSimpleSig("org.apache.commons.lang3.RandomStringUtils: * randomGraph(**)"),
            matchSimpleSig("org.apache.commons.lang3.RandomStringUtils: * randomNumeric(**)"),
            matchSimpleSig("org.apache.commons.lang3.RandomStringUtils: * randomPrint(**)"),
        ).forEach {
            atInvoke(it) { checkAndReport() }
        }
        listOf(
            matchSimpleSig("org.apache.commons.lang.math.RandomUtils: * nextBoolean(**)"),
            matchSimpleSig("org.apache.commons.lang.math.RandomUtils: * nextDouble(**)"),
            matchSimpleSig("org.apache.commons.lang.math.RandomUtils: * nextFloat(**)"),
            matchSimpleSig("org.apache.commons.lang.math.RandomUtils: * nextInt(**)"),
            matchSimpleSig("org.apache.commons.lang.math.RandomUtils: * nextLong(**)"),


            matchSimpleSig("org.apache.commons.lang3.math.RandomUtils: * nextBoolean(**)"),
            matchSimpleSig("org.apache.commons.lang3.math.RandomUtils: * nextDouble(**)"),
            matchSimpleSig("org.apache.commons.lang3.math.RandomUtils: * nextFloat(**)"),
            matchSimpleSig("org.apache.commons.lang3.math.RandomUtils: * nextInt(**)"),
            matchSimpleSig("org.apache.commons.lang3.math.RandomUtils: * nextLong(**)"),
        ).forEach {
            atInvoke(it) { checkAndReport() }
        }


        // scala

        listOf(
            matchSimpleSig("scala.util.Random: * <init>(**)"),
            matchSimpleSig("scala.util.Random: * nextBoolean(**)"),
            matchSimpleSig("scala.util.Random: * nextBytes(**)"),
            matchSimpleSig("scala.util.Random: * nextDouble(**)"),
            matchSimpleSig("scala.util.Random: * nextFloat(**)"),
            matchSimpleSig("scala.util.Random: * nextGaussian(**)"),
            matchSimpleSig("scala.util.Random: * nextInt(**)"),
            matchSimpleSig("scala.util.Random: * nextLong(**)"),
            matchSimpleSig("scala.util.Random: * nextString(**)"),
            matchSimpleSig("scala.util.Random: * nextPrintableChar(**)"),
        ).forEach {
            atInvoke(it) {
                checkAndReport()
            }
        }
    }

}