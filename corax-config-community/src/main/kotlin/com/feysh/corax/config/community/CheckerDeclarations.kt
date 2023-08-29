package com.feysh.corax.config.community

import com.feysh.corax.config.api.*
import com.feysh.corax.config.community.standard.CWERules
import com.feysh.corax.config.community.category.BugCategory
import com.feysh.corax.config.community.standard.CERTRules

object SqliChecker : IChecker {
    override val report: IRule =  CWERules.CWE89_SQLI
    override val category: IBugCategory = BugCategory.Sqli
    override val standards: Set<IRule> = setOf(
        CWERules.CWE89_SQLI,
        CERTRules.IDS00_J_SQLI
    )

    object SqlInjection : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "使用  ${args["type"]} 中的 `$callee` 可能容易受到 SQL 注入的攻击"},
            Language.EN to msgGenerator { "This use of `$callee` can be vulnerable to SQL injection in the ${args["type"]} " }
        )
        override val checker: IChecker = SqliChecker
    }

}

object InsecureCookieChecker : IChecker {
    override val report: IRule = CWERules.CWE614_SensitiveCookie
    override val category: IBugCategory = BugCategory.InsecureCookie
    override val standards: Set<IRule> = setOf(
        CWERules.CWE614_SensitiveCookie,
//      CERTRules.,
    )

    object InsecureCookie : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "创建了一个没有 secure 标志的 cookie" },
            Language.EN to msgGenerator { "A cookie without the secure flag was created" }
        )
        override val checker: IChecker = InsecureCookieChecker
    }
}

object HttponlyCookieChecker : IChecker {
    override val report: IRule = CWERules.CWE1004_SensitiveCookie
    override val category: IBugCategory = BugCategory.HttponlyCookie
    override val standards: Set<IRule> = setOf(
        CWERules.CWE1004_SensitiveCookie,
//      CERTRules.,
    )

    object HttponlyCookie : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "浏览器中的恶意脚本可能获取不带 HttpOnly 标志的 Cookie" },
            Language.EN to msgGenerator { "Cookie without the HttpOnly flag could be read by a malicious script in the browser" }
        )
        override val checker: IChecker = HttponlyCookieChecker
    }
}


object CmdiChecker : IChecker {
    override val report: IRule = CWERules.CWE78_CMDI
    override val category: IBugCategory = BugCategory.Cmdi
    override val standards: Set<IRule> = setOf(
        CWERules.CWE78_CMDI,
        CERTRules.IDS07_J_CMDI,
    )

    object CommandInjection : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "使用 `$callee` 可能容易受到命令注入的攻击" },
            Language.EN to msgGenerator { "This usage of `$callee` can be vulnerable to Command Injection" }
        )
        override val checker: IChecker = CmdiChecker
    }
}



object XssChecker : IChecker {
    override val report: IRule = CWERules.CWE79_XSSI
    override val category: IBugCategory = BugCategory.Xss
    override val standards: Set<IRule> = setOf(
        CWERules.CWE79_XSSI,
        CERTRules.IDS51_J_XSSI,
//      CERTRules.,
    )


    object XssInjection : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "此处使用 ${args["type"]} 中的 `$callee` 可能容易受到 XSS 攻击" },
            Language.EN to msgGenerator { "This use of `$callee` could be vulnerable to XSS in the ${args["type"]}" }
        )
        override val checker: IChecker = XssChecker
    }

    object XssSpringResponseBody : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "此处返回的 Spring Response Body 存在 XSS 攻击" },
            Language.EN to msgGenerator { "Return a Response Body could be vulnerable to XSS in the spring" }
        )
        override val checker: IChecker = XssChecker
    }

}


object SensitiveDataExposeChecker : IChecker {
    override val report: IRule = CWERules.CWE200_SensitiveInformation
    override val category: IBugCategory = BugCategory.SensitiveDataExpose
    override val standards: Set<IRule> = setOf(
        CWERules.CWE200_SensitiveInformation,
//      CERTRules.,
    )

    object SensitiveDataExposure : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "日志记录方法导致配置或系统信息泄露" },
            Language.EN to msgGenerator { "Logging methods result in configuration or system information leakage" }
        )
        override val checker: IChecker = SensitiveDataExposeChecker
    }
}


object WeakSslChecker : IChecker {
    override val report: IRule = CWERules.CWE757_AlgorithmDowngrade
    override val category: IBugCategory = BugCategory.WeakSsl
    override val standards: Set<IRule> = setOf(
        CWERules.CWE757_AlgorithmDowngrade,
//      CERTRules.,
    )

    object DefaultHttpClient : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "具有默认构造函数的 DefaultHttpClient 与 TLS 1.2 不兼容" },
            Language.EN to msgGenerator { "DefaultHttpClient with default constructor is not compatible with TLS 1.2" }
        )
        override val checker: IChecker = WeakSslChecker
    }

    object SslContext : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "SSLContext 需要兼容 TLS 1.2" },
            Language.EN to msgGenerator { "SSLContext needs to be compatible with TLS 1.2" }
        )
        override val checker: IChecker = WeakSslChecker
    }
}

