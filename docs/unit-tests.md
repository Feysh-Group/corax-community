# 单元测试

**Table of contents**

* [功能测试](#功能测试)
* [漏洞标注](#漏洞标注)
  * [不合规漏洞标注](#不合规漏洞标注)
  * [合规代码标注](#合规代码标注)

## 功能测试



## 漏洞标注

参考 [testcode/cmdi/CommandInjection.java](../corax-config-tests/src/main/java/testcode/cmdi/CommandInjection.java) 

理解为和考试一样

- testcase就是 考试题目
- 标注就是 标准答案（通过在代码中标注期望（expect）的报告和不期望（escape）的报告）
- checker 和分析器就是 考生

Corax Java 分析器会在分析结束后解析所有能找到的源码文件统计标注数据（正确答案），和分析得到的报告进行比对打分，然后再输出成绩（积分表），用来反馈驱动我们开发和完善

标注后的分析结果请参考 [误漏报表单](usage.md#误漏报表单)

### 不合规漏洞标注

在漏洞的同一行的起始或末尾追加一点注释来标注答案

格式为 `//  $ BUGNAME1 BUGNAME2 ... `  以 `$` 符开头的多个 `空格` 分割的 `BUGNAME`  （暂不支持 `/**/`注释）

`BUGNAME`  可以是 `CheckTypeName、CheckerName、CheckerName.CheckTypeName、{CheckType.aliasNames 自定义别名}、{Rule.realName 比如 cwe-123}`



### 合规代码标注

格式为 `//  !$ BUGNAME1 BUGNAME2 ... `  和不合规代码的标注格式高度一致，只是在前面加上 `!` 表示应该避免此报告，否则报告了这个 `BUGNAME` 就是误报

