# Customize library import check

You can custom library check via configuration rules. Configuration Steps:

Modify the file: `{Proble Extract Folder}/lib/Plugins/Canary/analysis-config/rules/custom.libs.versions.json`.

The meanings of the fields in `custom.libs.versions.json` file:

- `key`: **NotNullable**. The `unique identifier` for this rule. Please ensure its uniqueness; otherwise, it may cause the rule to fail.
- `bugMessage`: **Nullable**. The description of the rule is used to display it in the analysis report
- `libraryCoordinate`: The maven coordinate of library 
    - `groupId`: **NotNullable**. The `groupId` of library
    - `artifactId`: NotNull. The `artifactId` of library
- `compareMode`: **NotNullable**. The project may **import multiple different versions of the same library**, so multiple comparison modes are needed to support version detection. Currently, the following three comparison modes are supported:
  - `Must`: All versions of the target library must meet the conditions
  - `May`: At least one version of the target library must meet the conditions
  - `MayOrUnknown`: Both of the following conditions can be detected:
     - At least one version of the target library meet the conditions
     - The target library version is unknown
- `lowerbound`: **Nullable**. Left interval comparison operation for the target library version
    - `op`: **NotNullable**. Comparison operators, support: `LT("<")`、`LE("<="")`、`EQ("=="")`、`GE(">="")`、`GT(">)`
    - `version`: **NotNullable**. Left interval value for the target library version
- `upperbound`: **Nullable**. Right interval comparison operation for the target library version
    - `op`: is same to `op` of `lowerbound` 
    - `version`: **NotNullable**. Right interval value for the target library version

Here is an example to check if the log4j version is greater than or equal to 2.0-alpha1 and less than 2.16.0, and if the fastjson version is less than 1.2.83.

```json
[
  {
    "key": "custom-log4j",
    "bugMessage": "dangerous log4j library import",
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