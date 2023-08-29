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

    CWE78_CMDI("cwe-78", "The software constructs all or part of an OS command using externally-influenced input from an upstream component, but it does not neutralize or incorrectly neutralizes special elements that could modify the intended OS command when it is sent to a downstream component."),
    CWE79_XSSI("cwe-79", "Improper Neutralization of Input During Web Page Generation ('Cross-site Scripting')"),
    CWE89_SQLI("cwe-89", "Improper Neutralization of Special Elements used in an SQL Command"),
    CWE200_SensitiveInformation("cwe-200", "Exposure of Sensitive Information to an Unauthorized Actor"),
    CWE614_SensitiveCookie("cwe-614","Sensitive Cookie in HTTPS Session Without 'Secure' Attribute"),
    CWE757_AlgorithmDowngrade("cwe-757", "Selection of Less-Secure Algorithm During Negotiation ('Algorithm Downgrade')"),
    CWE1004_SensitiveCookie("cwe-1004", "Sensitive Cookie Without 'HttpOnly' Flag"),
    ;

    override val standard: IStandard = BuiltinGeneralStandard.CWE
}
