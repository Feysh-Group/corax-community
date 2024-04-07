/*
 *  CoraxJava - a Java Static Analysis Framework
 *  Copyright (C) 2024.  Feysh-Tech Group
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

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