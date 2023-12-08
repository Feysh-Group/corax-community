package com.feysh.corax.config.community.category

import com.feysh.corax.config.api.IBugCategory
import com.feysh.corax.config.builtin.category.BuiltinBugCategory

// FIXME: 目前无作用故未完善, 但保留
enum class BugCategory(
    override val parent: Set<IBugCategory>,
    override val realName: String,
    override val desc: String,
) : IBugCategory {
    // level 3
    PredictRandom(setOf(BuiltinBugCategory.SECURITY), "",""),
    Sqli(setOf(BuiltinBugCategory.Injection), "",""),
    InsecureCookie(setOf(BuiltinBugCategory.SECURITY), "",""),
    HttponlyCookie(setOf(BuiltinBugCategory.SECURITY), "",""),
    PathTraversal(setOf(BuiltinBugCategory.SECURITY), "",""),
    Cmdi(setOf(BuiltinBugCategory.Injection), "",""),
    WeakHash(setOf(BuiltinBugCategory.SECURITY), "",""),
    InsecureCipher(setOf(BuiltinBugCategory.SECURITY), "",""),
    Xxe(setOf(BuiltinBugCategory.Injection), "",""),
    Xpathi(setOf(BuiltinBugCategory.Injection), "",""),
    Csrf(setOf(BuiltinBugCategory.SECURITY), "",""),
    Ldapi(setOf(BuiltinBugCategory.SECURITY), "",""),
    CodeInject(setOf(BuiltinBugCategory.Injection), "",""),
    Templatei(setOf(BuiltinBugCategory.SECURITY), "",""),
    HttpRespSplit(setOf(BuiltinBugCategory.Injection), "",""),
    HardcodeCredential(setOf(BuiltinBugCategory.SECURITY), "",""),
    HardcodeKey(setOf(BuiltinBugCategory.SECURITY), "",""),
    Xss(setOf(BuiltinBugCategory.Injection), "",""),
    OpenRedirect(setOf(BuiltinBugCategory.SECURITY), "",""),
    Deserialization(setOf(BuiltinBugCategory.SECURITY), "",""),
    TrustBoundary(setOf(BuiltinBugCategory.SECURITY), "",""),
    SensitiveDataExpose(setOf(BuiltinBugCategory.SECURITY), "",""),
    Log4j(setOf(BuiltinBugCategory.Injection), "",""),
    Ssrf(setOf(BuiltinBugCategory.Injection), "",""),
    PermissiveCors(setOf(BuiltinBugCategory.SECURITY), "",""),
    WeakSsl(setOf(BuiltinBugCategory.SECURITY), "",""),
    IncompleteModelOfEndpointFeatures(setOf(BuiltinBugCategory.SECURITY), "",""),
    UnrestrictedFileUpload(setOf(BuiltinBugCategory.Injection), "",""),
    ;
}

