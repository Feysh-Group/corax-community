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


@Suppress("EnumEntryName", "SpellCheckingInspection")
enum class CWERules(
    override val realName: String,
    override val desc: String
) : IRule {
//    已经内置在 api 中, 下列的标准不必再次定义 {
//          CWE561_DeadCode("cwe-561","Contains dead code, which can never be executed."),
//          CWE563_UnusedVariable("cwe-563","Assignment to Variable without Use"),
//          CWE476_NULLPointer("cwe-476","NULL Pointer Dereference"),
//    }
    CWE20("cwe-20", "Improper Input Validation"),
    CWE22("cwe-22", "Improper Limitation of a Pathname to a Restricted Directory ('Path Traversal')"),
    CWE74("cwe-74", "Improper Neutralization of Special Elements in Output Used by a Downstream Component ('Injection')"),
    CWE78("cwe-78", "Improper Neutralization of Special Elements used in an OS Command ('OS Command Injection')"),
    CWE79("cwe-79", "Improper Neutralization of Input During Web Page Generation ('Cross-site Scripting')"),
    CWE89("cwe-89", "Improper Neutralization of Special Elements used in an SQL Command"),
    CWE90("cwe-90", "Improper Neutralization of Special Elements used in an LDAP Query ('LDAP Injection')"),
    CWE94("cwe-94", "Improper Control of Generation of Code ('Code Injection')"),
    CWE113("cwe-113","Improper Neutralization of CRLF Sequences in HTTP Headers ('HTTP Request/Response Splitting')"),
    CWE114("cwe-114","Process Control"),
    CWE117("cwe-117", "Improper Output Neutralization for Logs"),
    CWE200("cwe-200", "Exposure of Sensitive Information to an Unauthorized Actor"),
    CWE321("cwe-321","Use of Hard-coded Cryptographic Key"),
    CWE327("cwe-327", "Use of a Broken or Risky Cryptographic Algorithm"),

    CWE328("cwe-328", "Use of Weak Hash"),
    CWE330("cwe-330","Use of Insufficiently Random Values"),

    CWE347("cwe-347","Improper Verification of Cryptographic Signature"),


    CWE352("cwe-352","Cross-Site Request Forgery (CSRF)"),
    CWE400("cwe-400","Uncontrolled Resource Consumption"),
    CWE434("cwe-434","Unrestricted Upload of File with Dangerous Type"),
    CWE501("cwe-501", "Trust Boundary Violation"),

    CWE502("cwe-502", "Deserialization of Untrusted Data"),
    CWE524("cwe-524", "Use of Cache Containing Sensitive Information"),
    CWE532("cwe-532", "Insertion of Sensitive Information into Log File"),
    CWE539("cwe-539", "Information Exposure Through Persistent Cookies"),
    CWE601("cwe-601","URL Redirection to Untrusted Site ('Open Redirect')"),
    CWE611("cwe-611","Improper Restriction of XML External Entity Reference"),
    CWE614("cwe-614","Sensitive Cookie in HTTPS Session Without 'Secure' Attribute"),
    CWE643("cwe-643", "Improper Neutralization of Data within XPath Expressions ('XPath Injection')"),
    CWE757("cwe-757", "Selection of Less-Secure Algorithm During Negotiation ('Algorithm Downgrade')"),
    CWE798("cwe-798", "The product contains hard-coded credentials, such as a password or cryptographic key, which it uses for its own inbound authentication, outbound communication to external components, or encryption of internal data."),
    CWE918("cwe-918", "Server-Side Request Forgery (SSRF)"),
    CWE942("cwe-942", "Permissive Cross-domain Policy with Untrusted Domains"),
    CWE1004("cwe-1004", "Sensitive Cookie Without 'HttpOnly' Flag"),

    CWE1295("cwe-1295", "Debug Messages Revealing Unnecessary Information"),
    CWE1336("cwe-1336","Improper Neutralization of Special Elements Used in a Template Engine"),
    ;

    override val standard: IStandard = BuiltinGeneralStandard.CWE
}
