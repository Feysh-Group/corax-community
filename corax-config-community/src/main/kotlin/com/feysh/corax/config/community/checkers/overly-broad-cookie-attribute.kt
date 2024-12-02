package com.feysh.corax.config.community.checkers

import com.feysh.corax.config.api.AIAnalysisApi
import com.feysh.corax.config.api.AIAnalysisUnit
import com.feysh.corax.config.api.baseimpl.matchSoot
import com.feysh.corax.config.community.OverlyBroadCookieAttributeChecker

@Suppress("ClassName")
object `overly-broad-cookie-attribute` : AIAnalysisUnit() {
    context(AIAnalysisApi)
    override suspend fun config() {
            method(matchSoot("<javax.servlet.http.Cookie: void setDomain(java.lang.String)>")).modelNoArg {
                check(p0.getString().startsWith("."), OverlyBroadCookieAttributeChecker.OverlyBroadDomain)
            }
            method(matchSoot("<javax.servlet.http.Cookie: void setPath(java.lang.String)>")).modelNoArg {
                check(p0.getString().stringEquals("/"), OverlyBroadCookieAttributeChecker.OverlyBroadPath)
            }
    }
}