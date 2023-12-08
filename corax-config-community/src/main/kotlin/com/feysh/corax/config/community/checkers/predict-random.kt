package com.feysh.corax.config.community.checkers

import com.feysh.corax.config.api.baseimpl.matchConstructor
import com.feysh.corax.config.api.baseimpl.matchSimpleSig
import com.feysh.corax.config.api.baseimpl.matchStaticMethod
import com.feysh.corax.config.api.PreAnalysisApi
import com.feysh.corax.config.api.PreAnalysisUnit
import com.feysh.corax.config.community.PredictRandomChecker
import java.util.*

@Suppress("ClassName")
object `predict-random` : PreAnalysisUnit() {

    context (PreAnalysisApi)
    override fun config() {
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
            atInvoke(it) { report(PredictRandomChecker.PredictRandom) }
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
            atInvoke(it) { report(PredictRandomChecker.PredictRandom) }
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
            atInvoke(it) { report(PredictRandomChecker.PredictRandom) }
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
                report(PredictRandomChecker.PredictRandom)
            }
        }
    }

}