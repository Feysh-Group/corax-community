@file:Suppress("ClassName")

package com.feysh.corax.config.community.checkers.custom

import com.feysh.corax.config.api.PreAnalysisApi
import com.feysh.corax.config.api.PreAnalysisUnit
import com.feysh.corax.config.community.CustomizeChecker
import com.feysh.corax.config.general.model.ConfigCenter
import com.feysh.corax.config.general.utils.methodMatch

object `method-invoke-checker` : PreAnalysisUnit() {
    context (PreAnalysisApi)
    override suspend fun config() {
        val targets = ConfigCenter.methodAccessPathDataBase.getRulesByGroupKinds("method-invoke-checker:target")
        for (target in targets) {
            if (!target.enable) continue
            atInvoke(target.methodMatch) {
                report(CustomizeChecker.TargetMethodInvoke)
            }
        }
    }
}