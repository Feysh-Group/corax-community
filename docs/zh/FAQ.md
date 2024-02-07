## 常见问题


1：指定分析对象的路径不存在。

```
01:40:10.583 | ERROR | CoraxJava | An error occurred: java.lang.IllegalStateException: autoAppClasses option: "test\xxx" is invalid or target not exists
Exception in thread "main" java.lang.IllegalStateException: autoAppClasses option: "test\xxx" is invalid or target not exists
```

2：找不到类。一般因为 `--process` 或 `--auto-app-classes` 参数指定的路径下并不存在可以加载的类，一般是没有编译导致。或 `--auto-app-classes`  指定的路径下存在编译产物但是不存在对应源码。

```
Exception in thread "main" java.lang.IllegalStateException: application classes must not be empty. check your --process, --auto-app-classes path
```

3：报告数量均为 0。可能因为规则分析器或者引擎的错误导致抛出异常，且分析终止，请检查是否出现进度条，且没有 Error 关键字 或 异常栈打印。

```
ReportConverter | Started: Flushing 0 reports ... 
```

4：分析被长时间阻塞在某一步骤。

- 可以考虑检查 ApplicationClasses 数量对比 libraryClasses 数量是否过多，可能没有配置好 `--process` 参数，该参数错误包含了三方库。

- 查看 共计 classes 数量 是否很大（比如超过2w），如果类数量太多，加载时间和内存消耗会相应变多。

- 内存到达瓶颈。

    - 分析规模太大而物理机内存资源不足。可以通过 windows 任务管理器 或者 `linux htop` 命令等查看内存和 CPU 状态。如果卡在进度条可以关注这些信息：比如 剩余物理内存（phy）低于 1gb 或者 jvmMemoryCommitted 数值 非常趋近 jvmMemoryMax 数值 时（单位gb）均表现为内存不足。

  \>   15% │███████▏                                     │ 15/94e (0:00:00 / 0:00:02) ?e/s 6/7.9/7.9/8.0 phy: 0.5 cpu:  99%

  比如此进度条表示

  \>  分析进度 │███████▏   │ 已分析入口方法数量/总分析入口方法数量 (耗时 / 预测剩余时间) 任务处理速度 jvmMemoryUsed/maxUsedMemory/jvmMemoryCommitted/jvmMemoryMax phy:剩余真实物理内存 cpu:  负载百分比

    - 算法实现问题 导致内存耗费异常高

- cpu负载长时间为 0，可能因为某些原因导致协程相互阻塞挂起，暂未发现。

- 可能的引擎算法 bug，欢迎提交 issue。




### Q&A

**Q: 有哪些因素影响分析精度？**

A: 分析精度不仅受`核心分析引擎`的分析算法影响，**也受用户的输入参数配置影响**，目前大致有如下原因：

1. runtime classes 的提供是否完整，二进制是否存在混淆等（需要用户辅助解决）

2. 运行命令参数配置存在问题

3. 规则检查器的代码质量及配置中的各种参数

4. 核心分析引擎本身的算法精度和一些分析限制（必要的在时间和内存上的妥协）



**Q: 为什么分析需要编译源码后的产物？**

A: 在静态分析技术实现上，纯源码分析往往需要先将其转换为 AST（抽象语法树），对于 JVM 上运行的语言（Java、Kotlin、JSP、Scala 和 Groovy 等）都需要分别设计一对一的语言前端来解析，AST 又包含了各种各样复杂的语言特性和各种版本上的语法糖，这将严重增加分析引擎的负担，并且这不是静态分析的侧重点，所以往往 java 静态分析都会采用直接或者间接的方式分析 JVM字节码（即各种形式的编译产物 class） 来获得程序的操作语义信息。
另一方面，源码分析需要各种语言环境和依赖，比如有 `import package.a.*;` 如果环境没有 `package.a` 这个包，那么所有引用了该包下的声明都将因必要的依赖信息不完整导致解析失败或者丢失分析精度，所以通常来说，分析 class 能比分析源码得到更高精度的报告。
还有一方面就是使用源码分析，将极大地限制静态分析的使用场景，比如想要分析一个不带源码的二进制包。

**Q: 为什么分析需要源码？**

A: 源码不是必须提供的，只是为了更好地展示缺陷报告，以及在使用 `--auto-app-classes` 参数时候可以用来辅助分析器根据源码来匹配对应的 classes 以获知哪些类是项目类哪些是库类，这将帮助分析器更清楚分析的侧重点，简化参数的配置。

**Q: 为什么使用kotlin?**


A: kotlin 编写的代码更简单易懂，能极高提升开发者开发效率，易于维护；并且很难出现头疼的 `NullPointerException` 问题。kotlin 还支持协程异步执行，极高地提升了分析器效率；另外 kotlin 支持更多的语法特性，让我们更多的时间花费在算法本身而不是浪费在编程上：)，有木有心动？好吧！当然您仍然可以编写 java 代码并和 kotlin 代码放在一起混合调用，无需另外做任何事。



