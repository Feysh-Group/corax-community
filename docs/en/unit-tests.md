# Unit Test

**Table of contents**

[[_TOC_]]

## Functional Testing



## Vulnerability Tagging

Refer to [testcode/cmdi/CommandInjection.java](/corax-config-tests/normal/src/main/java/testcode/cmdi/CommandInjection.java) 

Think of it as an exam

- The testcase is like the exam question
- Tagging is like the standard answer (by marking the expected (expect) reports and unexpected (escape) reports in the code)
- The checker and analyzer are like students

After the analysis is completed, the Corax Java analyzer will parse all the source code files it can find, count the tagging data (correct answers), compare it with the reports obtained from the analysis, and then output the scores (score table) to provide feedback to drive our development and improvement.

Please refer to [Misreport Explanation](usage.md#false-positivenegative-forms) for the annotated analysis results.

### Non-compliant Vulnerability Tagging

Append a comment at the beginning or end of the line where the vulnerability is located to tag the answer.

The format is `//  $ BUGNAME1 BUGNAME2 ... `, starting with `$` followed by multiple `BUGNAME` separated by `spaces` (Comments with `/**/` are not supported for now).

`BUGNAME` can be `CheckTypeName, CheckerName, CheckerName.CheckTypeName, {CheckType.aliasNames custom aliases}, {Rule.realName, such as cwe-123}`



### Compliant Code Tagging

The format is `//  !$ BUGNAME1 BUGNAME2 ... `, and the format of compliant code tagging is highly consistent with non-compliant code tagging. The only difference is to add `!` at the beginning to indicate that this report should be avoided, otherwise reporting this `BUGNAME` is a false positive.