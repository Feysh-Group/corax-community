package com.feysh.corax.config.community.standard

import com.feysh.corax.config.api.IRule
import com.feysh.corax.config.api.IStandard
import com.feysh.corax.config.builtin.standard.BuiltinGeneralStandard


enum class CERTRules(
    override val realName: String,
    override val desc: String
) : IRule {

    IDS00_J_SQLI("IDS00-J", "SQL injection vulnerabilities arise in applications where elements of a SQL query originate from an untrusted source"),
    IDS07_J_CMDI("IDS07-J", "Sanitize untrusted data passed to the Runtime.exec() method"),
    IDS51_J_XSSI("IDS51-J", "Properly encode or escape output"),
    ;

    override val standard: IStandard = BuiltinGeneralStandard.CERT

}