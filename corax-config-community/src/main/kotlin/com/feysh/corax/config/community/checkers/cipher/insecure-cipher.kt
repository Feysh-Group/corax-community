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
    override fun config() {
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
    override fun config() {
        atInvoke(matchSoot("<com.hazelcast.config.SymmetricEncryptionConfig: void <init>()>")) {
            report(InsecureCipherChecker.HazelcastSymmetricEncryption)
        }
    }
}