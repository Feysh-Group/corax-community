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