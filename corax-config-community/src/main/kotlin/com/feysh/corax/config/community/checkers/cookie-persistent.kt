package com.feysh.corax.config.community.checkers

import com.feysh.corax.config.api.AIAnalysisApi
import com.feysh.corax.config.api.AIAnalysisUnit
import com.feysh.corax.config.api.baseimpl.matchSoot
import com.feysh.corax.config.community.CookiePersistentChecker

@Suppress("ClassName")
object `cookie-persistent` : AIAnalysisUnit() {
    context (AIAnalysisApi)
    override suspend fun config() {
        listOf(
            "<javax.servlet.http.Cookie: void setMaxAge(int)>",
        ).forEach {
            method(matchSoot(it)).modelNoArg {
                check(
                    (p0.getInt() ge literal(3600 * 24 * 365)) or (p0.getInt() le literal(-1)),
                    CookiePersistentChecker.CookiePersistent
                )
            }
        }
    }
}