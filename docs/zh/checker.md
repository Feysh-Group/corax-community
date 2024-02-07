<!-- 合并 custom-checker 和 plugin-infrastructure

目的是为了告诉开发者如何开发checker


编译的异常输出在 community/build/reports/tests/test/index.html
 -->



# 自定义CoraxJava规则检查器

## 概述

本项目即 `CoraxJava规则检查器`模块，该模块中可以实现多条规则检查，并编译打包成插件形式，被`CoraxJava核心分析引擎`模块加载执行。

通常需要自定义CoraxJava规则检查器，包含如下几种常见场景，复杂度由易到难，自定义的程度也是由浅入深:
1. 对CoraxJava规则检查器整个模块的粗粒度调整；如开启/关闭某些规则检查，增加已经实现好的规则检查器，对已经实现好的规则检查器增加一些数据检查属性等。
2. 对CoraxJava规则检查器做细粒度的调整；如对已实现好的某些规则检查器，新增修改其source，sink，或修改数据传递规则summary等配置。
3. 新增检查的规则，但可以通过现有的检查器为模板，通过修改稍许配置和代码，即可完成检查的。
4. 新增检查的规则，但需要完全自定义开发并实现其检查器。

## 场景一：CoraxJava规则检查器模块粗粒度调整

只需要修改 yaml 配置文件 [analysis-config/default-config.yml](/build/analysis-config/default-config.yml) 即可实现对`CoraxJava规则检查器`模块的粗粒度调整。

如开关特定的检查器，可在如下配置中将`enable`设置为`false`。
```yml
- !<CheckerUnitOptionalConfig>
    name: "feysh-config-community:com.feysh.corax.config.community.checkers.httponly-cookie"
    enable: true
    options: null
```

对检查器增加一些选项，包括给检查器增加`TaintTypes`。
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

更多可参考[analysis-config/default-config.yml](/build/analysis-config/default-config.yml) 及后续说明。

## 场景二：CoraxJava规则检查器细粒度调整

常见为修改现有规则检查器的source，sink，summary等配置。

以 `sources.json` 为后缀的文件 如 [general.sources.json](/corax-config-general/rules/general.sources.json) 这些 source 被放在一起并使用 `kind` 对这些 source 进行归类, 如：

```json
[
  {"kind":"remote","signature":"javax.servlet.ServletRequest: * getInputStream()","subtypes":false,"arg":"ReturnValue","provenance":"manual","ext":""},
  {"kind":"remote","signature":"javax.servlet.ServletRequest: * getParameter(String)","subtypes":false,"arg":"ReturnValue","provenance":"manual","ext":""},
  ...
]
```

同样的`sinks.json`为后缀的文件 如 [community.sinks.json](/corax-config-community/rules/community.sinks.json) 这些 sink 被放在一起并使用 `kind` 对这些 sink 进行归类：

```json
[
    {"kind":"command-injection","signature":"java.lang.Runtime: * load(String)","subtypes":false,"arg":"Argument[0]","provenance":"ai-manual","ext":""},
    {"kind":"command-injection","signature":"java.lang.Runtime: * loadLibrary(String)","subtypes":false,"arg":"Argument[0]","provenance":"ai-manual","ext":""},
    ...
]

```

以 `summaries.json` 为后缀的文件 如 [general.summaries.json](/corax-config-general/rules/general.summaries.json)

```json
[
  {"signature":"java.lang.AbstractStringBuilder: * <init>(String)","subtypes":true,"argTo":"Argument[this]","propagate":"taint","argFrom":"Argument[0]","provenance":"manual","ext":""},
  {"signature":"java.lang.AbstractStringBuilder: * append(**)","subtypes":true,"argTo":"ReturnValue","propagate":"value","argFrom":"Argument[this]","provenance":"manual","ext":""}
  ...
]
```

- `signature` 可以为 `matchSoot` 格式 或者 为 `matchSimpleSig` 格式。
- subtypes 表示是否 handle 子类的重写方法（static method 必定为 false, interface 必定为 true）
-  `"propagate": "taint"` 意为 taint kinds 传递，在方法执行后，to 和 from 可能指向了不同的对象，如果不同，那么 to 被 taint 了或副作用了，from 仍然不受影响，除非在程序中存在别名关系。
- `"propagate": "value"` 意为值传递，to 和 from 指向了一个对象。
- `provenance`，和 `ext` 无关紧要，保留字段。




## 场景三：CoraxJava规则检查器修改

通过修改部分代码或参数的形式，实现自定义的规则检查器。

开发者可以根据想要检测漏洞类型与本社区版[已开放实现的规则](feature_diff.md#已开放规则)类比，找到相近似的规则代码实现，再进行仿照即可。举例说明:

1. 命令注入规则检查器是典型的Taint类型的检查器，如注入问题（SSRF检测、SQL注入、XSS注入、路径穿越、模板注入、开放重定向、etc ...）和敏感信息泄露两大类taint问题都可以参考此类型的规则检查器实现。
2. SQL注入规则检查器的实现除了经典Taint类型的检查方式以外，还包括了适配MyBatis框架的注入Sink点检查支持。
3. XSS漏洞的Spring ResponseBody注入检查实现大致方式为通过匹配相应Java注解以动态检查sink点，类似的有 hibernate.jpa `Query` 注解和 ibatis `Select` 注解场景的 sql 注入检查。
4. cookie属性设置检查主要展示了API调用序列组合检查和特定参数的检测，类似的有 PermissiveCors 检查。
5. 不安全的TLS版本检测，实现主要是特定API的检测和常量参数的检测，类似的有 密码学误用、weak hash algorithm 和 cookie persistent maxAge、Ldap anonymous 等检查
6. 省略，可以详见本项目中其他规则开源实现



## 场景四：CoraxJava规则检查器定制

1. 定义 `Checker `参考 [IChecker](plugin-infrastructure.md#ichecker) （如果存在则下一步）
   1. 定义 `Rule` 参考 [IRule](plugin-infrastructure.md#irule)
   2. 定义 `BugCategory` [IBugCategory](plugin-infrastructure.md#ibugcategory)
   
2. 定义 `CheckType` 参考 [CheckType](plugin-infrastructure.md#checktype) （如果存在则下一步）
   
   1. 定义 bug message 参考 [BugMessage](plugin-infrastructure.md#bugmessage)
   
3. 选择一种 `CheckerUnit`，需要根据检查器要检查的 bug 及原理选择一个合适的 Analysis ，参考 [CheckerUnit](plugin-infrastructure.md#checkerunit)
   1. [PreAnalysisUnit](plugin-infrastructure.md#preanalysisunit)
   2. [AIAnalysisUnit](plugin-infrastructure.md#aianalysisunit)
   
4. 选择好后，开始按照需求和对应检查原理编写检查器，并在调用 `ICheckPoint.report(checkType)` 或者 `IOperatorFactory.check(boolExpr, checkType)` 时传入第二步中的 CheckType 即可。

   如果需要检查您编写的 checker 里的一些比较直接的bug，可以按如下两个步骤检查

   1. 编辑 [AnalyzerConfigRegistry](/corax-config-community/src/main/kotlin/com/feysh/corax/config/community/AnalyzerConfigRegistry.kt) 文件，在 `preAnalysisImpl` 或 `aiCheckerImpl` 添加新加的 checker 注册。
   2. 添加完后可以执行一次 `gradlw :corax-config-community:test --tests "com.feysh.corax.config.tests.ConfigValidate.validate"`，能够检查部分checker编写错误并提示纠正

5. 前往 [corax-config-tests/normal/src/main/java/testcode](/corax-config-tests/normal/src/main/java/testcode) 编写对应的不合格和合规代码用来测试和保障分析精度，参考 [单元测试](unit-tests.md) 

6. 执行 `gradlew build` 编译并打包出最终的配置 [build/analysis-config](/build/analysis-config)

7. 按照  [Readme.md#开始分析](/Readme.md#开始分析) 加载配置开始分析

8. 查看报告 [sarif](/build/output/sarif) ，如有误漏报请分析原因，是否需要改正优化检查器，参考[结果输出](usage.md#结果输出)

更详细的检测方法可参考 [自定义规则检查器详细说明](checker-detail.md)。