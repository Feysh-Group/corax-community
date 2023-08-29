package com.feysh.corax.config.community.checkers

import com.feysh.corax.config.api.CustomAttributeID
import com.feysh.corax.config.api.AIAnalysisApi
import com.feysh.corax.config.api.AIAnalysisUnit
import com.feysh.corax.config.community.InsecureCookieChecker
import javax.servlet.http.Cookie

@Suppress("ClassName")
object `insecure-cookie`  : AIAnalysisUnit() {

    private val secureAttr = CustomAttributeID<Boolean>("secure")

    context (AIAnalysisApi)
    override fun config() {
        constructor(::Cookie).modelNoArg {
            `this`.attr[secureAttr] = false  // default: `secure = false`
        }

        method(javax.servlet.http.Cookie::setSecure).modelNoArg {
            `this`.attr[secureAttr] = p0.getBoolean()
            check(!p0.getBoolean(), InsecureCookieChecker.InsecureCookie)
        }

        method(javax.servlet.http.HttpServletResponse::addCookie).modelNoArg {
            check(!p0.attr[secureAttr].getBoolean(), InsecureCookieChecker.InsecureCookie)
        }
    }
}