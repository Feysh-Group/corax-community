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


    CWE22_PathTraversal("cwe-22", "Improper Limitation of a Pathname to a Restricted Directory ('Path Traversal')"),
    CWE78_CMDI("cwe-78", "The software constructs all or part of an OS command using externally-influenced input from an upstream component, but it does not neutralize or incorrectly neutralizes special elements that could modify the intended OS command when it is sent to a downstream component."),
    CWE79_XSSI("cwe-79", "Improper Neutralization of Input During Web Page Generation ('Cross-site Scripting')"),
    CWE89_SQLI("cwe-89", "Improper Neutralization of Special Elements used in an SQL Command"),
    CWE90_LDAP("cwe-90", "Improper Neutralization of Special Elements used in an LDAP Query ('LDAP Injection')"),

    CWE94_CodeInjection("cwe-94", "Improper Control of Generation of Code ('Code Injection')"),
    CWE113_HttpRespSplit("cwe-113","Improper Neutralization of CRLF Sequences in HTTP Headers ('HTTP Request/Response Splitting')"),
    CWE200_SensitiveInformation("cwe-200", "Exposure of Sensitive Information to an Unauthorized Actor"),
    CWE321_HardCodedKey("cwe-321","Use of Hard-coded Cryptographic Key"),

    CWE327_RiskyCrypto("cwe-327", "Use of a Broken or Risky Cryptographic Algorithm"),

    CWE328_WeakHash("cwe-328", "Use of Weak Hash"),
    CWE330_InsufficientlyRandom("cwe-330","Use of Insufficiently Random Values"),
    CWE347_IncompleteModelOfEndpointFeatures("cwe-347","Incomplete Model of Endpoint Features"),

    CWE352_CSRF("cwe-352","Cross-Site Request Forgery (CSRF)"),

    CWE434_UnrestrictedFileUpload("cwe-434","Unrestricted Upload of File with Dangerous Type"),

    CWE501_TrustBoundaryViolation("cwe-501", "Trust Boundary Violation"),

    CWE502_UntrustedData("cwe-502", "Deserialization of Untrusted Data"),

    CWE601_URLRedirection("cwe-601","URL Redirection to Untrusted Site ('Open Redirect')"),
    CWE611_XXE("cwe-611","Improper Restriction of XML External Entity Reference"),
    CWE614_SensitiveCookie("cwe-614","Sensitive Cookie in HTTPS Session Without 'Secure' Attribute"),
    CWE643_XPathI("cwe-643", "Improper Neutralization of Data within XPath Expressions ('XPath Injection')"),
    CWE757_AlgorithmDowngrade("cwe-757", "Selection of Less-Secure Algorithm During Negotiation ('Algorithm Downgrade')"),
    CWE798_HardCodedCredentials("cwe-798", "The product contains hard-coded credentials, such as a password or cryptographic key, which it uses for its own inbound authentication, outbound communication to external components, or encryption of internal data."),
    CWE918_SSRF("cwe-918", "Server-Side Request Forgery (SSRF)"),
    CWE942_UntrustedDomains("cwe-942", "Permissive Cross-domain Policy with Untrusted Domains"),
    CWE1004_SensitiveCookie("cwe-1004", "Sensitive Cookie Without 'HttpOnly' Flag"),

    CWE1336_TemplateEngine("cwe-1336","Improper Neutralization of Special Elements Used in a Template Engine"),
    ;

    override val standard: IStandard = BuiltinGeneralStandard.CWE
}
