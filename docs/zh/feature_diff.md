**Table of contents**


**Table of contents**

<!-- toc -->

- [社区版&商业版 Diff](#%E7%A4%BE%E5%8C%BA%E7%89%88%E5%95%86%E4%B8%9A%E7%89%88-diff)
- [已开放规则](#%E5%B7%B2%E5%BC%80%E6%94%BE%E8%A7%84%E5%88%99)
- [SAST测试集表现](#sast%E6%B5%8B%E8%AF%95%E9%9B%86%E8%A1%A8%E7%8E%B0)
  * [BenchmarkJava](#benchmarkjava)
  * [alipay/ant-application-security-testing-benchmark/sast-java](#alipayant-application-security-testing-benchmarksast-java)
  * [Java Sec Code](#java-sec-code)

<!-- tocstop -->

#  社区版&商业版 Diff

|                  | Corax社区版 |Corax商业版|
|------------------|:--------:|:--:|
| 支持语言             |   Java   |JSP、JAVA、C/C++、Python、Go等十余种|
| 默认规则             |    27    |Java(100+)/全语言(1000+)|
| Java分析能力-路径敏感    |    √     |√|
| Java分析能力-污点分析    |    √     |√|
| Java分析能力-IFDS    |    √     |√|
| Java分析能力-抽象解释    |    √     |√|
| Java分析能力-无源码分析   |    √     |√|
| Java分析能力-无源码分析展示 |    ×     |√|
| Java分析能力-增量分析    |    ×     |√|
| 触发路径提示           |    √     |√|
| 自定义规则            |    √     |√|
| 命令行调用            |    √     |√|
| 格式化数据输出          |    √     |√|
| 可视化界面            |    ×     |√|
| 用户管理             |    ×     |√|
| 报告导出             |    ×     |√|
| 结果对比             |    ×     |√|
| 漏洞标记             |    ×     |√|
| 漏洞生命周期管理         |    ×     |√|
| 修复建议             |    ×     |√|
| 数据保护与清理          |    ×     |√|
| 自定义规则集           |    ×     |√|
| CI集成             |    ×     |√|

**如果您想了解更多与Corax商业版相关的信息，可以访问www.feysh.com或直接联系contact@feysh.com**



# 已开放规则

目前本项目中包含以下27个开放的规则检查器：

| 名称                             | 说明                                     |
|--------------------------------| ---------------------------------------- |
| SQL 注入检测                       | 支持检测常见的SQL注入，并完整支持mybatis框架(全部语法) |
| XSS 漏洞检测                       | 全部开放，支持检查多种注入场景检查 |
| 命令注入检测                         | 支持检测常见的命令注入                   |
| cookie 未设置 httpOnly            | 支持检测Cookie未设置httpOnly标志位       |
| cookie 未设置 secure              | 支持检测Cookie未设置secure标志位         |
| 不安全的 TLS 版本                    | 支持检测设置TLS版本低于1.2的情况         |
| 代码注入                           | 仅包括Groovy Shell、SpringEl和ScriptEngine注入场景 |
| 不安全密码学算法                       | 仅部分开放 |
| 不安全的硬编码认证信息                    | 仅开放基于代码语义分析的检查 |
| 不安全的硬编码密码学key                  | 仅开放基于代码语义分析的检查 |
| 可控的反序列化                        | 仅部分开放，其中fastjson反序列化检查支持识别和自定义漏洞版本区间 |
| 路径穿越                           | 任意文件读写以及ZipSlip |
| 任意类型文件上传                       | 详见开源规则实现 |
| 开放重定向                          |  |
| CSRF 跨站请求伪造                    |  |
| SSRF 服务端请求伪造                   | |
| Log4j jndi 注入                  |  |
| 模板注入                           |  |
| LDAP 注入                        |  |
| XPath 注入                        |  |
| XXE 攻击                         |  |
| Trust Boundary Violation       |  |
| Http Response Splitting (CRLF) |  |
| JWT 伪造                         |  |
| 过于宽松的跨域资源共享 (CORS) 策略          |  |
| 可预测的随机数生成                      |  |
| 使用弱 hash 算法                    |  |

您可以参考IDE命令参数模板：[.run/list-all-rules.run.xml](/.run/list-all-rules.run.xml) 打印插件中全部的规则名id

**如果您想了解更多与Corax商业版相关的信息，可以访问www.feysh.com或直接联系contact@feysh.com**



# SAST测试集表现

## BenchmarkJava

测试 `BenchmarkJava` 的静态分析工具需要不限于路径敏感、上下文敏感、常见容器建模、常量和字符串计算、污点分析、污点Sanitizer、反射分析、Spring框架建模等等特性为支撑，否则会导致大量误漏报！

Corax 静态分析工具展示其出色的分析能力和误漏报把控能力，目前 Java 社区版在 [OWASP-Benchmark/BenchmarkJava-1.2beta](https://github.com/OWASP-Benchmark/BenchmarkJava/tree/1.2beta) 测试集上的表现参考下表：

| Category     | CWE # | TP   | FN   | TN   | FP   | Total | TPR  | FPR  | Score |
| ------------ | ----- | ---- | ---- | ---- | ---- | ----- | ---- | ---- | ----- |
| cmdi         | 78    | 126  | 0    | 125  | 0    | 251   | 1    | 0    | 1     |
| securecookie | 614   | 36   | 0    | 31   | 0    | 67    | 1    | 0    | 1     |
| ldapi        | 90    | 27   | 0    | 32   | 0    | 59    | 1    | 0    | 1     |
| pathtraver   | 22    | 133  | 0    | 135  | 0    | 268   | 1    | 0    | 1     |
| sqli         | 89    | 272  | 0    | 232  | 0    | 504   | 1    | 0    | 1     |
| trustbound   | 501   | 83   | 0    | 43   | 0    | 126   | 1    | 0    | 1     |
| crypto       | 327   | 130  | 0    | 23   | 93   | 246   | 1    | 0.8  | 0.2   |
| hash         | 328   | 89   | 40   | 107  | 0    | 236   | 0.69 | 0    | 0.69  |
| weakrand     | 330   | 218  | 0    | 275  | 0    | 493   | 1    | 0    | 1     |
| xpathi       | 643   | 15   | 0    | 20   | 0    | 35    | 1    | 0    | 1     |
| xss          | 79    | 246  | 0    | 209  | 0    | 455   | 1    | 0    | 1     |
| Total        |       | 1375 | 40   | 1232 | 93   | 2740  | 0.97 | 0.07 | 0.9   |

`TP/FN/TN/FP/TPR/FPR` 解释和计算公式详见 [误漏报解释](usage.md#误漏报表单)

1. 另外 `BenchmarkJava` 中一个`testcase java`源文件仅包含一个或不包含不合规问题，且代码是自动合成的。
2. `crypto` 分类存在误报，其实是这个 testcase 因代码合成缘故导致的其他同类型漏洞也被包含到这个 testcase，本工具在其他位置产生了报告故被认为误报！
3. `hash` 分类的漏报的 testcase 因从配置文件中读取一个弱hash算法名，本工具暂不支持这种 case



## alipay/ant-application-security-testing-benchmark/sast-java

测试集链接: [sast-java](https://github.com/alipay/ant-application-security-testing-benchmark/tree/00f10e1cdcb5b95f1a34e18ab0dea2a3b16905fb/sast-java) 

| checker     | CheckType        | CWE     | Positive(TP | FN)  | Negative(TN | FP)  | Total | TPR  | FPR  | Score |
| ----------- | ---------------- | ------- | ----------- | ---- | ----------- | ---- | ----- | ---- | ---- | ----- |
| CmdiChecker | CommandInjection | cwe-78  | 74          | 7    | 4           | 0    | 85    | 0.91 | 0    | 0.91  |
| SsrfChecker | RequestForgery   | cwe-918 | 10          | 1    | 2           | 1    | 14    | 0.91 | 0.33 | 0.58  |
| Total       |                  |         | 84          | 8    | 6           | 1    | 99    | 0.91 | 0.14 | 0.77  |

注：

1. 此测试集注重测试sast工具对java语言基础特性支持的完整性（比如数组、别名赋值、循环、基础Collectors、字符串操作、等等）
2. 其中不合规**测试用例过少**，所以该单一类型对应的FPR没有太大参考意义



## Java Sec Code

Java社区版目前在 [JoyChou93/java-sec-code](https://github.com/JoyChou93/java-sec-code/tree/8604af55fb68834cf330169cb0a16c27c9e38480) 测试集上的表现参考下表：

统计中 ...



欢迎使用其他SAST的测试集进行测试！