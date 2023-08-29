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
    Sqli(setOf(BuiltinBugCategory.Injection), "",""),
    InsecureCookie(setOf(BuiltinBugCategory.SECURITY), "",""),
    HttponlyCookie(setOf(BuiltinBugCategory.SECURITY), "",""),
    Cmdi(setOf(BuiltinBugCategory.Injection), "",""),
    Xss(setOf(BuiltinBugCategory.Injection), "",""),
    SensitiveDataExpose(setOf(BuiltinBugCategory.SECURITY), "",""),
    WeakSsl(setOf(BuiltinBugCategory.SECURITY), "",""),
    ;
}

