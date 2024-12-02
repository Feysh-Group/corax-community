# Remote code injection in Log4j

在2.16.0版本之前的Log4j存在通过ldap JNDI解析器进行远程代码执行的漏洞。

根据[Apache的Log4j安全指南](https://logging.apache.org/log4j/2.x/security.html)：Apache Log4j2 <=2.14.1在配置、日志消息和参数中使用的JNDI功能不会防止受攻击者控制的LDAP和其他JNDI相关的终端。当启用消息查找替换时，能够控制日志消息或日志消息参数的攻击者可以执行从LDAP服务器加载的任意代码。从log4j 2.16.0开始，默认情况下已禁用此行为。

Log4j 2.15.0版本包含了对该漏洞的早期修复，但该补丁未在所有情况下禁用受攻击者控制的JNDI查找。有关更多信息，请参阅此公告的版本2.16.0的更新建议部分。

# Impact

使用有漏洞的Log4J版本记录不受信任或用户控制的数据可能导致远程代码执行（RCE）攻击您的应用程序。这包括记录错误信息（如异常跟踪、身份验证失败等）中包含的不受信任数据，以及其他意外的用户控制输入向量。

# Affected versions

在此特定问题中，任何Log4J版本在v2.15.0之前的都受到影响。

被视为终止生命周期（EOL）的Log4J v1分支存在其他远程代码执行（RCE）向量的漏洞，因此建议尽可能升级到2.16.0版本。

## Security releases

此修复的其他后移版本已在2.3.1、2.12.2和2.12.3版本中提供。

## Affected packages

此漏洞只直接影响 `org.apache.logging.log4j:log4j-core` 包。如果正在使用 `org.apache.logging.log4j:log4j-core` 包，请确保 `org.apache.logging.log4j:log4j-api` 包与其保持相同的版本以确保兼容性。

## Solution:

## Updated advice for version 2.16.0

Apache Logging Services团队在发布2.16.0版本时提供了更新的缓解措施建议，该版本默认禁用了JNDI，并完全删除了支持消息查找的功能。即使在2.15.0版本中，用于提供特定上下文信息的布局中的查找仍然会递归解析，可能触发JNDI查找。此问题被跟踪为[CVE-2021-45046](https://nvd.nist.gov/vuln/detail/CVE-2021-45046)。有关更多信息，请参阅[CVE-2021-45046的GitHub安全公告](https://github.com/advisories/GHSA-7rjr-3q55-vv33)。

如果用户想要避免受攻击者控制的JNDI查找，但无法升级到2.16.0版本，必须确保没有这种查找会解析为攻击者提供的数据，并确保不加载[JndiLookup类](https://issues.apache.org/jira/browse/LOG4J2-3221)。

请注意，Log4J v1已经终止生命周期（EOL），将不会为此问题提供补丁。Log4J v1还存在其他远程代码执行（RCE）的漏洞，我们建议您尽可能迁移到[Log4J 2.16.0版本](https://logging.apache.org/log4j/2.x)。