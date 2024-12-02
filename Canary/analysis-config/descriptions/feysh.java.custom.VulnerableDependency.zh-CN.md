# 自定义三方库的引用检查

可以通过配置三方库检查规则来实现自定义三方库的检查，配置步骤如下：

修改探针目录下 `{探针解压目录}/lib/Plugins/Canary/analysis-config/rules/` 文件夹下的 `custom.libs.versions.json` 文件。

`custom.libs.versions.json` 各字段解释如下：

- `key`: **不能为null**，该条规则的**唯一标识**，请确保其唯一性，否则可能导致规则失效
- `bugMessage`: **可以为null**，该条规则的描述，用于在分析报告中显示
- `libraryCoordinate`: 三方库的maven坐标
    - `groupId`: **不能为null**，三方库的`groupId`
    - `artifactId`: **不能为null**，三方库的`artifactId`
- `compareMode`: **不能为null**，项目中可能同时引用**同一个三方库的多个不同版本**，因此需要多种比较模式来支持版本检测，现支持如下三种比较模式：
  - `Must`: 目标库的所有版本都必须满足条件
  - `May`: 目标库的所有版本中至少有一个满足条件
  - `MayOrUnknown`: 以下两种情况都可通过检测：
    - 目标库的所有版本中至少有一个满足条件
    - 目标库的版本号未知
- `lowerbound`: **可以为null**，目标库版本左区间比较运算
    - `op`: **不能为null**，比较运算符，支持 `LT("<")`、`LE("<="")`、`EQ("=="")`、`GE(">="")`、`GT(">)`
    - `version`: **不能为null**，目标库版本左区间值
- `upperbound`: **可以为null**，目标库版本右区间比较运算
    - `op`: 同上
    - `version`: **不能为null**，目标库版本右区间值

以下是 一个例子，用于检查 log4j 版本是否大于等于 2.0-alpha1 并且小于 2.16.0；fastjson 版本是否小于 1.2.83。

```json
[
  {
    "key": "custom-log4j",
    "bugMessage": "危险的log4j库引用",
    "libraryCoordinate": {
      "groupId": "org.apache.logging.log4j",
      "artifactId": "log4j-core"
    },
    "compareMode": "Must",
    "lowerbound": {
      "op": "GE",
      "version": "2.0-alpha1"
    },
    "upperbound": {
      "op": "LT",
      "version": "2.16.0"
    }
  },
  {
    "key": "custom-fastjson",
    "bugMessage": null,
    "libraryCoordinate": {
      "groupId": "com.alibaba",
      "artifactId": "fastjson"
    },
    "compareMode": "Must",
    "lowerbound": null,
    "upperbound": {
      "op": "LT",
      "version": "1.2.83"
    }
  }
]
```