@file:Suppress("ClassName")

package com.feysh.corax.config.general.model

import com.feysh.corax.config.api.Elements
import com.feysh.corax.config.api.AIAnalysisApi
import com.feysh.corax.config.api.AIAnalysisUnit
import com.feysh.corax.config.api.MethodConfig
import com.feysh.corax.config.general.checkers.userInputSource


object `main-method-source` : AIAnalysisUnit() {
    context (AIAnalysisApi)
    override suspend fun config() {
        eachMethod {
            if (sootMethod.subSignature != "void main(java.lang.String[])") {
                return@eachMethod
            }
            this.modelNoArg(config = { at = MethodConfig.CheckCall.PrevCallInCallee}) {
                p0.field(Elements).taint += taintOf(userInputSource)
            }
        }
    }
}