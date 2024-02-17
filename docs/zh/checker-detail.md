# 自定义checker

**Table of contents**

<!-- toc -->

- [概要](#%E6%A6%82%E8%A6%81)
- [静态代码属性检查](#%E9%9D%99%E6%80%81%E4%BB%A3%E7%A0%81%E5%B1%9E%E6%80%A7%E6%A3%80%E6%9F%A5)
  * [函数调用检查](#%E5%87%BD%E6%95%B0%E8%B0%83%E7%94%A8%E6%A3%80%E6%9F%A5)
  * [文本内容解析检查](#%E6%96%87%E6%9C%AC%E5%86%85%E5%AE%B9%E8%A7%A3%E6%9E%90%E6%A3%80%E6%9F%A5)
  * [Java AST 检查](#java-ast-%E6%A3%80%E6%9F%A5)
  * [Soot Jimple IR 检查](#soot-jimple-ir-%E6%A3%80%E6%9F%A5)
  * [注解检查](#%E6%B3%A8%E8%A7%A3%E6%A3%80%E6%9F%A5)
- [框架建模适配](#%E6%A1%86%E6%9E%B6%E5%BB%BA%E6%A8%A1%E9%80%82%E9%85%8D)
  * [mybatis配置解析](#mybatis%E9%85%8D%E7%BD%AE%E8%A7%A3%E6%9E%90)
  * [spring建模](#spring%E5%BB%BA%E6%A8%A1)
- [rules数据](#rules%E6%95%B0%E6%8D%AE)
- [数据流建模](#%E6%95%B0%E6%8D%AE%E6%B5%81%E5%BB%BA%E6%A8%A1)
  * [值传递建模](#%E5%80%BC%E4%BC%A0%E9%80%92%E5%BB%BA%E6%A8%A1)
    + [TODO：待实现](#todo%E5%BE%85%E5%AE%9E%E7%8E%B0)
  * [taint传播建模](#taint%E4%BC%A0%E6%92%AD%E5%BB%BA%E6%A8%A1)
  * [配置文件建模](#%E9%85%8D%E7%BD%AE%E6%96%87%E4%BB%B6%E5%BB%BA%E6%A8%A1)
  * [属性扩展](#%E5%B1%9E%E6%80%A7%E6%89%A9%E5%B1%95)
- [数据流检查](#%E6%95%B0%E6%8D%AE%E6%B5%81%E6%A3%80%E6%9F%A5)
  * [表达式](#%E8%A1%A8%E8%BE%BE%E5%BC%8F)
  * [污点检查](#%E6%B1%A1%E7%82%B9%E6%A3%80%E6%9F%A5)
    + [注入检查](#%E6%B3%A8%E5%85%A5%E6%A3%80%E6%9F%A5)
    + [隐私泄露检查](#%E9%9A%90%E7%A7%81%E6%B3%84%E9%9C%B2%E6%A3%80%E6%9F%A5)
  * [数值检查](#%E6%95%B0%E5%80%BC%E6%A3%80%E6%9F%A5)
  * [硬编码检查](#%E7%A1%AC%E7%BC%96%E7%A0%81%E6%A3%80%E6%9F%A5)
  * [扩展属性检查](#%E6%89%A9%E5%B1%95%E5%B1%9E%E6%80%A7%E6%A3%80%E6%9F%A5)
  * [资源未释放检查](#%E8%B5%84%E6%BA%90%E6%9C%AA%E9%87%8A%E6%94%BE%E6%A3%80%E6%9F%A5)
    + [TODO 待实现](#todo-%E5%BE%85%E5%AE%9E%E7%8E%B0)

<!-- tocstop -->

## 概要
自定义按照需求而定，现有如下几种方法（有序）：

1. 修改 yml 配置文件 [analysis-config/default-config.yml](/build/analysis-config/default-config.yml)
2. 修改 rules 文件夹中的配置文件 [analysis-config/rules](/build/analysis-config/rules)
3. 修改现有 checker 源码并编译
4. 自定义开发新的 checker 并编译



如何编写 checker 是 本文主要的介绍内容。

**自定义开发一个新的 checker 需要如下几个简单步骤（有序）：**

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

5. 前往 [corax-config-tests/normal/src/main/java/testcode](/corax-config-tests/normal/src/main/java/testcode) 编写对应的不合规和合规代码用来测试和保障分析精度，参考 [单元测试](unit-tests.md) 

6. 执行 `gradlew build` 编译并打包出最终的配置 [build/analysis-config](/build/analysis-config)

7. 按照  [Readme.md#开始分析](/Readme.md#开始分析) 加载配置开始分析

8. 查看报告 [sarif](/build/output/sarif) ，如有误漏报请分析原因，是否需要改正优化检查器，参考[结果输出](usage.md#结果输出)



## 静态代码属性检查

需要继承 [PreAnalysisUnit](plugin-infrastructure.md#preanalysisunit) 并 override  `context (PreAnalysisApi) fun config()`

### 函数调用检查

使用如下的 api 自定义检查：

```kotlin
PreAnalysisApi.atAnyInvoke(){}                 // 遍历所有的 Java 方法调用点 (call edge)
PreAnalysisApi.atInvoke(callee){ }             // 返回调用了 callee 方法的调用点 (call edge)
```

上述两种方法的参数二是 kotlin 回调方法，此方法参数 `this` 是 `IInvokeCheckPoint`

```kotlin
atInvoke(methodMatch) {  this: IInvokeCheckPoint ->
    // 您可以访问所有 this 中的属性进行检查
    if (是不合规代码) {
        report(CheckType)
        // 或者
        report(CheckType){ this: BugMessage.Env ->
            // 您可以自定义报告的行数和列数等
            this.lineNumber = ?
            this.columnNumber = ?
            // 您可以自定义传入一些键值对
            // 这样 ` msgGenerator { "调用 ${args["some key"]} 中的 $callee 方法是不合规的"} ` 就可以读取 some value 以动态生成 message
            this.args["some key"] = "some value"
        }
    }
}
```



完整的 `IInvokeCheckPoint` 包含如下成员以供使用

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

完整示例 [weak-ssl.default-http-client](/corax-config-community/src/main/kotlin/com/feysh/corax/config/community/checkers/weak-ssl.kt)



### 文本内容解析检查

可以检查：

- 明文的敏感数据，邮箱、url、地址、公私钥、电话 等等
- 自定义的其他解析工具来检查问题
- 不检查问题但解析配置并存储关键数据提供给 `AIAnalysisUnit` 辅助分析

使用如下的 api 自定义检查：

```kotlin
atAnySourceFile(extension=?){ }  // 遍历所有的 特定后缀 的资源文件
```

上述这种方法的参数二是 kotlin 回调方法，此方法参数 `this` 是 `ISourceFileCheckPoint`

```kotlin
// extension = null 则可以检查所有项目文件
// extension = "xml" 则可以检查所有项目中的所有 xml 后缀文件
atAnySourceFile(extension = "xml") {
    val doc = parseXmlSafe(path) ?: return@atAnySourceFile
    if (doc是不合规的) {
        report(CheckType)
    }
}
```



完整的 `ISourceFileCheckPoint` 包含如下成员以供使用

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

完整示例 [mybatis/MybatisMapperXmlSQLSinkProvider.kt](/corax-config-community/src/main/kotlin/com/feysh/corax/config/community/checkers/frameworks/persistence/ibatis/mybatis/MybatisMapperXmlSQLSinkProvider.kt)



### Java AST 检查

先编辑 [corax-config-community/build.gradle.kts](/corax-config-community/build.gradle.kts) 然后添加依赖

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
            // 解析只需要一行代码
            // 异步执行
            val parseResult: ParseResult<CompilationUnit> = AnalysisCache.G.getAsync(CompilationUnitAnalysisKey(path))
            if (!parseResult.isSuccessful) {
                return@atAnySourceFile
            }
            val javaCompilationUnit = parseResult.result.get()
            if (此 javaCompilationUnit 是不合规的) {
                report(CheckType)
            }
        }
    }
}
```



正如所见，您可以定义这种类似的解析器 `Analysis`，通过全局的 `AnalysisCache.G` 非常简单地访问解析结果。

全局缓存 `AnalysisCache.G`  可以有效节约内存和减少重复计算，并且是多线程的。

使用  [javaparser](https://github.com/javaparser/javaparser) 解析 java源码 为 AST 的封装如下所示：（这是内置的，无需再添加）

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

// 定义输入
data class CompilationUnitAnalysisKey(val sourceFile: Path) :
    AnalysisKey<ParseResult<CompilationUnit>>(CompilationUnitAnalysisDataFactory.key/*选择处理该sourceFile的实现*/)

// 定义输出
// 工厂函数定义虽然眼花缭乱了一点，但是在使用上非常方便且友好
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



### Soot Jimple IR 检查

要检查 Jimple IR 就需要拿到 `SootMethod`，使用如下的 api 自定义检查：

```kotlin
PreAnalysisApi.atAnyMethod(){ }                 // 遍历所有的 SootMethod
PreAnalysisApi.atMethod(method){ }              // 匹配指定的 SootMethod
```

**当然，如果您是专业人士，可以直接获取 Jimple IR 在用于自定义的 过程间/内的数据流 Analysis 中，再把此 Analysis 封装加入到全局的 `AnalysisCache.G`（AnalysisCache 参考上面），可以非常方便地扩展功能 **

```kotlin

object ExampleCheckUnit: PreAnalysisUnit() {    
    context (PreAnalysisApi)
    override fun config() {
        atAnyMethod { this: IMethodCheckPoint ->

            if (sootMethod 需要排除)
                return@atAnyMethod

            eachUnit { this: IUnitCheckPoint ->
                eachExpr { expr: soot.jimple.Expr ->
                    // 例如获取 Java 中的 == 和 != Expr
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



完整的 `IMethodCheckPoint` 包含如下成员以供使用

```kotlin
interface IMethodCheckPoint : ICheckPoint {
    val sootMethod: SootMethod
    val visibilityAnnotationTag: VisibilityAnnotationTag?

    fun eachUnit(block: IUnitCheckPoint.() -> Unit)
}
```

完整的 `IUnitCheckPoint` 包含如下成员以供使用

```kotlin
interface IUnitCheckPoint : ICheckPoint {
    val unit: soot.Unit
    fun eachExpr(block: (Expr) -> Unit)
}
```





### 注解检查

Java `Annotation` 注解一般存在于 Class、Field、Method，使用如下的 api 自定义检查：

```kotlin
PreAnalysisApi.atAnyClass(){ }                  // 遍历所有的 SootClass
PreAnalysisApi.atAnyField(){ }                  // 遍历所有的 SootField
PreAnalysisApi.atAnyMethod(){ }                 // 遍历所有的 SootMethod
PreAnalysisApi.atClass(clazz){ }                // 匹配指定的 SootClass
PreAnalysisApi.atField(field){ }                // 匹配指定的 SootField
PreAnalysisApi.atMethod(method){ }              // 匹配指定的 SootMethod
```



例子

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





## 框架建模适配

需要继承 [AIAnalysisUnit](plugin-infrastructure.md#aianalysisunit) 并 override  `context (AIAnalysisApi) fun config()` 方法



### mybatis配置解析

mybatis 的 `mapper.xml` 文件中存在 sql 字符串拼接（如`$`拼接），容易导致注入，检查此类问题需要先解析所有的 `mapper.xml` 并提取 risk method signatures 然后检查危险参数是否存在注入。

所以该框架的检测需要分为两步：

1. 实现 `PreAnalysisUnit` 来解析资源文件中的 `xml` 后缀文件获得危险方法及参数。
   参考 [mybatis-sql-injection-checker.kt#parseMybatisMapperAndConfig](/corax-config-community/src/main/kotlin/com/feysh/corax/config/community/checkers/frameworks/persistence/ibatis/mybatis/mybatis-sql-injection-checker.kt).
2. 实现 `AIAnalysisUnit` 来检查注入，需要 handle 并检查上一步提供的 sql query 方法的危险参数是否存在污染。
   参考 [mybatis-sql-injection-checker.kt#checkMybatisStatement](/corax-config-community/src/main/kotlin/com/feysh/corax/config/community/checkers/frameworks/persistence/ibatis/mybatis/mybatis-sql-injection-checker.kt).



完整源码：[frameworks/persistence/ibatis/mybatis](/corax-config-community/src/main/kotlin/com/feysh/corax/config/community/checkers/frameworks/persistence/ibatis/mybatis)



### spring建模

完整源码：

source: [general/model/framework/spring/SpringAnnotationSource.kt](/corax-config-general/src/main/kotlin/com/feysh/corax/config/general/model/javaee/JavaeeAnnotationSource.kt)

sink：[community/checkers/frameworks/spring/ResponseBodyCheck.kt](/corax-config-community/src/main/kotlin/com/feysh/corax/config/community/checkers/frameworks/spring/ResponseBodyCheck.kt)



## rules数据

存在项目中的如下位置：

[corax-config-general/rules](/corax-config-general/rules)

[corax-config-community/rules](/corax-config-community/rules)

`gradlew build` 后会被拷贝到这个位置：[build/analysis-config/rules](/build/analysis-config/rules)



您可以自定义任意可序列化类并从 rules 文件中加载 参考

```kotlin
@Serializable // kotlin serialization
data class Custom(
    @Required @SerialName("custom-name") 
    val yourCustomField: AnySerializableObject,
    ....
)

val jsonDirs = ConfigCenter.getConfigDirectories()
val someJsonFiles = walkFiles(jsonDirs){ file -> file.name.endsWith("customSuffix.json") }

val rules = RuleManager.load<Custom>(someJsonFiles, serializer())
```



目前 `corax-config-general` 模块包含如下类型文件及对应 `serializer`

`*.sources.json` 对应 `com.feysh.corax.config.general.model.taint.TaintRule.Source`

`*.summaries.json` 对应 `com.feysh.corax.config.general.model.taint.TaintRule.Summary`

`*.sinks.json` 对应 `com.feysh.corax.config.general.model.taint.TaintRule.Sink`

`*.access-path.json` 对应 `com.feysh.corax.config.general.rule.MethodAccessPath`



## 数据流建模

### 值传递建模

值传递建模例子：

如 `java.util.Map`：

```kotlin
        listOf(
            method(java.util.Map<Any, Any>::put),
            method(java.util.HashMap<Any, Any>::put),
            method(java.util.LinkedHashMap<Any, Any>::put),
            method(java.util.WeakHashMap<Any, Any>::put),
            method(java.util.IdentityHashMap<Any, Any>::put)
        ).forEach {
            it.modelNoArg {
                // 值传递（对象或者数值）
                // 表示 return 可能指向 原来 retuan 的值（分析器分析到的值）或者 this 任意 MapValue 的值
                `return`.value = `return` anyOr `this`.field(MapValues)
                `this`.field(MapKeys).value = `this`.field(MapKeys).value anyOr (p0.value)
                `this`.field(MapValues).value = `this`.field(MapValues).value anyOr (p1.value)
                
                // 如果是 `return`.value = p0, 那么表示 p0 必定赋值给了 return
            }
        }
```



另外介绍下专业名词：别名 （Alias），什么意思呢？

比如 鲁迅 也叫 周树人，鲁迅和周树人两个名字都指向同一个人，这俩名字互为别名关系

```
local space:
	x -> object-Foo
	a -> person-object-鲁迅
	
stmts:
	1: x.person = a
	2: c = x.person
```

那么 stmt 执行完后 `{ a，object-Foo.person，c }` 两两存在别名关系，他们的 PointToSet 都是 `{ person-object-鲁迅 }`





#### TODO：待实现

类成员 Field 的读写也可以 handle





### taint传播建模

taint 即 污点，一般的程序分析使用一个 Boolean 标志，用来标记对象是否 Tainted。但是在 corax 中的 taint 是由一些列 taint kind 组合而成的一个 set 集合。

taint 属于语义分析，所以应该使用 [AIAnalysisUnit](plugin-infrastructure.md#aianalysisunit) 

先看一个 taint source 的定义：

```kotlin

import javax.servlet.ServletRequest
import javax.servlet.ServletRequestWrapper
import javax.servlet.http.HttpServletRequest // 如果不存在可以手动添加模块依赖库
import javax.servlet.http.HttpServletRequestWrapper

object ExampleCustomSourceUnit : AIAnalysisUnit() {
    
    context(AIAnalysisApi) 
    override fun config() {
        // 可以使用 matchSimpleSig 或者 matchSoot 一类的 method match 来匹配一到多个 methods
        method(matchSimpleSig("javax.servlet.ServletRequest: String getParameter(String)")) // 返回一个 IMethodDecl
            .modelNoArg {                                                                   // 添加一个 handler
                 // 由于 this, return 都是 kotlin 关键字，需要使用 `` 包围起来
                 // 此处的语句 “=” 具有副作用，把右边的 taint set 集合（）传递给了左边 `return` 的 taint 字段
                `return`.taint = taintOf(internetSource)  
            }
        
        
        // 可以直接使用 类名:方法名 快速匹配一个方法，如果存在模板，可以使用 method<R, THIS, P0, P1...>(CLASS::METHOD)
        // 如下三个方法都可以传入一个 kfunction 来获取 IMethodDecl
        //  1: constructor(kfunction) kfunction 必须是构造方法，否则会直接报错
        //  2: staticMethod(kfunction) kfunction 必须是静态方法
        //  3: method(kfunction) kfunction 必须是成员方法
        listOf(
            method(ServletRequest::getParameterMap),// return type: Map<String, String[]>
            method(ServletRequestWrapper::getParameterMap),
            method(HttpServletRequest::getParameterMap),
            method(HttpServletRequestWrapper::getParameterMap),
        ).forEach { method ->
            method.modelNoArg {
                // 向Map返回值的每个 MapKey 的 taint 属性设置为 taint kinds：internetSource
                `return`.field(MapKeys).taint = taintOf(internetSource)
                // 向Map返回值的每个 MapValue 的每个 array element 的 taint 属性设置为 taint kinds：internetSource
                `return`.field(MapValues).field(Elements).taint = taintOf(internetSource)  
            }
        }
        
    }
}
```



java testcode:

```java
    @RequestMapping("/sendRedirect")
    @ResponseBody
    public static void sendRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = request.getParameter("url");
        response.sendRedirect(url); // $UnvalidatedRedirect
    }
```

这样一来 postCall `ServletRequest.getParameter()`  时的 返回值 带上 tiant 属性，再经过赋值传递给 url 变量。

我们只需要在 sink 点拿到传入参数的 taint 属性，检查其是否带有某些 taint kind 并报告。



再看一个 summary 的定义：

比如想要对 URL 进行建模，这是 URL  的成员字段

```java
package java.net;
public final class URL implements java.io.Serializable {
	private String protocol;
	private String host;
	private String file;
    ....
}
```

一般 jdk 内部的各种实现（代码）非常庞大，考虑到分析的效率，一般默认不分析这部分代码（虽然可以手动分析但是没必要）并使用 Summary 摘要系统来抽象 方法行为和作用 （wrapper）。

```kotlin
		
        // 如果 file 是被 taint, 那么 this.file 才被 tain, this.protocol, this.host 都不是 tainted, 但是我们不必太过精准地表示 taint 绑定的对象，可以直接将 taint 传递给 URL 对象本身，也就是参数 protocol，host，file 只要有一个是 tainted，那么 URL 对象 this 就是 tainted；当然也可以精准表示分别taint URL 的每个字段，但是不是很有必要
        constructor<URL, String, String, String>(::URL) // URL(String protocol, String host, String file)
            .modelNoArg {
                 // 注意这三个 stmt 这是有序的，流敏感的
                `this`.taint += p0.taint
                `this`.taint += p1.taint
                `this`.taint += p2.taint
            }

		// taint 传递给 URL 对象本身，那么还应该把这个 taint 属性传播出去或者在 sink 位置进行检查，否则建模是无效的或是不完整且，
        // 例如下面的例子, 将 taint 传递给了 String 类型，String类型也做了方法摘要，那么 taint 就可以在java程序中持续传递下去保证低的漏报：
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
                // 等价为 `return`.taint = `return`.taint + `this`.taint，一般用来防止同一方法的多个建模导致冲突，所以建议使用 += 来保证属性只增不少
                `return`.taint += `this`.taint
            }
        }
```







### 配置文件建模

​		对于 taint 传递和 source, sink 这种污点建模来说，其格式大部分比较单一，写在代码中可能比较臃肿，可以按照一定的格式将简单的建模写到文件中，复杂的仍然使用 kotlin 硬编码方式 来建模。

​		前边介绍过的 [RuleManager](plugin-infrastructure.md#rulemanager) 就是在做规则文件解析事情，它被用来加载规则，然后在  [model/taint/TaintModelingConfig.kt](/corax-config-general/src/main/kotlin/com/feysh/corax/config/general/model/taint/TaintModelingConfig.kt) 中读取 `ConfigCenter.taintRulesManager.sources` 和 `ConfigCenter.taintRulesManager.summaries` 并应用这些规则，即可达到统一快速建模目的。所有的配置格式、解析、管理均可以自定义无任何限制。

```kotlin
    context (AIAnalysisApi)
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



**source 配置文件的规则**：

​		以 `sources.json` 为后缀的文件 如 [general.sources.json](/corax-config-general/rules/general.sources.json) 这些 sinks 被放在一起并使用 `kind` 对这些 sinks 进行归类, 如：

```json
[
  {"kind":"remote","signature":"javax.servlet.ServletRequest: * getInputStream()","subtypes":false,"arg":"ReturnValue","provenance":"manual","ext":""},
  {"kind":"remote","signature":"javax.servlet.ServletRequest: * getParameter(String)","subtypes":false,"arg":"ReturnValue","provenance":"manual","ext":""},
  ...
]
```

​		在 [model/taint/TaintModelingConfig.kt](/corax-config-general/src/main/kotlin/com/feysh/corax/config/general/model/taint/TaintModelingConfig.kt) 文件中的 `sourceKindToAppendTaintTypesMap` 定义了 kind 到 source taint kinds 的映射关系

```kotlin
val sourceKindToAppendTaintTypesMap: Map<String, Set<ITaintType>> = mapOf(
    "remote" to internetSource,
    "fileIo" to fileIoSource,
    "userInput" to userInputSource,
)
```

​		比如上面的 `"kind":"remote" ` 对应 `com.feysh.corax.config.general.checkers.internetSource` 这些 taint kinds，在应用这些 rules 时候会根据 kind 自动对 source 方法加上对应的 taint 属性。

​		当然简单的方式是直接自定义修改 yml 主配置文件来定义映射数据无需修改代码（由于是 `SAOptions` 的子类，可以在文件中快速自定义配置）。比如 [build/analysis-config/default-config.yml](/build/analysis-config/default-config.yml)

```yaml
  ....
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



**summary 配置文件的规则**

​		以 `summaries.json` 为后缀的文件 如 [general.summaries.json](/corax-config-general/rules/general.summaries.json)

```json
[
  {"signature":"java.lang.AbstractStringBuilder: * <init>(String)","subtypes":true,"argTo":"Argument[this]","propagate":"taint","argFrom":"Argument[0]","provenance":"manual","ext":""},
  {"signature":"java.lang.AbstractStringBuilder: * append(**)","subtypes":true,"argTo":"ReturnValue","propagate":"value","argFrom":"Argument[this]","provenance":"manual","ext":""}
  ...
]
```

1. `signature` 可以为 `matchSoot` 格式 或者 为 `matchSimpleSig` 格式
2. subtypes 表示是否 handle 子类的重写方法（static method 必定为 false, interface 必定为 true）
3. - "propagate": "taint" 意为 taint kinds 传递，在方法执行后，to 和 from 可能指向了不同的对象，如果不同，那么 to 被 taint 了或副作用了，from 仍然不受影响，除非在程序中存在别名关系
   - "propagate": "value" 意为值传递，就是上面讲到的别名，to 和 from 指向了一个对象
4. `provenance`，和 `ext` 无关紧要，保留字段



**sanitizer 配置**

打开 [`???.summaries.json`](/corax-config-general/rules/supplement.summaries.json) 配置文件并添加如下类似的 `sanitizer rule` 去除 TaintKind:

```json
  {"signature":"org.springframework.web.util.HtmlUtils: * htmlEscape(**)","subtypes":false,"to":"ReturnValue","propagate":"taint","from":"Argument[0]","provenance":"manual","ext":""},
  "在紧跟着上面的taint传递的rule后面添加一条 xss 的 sanitizer，表示此 htmlEscape 方法返回的数据受到过滤或限制无法造成 XSS 注入，但是 Argument[0] 依旧可以造成 XSS 注入"
  {"signature":"org.springframework.web.util.HtmlUtils: * htmlEscape(**)","subtypes":false,"to":"ReturnValue","propagate":"sanitizer","from":"xss","provenance":"manual","ext":""}
```



如果分析引擎通过分析`taintInTaintOut`方法后后认为 `Argument[0]` 会污染返回值 `ReturnValue` , **实际上却是无法污染**，所以可能导致误报，您可以使用如下规则强制全部去除的 taint 标记来降低误报。

```json
  {"signature":"org.test.Utils: * taintInTaintOut(**)","subtypes":false,"to":"ReturnValue","propagate":"taint","from":"empty","provenance":"manual","ext":""}
```

`sanitizer` 实现在 [TaintSanitizerPropagate](/corax-config-general/src/main/kotlin/com/feysh/corax/config/general/model/processor/IPropagate.kt) 

其中规则中的 `"from":"xss"` 中的 **xss** 定义在 [sanitizerTaintTypesMap](/corax-config-general/src/main/kotlin/com/feysh/corax/config/general/model/taint/TaintModelingConfig.kt) ，可以通过修改主配置或者编辑代码方式进行自定义扩展




### 属性扩展

可以自定义创建一些属性并绑定到一些对象来辅助我们分析。特别是一些类不存在时或者不太好检查某些属性时，使用自定义属性并维护其状态辅助检查是个不错的办法。

```kotlin
object `insecure-cookie`  : AIAnalysisUnit() {
    
    // 自定义创建一个类型为 Boolean 名为 "secure" 的属性
    private val secureAttr = CustomAttributeID<Boolean>("secure")

    context (AIAnalysisApi)
    override fun config() {
        constructor(::Cookie).modelNoArg {
            `this`.attr[secureAttr] = false  // default: `secure = false`
        }

        method(javax.servlet.http.Cookie::setSecure).modelNoArg {
            `this`.attr[secureAttr] = p0.getBoolean() // 更新 this.secureAttr 属性
            check(!p0.getBoolean(), InsecureCookieChecker.InsecureCookie)     // 检查参数
        }

        method(javax.servlet.http.HttpServletResponse::addCookie).modelNoArg {
            check(!p0.attr[secureAttr], InsecureCookieChecker.InsecureCookie) // 获取并检查属性
        }
    }
}
```

完整代码 [insecure-cookie.kt](/corax-config-community/src/main/kotlin/com/feysh/corax/config/community/checkers/insecure-cookie.kt)



当然您也可以直接访问类成员 boolean Field：`secure` 来进行检查，注意 `secure` 类型是 `boolean` 并不是 `java.lang.Boolean`，前提是 `Cookie` 类能够被够分析器完整加载且分析器开启了库代码分析。

例如：

```kotlin
method(javax.servlet.http.HttpServletResponse::addCookie).modelNoArg {
    // 可以通过如下几种方法检查 secure 字段的值
    check(!p0.field(Cookie::class, "secure").getBoolean(), InsecureCookieChecker.InsecureCookie)
    check(!p0.field(Cookie::class, "secure", "boolean").getBoolean(), InsecureCookieChecker.InsecureCookie)
    check(!p0.field(Cookie::secure).getBoolean(), InsecureCookieChecker.InsecureCookie) // 如果非 public 则使用上面的方法
    // 不可以使用
    // check(!p0.field(Cookie::class, "secure", Boolean::class).getBoolean(), InsecureCookieChecker.InsecureCookie)
    
    // Cookie::value 类型是 String, 则可以使用 String::class 传入参数作为 field 类型
    check(p0.field(Cookie::class, "value", String::class).getString().stringEquals("???"), ?)
}
```





## 数据流检查

属于语义分析，所以应该使用 [AIAnalysisUnit](plugin-infrastructure.md#aianalysisunit) ，为了方便程序分析，分析器设计了一套 Config IR 来完成各种建模和检查，较传统程序分析，corax java 使用该 IR 统一了 taint 分析和 数值分析，并且可以根据需求快速扩展 IR 的 `operator code` 来分析更多问题，具备非常良好的扩展性 （参考了[valgrind](https://valgrind.org/) VEX IR）。

### 表达式

检查一个问题最终就是判断一个 Bool 值

在分析器中，可以构建各种语句（Stmt）和 表达式（Expr）来描述方法行为或使用 BoolExpr 检查程序的某个状态是否满足某一约束

首先列出目前支持的 opcode:

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

引擎中的 IExpr 分为 API 和 Impl (如 api: IIexLoad 和 impl: IexLoad)， Hierarchy 树如下：

（com.feysh.corax.config.api.baseimpl 中的类为 api 的 基础实现，也可以自己扩展）

```
IExpr (com.feysh.corax.config.api)
	IIexLoad (com.feysh.corax.config.api)               // 读取一个变量
		IexLoad (com.feysh.corax.config.api.baseimpl)   
	IIexGetField (com.feysh.corax.config.api)           // local + access path + access path ...
		IexGetFieldExpr (com.feysh.corax.config.api.baseimpl)
	IUnOpExpr (com.feysh.corax.config.api)              // UnOp + 单操作数表达式
		UnOpExpr (com.feysh.corax.config.api.baseimpl)
	IBinOpExpr (com.feysh.corax.config.api)             // BinOp + 双操作数表达式
		BinOpExpr (com.feysh.corax.config.api.baseimpl)
	ITriOpExpr (com.feysh.corax.config.api)             // TriOp + 三操作数表达式
		ITEExpr (com.feysh.corax.config.api.baseimpl)  		// if then else 的一个实现
	IQOpExpr (com.feysh.corax.config.api)               // QOp + 四操作数表达式
	IIexConst (com.feysh.corax.config.api)              // literal 字面量，比如一个 常量的字符串，一个常量数值 123
		IexConstNull (com.feysh.corax.config.api)       // 一些特殊的常量
		IexConstTrue (com.feysh.corax.config.api)
		IexConstFalse (com.feysh.corax.config.api)
		IexConst (com.feysh.corax.config.api.baseimpl)  // 一个实现
```





表达式分为这几种类型：

- `IBoolExpr`
- `IStringExpr`
- `IIntExpr`
- `ILongExpr`



Tips: 

​		由于分析器是闭源的，配置是通用的，所以您只需要完整实现 `AIAnalysisApi` 和 `PreAnalysisApi` 这几个 api, 就可以打造属于自己的分析器，而无需更改配置项目。

​		但是 AIAnalysisApi 下的配置都是数据流分析的配置，无法应用到更高精度需求的分析（如程序验证要求方法规约（摘要）的精度完全一致），如有需要再进行扩展高精度的表达能力。



### 污点检查

​		前面的 source 和 summary 就是构建一个污点传递图，最终我们需要在这个图上的某一点检查标记的这些污点类型中是否存在某些特定 taint kind。

#### 注入检查

比如：[checkers/taint-checker.kt](/corax-config-community/src/main/kotlin/com/feysh/corax/config/community/checkers/taint-checker.kt)

1. 首先添加 sink 方法到 [corax-config-community/rules](/corax-config-community/rules) ，如果是通用的sink请添加到 community.sink.json 中，如果是某一 java 项目特有的 sinks, 请另起名字并以 `.sinks.json` 为后缀创建新的文件。

   sink rule 格式上就如 [community.sinks.json](/corax-config-community/rules/community.sinks.json) 中的规则，这些 sinks 被放在一起并使用 `kind` 对这些 sinks 进行归类, 如：

```json
[
  {"kind":"command-injection","signature":"java.lang.Runtime: * load(String)","subtypes":false,"arg":"Argument[0]","provenance":"ai-manual","ext":""},
  {"kind":"command-injection","signature":"java.lang.Runtime: * loadLibrary(String)","subtypes":false,"arg":"Argument[0]","provenance":"ai-manual","ext":""},
    .....
]
```



2. 再到 [checkers/taint-checker.kt](/corax-config-community/src/main/kotlin/com/feysh/corax/config/community/checkers/taint-checker.kt) 中的 `kind2Checker` 加入如下代码:

```		kotlin
val kind2Checker: Map<String, CustomSinkDataForCheck> = mapOf(
    // 会自动查询 rules 文件下的 sinks.json 后缀文件的所有 kind = "command-injection" 的 rule 并应用这些规则，如果存在注入（即检查每个 sink 点是否包含 control + GeneralTaintTypes.CONTAINS_COMMAND_INJECT 这些 kinds ）则使用 CommandInjection checker 进行报告。
    "command-injection" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_COMMAND_INJECT, reportType = CmdiChecker.CommandInjection)
)
```

​		只需要按照上述代码中的封装后的模板扩充检查就行，检查的部分代码其实和下面代码等价or类似：

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
            matchSoot("<java.lang.ProcessBuilder: java.lang.ProcessBuilder command(java.lang.String[])>") to 0,
            matchSoot("<java.lang.ProcessBuilder: java.lang.ProcessBuilder command(java.util.List)>") to 0,
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

只不过因为 taint 检查形式比较单一，全部放到外部 rule 文件夹中进行管理



#### 隐私泄露检查

污点分析的分类有 注入 和 敏感信息泄露类，都属于 taint , 所以检查和上面的注入检查是一致的。

直接在 [checkers/taint-checker.kt](/corax-config-community/src/main/kotlin/com/feysh/corax/config/community/checkers/taint-checker.kt) 中加入

```kotlin
val kind2Checker: Map<String, CustomSinkDataForCheck> = mapOf(
    "sensitive-data-exposure" to CustomSinkDataForCheck(setOf(GeneralTaintTypes.CONTAINS_SENSITIVE_DATA), reportType = SensitiveDataExposeChecker.SensitiveDataExposure),
    "log-injection" to CustomSinkDataForCheck(setOf(GeneralTaintTypes.CONTAINS_SENSITIVE_DATA), reportType = SensitiveDataExposeChecker.SensitiveDataExposure),
    ...等等
)
```



### 数值检查

例如检查一个数值是否大于等于 `3600 * 24 * 365`：

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



### 硬编码检查

使用 `isConstant` 判断值是否为常量（对应上面的 `UnOp.IsConstant`）

```kotlin
listOf(
	"<javax.crypto.spec.IvParameterSpec: void <init>(byte[])>" to 0
).forEach { (method, iv) ->
	method(matchSoot(method)).modelNoArg {
		check(parameter(iv).field(Elements).getInt().isConstant, StaticIvChecker.StaticIv)
	}
}
```



### 扩展属性检查

参考上面的 [属性扩展](#属性扩展), 获取该属性并check, 如果为真则分析器会报告 `InsecureCookie`

```kotlin
check(!p0.attr[secureAttr], InsecureCookieChecker.InsecureCookie) // 获取并检查属性
```



### 资源未释放检查

#### TODO 待实现