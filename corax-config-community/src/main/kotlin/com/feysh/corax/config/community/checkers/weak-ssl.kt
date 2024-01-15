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