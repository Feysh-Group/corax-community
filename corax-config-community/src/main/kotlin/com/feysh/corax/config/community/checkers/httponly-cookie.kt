package com.feysh.corax.config.community.checkers

import com.feysh.corax.config.api.CustomAttributeID
import com.feysh.corax.config.api.AIAnalysisApi
import com.feysh.corax.config.api.AIAnalysisUnit
import com.feysh.corax.config.community.HttponlyCookieChecker
import javax.servlet.http.Cookie

@Suppress("ClassName")
object `httponly-cookie` : AIAnalysisUnit() {

    private val httpOnlyAttr = CustomAttributeID<Boolean>("httpOnly")

    context (AIAnalysisApi)
    override suspend fun config() {
        constructor(::Cookie).modelNoArg {
            `this`.attr[httpOnlyAttr] = false  // default: `isHttpOnly = false`
        }

        method(javax.servlet.http.Cookie::setHttpOnly).modelNoArg {
            `this`.attr[httpOnlyAttr] = p0.getBoolean()
            check(!p0.getBoolean(), HttponlyCookieChecker.HttponlyCookie)
        }

        method(javax.servlet.http.HttpServletResponse::addCookie).modelNoArg {
            check(!p0.attr[httpOnlyAttr].getBoolean(), HttponlyCookieChecker.HttponlyCookie)
        }

    }
}