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