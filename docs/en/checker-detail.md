# Custom Checker

**Table of contents**

[[_TOC_]]


## Overview
Customization is based on requirements, and there are several methods available:

1. Modify the yml configuration file [analysis-config/default-config.yml](/build/analysis-config/default-config.yml).
2. Modify the configuration files in the rules folder [analysis-config/rules](/build/analysis-config/rules).
3. Modify existing checker source code and compile.
4. Custom develop a new checker and compile.



How to write a checker is the main content of this document.

**To custom develop a new checker, the following simple steps are required (in order):**

1. Define `Checker` referring to [IChecker](plugin-infrastructure.md#ichecker) (if it exists then proceed)
   1. Define `Rule` referring to [IRule](plugin-infrastructure.md#irule)
   2. Define `BugCategory` [IBugCategory](plugin-infrastructure.md#ibugcategory)
   
2. Define `CheckType` referring to [CheckType](plugin-infrastructure.md#checktype) (if it exists then proceed)
   
   1. Define bug message referring to [BugMessage](plugin-infrastructure.md#bugmessage)
   
3. Choose a `CheckerUnit`, based on the bugs and principles to be checked by the checker, choose a suitable Analysis, refer to [CheckerUnit](plugin-infrastructure.md#checkerunit)
   1. [PreAnalysisUnit](plugin-infrastructure.md#preanalysisunit)
   2. [AIAnalysisUnit](plugin-infrastructure.md#aianalysisunit)
   
4. Once chosen, start writing the checker according to the requirements and corresponding check principles, and pass the CheckType from the second step when calling `ICheckPoint.report(checkType)` or `IOperatorFactory.check(boolExpr, checkType)`.

   If you need to check some more direct bugs in your written checker, you can follow these two steps to check:

   1. Edit the [AnalyzerConfigRegistry](/corax-config-community/src/main/kotlin/com/feysh/corax/config/community/AnalyzerConfigRegistry.kt) file, and add newly registered checkers in `preAnalysisImpl` or `aiCheckerImpl`.
   2. After adding, you can execute `gradlew :corax-config-community:test --tests "com.feysh.corax.config.tests.ConfigValidate.validate"`, which can check for some errors in the checker and prompt for correction.

5. Go to [corax-config-tests/normal/src/main/java/testcode](/corax-config-tests/normal/src/main/java/testcode) to write corresponding non-compliant and compliant code for testing and ensuring analysis accuracy, refer to [Unit Testing](unit-tests.md).

6. Execute `gradlew build` to compile and package the final configuration in [build/analysis-config](/build/analysis-config).

7. Follow [Readme.md#Getting Started with Analysis](/Readme.md#Getting Started with Analysis) to load the configuration and start analysis.

8. View the report in [sarif](/build/output/sarif), if there are any false positives or false negatives, analyze the reasons and decide whether corrections or optimizations are needed, refer to [Result Output](usage.md).



## Static Code Property Checks

Need to inherit [PreAnalysisUnit](plugin-infrastructure.md#preanalysisunit) and override `context (PreAnalysisApi) fun config()`

### Function Call Checks

Custom checks can be implemented using the following API:

```kotlin
PreAnalysisApi.atAnyInvoke(){}                 // Traverse all Java method invocation points (call edge)
PreAnalysisApi.atInvoke(callee){ }             // Return invocation points (call edge) calling the callee method
```

The two methods mentioned above are Kotlin callback methods, where the parameter `this` is of type `IInvokeCheckPoint`.

```kotlin
atInvoke(methodMatch) {  this: IInvokeCheckPoint ->
    // You can access all properties in 'this' for inspection
    if (nonCompliantCode) {
        report(CheckType)
        // Or
        report(CheckType){ this: BugMessage.Env ->
            // You can customize the reported line and column numbers, etc.
            this.lineNumber = ?
            this.columnNumber = ?
            // You can customize passing key-value pairs
            // So, ` msgGenerator { "Calling ${args["some key"]} in $callee method is non-compliant" } ` can dynamically generate a message by reading some value
            this.args["some key"] = "some value"
        }
    }
}
```



The complete `IInvokeCheckPoint` contains the following members for use.

```kotlin
interface IInvokeCheckPoint : ICheckPoint {
    // The method at the callSite unit
    val container: SootMethod

    // the unit contains a call edge
    val callSite: soot.Unit?

    // The declared method to (virtual)Invoke
    val invokeMethodRef: SootMethodRef?

    // instanceInvokeExpr.base
    val declaredReceiverType: Type?

    // the actual method dispatched by the declared invokeMethodRef
    val callee: SootMethod

    val invokeExpr: InvokeExpr?
}
```

Complete Example: [weak-ssl.default-http-client](/corax-config-community/src/main/kotlin/com/feysh/corax/config/community/checkers/weak-ssl.kt)



### Text Content Parsing Checks

You can perform checks for:

- Plaintext sensitive data such as email addresses, URLs, addresses, public/private keys, phone numbers, and more.
- Utilize custom parsing tools to check for specific issues.
- Parse configurations without performing checks, but store critical data for assistance in analysis through the `AIAnalysisUnit`.

Customize checks using the following API:

```kotlin
atAnySourceFile(extension=?) { }  // Traverse all resource files with a specific extension
```

The second parameter in the above method is a Kotlin callback function. In this method, the `this` parameter refers to the `ISourceFileCheckPoint`.

```kotlin
// extension = null allows checking all project files
// extension = "xml" allows checking all files with the xml extension in the project
atAnySourceFile(extension = "xml") {
    val doc = parseXmlSafe(path) ?: return@atAnySourceFile
    if (doc isNonCompliant) {
        report(CheckType)
    }
}
```



Full `ISourceFileCheckPoint` includes the following members for use:

```kotlin
interface ISourceFileCheckPoint : ICheckPoint {
    val path: Path
    val fielname: String get() = path.name

    @Throws(IOException::class)
    suspend fun readAllBytes(): ByteArray
    suspend fun text(): String?
    suspend fun lines(): List<IndexedValue<String>>
}
```

Complete Example: [frameworks/persistence/ibatis/mybatis](/corax-config-community/src/main/kotlin/com/feysh/corax/config/community/checkers/frameworks/persistence/ibatis/mybatis)



### Java AST Checks

Edit [corax-config-community/build.gradle.kts](/corax-config-community/build.gradle.kts) and add the dependency:

```kotlin

val javaparserVersion: String by rootProject

dependencies {
    ...
    implementation(group = "com.github.javaparser", name = "javaparser-core", version = javaparserVersion)
    ...
}
```



```kotlin
import com.feysh.corax.cache.AnalysisCache
import com.feysh.corax.cache.analysis.CompilationUnitAnalysisKey
import com.github.javaparser.ParseResult
import com.github.javaparser.ast.CompilationUnit

object ExampleCheckUnit: PreAnalysisUnit() {
    context (PreAnalysisApi)
    override fun config() {
        atAnySourceFile(extension = "java") {
            // Parsing is as simple as this one-liner
            // Executed asynchronously
            val parseResult: ParseResult<CompilationUnit> = AnalysisCache.G.getAsync(CompilationUnitAnalysisKey(path))
            if (!parseResult.isSuccessful) {
                return@atAnySourceFile
            }
            val javaCompilationUnit = parseResult.result.get()
            if (javaCompilationUnit.isNonCompliant) {
                report(CheckType)
            }
        }
    }
}
```



As seen, you can define similar parsers like this `Analysis`, and accessing the parsing results through the global `AnalysisCache.G` is straightforward.

The global cache `AnalysisCache.G` efficiently saves memory, reduces redundant computations, and is thread-safe.

Using [javaparser](https://github.com/javaparser/javaparser) to parse Java source code into an AST is encapsulated as follows (this is built-in, no need to add it again):

```kotlin
package com.feysh.corax.cache.analysis

import com.feysh.corax.cache.AnalysisCache
import com.feysh.corax.cache.AnalysisDataFactory
import com.feysh.corax.cache.AnalysisDataFactory.Companion.defaultBuilder
import com.feysh.corax.cache.AnalysisKey
import com.github.benmanes.caffeine.cache.LoadingCache
import com.github.javaparser.JavaParser
import com.github.javaparser.ParseResult
import com.github.javaparser.ast.CompilationUnit
import java.nio.file.Path
import kotlin.io.path.inputStream

// Define input
data class CompilationUnitAnalysisKey(val sourceFile: Path) :
    AnalysisKey<ParseResult<CompilationUnit>>(CompilationUnitAnalysisDataFactory.key/*Select the implementation that handles this sourceFile*/)

// Define output
// The factory function definition may seem a bit complex, but it is very convenient and user-friendly in usage.
object CompilationUnitAnalysisDataFactory :
    AnalysisDataFactory<ParseResult<CompilationUnit>, CompilationUnitAnalysisKey> {
    override val cache: LoadingCache<CompilationUnitAnalysisKey, ParseResult<CompilationUnit>> = defaultBuilder
        .build { key ->
            val configuration = ParserConfiguration()
            val parser = JavaParser(configuration)
            key.sourceFile.inputStream().use { inputStream ->
                parser.parse(inputStream, Charsets.UTF_8)
            }
        }

    override val key = object : AnalysisDataFactory.Key<ParseResult<CompilationUnit>>() {}
    
    init {
        AnalysisCache.G.registerFactory(this)
    }
}
```



### Soot Jimple IR Checks

To check Jimple IR, you need to obtain `SootMethod`, and use the following API for custom checks:

```kotlin
PreAnalysisApi.atAnyMethod(){ }                 // Traverse all SootMethods
PreAnalysisApi.atMethod(method){ }              // Match a specified SootMethod
```

**Of course, if you are a professional, you can directly get the Jimple IR, use it in intra/inter procedural data flow Analysis, and then add this Analysis to the global `AnalysisCache.G` (Refer to the above AnalysisCache), which can be easily extended.**

```kotlin

object ExampleCheckUnit: PreAnalysisUnit() {    
    context (PreAnalysisApi)
    override fun config() {
        atAnyMethod { this: IMethodCheckPoint ->

            if (sootMethod needs to be excluded)
                return@atAnyMethod

            eachUnit { this: IUnitCheckPoint ->
                eachExpr { expr: soot.jimple.Expr ->
                    // For example, get == and != Expr in Java
                    val condExpr: ConditionExpr = when (expr) {
                        is JEqExpr -> expr
                        is JNeExpr -> expr
                        else -> null
                    } ?: return@eachExpr
                }
            }
        }
    }
}
```



// Complete `IMethodCheckPoint` includes the following members for use

```kotlin
interface IMethodCheckPoint : ICheckPoint {
    val sootMethod: SootMethod
    val visibilityAnnotationTag: VisibilityAnnotationTag?

    fun eachUnit(block: IUnitCheckPoint.() -> Unit)
}
```

Complete `IUnitCheckPoint` includes the following members for use

```kotlin
interface IUnitCheckPoint : ICheckPoint {
    val unit: soot.Unit
    fun eachExpr(block: (Expr) -> Unit)
}
```





### Annotation Checks

Java `Annotation` annotations generally exist in Class, Field, and Method. Use the following API for custom checks:

```kotlin
PreAnalysisApi.atAnyClass(){ }                  // Traverse all SootClasses
PreAnalysisApi.atAnyField(){ }                  // Traverse all SootFields
PreAnalysisApi.atAnyMethod(){ }                 // Traverse all SootMethods
PreAnalysisApi.atClass(clazz){ }                // Match a specified SootClass
PreAnalysisApi.atField(field){ }                // Match a specified SootField
PreAnalysisApi.atMethod(method){ }              // Match a specified SootMethod
```



Example

```kotlin
object ExampleCheckUnit: PreAnalysisUnit() {    
    context (PreAnalysisApi)
    override fun config() {
        atAnyMethod { this: IMethodCheckPoint ->
            visibilityAnnotationTag?.annotations?.forEach {
                when (it.type) {
                    "Ljavax/jws/WebMethod;" -> report(EndpointExposeChecker.JaxwsEndpoint)
                    "Ljavax/ws/rs/Path;" -> report(EndpointExposeChecker.JaxrsEndpoint)
                    in SpringConfigs.option.REQUEST_MAPPING_ANNOTATION_TYPES -> report(EndpointExposeChecker.SpringEndpoint)
                }
            }
        }
    }
}
```





## Framework Modeling Adaptation

You need to inherit [AIAnalysisUnit](plugin-infrastructure.md#aianalysisunit) and override the `context (AIAnalysisApi) fun config()` method.



### MyBatis Configuration Parsing

For checking issues in mybatis `mapper.xml` files where SQL strings are concatenated (e.g., `$` concatenation), which can lead to injection vulnerabilities.

Therefore, the detection in this framework needs to be divided into two steps:

1. Implement `PreAnalysisUnit` to parse `xml` files in resources and extract risk method signatures. 
   See [mybatis-sql-injection-checker.kt#parseMybatisMapperAndConfig](/corax-config-community/src/main/kotlin/com/feysh/corax/config/community/checkers/frameworks/persistence/ibatis/mybatis/mybatis-sql-injection-checker.kt).
2. Implement `AIAnalysisUnit` to check for injections. Handle and check if the dangerous parameters in the SQL query methods provided in the previous step are tainted.
   See [mybatis-sql-injection-checker.kt#checkMybatisStatement](/corax-config-community/src/main/kotlin/com/feysh/corax/config/community/checkers/frameworks/persistence/ibatis/mybatis/mybatis-sql-injection-checker.kt).



Complete Source Code: [mybatis modeling](/corax-config-community/src/main/kotlin/com/feysh/corax/config/community/checkers/frameworks/persistence/ibatis/mybatis)



### Spring Modeling

Complete source code:

source: [general/model/framework/spring/SpringAnnotationSource.kt](/corax-config-general/src/main/kotlin/com/feysh/corax/config/general/model/javaee/JavaeeAnnotationSource.kt)

sink: [community/checkers/frameworks/spring/ResponseBodyCheck.kt](/corax-config-community/src/main/kotlin/com/feysh/corax/config/community/checkers/frameworks/spring/ResponseBodyCheck.kt)



## Rules Data

It exists in the following locations within the project:

[corax-config-general/rules](/corax-config-general/rules)

[corax-config-community/rules](/corax-config-community/rules)

After running `gradlew build`, it will be copied to this location: [build/analysis-config/rules](/build/analysis-config/rules)



You can customize any serializable class and load it from the rules file. For reference:

```kotlin
@Serializable // kotlin serialization
data class Custom(
    @Required @SerialName("custom-name") 
    val yourCustomField: AnySerializableObject,
    ....
)

val jsonDirs = ConfigCenter.getConfigDirectories()
val someJsonFiles = walkFiles(jsonDirs) { file -> file.name.endsWith("customSuffix.json") }

val rules = RuleManager.load<Custom>(someJsonFiles, serializer())
```



Currently, the `corax-config-general` module includes the following types of files and their corresponding serializers:

`*.sources.json` corresponds to `com.feysh.corax.config.general.model.taint.TaintRule.Source`

`*.summaries.json` corresponds to `com.feysh.corax.config.general.model.taint.TaintRule.Summary` 

`*.sinks.json` corresponds to `com.feysh.corax.config.general.model.taint.TaintRule.Sink`

`*.access-path.json` corresponds to `com.feysh.corax.config.general.rule.MethodAccessPath`



## Data Flow Modeling

### Value Transmission Modeling

An example of value transmission modeling：
For example, consider `java.util.Map`:

```kotlin
listOf(
    method(java.util.Map<Any, Any>::put),
    method(java.util.HashMap<Any, Any>::put),
    method(java.util.LinkedHashMap<Any, Any>::put),
    method(java.util.WeakHashMap<Any, Any>::put),
    method(java.util.IdentityHashMap<Any, Any>::put)
).forEach {
    it.modelNoArg {
        // Value transmission (objects or numbers)
        // Indicates that the return may point to the original return value (value analyzed by the analyzer) or to any MapValue of this
        `return`.value = `return` anyOr `this`.field(MapValues)
        `this`.field(MapKeys).value = `this`.field(MapKeys).value anyOr (p0.value)
        `this`.field(MapValues).value = `this`.field(MapValues).value anyOr (p1.value)
        
        // If `return`.value = p0, it means p0 is definitely assigned to return
    }
}
```



Additionally, let's introduce the professional term: Alias. What does it mean?

For example, Lu Xun is also known as Zhou Shuren. Both names refer to the same person, and these two names are aliases for each other.

```
local space:
    x -> object-Foo
    a -> person-object-Lu Xun

stmts:
    1: x.person = a
    2: c = x.person
```

After the stmt is executed, `{ a, object-Foo.person, c }` will have alias relationships, and their PointToSet will be `{ person-object-Lu Xun }`.





#### TODO: To be implemented

Handling class member Field reads and writes is also possible.





### Taint Propagation Modeling

Taint, or stain, is generally a Boolean flag used in program analysis to mark whether an object is tainted. However, in Corax, taint is a set collection formed by a series of taint kinds.

Taint belongs to semantic analysis, so [AIAnalysisUnit](plugin-infrastructure.md#aianalysisunit) should be used.

First, let's look at the definition of a taint source:

```kotlin

import javax.servlet.ServletRequest
import javax.servlet.ServletRequestWrapper
import javax.servlet.http.HttpServletRequest // If it doesn't exist, you can manually add the module dependency
import javax.servlet.http.HttpServletRequestWrapper

object ExampleCustomSourceUnit : AIAnalysisUnit() {
    
    context(AIAnalysisApi) 
    override fun config() {
        // You can use matchSimpleSig or matchSoot-like method matching to match one or more methods
        method(matchSimpleSig("javax.servlet.ServletRequest: String getParameter(String)")) // Returns an IMethodDecl
            .modelNoArg {                                                                   // Add a handler
                // Since this, return are Kotlin keywords, they need to be enclosed in ``
                // The statement "=" here has a side effect, passing the taint set collection on the right () to the left `return`'s taint field
                `return`.taint = taintOf(internetSource)  
            }
        
        
        // You can directly use ClassName:MethodName to quickly match a method. If there is a template, you can use method<R, THIS, P0, P1...>(CLASS::METHOD)
        // The following three methods can all pass in a kfunction to get IMethodDecl
        //  1: constructor(kfunction) kfunction must be a constructor, otherwise it will throw an error directly
        //  2: staticMethod(kfunction) kfunction must be a static method
        //  3: method(kfunction) kfunction must be an instance method
        listOf(
            method(ServletRequest::getParameterMap),// return type: Map<String, String[]>
            method(ServletRequestWrapper::getParameterMap),
            method(HttpServletRequest::getParameterMap),
            method(HttpServletRequestWrapper::getParameterMap),
        ).forEach { method ->
            method.modelNoArg {
                // Set the taint attribute of each MapKey in the returned value of Map to taint kinds: internetSource
                `return`.field(MapKeys).taint = taintOf(internetSource)
                // Set the taint attribute of each array element of MapValue in the returned value of Map to taint kinds: internetSource
                `return`.field(MapValues).field(Elements).taint = taintOf(internetSource)  
            }
        }
        
    }
}
```



Java test code:

```java
@RequestMapping("/sendRedirect")
@ResponseBody
public static void sendRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String url = request.getParameter("url");
    response.sendRedirect(url); // $UnvalidatedRedirect
}
```

This way, the taint attribute is attached to the returned value when `ServletRequest.getParameter()` is called and then passed to the url variable.

We only need to get the taint attribute of the incoming parameter at the sink point, check if it contains certain taint kinds, and report.



Next, let's look at the definition of a summary:

Suppose we want to model a URL. Here are the member fields of the URL:

```java
package java.net;
public final class URL implements java.io.Serializable {
    private String protocol;
    private String host;
    private String file;
    ....
}
```

Generally, the internal implementation (code) of various implementations in the JDK is very large. Considering the efficiency of analysis, this part of the code is generally not analyzed by default (although it can be manually analyzed, it is not necessary) and the Summary abstraction system is used to abstract method behavior and effects (wrapper).

```kotlin

// If the file is tainted, then only `this.file` is tainted, `this.protocol` and `this.host` are not tainted. However, we don't need to represent the taint-bound objects too precisely. We can directly propagate the taint to the URL object itself, which means if any of the parameters (protocol, host, file) is tainted, the URL object `this` is tainted. Of course, we can also accurately represent tainting each field of the URL separately, but it's not very necessary.
constructor<URL, String, String, String>(::URL) // URL(String protocol, String host, String file)
    .modelNoArg {
        // Note that these three statements are ordered and flow-sensitive.
        `this`.taint += p0.taint
        `this`.taint += p1.taint
        `this`.taint += p2.taint
    }

// Taint is propagated to the URL object itself, so the taint attribute should also be propagated or checked at the sink location. Otherwise, the modeling is ineffective or incomplete.
// For example, in the following case, the taint is propagated to the String type, and the String type also has a method summary. In this way, the taint can continue to be propagated in the Java program to ensure low false negatives:
listOf(
    method(java.net.URI::getAuthority),
    method(java.net.URI::getFragment),
    method(java.net.URI::getHost),
    method(java.net.URI::getPath),
    method(java.net.URI::getQuery),
    method(java.net.URI::getRawAuthority),
    method(java.net.URI::getRawFragment),
    method(java.net.URI::getRawPath),
    method(java.net.URI::getRawQuery),
    method(java.net.URI::getRawSchemeSpecificPart),
    method(java.net.URI::getRawUserInfo),
    method(java.net.URI::getScheme),
    method(java.net.URI::getSchemeSpecificPart),
    method(java.net.URI::getUserInfo),
    method(java.net.URI::normalize),
    method(java.net.URI::parseServerAuthority),
    method(java.net.URI::toASCIIString),
    method(java.net.URI::toString),
    method(java.net.URI::toURL),
).forEach { method ->
    method.modelNoArg {
        // Equivalent to `return`.taint = `return`.taint + `this`.taint, generally used to prevent conflicts caused by multiple modeling of the same method. Therefore, it is recommended to use += to ensure that the property only increases and does not decrease.
        `return`.taint += `this`.taint
    }
}
```







### Configuration File Modeling

​		For taint propagation, source, sink modeling, the format is mostly monotonous, and it may be cumbersome to write in code. It can be more straightforward by writing simple modeling to a file according to a certain format. However, for complex modeling, it is still recommended to use Kotlin hard coding. 

​		The [RuleManager](plugin-infrastructure.md#rulemanager) introduced earlier is responsible for parsing rule files. It loads rules, and then, in [model/taint/TaintModelingConfig.kt](/corax-config-general/src/main/kotlin/com/feysh/corax/config/general/model/taint/TaintModelingConfig.kt), it reads `ConfigCenter.taintRulesManager.sources` and `ConfigCenter.taintRulesManager.summaries`, applying these rules to achieve unified and rapid modeling. All configuration formats, parsing, and management are customizable without any restrictions.

```kotlin
context(AIAnalysisApi)
override fun config() {
    val taintRulesManager = ConfigCenter.taintRulesManager
    for ((kind, taintTypes) in option.sourceKindToAppendTaintTypesMap) {
        val sources = taintRulesManager.sources.getRulesByGroupKinds(kind)
        for (sourceRule in sources) {
            applySourceRule(sourceRule, taintTypes)
        }
    }
    for (summary in taintRulesManager.summaries.rules) {
        applySummaryRule(summary)
    }
}
```



**Source Configuration File Rules**:

​		Files with the suffix `sources.json`, such as [general.sources.json](/corax-config-general/rules/general.sources.json). These sinks are grouped together and classified by `kind`. For example:

```json
[
  {"kind":"remote","signature":"javax.servlet.ServletRequest: * getInputStream()","subtypes":false,"arg":"ReturnValue","provenance":"manual","ext":""},
  {"kind":"remote","signature":"javax.servlet.ServletRequest: * getParameter(String)","subtypes":false,"arg":"ReturnValue","provenance":"manual","ext":""},
  ...
]
```

​		In the [model/taint/TaintModelingConfig.kt](/corax-config-general/src/main/kotlin/com/feysh/corax/config/general/model/taint/TaintModelingConfig.kt) file, the `sourceKindToAppendTaintTypesMap` defines the mapping relationship from `kind` to source taint kinds.

```kotlin
val sourceKindToAppendTaintTypesMap: Map<String, Set<ITaintType>> = mapOf(
    "remote" to internetSource,
    "fileIo" to fileIoSource,
    "userInput" to userInputSource,
)
```

​		For example, the `"kind":"remote"` corresponds to `com.feysh.corax.config.general.checkers.internetSource`. These taint kinds are automatically added to source methods based on their kind when applying these rules.

​		A simpler way is to directly customize and modify the yml main configuration file to define the mapping data without modifying the code. For example, [build/analysis-config/default-config.yml](/build/analysis-config/default-config.yml):

```yaml
...
- !<CheckerUnitOptionalConfig>
  name: "feysh-config-general:com.feysh.corax.config.general.model.taint.TaintModelingConfig"
  enable: true
  options: !<com.feysh.corax.config.general.model.taint.TaintModelingConfig.Options>
    sourceKindToAppendTaintTypesMap:
      "remote":
        - !<GeneralTaintTypes> "CONTAINS_CRLF"
        - !<GeneralTaintTypes> "CONTAINS_PATH_TRAVERSAL"
        - !<GeneralTaintTypes> "CONTAINS_SQL_INJECT"
        - !<GeneralTaintTypes> "CONTAINS_XSS_INJECT"
        - !<GeneralTaintTypes> "CONTAINS_XPATH_INJECT"
        - !<GeneralTaintTypes> "CONTAINS_COMMAND_INJECT"
        - !<GeneralTaintTypes> "CONTAINS_REDIRECTION_INJECT"
        - !<GeneralTaintTypes> "CONTAINS_OGNL_INJECT"
        - !<GeneralTaintTypes> "InternetData"
        - !<GeneralTaintTypes> "ControlData"
        "fileIo":
        - !<GeneralTaintTypes> "CONTAINS_CRLF"
        - ...
        ....
```



**Summary Configuration File Rules**

​		The configuration files with the suffix `summaries.json` are structured like [general.summaries.json](/corax-config-general/rules/general.summaries.json).

```json
[
  {"signature":"java.lang.AbstractStringBuilder: * <init>(String)","subtypes":true,"argTo":"Argument[this]","propagate":"taint","argFrom":"Argument[0]","provenance":"manual","ext":""},
  {"signature":"java.lang.AbstractStringBuilder: * append(**)","subtypes":true,"argTo":"ReturnValue","propagate":"value","argFrom":"Argument[this]","provenance":"manual","ext":""}
  ...
]
```

1. The `signature` can be in the `matchSoot` format or the `matchSimpleSig` format.
2. `subtypes` indicates whether to handle overridden methods in subclasses (static methods are always false, interfaces are always true).
3. - `"propagate": "taint"` means the propagation of taint kinds. After the method execution, `to` and `from` may point to different objects. If they are different, `to` has been tainted or affected, but `from` remains unaffected unless there is an alias relationship in the program.
   - `"propagate": "value"` means value propagation, as mentioned earlier, where `to` and `from` point to the same object.
4. `provenance` and `ext` are relatively unimportant and reserved fields.



**Sanitizer Configuration**

Open the [`???.summaries.json`](/corax-config-general/rules/supplement.summaries.json) configuration file and add a similar `sanitizer rule` to remove TaintKind:

```json
  {"signature":"org.springframework.web.util.HtmlUtils: * htmlEscape(**)","subtypes":false,"to":"ReturnValue","propagate":"taint","from":"Argument[0]","provenance":"manual","ext":""},
  "Following the taint propagation rule mentioned above, add an XSS sanitizer to indicate that the data returned by the htmlEscape method has been filtered or restricted, preventing XSS injection. However, Argument[0] can still cause XSS injection."
  {"signature":"org.springframework.web.util.HtmlUtils: * htmlEscape(**)","subtypes":false,"to":"ReturnValue","propagate":"sanitizer","from":"xss","provenance":"manual","ext":""}
```



If the analysis engine mistakenly believes that `Argument[0]` will contaminate the return value `ReturnValue` after analyzing the `taintInTaintOut` method, but in reality, it cannot contaminate it, which may lead to false positives. In such cases, you can use the following rule to forcefully remove all taint marks and reduce false positives.

```json
  {"signature":"org.test.Utils: * taintInTaintOut(**)","subtypes":false,"to":"ReturnValue","propagate":"taint","from":"empty","provenance":"manual","ext":""}
```

The implementation of the `sanitizer` is in [TaintSanitizerPropagate](/corax-config-general/src/main/kotlin/com/feysh/corax/config/general/model/processor/IPropagate.kt).

The definition of **xss** in `"from":"xss"` in the rules is specified in [sanitizerTaintTypesMap](/corax-config-general/src/main/kotlin/com/feysh/corax/config/general/model/taint/TaintModelingConfig.kt) and can be customized by modifying the main configuration or editing the code.




### Attribute Extension

You can customize the creation of some attributes and bind them to certain objects to assist in the analysis. Especially in cases where certain classes do not exist or it's challenging to inspect specific attributes, using custom attributes to maintain their state is a good approach.

```kotlin
object `insecure-cookie`  : AIAnalysisUnit() {
    
    // Custom creation of an attribute of type Boolean named "secure"
    private val secureAttr = CustomAttributeID<Boolean>("secure")

    context (AIAnalysisApi)
    override fun config() {
        constructor(::Cookie).modelNoArg {
            `this`.attr[secureAttr] = false  // default: `secure = false`
        }

        method(javax.servlet.http.Cookie::setSecure).modelNoArg {
            `this`.attr[secureAttr] = p0.getBoolean() // Update the secureAttr attribute
            check(!p0.getBoolean(), InsecureCookieChecker.InsecureCookie)     // Check the parameter
        }

        method(javax.servlet.http.HttpServletResponse::addCookie).modelNoArg {
            check(!p0.attr[secureAttr], InsecureCookieChecker.InsecureCookie) // Get and check the attribute
        }
    }
}
```

Complete code in [insecure-cookie.kt](/corax-config-community/src/main/kotlin/com/feysh/corax/config/community/checkers/insecure-cookie.kt).



Alternatively, you can directly access the boolean field `secure` of the class for inspection. Note that the type of `secure` is `boolean`, not `java.lang.Boolean`. This assumes that the `Cookie` class can be fully loaded by the analyzer, and library code analysis is enabled.

For example:

```kotlin
method(javax.servlet.http.HttpServletResponse::addCookie).modelNoArg {
    // You can check the value of the secure field using the following methods
    check(!p0.field(Cookie::class, "secure").getBoolean(), InsecureCookieChecker.InsecureCookie)
    check(!p0.field(Cookie::class, "secure", "boolean").getBoolean(), InsecureCookieChecker.InsecureCookie)
    check(!p0.field(Cookie::secure).getBoolean(), InsecureCookieChecker.InsecureCookie) // Use the above methods if it's not public
    // Cannot use
    // check(!p0.field(Cookie::class, "secure", Boolean::class).getBoolean(), InsecureCookieChecker.InsecureCookie)
    
    // If the type of Cookie::value is String, you can use String::class as a parameter for the field type
    check(p0.field(Cookie::class, "value", String::class).getString().stringEquals("???"), ?)
}
```





## Data Flow Analysis

Data flow analysis belongs to semantic analysis, so you should use [AIAnalysisUnit](plugin-infrastructure.md#aianalysisunit). To facilitate program analysis, the analyzer designs a set of Config IR to perform various modeling and checks. Compared to traditional program analysis, corax java uses this IR to unify taint analysis and numerical analysis. It can quickly extend the IR's `operator code` to analyze more issues according to the requirements, providing excellent extensibility (inspired by [Valgrind](https://valgrind.org/) VEX IR).

### Expressions

Checking a problem ultimately involves judging a Boolean value.

In the analyzer, you can construct various statements (`Stmt`) and expressions (`Expr`) to describe the behavior of methods or use `BoolExpr` to check if a program's state satisfies a certain constraint.

First, list the currently supported opcodes:

```kotlin
enum class UnOp {
    GetSet,
    GetBoolean,
    GetInt,
    GetLong,
    GetString,
    GetEnumName,
    ToLowerCase,
    Not,
    IsConstant
}

enum class BinOp {
    // ----------- Primitive Operators -----------
    // Relational Op (Int, Float)
    LT, LE, EQ, GE, GT,

    // Arithmetic Op (Int, Float)
    Add, Sub, Mul, Div, Mod,

    // Logical Op (Bool)
    Or, And, Xor,

    // Bitwise Op
    BvAnd, BvOr, BvXor, BvShr, BvShl, BvLShr,

    // ----------- High Order Operators -----------
    OrSet, // (set, set) Union two sets
    AndSet, // (set, set) Intersection two sets
    RemoveSet, // (set a, set b) remove set b from set a between two sets
    HasIntersectionSet, // (set, set) has Intersection return boolean expr
    ContainsSet, // (set a, set b) a.containsAll(b) return boolean expr
    StartsWith, // (string a, string b) a.startsWith(b)
    EndsWith, // (string a, string b) a.endsWith(b)
    Contains, // (string a, string b) a.contains(b)
    StringEquals, // (string a, string b) a.equals(b)

    IsInstanceOf, // (a, Class b) a.isInstanceOf(b)

    AnyOf
}

enum class TriOp {
    ITE
}

enum class QOp {
    ...
}
```

The IExpr in the engine is divided into API and Impl (such as api: IIexLoad and impl: IexLoad). The hierarchy is as follows:

(`com.feysh.corax.config.api.baseimpl` classes are basic implementations for the API and can be extended)

```
IExpr (com.feysh.corax.config.api)
	IIexLoad (com.feysh.corax.config.api)               // Load a variable
		IexLoad (com.feysh.corax.config.api.baseimpl)   
	IIexGetField (com.feysh.corax.config.api)           // local + access path + access path ...
		IexGetFieldExpr (com.feysh.corax.config.api.baseimpl)
	IUnOpExpr (com.feysh.corax.config.api)              // UnOp + single operand expression
		UnOpExpr (com.feysh.corax.config.api.baseimpl)
	IBinOpExpr (com.feysh.corax.config.api)             // BinOp + two operand expression
		BinOpExpr (com.feysh.corax.config.api.baseimpl)
	ITriOpExpr (com.feysh.corax.config.api)             // TriOp + three operand expression
		ITEExpr (com.feysh.corax.config.api.baseimpl)  		// Implementation of if then else
	IQOpExpr (com.feysh.corax.config.api)               // QOp + four operand expression
	IIexConst (com.feysh.corax.config.api)              // Literal, such as a constant string, a constant numeric value 123
		IexConstNull (com.feysh.corax.config.api)       // Some special constants
		IexConstTrue (com.feysh.corax.config.api)
		IexConstFalse (com.feysh.corax.config.api)
		IexConst (com.feysh.corax.config.api.baseimpl)  // Implementation
```





Expressions are categorized into these types:

- `IBoolExpr`
- `IStringExpr`
- `IIntExpr`
- `ILongExpr`



Tips: 

​		Since the analyzer is closed source and the configuration is universal, you only need to fully implement `AIAnalysisApi` and `PreAnalysisApi`. This way, you can build your own analyzer without changing the configuration project.

​		However, the configurations under AIAnalysisApi are for data flow analysis, and they cannot be applied to higher-precision analysis requirements, such as program verification requiring complete consistency of method contracts (summaries). If needed, extend the expressive power for higher precision.



### Taint Checks

​		Previously discussed sources and summaries are used to construct a taint transfer graph. Ultimately, we need to check at some point on this graph whether certain taint types are present.

#### Injection Checks

For example: [checkers/taint-checker.kt](/corax-config-community/src/main/kotlin/com/feysh/corax/config/community/checkers/taint-checker.kt)

1. First, add sink methods to [corax-config-community/rules](/corax-config-community/rules). If it is a common sink, add it to community.sink.json. If it is sinks specific to a Java project, create a new file with a different name, suffixing it with .sinks.json.

   The sink rule format is similar to the rules in [community.sinks.json](/corax-config-community/rules/community.sinks.json), where sinks are grouped together and categorized by `kind`, like:

```json
[
  {"kind":"command-injection","signature":"java.lang.Runtime: * load(String)","subtypes":false,"arg":"Argument[0]","provenance":"ai-manual","ext":""},
  {"kind":"command-injection","signature":"java.lang.Runtime: * loadLibrary(String)","subtypes":false,"arg":"Argument[0]","provenance":"ai-manual","ext":""},
    .....
]
```



2. Add the following code to [checkers/taint-checker.kt](/corax-config-community/src/main/kotlin/com/feysh/corax/config/community/checkers/taint-checker.kt) under `kind2Checker`:

```		kotlin
val kind2Checker: Map<String, CustomSinkDataForCheck> = mapOf(
    // Automatically query all rules in files with the .sinks.json suffix under the rules folder, with kind = "command-injection", and apply these rules. If there is an injection (i.e., check if each sink point contains control + GeneralTaintTypes.CONTAINS_COMMAND_INJECT), then use the CommandInjection checker for reporting.
    "command-injection" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_COMMAND_INJECT, reportType = CmdiChecker.CommandInjection)
)
```

​		Just extend the checks according to
 the template above. The check code is essentially similar to the following:
```kotlin
object `cmdi-sinks` : AIAnalysisUnit() {
    context (AIAnalysisApi)
    override fun config() {
        listOf(
            matchSoot("<java.lang.Runtime: java.lang.Process exec(java.lang.String)>") to 0,
            matchSoot("<java.lang.Runtime: java.lang.Process exec(java.lang.String[])>") to 0,
            matchSoot("<java.lang.Runtime: java.lang.Process exec(java.lang.String,java.lang.String[])>") to 1,
            matchSoot("<java.lang.Runtime: java.lang.Process exec(java.lang.String,java.lang.String[])>") to 0,
            matchSoot("<java.lang.Runtime: java.lang.Process exec(java.lang.String[],java.lang.String[])>") to 1,
            matchSoot("<java.lang.Runtime: java.lang.Process exec(java.lang.String[],java.lang.String[])>") to 0,
            matchSoot("<java.lang.Runtime: java.lang.Process exec(java.lang.String,java.lang.String[],java.io.File)>") to 1,
            matchSoot("<java.lang.Runtime: java.lang.Process exec(java.lang.String,java.lang.String[],java.io.File)>") to 0,
            matchSoot("<java.lang.Runtime: java.lang.Process exec(java.lang.String[],java.lang.String[],java.io.File)>") to 1,
            matchSoot("<java.lang.Runtime: java.lang.Process exec(java.lang.String[],java.lang.String[],java.io.File)>") to 0,
            matchSoot("<java.lang.ProcessBuilder: void <init>(java.lang.String[])>") to 0,
            matchSoot("<java.lang.ProcessBuilder: void <init>(java.util.List)>") to 0,
            matchSoot("<java.lang.ProcessBuilder: java.lang.Process exec(java.lang.String[])>") to 0,
            matchSoot("<java.lang.ProcessBuilder: java.lang.Process exec(java.util.List)>") to 0,
            ....
        ).forEach{ (sinkMethod, sinkParameters) ->
            method(sinkMethod).modelNoArgSoot {
                sinkParameters.enumerate { sinkParameter ->
                    check(
                        parameter(sinkParameter).mayElement.taint
                            .containsAll(taintOf(GeneralTaintTypes.ControlData, GeneralTaintTypes.CONTAINS_COMMAND_INJECT)),
                        CmdiChecker.CommandInjection
                    )
                }
            }
        }
    }
}
```

This is mainly externalizing the taint check form into the rule folder.



#### Privacy Leakage Checks

The categorization for taint analysis is typically injection and sensitive information leakage. Both fall under taint analysis, so the checks are similar to the injection checks.

Simply add the following to [checkers/taint-checker.kt](/corax-config-community/src/main/kotlin/com/feysh/corax/config/community/checkers/taint-checker.kt) 

```kotlin
val kind2Checker: Map<String, CustomSinkDataForCheck> = mapOf(
    "sensitive-data-exposure" to CustomSinkDataForCheck(setOf(GeneralTaintTypes.CONTAINS_SENSITIVE_DATA), reportType = SensitiveDataExposeChecker.SensitiveDataExposure),
    "log-injection" to CustomSinkDataForCheck(setOf(GeneralTaintTypes.CONTAINS_SENSITIVE_DATA), reportType = SensitiveDataExposeChecker.SensitiveDataExposure),
    ...and so on
)
```



### Numerical Checks

For example, checking if a numeric value is greater than or equal to `3600 * 24 * 365`:

```kotlin
object `cookie-persistent` : AIAnalysisUnit() {
    context (AIAnalysisApi)
    override fun config() {
        listOf(
            "<javax.servlet.http.Cookie: void setMaxAge(int)>",
        ).forEach {
            method(matchSoot(it)).modelNoArg {
                check(
                    p0.getInt() ge literal(3600 * 24 * 365) ,
                    CookiePersistentChecker.CookiePersistent
                )
            }
        }
    }
}
```



### Hardcoding Checks

Use `isConstant` to determine if a value is a constant (corresponding to `UnOp.IsConstant` mentioned earlier).

```kotlin
listOf(
	"<javax.crypto.spec.IvParameterSpec: void <init>(byte[])>" to 0
).forEach { (method, iv) ->
	method(matchSoot(method)).modelNoArgSoot {
		check(parameter(iv).field(Elements).getInt().isConstant, StaticIvChecker.StaticIv)
	}
}
```



### Attribute Extension Checks

Refer to the [Attribute Extension](#attribute-extension) section. Retrieve the attribute and check it. If true, the analyzer will report `InsecureCookie`.

```kotlin
check(!p0.attr[secureAttr], InsecureCookieChecker.InsecureCookie) // Get and check the attribute
```



### Resource Not Released Checks

#### TODO To be implemented