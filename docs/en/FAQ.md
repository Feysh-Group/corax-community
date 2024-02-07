# Q&A

**Q: What factors affect analysis accuracy?**

A: Analysis accuracy is influenced not only by the analysis algorithm of the `core analysis engine` but also by the user's input parameter configuration. Currently, there are several reasons:

1. Completeness of provided runtime classes, binary existence of obfuscation, etc. (requires user assistance).

2. Problems with runtime command parameter configuration.

3. Code quality of the rule checker and various parameters in the configuration.

4. The accuracy of the core analysis engine algorithm and some analysis limitations (necessary compromises in time and memory).



**Q: Why does analysis require the compiled output of source code?**

A:  Here is the translated content in English:

**Q: Why is it necessary to analyze the output of compiled source code?**

A:  In terms of implementing static analysis techniques, pure source code analysis often requires it to be first converted into an Abstract Syntax Tree (AST). For languages running on the JVM (such as Java, Kotlin, JSP, Scala, and Groovy), a one-to-one language front-end needs to be designed for parsing. The AST contains various complex language features and syntax sugars from different versions, which significantly increases the burden on the analysis engine. Moreover, this is not the focus of static analysis. Therefore, static analysis of Java often directly or indirectly analyzes the JVM bytecode (i.e., various forms of compiled output such as class files) to obtain program operational semantic information.

​	On one hand, source code analysis requires various language environments and dependencies. For example, if there is an `import package.a.*;` statement but the environment does not have the package `package.a`, then all references to declarations in that package will fail to parse or result in loss of analysis accuracy due to incomplete dependency information.

​	On the other hand, using source code analysis will greatly limit the usage scenarios of static analysis. For example, if you want to analyze a binary package without the accompanying source code.

​	Ultimately, source code analysis can only analyze source code. However, the vast majority of third-party libraries do not have source code. Analyzers that depend on source code will encounter broken analysis chains when analyzing cross-module calls (i.e., inter-procedural calls) due to the absence of source code. For example, when methods for sources, sinks, or propagation of tainted data are in third-party libraries, analysis schemes that rely on source code will result in a large number of false negatives.

​	Therefore, analyzing classes often yields more accurate reports than analyzing source code.

**Q: Why is source code necessary for analysis?**

A: Source code is not mandatory; it is provided for better display of defect reports. Also, when using the `--auto-app-classes` parameter, it can help the analyzer match corresponding classes based on source code to determine which classes are project classes and which are library classes. This assists the analyzer in focusing on analysis and simplifying parameter configuration.

**Q: Why use Kotlin?**

A: Code written in Kotlin is simpler and easier to understand, significantly improving developer productivity and maintainability. It is less prone to the headache of `NullPointerException` issues. Kotlin also supports coroutine asynchronous execution, greatly boosting the efficiency of the analyzer. Furthermore, Kotlin supports more language features, allowing more time to be spent on algorithms rather than programming :). Exciting, right? Of course, you can still write Java code and mix it with Kotlin code without any additional effort.
