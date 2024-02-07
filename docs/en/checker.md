<!-- Merge custom-checker and plugin-infrastructure

The purpose is to tell developers how to develop a checker


The compiled exception output is located at community/build/reports/tests/test/index.html
-->



# Custom CoraxJava Rule Checker

## Overview

This project is the `CoraxJava Rule Checker` module, which can implement multiple rule checks and compile them into a plugin form, to be loaded and executed by the `CoraxJava Core Analysis Engine` module.

It is often necessary to customize CoraxJava rule checkers, including several common scenarios, from simple to complex, and from shallow to deep customization:
1. Coarse-grained adjustments to the CoraxJava rule checker module; such as enabling/disabling certain rule checks, adding already implemented rule checkers, and adding some data check properties to already implemented rule checkers.
2. Fine-grained adjustments to the CoraxJava rule checker; such as modifying the source, sink, or modifying data transmission rules summary configuration of certain already implemented rule checkers.
3. Adding new rules for checks, but using existing checkers as templates, with slight modifications to configurations and code to complete the check.
4. Adding new rules for checks, but requiring completely custom development and implementation of their checkers.

## Scenario One: Coarse-Grained Adjustment of the CoraxJava Rule Checker Module

Simply modify the yaml configuration file [analysis-config/default-config.yml](/build/analysis-config/default-config.yml) to achieve coarse-grained adjustments to the `CoraxJava Rule Checker` module.

For example, to enable/disable specific checkers, you can set `enable` to `false` in the following configuration:
```yml
- !<CheckerUnitOptionalConfig>
    name: "feysh-config-community:com.feysh.corax.config.community.checkers.httponly-cookie"
    enable: true
    options: null
```

To add options to a checker, including adding `TaintTypes` to the checker
```yaml
 - !<CheckerUnitOptionalConfig>
    name: "feysh-config-community:com.feysh.corax.config.community.checkers.taint-checker"
    enable: true
    options: !<com.feysh.corax.config.community.checkers.taint-checker.Options>
      kind2Checker:
        "command-injection":
          checkTaintTypes:
          - !<GeneralTaintTypes> "ControlData"
          - !<GeneralTaintTypes> "CONTAINS_COMMAND_INJECT"
          taintTypeExcludes: []
          reportType: !<com.feysh.corax.config.community.CmdiChecker.CommandInjection> {}
          msgArgs: {}
          enable: true
```

For more information, refer to [analysis-config/default-config.yml](/build/analysis-config/default-config.yml) and subsequent explanations.

## Scenario Two: Fine-Grained Adjustment of the CoraxJava Rule Checker

Commonly involves modifying the source, sink, summary, and other configurations of existing rule checkers.

Files with the suffix `sources.json` such as [general.sources.json](/corax-config-general/rules/general.sources.json) contain sources that are grouped together and categorized using `kind`, for example:

```json
[
  {"kind":"remote","signature":"javax.servlet.ServletRequest: * getInputStream()","subtypes":false,"arg":"ReturnValue","provenance":"manual","ext":""},
  {"kind":"remote","signature":"javax.servlet.ServletRequest: * getParameter(String)","subtypes":false,"arg":"ReturnValue","provenance":"manual","ext":""},
  ...
]
```

Similarly, files with the suffix `sinks.json` such as [community.sinks.json](/corax-config-community/rules/community.sinks.json) contain sinks that are grouped together and categorized using `kind`:

```json
[
    {"kind":"command-injection","signature":"java.lang.Runtime: * load(String)","subtypes":false,"arg":"Argument[0]","provenance":"ai-manual","ext":""},
    {"kind":"command-injection","signature":"java.lang.Runtime: * loadLibrary(String)","subtypes":false,"arg":"Argument[0]","provenance":"ai-manual","ext":""},
    ...
]

```

Files with the suffix `summaries.json`, such as [general.summaries.json](/corax-config-general/rules/general.summaries.json), contain summaries related to the rules and configurations.

```json
[
  {"signature":"java.lang.AbstractStringBuilder: * <init>(String)","subtypes":true,"argTo":"Argument[this]","propagate":"taint","argFrom":"Argument[0]","provenance":"manual","ext":""},
  {"signature":"java.lang.AbstractStringBuilder: * append(**)","subtypes":true,"argTo":"ReturnValue","propagate":"value","argFrom":"Argument[this]","provenance":"manual","ext":""}
  ...
]
```

- `signature` can be in the format of `matchSoot` or in the format of `matchSimpleSig`.
- `subtypes` indicates whether to handle overridden methods in subclasses (static methods are always false, interfaces are always true).
- `"propagate": "taint"` means taint kinds propagation. After method execution, `to` and `from` may point to different objects. If they are different, then `to` has been tainted or affected, while `from` remains unaffected, unless there are alias relationships in the program.
- `"propagate": "value"` means value propagation, where `to` and `from` point to the same object.
- `provenance` is a reserved field and is not closely related to `ext`.




## Scenario Three: Modification of CoraxJava Rule Checker

By modifying some code or parameters, you can customize the rule checker.

Developers can compare the types of vulnerabilities they want to detect with the rules already implemented in the [community version](feature_diff.md#已开放规则) and then proceed to implement similar rule codes. For example:

1. Command injection rule checker is a typical Taint-type checker, which can be referenced for implementing injection issues (SSRF detection, SQL injection, XSS injection, path traversal, template injection, open redirection, etc.) and sensitive information leakage, both fall under the category of taint issues.
2. The implementation of SQL injection rule checker includes not only classic Taint-type checking but also includes support for adapting MyBatis framework injection sink point checking.
3. The Spring Response Body injection check for XSS vulnerability is implemented by dynamically checking sink points through matching corresponding Java annotations, similar to SQL injection checks in scenarios with hibernate.jpa `Query` annotations and ibatis `Select` annotations.
4. The cookie attribute setting check mainly demonstrates API call sequence combination checks and detection of specific parameters, similar to PermissiveCors check.
5. Detection of insecure TLS versions mainly involves specific API detection and constant parameter detection, similar to checks for cryptographic misuse, weak hash algorithms, cookie persistent maxAge, Ldap anonymous, etc.
6. Omitted, can be found in other open source implementations in this project.



## Scenario Four: Customization of CoraxJava Rule Checker

1. Define `Checker` referring to [IChecker](plugin-infrastructure.md#ichecker) (if it exists then proceed)
   1. Define `Rule` referring to [IRule](plugin-infrastructure.md#irule)
   2. Define `BugCategory` [IBugCategory](plugin-infrastructure.md#ibugcategory)
   
2. Define `CheckType` referring to [CheckType](plugin-infrastructure.md#checktype) (if it exists then proceed)
   
   1. Define bug message referring to [BugMessage](plugin-infrastructure.md#bugmessage)
   
3. Choose a `CheckerUnit`, based on the bugs and principles to be checked by the checker, choose a suitable Analysis, refer to [CheckerUnit](plugin-infrastructure.md#checkerunit)
   1. [PreAnalysisUnit](plugin-infrastructure.md#preanalysisunit)
   2. [AIAnalysisUnit](plugin-infrastructure.md#aianalysisunit)
   
4. Once chosen, start writing the checker according to the requirements and corresponding check principles, and when calling `ICheckPoint.report(checkType)` or `IOperatorFactory.check(boolExpr, checkType)`, pass in the CheckType from step two.

   If you need to check for some more direct bugs in your written checker, you can follow these two steps:

   1. Edit the [AnalyzerConfigRegistry](/corax-config-community/src/main/kotlin/com/feysh/corax/config/community/AnalyzerConfigRegistry.kt) file and add the newly added checker registration in `preAnalysisImpl` or `aiCheckerImpl`.
   2. After adding it, you can run `gradlew :corax-config-community:test --tests "com.feysh.corax.config.tests.ConfigValidate.validate"`, which can check for some checker writing errors and prompt for corrections.

5. Go to [corax-config-tests/normal/src/main/java/testcode](/corax-config-tests/normal/src/main/java/testcode) and write corresponding non-compliant and compliant code to test and ensure analysis accuracy, refer to [Unit Testing](unit-tests.md)

6. Run `gradlew build` to compile and package the final configuration [build/analysis-config](/build/analysis-config)

7. Follow [Readme.md#Getting Started with Analysis](/Readme.md#开始分析) to load the configuration and start analysis

8. View the report [sarif](/build/output/sarif), if there are any false positives or missed reports, analyze the reasons and consider if corrections or optimizations are needed, refer to [Result Output](usage.md#结果输出)

For more detailed detection methods, you can refer to [Detailed Explanation of Custom Rule Checker](checker-detail.md).