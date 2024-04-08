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

package com.feysh.corax.config.community.standard

import com.feysh.corax.config.api.IRule
import com.feysh.corax.config.api.IStandard
import com.feysh.corax.config.builtin.standard.BuiltinGeneralStandard


enum class FeyshRules(
    override val realName: String,
    override val desc: String
) : IRule {
    PredictRandom("feysh.java.predict-random", CWERules.CWE330.desc),
    Sqli("feysh.java.sqli", CWERules.CWE89.desc),
    InsecureCookie("feysh.java.insecure-cookie", CWERules.CWE614.desc),
    HttponlyCookie("feysh.java.httponly-cookie", CWERules.CWE1004.desc),
    PathTraversal("feysh.java.path-traversal", CWERules.CWE22.desc),
    Cmdi("feysh.java.cmdi", CWERules.CWE78.desc),
    WeakHash("feysh.java.weak-hash", CWERules.CWE328.desc),
    InsecureCipher("feysh.java.insecure-cipher", CWERules.CWE327.desc),
    Xxe("feysh.java.xxe", CWERules.CWE611.desc),
    Xpathi("feysh.java.xpathi", CWERules.CWE643.desc),
    Csrf("feysh.java.csrf", CWERules.CWE352.desc),
    Ldapi("feysh.java.ldapi", CWERules.CWE90.desc),
    CodeInject("feysh.java.codei", CWERules.CWE94.desc),
    Templatei("feysh.java.templatei", CWERules.CWE1336.desc),//cwe-1336
    HttpRespSplit("feysh.java.http-resp-split", CWERules.CWE113.desc),//cwe-113
    CrlfiLog("feysh.java.crlfi-log", CWERules.CWE117.desc),
    Log4J("feysh.java.log4j-injection", "log4j JNDI Appender injection"),
    HardcodeCredential("feysh.java.hardcode-credential", CWERules.CWE798.desc),
    HardcodeKey("feysh.java.hardcode-key", CWERules.CWE321.desc),
    Xss("feysh.java.xss", CWERules.CWE79.desc),
    OpenRedirect("feysh.java.open-redirect", CWERules.CWE601.desc),
    Deserialization("feysh.java.deserialization", CWERules.CWE502.desc),
    TrustBoundary("feysh.java.trust-boundary", CWERules.CWE501.desc),
    SensitiveDataExpose("feysh.java.sensitive-data-expose", CWERules.CWE200.desc),
    Ssrf("feysh.java.ssrf", CWERules.CWE918.desc),
    PermissiveCors("feysh.java.permissive-cors", CWERules.CWE942.desc),
    WeakSsl("feysh.java.weak-ssl", CWERules.CWE757.desc),
    IncompleteModelOfEndpointFeatures("feysh.java.incomplete-model", CWERules.CWE347.desc),
    UnrestrictedFileUpload("feysh.java.file-upload", CWERules.CWE434.desc),
    ;

    override val standard: IStandard = BuiltinGeneralStandard.FEYSH
}
