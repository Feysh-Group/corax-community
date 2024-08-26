**Table of contents**

<!-- toc -->

- [Community Edition & Commercial Edition Diff](#community-edition--commercial-edition-diff)
- [Open Rules](#open-rules)
- [SAST Test Set Performance](#sast-test-set-performance)
  * [BenchmarkJava](#benchmarkjava)
  * [alipay/ant-application-security-testing-benchmark/sast-java](#alipayant-application-security-testing-benchmarksast-java)
  * [Java Sec Code](#java-sec-code)

<!-- tocstop -->

# Community Edition & Commercial Edition Diff

|                  | Corax Community Edition | Corax Commercial Edition |
|------------------|:-----------------------:|:------------------------:|
| Language Support |          Java           | JSP, Java, C/C++, Python, Go, and over ten more |
| Default Rules    |            27           |  Java(100+)/All languages(1000+) |
| Java Analysis Capability - Path Sensitivity |           ✔           |            ✔             |
| Java Analysis Capability - Taint Analysis  |           ✔           |            ✔             |
| Java Analysis Capability - IFDS            |           ✔           |            ✔             |
| Java Analysis Capability - Abstract Interpretation | ✔           |            ✔             |
| Java Analysis Capability - No Source Code Analysis | ✔           |            ✔             |
| Java Analysis Capability - No Source Code Analysis Display | ✘           |            ✔             |
| Java Analysis Capability - Incremental Analysis | ✘           |            ✔             |
| Trigger Path Prompt |           ✔           |            ✔             |
| Custom Rules     |           ✔           |            ✔             |
| Command Line Invocation |         ✔           |            ✔             |
| Formatted Data Output |         ✔           |            ✔             |
| Visual Interface  |           ✘           |            ✔             |
| User Management   |           ✘           |            ✔             |
| Report Export     |           ✘           |            ✔             |
| Result Comparison  |           ✘           |            ✔             |
| Vulnerability Tagging |         ✘           |            ✔             |
| Vulnerability Lifecycle Management | ✘           |            ✔             |
| Repair Suggestions |         ✘           |            ✔             |
| Data Protection and Cleaning |     ✘           |            ✔             |
| Custom Rule Sets  |           ✘           |            ✔             |
| CI Integration    |           ✘           |            ✔             |

**If you want to learn more about Corax Commercial Edition, you can visit www.feysh.com or contact directly at contact@feysh.com**



# Open Rules

Currently, this project includes the following 27 open rule checkers:

| Name                                 | Description                                             |
|--------------------------------------| --------------------------------------------------------|
| SQL Injection Detection               | Supports detecting common SQL injection and fully supports mybatis framework (all syntax) |
| XSS Vulnerability Detection           | Fully open, supports checking multiple injection scenarios |
| Command Injection Detection           | Supports detecting common command injection            |
| Cookie Not Set httpOnly               | Supports detecting the absence of the httpOnly flag for Cookies |
| Cookie Not Set secure                 | Supports detecting the absence of the secure flag for Cookies |
| Insecure TLS Version                  | Supports detecting cases where the TLS version is set below 1.2 |
| Code Injection                       | Includes Groovy Shell, SpringEl, and ScriptEngine injection scenarios |
| Insecure Cryptographic Algorithms     | Partially open                                         |
| Insecure Hardcoded Authentication Information | Open only for checks based on code semantic analysis |
| Insecure Hardcoded Cryptographic Keys | Open only for checks based on code semantic analysis |
| Controllable Deserialization          | Partially open, with support for recognizing and customizing vulnerability versions for fastjson deserialization checks |
| Path Traversal                        | Arbitrary file read/write and ZipSlip scenarios         |
| Arbitrary File Upload                 | See open source rule implementation |
| Open Redirect                         |                                                        |
| CSRF (Cross-Site Request Forgery)    |                                                        |
| SSRF (Server-Side Request Forgery)   |                                                        |
| Log4j JNDI Injection                 |                                                        |
| Template Injection                    |                                                        |
| LDAP Injection                        |                                                        |
| XPath Injection                       |                                                        |
| XXE (XML External Entity) Attack     |                                                        |
| Trust Boundary Violation             |                                                        |
| Http Response Splitting (CRLF)       |                                                        |
| JWT (JSON Web Token) Forgery          |                                                        |
| Too Permissive Cross-Origin Resource Sharing (CORS) Policy |                                                        |
| Predictable Random Number Generation  |                                                        |
| Weak Hash Algorithm                   |                                                        |


You can refer to the IDE command parameter template: [.run/list-all-rules.run.xml](/.run/list-all-rules.run.xml) to print all rule names and IDs in the plugin.

**If you want to learn more about Corax Commercial Edition, you can visit www.feysh.com or contact directly at contact@feysh.com**



# SAST Test Set Performance

## BenchmarkJava

Static analysis tools testing `BenchmarkJava` requires support for features such as path sensitivity, context sensitivity, common container modeling, constant and string calculation, taint analysis, taint sanitizer, reflection analysis, Spring framework modeling, etc., to avoid a large number of false positives and negatives!

Corax Static Analysis Tool demonstrates its excellent analysis capability and false positive and negative control ability on the `BenchmarkJava` test set. The performance of the Java Community Edition on the [OWASP-Benchmark/BenchmarkJava-1.2beta](https://github.com/OWASP-Benchmark/BenchmarkJava/tree/1.2beta) test set is as follows:

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

Explanation and calculation formulas for `TP/FN/TN/FP/TPR/FPR` can be found [here](usage.md#false-positivenegative-forms)

1. Additionally, a `testcase java` source file in `BenchmarkJava` contains either one or no non-compliant issue, and the code is automatically synthesized.
2. The `crypto` category has false positives due to the fact that other vulnerabilities of the same type are included in this testcase due to code synthesis. The tool produced reports in other locations, leading to being considered false positives!
3. False negatives in the `hash` category are due to the testcase reading a weak hash algorithm name from a configuration file, which is not currently supported by this tool.



## alipay/ant-application-security-testing-benchmark/sast-java

Test set link: [sast-java](https://github.com/alipay/ant-application-security-testing-benchmark/tree/00f10e1cdcb5b95f1a34e18ab0dea2a3b16905fb/sast-java) 

| Checker     | CheckType        | CWE     | Positive(TP | FN)  | Negative(TN | FP)  | Total | TPR  | FPR  | Score |
| ----------- | ---------------- | ------- | ----------- | ---- | ----------- | ---- | ----- | ---- | ---- | ----- |
| CmdiChecker | CommandInjection | cwe-78  | 74          | 7    | 4           | 0    | 91.4 | 0    | 0.92  |
| SsrfChecker | RequestForgery   | cwe-918 | 10          | 1    | 2           | 1    | 93.8 | 0.33 | 0.81  |
| Total       |                  |         | 84          | 8    | 6           | 1    | 91.3 | 0.14 | 0.87  |

Note:

1. This test set focuses on testing the completeness of SAST tools' support for basic features of the Java language (such as arrays, alias assignment, loops, basic Collectors, string operations, etc.).
2. There are too few non-compliant test cases in this category, so the FPR for this single type has little significance.



## Java Sec Code

The performance of the Java Community Edition on the [JoyChou93/java-sec-code](https://github.com/JoyChou93/java-sec-code/tree/8604af55fb68834cf330169cb0a16c27c9e38480) test set is currently under review.

In progress...



Feel free to use other SAST test sets for testing!