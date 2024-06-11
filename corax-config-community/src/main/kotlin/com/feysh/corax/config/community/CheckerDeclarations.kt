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

package com.feysh.corax.config.community

import com.feysh.corax.config.api.*
import com.feysh.corax.config.community.standard.CWERules
import com.feysh.corax.config.community.standard.CERTRules
import com.feysh.corax.config.community.standard.FeyshRules

object PredictRandomChecker : IChecker {
    override val report: IRule = FeyshRules.PredictRandom
    override val standards: Set<IRule> = setOf(
        CWERules.CWE330,
        CERTRules.MSC02_J
    )

    object PredictRandom : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "这个随机生成器 `${callee?.declaringClass}` 是可预测的" },
            Language.EN to msgGenerator { "This random generator `${callee?.declaringClass}` is predictable" }
        )
        override val checker: IChecker = PredictRandomChecker
    }
}

object SqliChecker : IChecker {
    override val report: IRule = FeyshRules.Sqli
    override val standards: Set<IRule> = setOf(
        CWERules.CWE89,
        CERTRules.IDS00_J
    )

    object SqlInjection : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "使用  ${args["type"]} 中的 `$callee` 可能容易受到 SQL 注入的攻击.${args["msg"]?.let { " $it" } ?: ""}" },
            Language.EN to msgGenerator { "This use of `$callee` can be vulnerable to SQL injection in the ${args["type"]}.${args["msg"]?.let { " $it" } ?: ""}" }
        )
        override val checker: IChecker = SqliChecker
    }

    object MybatisSqlInjectionSinkHint : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "找到 SQL 注入 sink 点 ${args["numSinks"]} 个 . 存在风险的 Mybatis SQL 查询语句为: `${args["boundSql"]}`" },
            Language.EN to msgGenerator { "Found `${args["numSinks"]}` SQL injection sinks in the risk Mybatis SQL statement: `${args["boundSql"]}`" }
        )
        override val checker: IChecker = SqliChecker
    }
}

object InsecureCookieChecker : IChecker {
    override val report: IRule = FeyshRules.InsecureCookie
    override val standards: Set<IRule> = setOf(
        CWERules.CWE614,
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
    override val report: IRule = FeyshRules.HttponlyCookie
    override val standards: Set<IRule> = setOf(
        CWERules.CWE1004,
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

object PathTraversalChecker : IChecker {
    override val report: IRule = FeyshRules.PathTraversal
    override val standards: Set<IRule> = setOf(
        CWERules.CWE22,
        CERTRules.FIO16_J,
    )

    object PathTraversalIn : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "此 API `$callee` 读取的可能是由用户输入指定的文件" },
            Language.EN to msgGenerator { "This API `$callee` reads a file whose location might be specified by user input" }
        )
        override val checker: IChecker = PathTraversalChecker
    }
    object ZipSlip : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "此处的方法 `$callee` 可能产生 zipSlip 漏洞" },
            Language.EN to msgGenerator { "This method `$callee` may create a zipSlip vulnerability" }
        )
        override val checker: IChecker = PathTraversalChecker
    }
}

object UnrestrictedFileUploadChecker : IChecker {
    override val report: IRule = FeyshRules.UnrestrictedFileUpload
    override val standards: Set<IRule> = setOf(
        CWERules.CWE434,
        CERTRules.IDS56_J
    )

    object UnrestrictedFileUpload : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "此 API `$callee` 可能上传了一个来自用户的危险类型文件" },
            Language.EN to msgGenerator { "This API `$callee` upload a file whose type might be dangerous from user" }
        )
        override val checker: IChecker = UnrestrictedFileUploadChecker
    }
}


object CmdiChecker : IChecker {
    override val report: IRule = FeyshRules.Cmdi
    override val standards: Set<IRule> = setOf(
        CWERules.CWE78,
        CERTRules.IDS07_J,
    )

    object CommandInjection : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "使用 `$callee` 可能容易受到命令注入的攻击" },
            Language.EN to msgGenerator { "This usage of `$callee` can be vulnerable to Command Injection" }
        )
        override val checker: IChecker = CmdiChecker
    }
}


object WeakHashChecker : IChecker {
    override val report: IRule = FeyshRules.WeakHash
    override val standards: Set<IRule> = setOf(
        CWERules.CWE328
    )

    object WeakMessageDigestMd5 : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "此 API `$callee` (MDX) 不是推荐的哈希加密函数" },
            Language.EN to msgGenerator { "This API `$callee` (MDX) is not a recommended cryptographic hash function" }
        )
        override val checker: IChecker = WeakHashChecker
    }

    object WeakMessageDigestSha1 : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "此 API `$callee` (SHA-1) 不是推荐的哈希加密函数" },
            Language.EN to msgGenerator { "This API `$callee` (SHA-1) is not a recommended cryptographic hash function" }
        )
        override val checker: IChecker = WeakHashChecker
    }

    object CustomMessageDigest : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "`$clazz` 是一个自定义的加密哈希函数实现" },
            Language.EN to msgGenerator { "`$clazz` is a custom cryptographic hash function implementation" }
        )
        override val checker: IChecker = WeakHashChecker
    }
}
object InsecureCipherChecker : IChecker {
    override val report: IRule = FeyshRules.InsecureCipher
    override val standards: Set<IRule> = setOf(
        CWERules.CWE327,
        CERTRules.MSC61_J
    )

    object DesUsage : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "DES 应替换为 AES" },
            Language.EN to msgGenerator { "DES should be replaced with AES" }
        )
        override val checker: IChecker = InsecureCipherChecker
    }

    object TdesUsage : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "DESede 应替换为 AES" },
            Language.EN to msgGenerator { "DESede should be replaced with AES" }
        )
        override val checker: IChecker = InsecureCipherChecker
    }

    object EcbMode : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "密码使用 ECB 模式, 这为加密数据提供了较差的保密性" },
            Language.EN to msgGenerator { "The cipher uses ECB mode, which provides poor confidentiality for encrypted data" }
        )
        override val checker: IChecker = InsecureCipherChecker
    }

    object HazelcastSymmetricEncryption : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "Hazelcast 的网络通信被配置为使用对称密码" },
            Language.EN to msgGenerator { "The network communication for Hazelcast are configured to use a symmetric cipher" }
        )
        override val checker: IChecker = InsecureCipherChecker
    }
}
object XxeChecker : IChecker {
    override val report: IRule = FeyshRules.Xxe
    override val standards: Set<IRule> = setOf(
        CWERules.CWE611,
//        CERTRules.,
    )

    object XxeRemote : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "XML (`${args["type"]}`) 外部实体扩展中使用的未验证的远程用户输入" },
            Language.EN to msgGenerator { "Unvalidated remote user input that is used in XML (`${args["type"]}`) external entity expansion" }
        )
        override val checker: IChecker = XxeChecker
    }
    object XxeLocal : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "XML (`${args["type"]}`) 外部实体扩展中使用的未验证的本地用户输入" },
            Language.EN to msgGenerator { "Unvalidated local user input that is used in XML (`${args["type"]}`) external entity expansion" }
        )
        override val checker: IChecker = XxeChecker
    }
}

object XpathiChecker : IChecker {
    override val report: IRule = FeyshRules.Xpathi
    override val standards: Set<IRule> = setOf(
        CWERules.CWE643,
//        CERTRules.,
    )

    object XpathInjection : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "使用 `$callee` 可能容易受到 XPath 注入的攻击" },
            Language.EN to msgGenerator { "This use of `$callee` can be vulnerable to XPath Injection" }
        )
        override val checker: IChecker = XpathiChecker
    }
}

object CsrfChecker : IChecker {
    override val report: IRule = FeyshRules.Csrf
    override val standards: Set<IRule> = setOf(
        CWERules.CWE352
    )

    object SpringCsrfProtectionDisabled : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "禁用 Spring Security 的 CSRF 保护对于标准 web 应用程序来说是不安全的" },
            Language.EN to msgGenerator { "Disabling Spring Security's CSRF protection is unsafe for standard web applications" }
        )
        override val checker: IChecker = CsrfChecker
    }

    object SpringCsrfUnrestrictedRequestMapping : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "不受限制的 Spring 的 RequestMapping 使该方法容易受到 CSRF 攻击" },
            Language.EN to msgGenerator { "Unrestricted Spring's RequestMapping makes the method vulnerable to CSRF attacks" }
        )
        override val checker: IChecker = CsrfChecker
    }
}

object LdapiChecker : IChecker {
    override val report: IRule = FeyshRules.Ldapi
    override val standards: Set<IRule> = setOf(
        CWERules.CWE90,
        CERTRules.IDS54_J,
    )

    object LdapInjection : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "使用 `$callee` 可能容易受到 LDAP 注入的攻击" },
            Language.EN to msgGenerator { "This use of `$callee` can be vulnerable to LDAP injection" }
        )
        override val checker: IChecker = LdapiChecker
    }
}
object CodeInjectionChecker : IChecker {
    override val report: IRule = FeyshRules.CodeInject
    override val standards: Set<IRule> = setOf(
        CWERules.CWE94,
//        CERTRules.,
    )

    object ScriptEngineInjection : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "使用 `$callee` 可能容易受到代码注入的攻击" },
            Language.EN to msgGenerator { "This use of `$callee` can be vulnerable to code injection" }
        )
        override val checker: IChecker = CodeInjectionChecker
    }

    object SpringElInjection : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "使用 `$callee` 可能容易受到代码注入的攻击 (Spring Expression)" },
            Language.EN to msgGenerator { "This use of `$callee` could be vulnerable to code injection (Spring Expression)" }
        )
        override val checker: IChecker = CodeInjectionChecker
    }

    object GroovyShell : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "使用 GroovyShell 时可能存在代码注入风险" },
            Language.EN to msgGenerator { "Potential code injection when using GroovyShell" }
        )
        override val checker: IChecker = CodeInjectionChecker
    }
}
object TemplateIChecker : IChecker {
    override val report: IRule = FeyshRules.Templatei
    override val standards: Set<IRule> = setOf(
        CWERules.CWE1336,
//        CERTRules.,
        // TODO:
    )

    object TemplateInjection : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "使用 Velocity 模板存在模板注入" },
            Language.EN to msgGenerator { "Potential template injection with Velocity template" }
        )
        override val checker: IChecker = TemplateIChecker
    }
}
object HttpRespSplitChecker : IChecker {
    override val report: IRule = FeyshRules.HttpRespSplit
    override val standards: Set<IRule> = setOf(
        CWERules.CWE113,
//        CERTRules.,
        // TODO:
    )

    object HttpResponseSplitting : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "`$callee` 的使用可能用于将 CRLF 字符包括在 HTTP 标头中" },
            Language.EN to msgGenerator { "This use of `$callee` might be used to include CRLF characters into HTTP headers" }
        )
        override val checker: IChecker = HttpRespSplitChecker
    }
}


object HardcodeCredentialChecker : IChecker {
    override val report: IRule = FeyshRules.HardcodeCredential
    override val standards: Set<IRule> = setOf(
        CWERules.CWE798,
//        CERTRules.,
        // TODO
    )

    object HardCodeUserName : CheckType() {
        override val aliasNames: Set<String> = setOf("HardcodedCredentialsApiCall", "HardcodedCredentialsSourceCall")
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "在对敏感 Java API `$callee` 的调用中使用硬编码用户名凭据" },
            Language.EN to msgGenerator { "Hard-coded of User-Name credential in a call to a sensitive Java API: `$callee`" }
        )
        override val checker: IChecker = HardcodeCredentialChecker
    }
    object HardCodePassword : CheckType() {
        override val aliasNames: Set<String> = setOf("HardcodedCredentialsApiCall", "HardcodedCredentialsSourceCall")
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "在对敏感 Java API `$callee` 的调用中使用硬编码密码" },
            Language.EN to msgGenerator { "Hard-coded of password credential in a call to a sensitive Java API: `$callee`" }
        )
        override val checker: IChecker = HardcodeCredentialChecker
    }

    object HardCodeOthers : CheckType() {
        override val aliasNames: Set<String> = setOf("HardcodedCredentialsApiCall", "HardcodedCredentialsSourceCall")
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "对敏感 Java API `$callee` 的调用中使用硬编码" },
            Language.EN to msgGenerator { "Hard-coded credential in a call to a sensitive Java API: `$callee`" }
        )
        override val checker: IChecker = HardcodeCredentialChecker
    }
}


object HardcodeKeyChecker : IChecker {
    override val report: IRule = FeyshRules.HardcodeKey
    override val standards: Set<IRule> = setOf(
        CWERules.CWE321
    )

    object HardCodeKey : CheckType() {
        override val aliasNames: Set<String> = setOf("HardcodedCredentialsApiCall")
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "硬编码的加密密钥" },
            Language.EN to msgGenerator { "Hard coded cryptographic key found" }
        )
        override val checker: IChecker = HardcodeKeyChecker
    }
}
object XssChecker : IChecker {
    override val report: IRule = FeyshRules.Xss
    override val standards: Set<IRule> = setOf(
        CWERules.CWE79,
        CERTRules.IDS51_J,
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


object OpenRedirectChecker : IChecker {
    override val report: IRule = FeyshRules.OpenRedirect
    override val standards: Set<IRule> = setOf(
        CWERules.CWE601,
//        CERTRules.,
        // TODO
    )

    object UnvalidatedRedirect : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "攻击者可以使用以下重定向将用户重定向到网络钓鱼网站" },
            Language.EN to msgGenerator { "The following redirection could be used by an attacker to redirect users to a phishing website" }
        )
        override val checker: IChecker = OpenRedirectChecker
    }

}
object DeserializationChecker : IChecker {
    override val report: IRule = FeyshRules.Deserialization
    override val standards: Set<IRule> = setOf(
        CWERules.CWE502,
//        CERTRules.,
        // TODO
    )

    object ObjectDeserialization : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "此处调用 `$callee` 可能触发反序列化漏洞" },
            Language.EN to msgGenerator { "This use of `$callee` can be vulnerable to object deserialization" }
        )
        override val checker: IChecker = DeserializationChecker
    }
    
    object UnrestrictedObjectDeserialization : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "此处调用 `$callee` 对不可信数据进行反序列化是危险的（可能产生如RCE、拒绝服务等漏洞）" },
            Language.EN to msgGenerator { "This use of `$callee` to deserialize untrusted data here is dangerous (potentially leading to RCE, Denial of service attack)." }
        )
        override val checker: IChecker = DeserializationChecker
    }
    
    object JacksonUnsafeDeserialization : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "方法 `$callee` 使用了不安全的 Jackson 反序列化配置" },
            Language.EN to msgGenerator { "Unsafe Jackson deserialization configuration used in method: `$callee`" }
        )
        override val checker: IChecker = DeserializationChecker
    }
}

object TrustBoundaryChecker : IChecker {
    override val report: IRule = FeyshRules.TrustBoundary
    override val standards: Set<IRule> = setOf(
        CWERules.CWE501,
//        CERTRules.,
        // TODO
    )

    object TrustBoundaryViolation : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "应用程序在会话属性中混合了受信任和不受信任的数据" },
            Language.EN to msgGenerator { "The application mixes trusted and untrusted data in session attributes" }
        )
        override val checker: IChecker = TrustBoundaryChecker
    }
}
object SensitiveDataExposeChecker : IChecker {
    override val report: IRule = FeyshRules.SensitiveDataExpose
    override val standards: Set<IRule> = setOf(
        CWERules.CWE200,
//      CERTRules.,
    )

    object SensitiveDataExposure : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "高密级信息泄露" },
            Language.EN to msgGenerator { "High-level secret information leakage" }
        )
        override val checker: IChecker = SensitiveDataExposeChecker
    }
}

object IncompleteModelOfEndpointFeatures : IChecker {
    override val report: IRule = FeyshRules.ImproperVerification
    override val standards: Set<IRule> = setOf(
        CWERules.CWE347,
    )

    object HasMissingJwtSignatureCheck : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "未检查 Json Web Token (JWT) 签名将允许攻击者任意伪造自己的令牌" },
            Language.EN to msgGenerator { "Failing to check the Json Web Token (JWT) signature may allow an attacker to forge their own tokens" }
        )
        override val checker: IChecker = IncompleteModelOfEndpointFeatures
    }
}

object Log4jChecker : IChecker {
    override val report: IRule = FeyshRules.Log4J
    override val standards: Set<IRule> = setOf(
        CWERules.CWE502,
        CWERules.CWE400,
        CWERules.CWE74,
        CWERules.CWE20
//        CERTRules.,
        // TODO
    )

    object Log4jInjection : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "此处存在远程代码执行 Log4j 漏洞" },
            Language.EN to msgGenerator { "There is a Log4j vulnerability that allows remote code execution" }
        )
        override val checker: IChecker = Log4jChecker
    }
}

object SsrfChecker : IChecker {
    override val report: IRule = FeyshRules.Ssrf
    override val standards: Set<IRule> = setOf(
        CWERules.CWE918
    )

    object RequestForgery : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "攻击者可以使用此 Web 服务器请求来暴露内部服务和文件系统" },
            Language.EN to msgGenerator { "This web server request could be used by an attacker to expose internal services and filesystem" }
        )
        override val checker: IChecker = SsrfChecker
    }
}
object PermissiveCorsChecker : IChecker {
    override val report: IRule = FeyshRules.PermissiveCors
    override val standards: Set<IRule> = setOf(
        CWERules.CWE942,
//        CERTRules.,
        // TODO
    )

    object PermissiveCors : CheckType() {
        override val bugMessage: Map<Language, BugMessage> = mapOf(
            Language.ZH to msgGenerator { "该程序定义了过于宽松的跨域资源共享 (CORS) 策略" },
            Language.EN to msgGenerator { "The program defines an overly permissive Cross-Origin Resource Sharing (CORS) policy" }
        )
        override val checker: IChecker = PermissiveCorsChecker
    }
}
object WeakSslChecker : IChecker {
    override val report: IRule = FeyshRules.WeakSsl
    override val standards: Set<IRule> = setOf(
        CWERules.CWE757,
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

