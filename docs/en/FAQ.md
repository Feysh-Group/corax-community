### Q&A

**Q: What factors affect analysis accuracy?**

A: Analysis accuracy is influenced not only by the analysis algorithm of the `core analysis engine` but also by the user's input parameter configuration. Currently, there are several reasons:

1. Completeness of provided runtime classes, binary existence of obfuscation, etc. (requires user assistance).

2. Problems with runtime command parameter configuration.

3. Code quality of the rule checker and various parameters in the configuration.

4. The accuracy of the core analysis engine algorithm and some analysis limitations (necessary compromises in time and memory).



**Q: Why does analysis require the compiled output of source code?**

A: In terms of static analysis technology implementation, pure source code analysis often requires converting it into an AST (Abstract Syntax Tree). For languages running on the JVM (Java, Kotlin, JSP, Scala, and Groovy, etc.), designing a one-to-one language front end to parse is required. AST contains various complex language features and syntax sugars across different versions, significantly increasing the burden on the analysis engine. This is not the focus of static analysis, so Java static analysis often directly or indirectly analyzes JVM bytecode (various forms of compiled artifacts class) to obtain the program's operational semantic information.
On the other hand, source code analysis requires various language environments and dependencies. For example, with `import package.a.*;` if the environment does not have the `package.a` package, all references to declarations under that package will fail to resolve due to incomplete necessary dependency information, leading to loss of analysis accuracy. Therefore, analyzing classes usually yields more accurate reports than analyzing source code.
Additionally, using source code analysis would greatly limit the use cases of static analysis, for example, analyzing a binary package without source code.

**Q: Why is source code necessary for analysis?**

A: Source code is not mandatory; it is provided for better display of defect reports. Also, when using the `--auto-app-classes` parameter, it can help the analyzer match corresponding classes based on source code to determine which classes are project classes and which are library classes. This assists the analyzer in focusing on analysis and simplifying parameter configuration.

**Q: Why use Kotlin?**

A: Code written in Kotlin is simpler and easier to understand, significantly improving developer productivity and maintainability. It is less prone to the headache of `NullPointerException` issues. Kotlin also supports coroutine asynchronous execution, greatly boosting the efficiency of the analyzer. Furthermore, Kotlin supports more language features, allowing more time to be spent on algorithms rather than programming :). Exciting, right? Of course, you can still write Java code and mix it with Kotlin code without any additional effort.
