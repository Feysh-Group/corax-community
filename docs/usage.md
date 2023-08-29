# CoraxJava使用

**Table of contents**



* [分析概述](#分析概述)
* [前提知识](#前提知识)
* [命令行参数](#命令行参数)
* [常见使用场景](#常见使用场景)
* [结果输出](#结果输出)
* [常见问题](#常见问题)


## 分析概述

CoraxJava主要由两部分组成，分别是`CoraxJava核心分析引擎`，和`CoraxJava规则检查器`。其中`CoraxJava核心分析引擎`不开源，可直接下载相应的`JAR`包执行。本项目为`CoraxJava规则检查器`模块，编译构建（过程中需指定`CoraxJava核心分析引擎`文件路径）成功后，会生成相应的分析插件，及配置文件。

`CoraxJava`静态分析工具的输入包含：编译后的各种格式存储的Java字节码`class`文件（`.class/.jar/.war/.apk/.dex`），被分析项目的源代码文件，以及一些`CoraxJava`所需的资源和配置文件。

1. **被分析的Java源代码项目**：应尽量保证是完整的项目，因为CoraxJava能够分析项目的一些配置文件。此外项目所依赖的其他第三方库的源代码不是必需的。
2. **项目的Java字节码class文件**：由项目的Java源码编译而成的各种形式的字节码文件是CoraxJava的主要分析对象，因此是必需的。尚未编译的Java项目需要根据配置手动执行编译过程，生成相应的字节码或二进制产物。例如：`maven`项目执行```mvn package```；`gradle`项目执行```gradle build``` 等。
3. **CoraxJava资源和配置文件**：通过gradle构建本项目（CoraxJava规则检查器模块），会自动生成运行CoraxJava所需的规则检查器插件及配置文件，执行CoraxJava分析时需载入配置文件，CoraxJava核心分析引擎会根据配置文件路径自动寻找相应的插件及其他配置文件，进行静态分析。


## 前提知识

### `class` 分类

首先需要了解`CoraxJava`中对 `class` 的分类。一般的，对于静态分析来说，被分析的`Java`应用程序的`class`类，我们统称为 `runtime classes`，`runtime classes` 可由如下三种class类组成：

- 用户项目类，也称 `ApplicationClasses`，一般的，项目被编译后，项目中包含的用户自己编写的源码，所对应的类被分类为 `ApplicationClasses`。
- 三方库类，也称 `LibraryClasses`，一般的，项目依赖的三方库被分类为 `LibraryClasses`。
  - 其中，依赖`Java`默认的执行环境的 `JRE` 的类，也称 `JavaLibraryClasses`，也是一种特定的 `LibraryClasses`。
- 无法找到的类，也称 `PhantomClasses`，一般的，项目代码中引用了一个类但是`CoraxJava`无法从输入的资源中找到的类,被分类为 `PhantomClasses`。



**注意：理解`class`的分类对于如何正确使用`CoraxJava`的命令行参数有重要的意义，并牢记和理解`ApplicationClasses`，`LibraryClasses` 等分类的含义。**

**注意：在分析过程中，应尽可能地提供完整的 `runtime classes`，减少 `PhantomClasses` 数量，提高分析结果的精准度（更少的漏报和更少的误报）。**



## 命令行参数

`CoraxJava` 的执行方式是使用Java运行`CoraxJava核心分析引擎` 的JAR包，通过参数指定分析对象、配置文件等，`CoraxJava核心分析引擎`会根据配置文件自动寻找相应的 `CoraxJava规则检查器`插件。主要的几个参数图示如下：

<img src="image/Main parameter configuration.jpg" style="zoom: 50%;" alt="Main parameter configuration"/>


简单的使用可参考  [开始分析](../Readme.md#开始分析) 。完整参数如下所示（对部分不重要选项有删减）：

```YAML
Usage: CoraxJava [OPTIONS]

Java Target Options:
  --custom-entry-point TEXT
                Sets the entry point method(s) for analyze. Eg: [file, soot
                signature, apk]
  --make-component-dummy-main
                Simple entry point creator that builds a sequential list of
                method invocations. Each method is invoked only once.

Android Target Options:
  --android-platform-dir PATH
                Sets the android platform directory or path of android.jar.
                The value of environment variable "ANDROID_JARS" is also
                accepted.
  --one-component-at-atime
                Set if analysis shall be performed on one entry of (Android
                component/Web application) at a time

FlowDroid Engine Options:
  --enable-flow-droid BOOL
                Set if the FlowDroid engine shall be enabled

DataFlow Engine Options:
  --enable-data-flow BOOL
                Set if the DataFlow engine shall be enabled


Options:
  --version     Show the version and exit
  --verbosity [ERROR|WARN|INFO|DEBUG|TRACE]
                Sets verbosity level of command line interface
  --config [NAME@]PATH[{pathSeparator}PATH]#pluginId or custom-config.yml@PATH[{pathSeparator}PATH]
                Specify the configuration jar and portal name which will be
                used as the analysis configuration. The environment variable:
                CORAX_CONFIG_DEFAULT_DIR Eg:
                "feysh.main.v1@corax-config/build/plugins"
  --output PATH
                Sets output directory of analysis result and metadata
  --dump-soot-scene
                dump soot scene
  --result-type [PLIST|SARIF|COUNTER]
                Sets output format of analysis result. This can be set
                multiple times, then different format of output will be given
                simultaneously. Eg: --result-type plist --result-type sarif
  --target [java|android]
                Specify the analyze target. Warning: Only corresponding target
                options are valid and others are ignored.
  --process TEXT
                Specify the classes that shall be processed(analyzed). Eg:
                --process [directory, dex, apk, jar, zip]
  --class-path TEXT
                Specify the [JAR/CLASS/SOURCE] paths. Hint: There are library
                classes and system classes in a project. Specify the
                "--process" with application classes to exclude them.
  --auto-app-classes TEXT
                The automatically classified classes from the specified paths
  --disable-library-load
                Disable load the library automatically.
  --disable-default-java-class-path
                Disable the default jdk/jre path for class path. Then a custom
                java class path should be given by "--class-path".
  --source-path PATH
                Specify the source file path with source directory root or
                source.jar file
  --src-precedence [prec_class|prec_only_class|prec_jimple|prec_java|prec_java_soot|prec_apk|prec_apk_class_jimple|prec_dotnet]
                Sets the source precedence type of analysis targets
  --ecj-options-file JSON_FILE
                Sets custom ecj options file
  --hide-no-source
                Set if problems found shall be hidden in the final report when
                source code is not available
  --indexes-in-archive
                Set whether to find the source code file from the compressed
                package
  --project-scan-config PATH
                Specify the path of project scan config file
  --disable-analyze-library-classes
                Sets whether classes that are declared library classes in Soot
                shall be excluded from the analysis, i.e., no flows shall be
                tracked through them
  --app-only    Setting this option to true causes Soot to only consider
                application classes when building the callgraph. The resulting
                callgraph will be inherently unsound.
  --disable-pre-analysis
                Skip the pre analysis phase. This will disable some checkers.
  --disable-built-in-analysis
                Skip the flow built-in analysis phase. This will disable some
                checkers.
  --static-field-tracking-mode [ContextFlowSensitive|ContextFlowInsensitive|None]
  --call-graph-algorithm TEXT
  --disable-reflection
                True if reflective method calls shall be not supported,
                otherwise false
  --max-thread-num INT
  --make-scorecard
                auto make scores for reports.
  -h, --help    Show this message and exit
```

### --target [java|android]

区别在于分析入口类和方法的选择，android有其特定的组件生命周期及入口点类，java则会包含更多的入口点类。

### --android-platform-dir

**当 --target 为 android 时，此参数为必填项**

指向 android platform 目录 (包含多个版本的 android.jar 文件)，可以 克隆 此项目 [android-platforms](https://github.com/Sable/android-platforms) 并将本参数指向 android-platforms 项目根目录。或者指向 [corax-config-tests/libs/platforms](../corax-config-tests/libs/platforms) （不建议，只有 android-7）



### --auto-app-classes

- 支持多参数
- 指向项目或文件路径
- 自动分析指定路径中的内容，对class自动分类，降低使用门槛

​**本参数指定路径下必须包含完整的项目源代码，及尽可能完整的运行时类 `runtime classes`（格式为..jar/.class/.war/.dex等，包括完整的项目类文件 `ApplicationClasses` 和三方依赖类 `LibraryClasses` ）**：

**原理**： `CoraxJava`会在本参数指定的路径中，递归查找所有的 Java字节码文件（class文件） 和 源代码文件，如果一个字节码类存在对应源码，则会被自动分类为 `ApplicationClasses`，如果一个字节码类（class）在本参数指定的路径下不存在源码，则被分类为 `LibrarayClasses`，**所以使用此参数时，如果源码缺少或不完整，会使`CoraxJava`以为该类为三方依赖库而非项目本身的源代码，从而导致漏报**。
	
​一般可将本参数直接指定到 **项目根目录（含源代码及编译产物）**，`CoraxJava` 将自动找出指定路径下的所有用户类、库类、源文件、资源和配置文件等作为分析目标开始分析。也可以分别指定到项目源代码目录，二进制编译产物目录（.jar/.class目录/.apk/dex/任意包含左侧文件的任意父目录），由于该选项支持多参数模式，也就是说二进制编译产物和项目源码不必放在一起同一目录下，甚至多个项目也可以一并指定后联合分析。

使用此参数时可以配合 --process, --class-path, --source-path 三个参数同时使用

以maven项目举例：

```BASH
cd {YourProject}
mvn package
... --enable-data-flow true --target java --auto-app-classes .
```
执行上述命令，会在maven项目根目录下执行分析，自动查找根据本项目的class字节码类有无对应的源代码，查找用户项目类，并建议将三方依赖库一并加入分析，如本项目目录下无三方依赖库，可以手动使用如下命令将三方库拉取到本项目目录。

Maven: **`mvn dependency:copy-dependencies -DoutputDirectory=target\libs`**

> 注：不建议直接将系统的三方库目录（如/User/xxx/.m2/）加入分析，因为数量太大导致分析效率低，建议只将本项目依赖的三方库加入分析。

Gradle:  参考 [Gradle项目](#Gradle项目) 



### --process

- 支持多参数

指定需要扫描的目标 classes，**该参数指向的 classes 被分类为 ApplicationClasses （分析的着重点）**

**如果此参数指定的类包含了三方库代码，那么将会浪费大量计算资源扫描三方库，得到大量我们不关心的问题报告。**

支持指定如下几种类型

- .class 所在任意父目录 (dir)
- jar|war|apk|dex 包文件路径 (file)
- /dir/dir/\*.jar 等等（支持 glob pattern）
- dir/example.jar!\BOOT-INF\classes (支持 zip inner resources 指定) 
- dir/example.jar!\BOOT-INF\lib\\*.jar (同样支持 glob pattern)
- \*\*/\*.jar!/\*\*/\*.jar （支持任意递归深度搜索jar内部来指定您想分析的资源）(linux shell 请加引号)

### --class-path

- 支持多参数


指定 ApplicationClasses 依赖的三方库 classes，**该参数指向的 classes 被分类为 `LibraryClasses`**

可以是 .class 文件目录或者 jar 包及其任意父目录

> **注意:  `LibraryClasses` 用于添加分析依赖，但分析器不会从中分析缺陷，用于提高分析精度减少误报漏报**

支持指定如下几种类型

- .class 所在任意父目录 (dir)
- jar|dex 包文件路径 (file)
- /dir/dir/\*.jar 等等（支持 glob pattern）
- dir/example.jar!\BOOT-INF\classes (支持 zip inner resources 指定) 
- dir/example.jar!\BOOT-INF\lib\\*.jar (同样支持 glob pattern)
- \*\*/\*.jar!/\*\*/\*.jar （支持任意递归深度搜索jar内部来指定您想分析的资源）(linux shell 请加引号)

### --source-path

- 支持多参数


指定代码缺陷展示所需的 java 源代码，如找不到源代码，则`CoraxJava`会丢弃已经扫描到的对应缺陷，从而导致漏报

仅仅需要指定源代码所在目录的任意父目录

> **注意: CoraxJava会根据 bug 所在 class 的名字（如com.feysh.testcode.cmdi） 递归该路径下的所有的源码来匹配**

### --disable-analyze-library-classes

flag option。 此选项默认关闭以保证分析精度
 java 过程间分析中会分析依赖的属于 `LibraryClasses` 的类方法，此选项决定是否跳过分析库方法，如果关闭将会减少一些分析资源消耗，但是很可能会降低分析精度(更多的漏报和误报，受具体项目而定)，按需开启。



### --project-scan-config

在 **ApplicationClasses** 和 资源文件（比如source file, config files） 对象中 进一步筛选要分析的目标，参考 [命令行参数](#命令行参数) 中的图示。

> 作用：选取项目中部分代码和资源文件进行分析

eg：

```
--project-scan-config JavaScanFilter.yml
```

获取详细内容请查看此文件 [JavaScanFilter.yml](JavaScanFilter.yml)



请注意正确的转义，比如这里的 `\.` 是正则的转义。如果想要知道该配置产生的影响，可以查看 output 目录中的这两个文件

`${output}/analyzeSkip/classSkipList.txt `（过滤掉的类）

`${output}/analyzeSkip/sourceFileSkipList.txt `（过滤掉的资源文件）

### --serialize-cg
flag option。 此选项默认关闭

是否需要在分析结束时，生成调用图（Call Graph）到输出目录。（要求 corax-cli.jar version >= 1.7）

您将在输出目录看到三个文件：`forward_interprocedural_callgraph.dot`，`forward_interprocedural_callgraph.json`，`forward_interprocedural_callgraph_complete.json`

您可以打开 dot 预览软件（比如 vscode 的 graphviz 插件） 来查看调用链。



### --make-scorecard
flag option。 此选项默认关闭

在分析器输出目录中生成统计结果。请参考 [unit-tests.md](unit-tests.md)，使用该方式编写测试用例后，开启该选项会自动生成准确性的统计结果。









## 常见使用场景


> 提示：本章节描述的几种场景均为手动模式，即手动指定`ApplicationClasses`，`LibraryClasses`和源代码目录。目的是更详细描述CoraxJava分析引擎的各个选项和参数作用，及常见 java 项目输出结构。理论上下列几种案例均可使用 **--auto-app-classes {项目根目录}** 一个参数来达到相同的分析效果。所以除非特殊需求，可跳过本章节内容。

**建议使用包含 corax-cli-v1.6.jar 及以上版本 的探针进行扫描**

### Java Sec Code

项目代码位于 github: [java-sec-code](https://github.com/JoyChou93/java-sec-code.git)，maven构建，无 sub module。

<!--web-boot-jar（org.springframework.boot: spring-boot-maven-plugin）-->

#### 第一步：编译

```Shell
git clone https://github.com/JoyChou93/java-sec-code.git
cd java-sec-code
mvn clean package
```

#### 第二步：观察编译输出的产物类型

得到如下信息：这是一个 spring-boot-maven-plugin 打包出来的 spring-boot jar 包，用户代码和三方库代码均包含在 `java-sec-code-1.0.0.jar` 中, 分析无需手动解压。

ApplicationClasses：target/java-sec-code-1.0.0.jar!\BOOT-INF\classes

LibraryClasses: target/java-sec-code-1.0.0.jar!\BOOT-INF\lib\*.jar

SourceCodeDir: .

使用`!`符号表示无需解压，指定压缩包内的文件或路径。

#### 第三步：编写分析命令

```shell
--process target/java-sec-code-1.0.0.jar!\BOOT-INF\classes
--class-path target/java-sec-code-1.0.0.jar!\BOOT-INF\lib\*.jar
--source-path .
```

### Alibaba:nacos

项目代码位于 github: [nacos](https://github.com/alibaba/nacos.git)，maven构建，多 sub modules，输出无三方 libs。

根据：nacos\pom.xml 中包含如下内容

```xml
 <!-- Submodule management -->
    <modules>
        <module>config</module>
        <module>core</module>
        <module>naming</module>
        <module>address</module>
        <module>test</module>
        <module>api</module>
        <module>client</module>
        <module>example</module>
        <module>common</module>
        <module>distribution</module>
        <module>console</module>
        <module>cmdb</module>
        <module>istio</module>
        <module>consistency</module>
        <module>auth</module>
        <module>sys</module>
        <module>plugin</module>
        <module>plugin-default-impl</module>
        <module>prometheus</module>
    </modules>
```

#### 第一步：编译

```Shell
git clone https://github.com/alibaba/nacos.git
cd nacos
git checkout 2.2.0
mvn package -Dmaven.test.skip=true -Prelease-nacos
```

#### 第二步：观察编译输出的产物类型

发现多个项目class输出目录如：

```Shell
nacos\istio\target\classes
nacos\client\target\classes
nacos\common\target\classes
......
```

但是并没有在项目目录中找到其依赖的三方库jar，这时候可以使用如下命令

```
mvn dependency:copy-dependencies -DoutputDirectory=target\libs
```

该命令会分别将项目每个module依赖的三方库放入 **`{module}\target\libs`** 文件夹下

ApplicationClasses：{module}\target\classes

LibraryClasses: {module}\target\libs

SourceCodeDir: nacos

#### 第三步：编写分析命令

```Shell
--process **\target\classes 
--class-path **\target\libs\*.jar
--source-path .
```

> 注意：请不要在最后加多余斜杠，如： ```**\target\classes\```

### Fat jar

某些项目编译后直接得到一个完整的 fat jar 文件，甚至是exe后缀的，比如以 [jadx 1.4.3](https://github.com/skylot/jadx/releases/download/v1.4.3/jadx-gui-1.4.3-with-jre-win.zip)为例，下载后将执行程序的 .exe 后缀改为 .jar 如下图所示：

<img src="image/fat jar-1.png" alt="img" style="zoom:50%;" />

此类项目的项目类文件和三方库类文件完全混合到一起，这种时候就需要使用 **--project-scan-config** 参数进一步筛选分析目标。

可以使用如下命令下载代码

```Shell
git clone https://github.com/skylot/jadx.git jadx-src
```

配合已经下载的编译产物，关键选项和参数可以如下：

```Shell
# 全部标记为 application class
--process jadx-gui-1.4.3-with-jre-win\jadx-gui-1.4.3.jar 
# 配合 filter 就可以从上面的 application class 中过滤掉 非jadx 开头的类，所以 application 只包含 jadx 开头的类了。过滤掉的类都成为了 library class，这部分类也会辅助分析以得到更精确的报告
--project-scan-config Jadx-JavaScanFilter.yml       
--source-path .\jadx-src
```

其中，Jadx-JavaScanFilter.yml 如下编写：

```YAML
# 部分省略
analyze-filter:
  class:
    exclude:
      - ..+$ # 所有除了 jadx 开头的类全部标记为 library class
    include:
      - jadx\..+$
```


**或者更简单的配合 --auto-app-classes 参数指向项目****源码****，也就自带 filter 效果，不再需要手动编写 filter 文件**

```YAML
--process jadx-gui-1.4.3-with-jre-win\jadx-gui-1.4.3.jar   
--auto-app-classes .\jadx-src
```


### Gradle项目

gradle 项目中的分析配置 和 maven 项目是没有什么区别的，因为`CoraxJava`分析的是编译产物，构建工具如何的运作是不影响的。

如果 gradle 构建后在项目目录下找不到三方库，可以修改项目的 `build.gradle` 文件，并添加

```Groovy
task copyRuntimeLibs(type: Copy) {
    into "build/libs"
    from configurations.runtime
}
```

Kotlin项目，修改项目的 `build.gradle.kts` 文件，并添加

```Kotlin
tasks.register<Copy>("copyRuntimeDependencies") {
    into("build/libs")
    from(configurations.runtimeClasspath)
}
```


扫描命令参数

```Bash
--process **\build\classes
--class-path **\build\libs\*.jar
--source-path .
```


## 结果输出

`--output output` 指向的输出目录在命令执行后会生成如下结果

```
output
├── analyzeSkip                      // --project-scan-config 参数的输出
│   ├── classSkipList.txt            // ApplictionClasses 中被过滤掉的类
│   └── sourceFileSkipList.txt       // 扫描资源文件中被过滤掉的文件
├── command.txt                      // 分析命令解析结果
├── source_files_which_class_not_found.txt // 项目中缺少对应class的源码, 可能是未能完整编译或没有正确指定到完整的classes
├── missing_method_summary.txt       // 被分析到且没有找到行为描述摘要的方法
├── phantom_dependence_classes.txt   // 被调用方法的 declaringClass 属于 phantomClasses. 表明依赖的三方库不完整
├── report-accuracy-forms            // --make-scorecard 参数的输出，分析报告的统计信息，用来检查误漏报
│   ├── FalsePN.csv                  // False(positive|negtive) 误漏报代码列表
│   ├── Scorecard.csv                // 报告的积分统计表.csv
│   ├── Scorecard.txt                // 报告的积分统计表.txt
│   ├── check-type-no-annotated.txt  // 在配置项目中找不到对应的BugType名字的标注漏洞points
│   └── source-not-found.txt         // 在报告中但无法找到对应源码的 class。故这部分无法被统计数据
└── sarif                            // sarif 格式的报告
    ├── **.sarif


```


### 误漏报表单

- [ ] Positive   (TP,FN)   <=>  阳性数量  =（真阳性数量  + 假阴性数量 ）<=> 不合格代码数量  =（真不合格数量  + 漏报数量 ）

- [ ] Negative  (TN,FP)  <=>  阴性数量 =（真阴性数量  + 假阳性数量 ） <=>    合规代码数量  =（     真合规数量  + 误报数量 ）



| True Positive Rate (TPR) = TP / ( TP + FN )  | The rate at which the tool correctly reports real vulnerabilities. Also referred to as Recall, as defined at [Wikipedia](https://en.wikipedia.org/wiki/Precision_and_recall). |
|:---------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| False Positive Rate (FPR) = FP / ( FP + TN ) | The rate at which the tool incorrectly reports fake vulnerabilities as real.                                                                                                  |
| Score = TPR - FPR                            | Normalized distance from the random guess line.                                                                                                                               |



### 缺失的依赖

​		被调用方法的 declaringClass 属于 phantomClasses，这表明依赖的三方库不完整，应该尽力保证除了 soot exclude 掉的类都不会出现在此文件中。（soot exclude 默认有：[java.*, sun.*, javax.*, com.sun.*, com.ibm.*, org.xml.*, org.w3c.*, apple.awt.*, com.apple.*]，具体排除加载了哪些类可以到命令输出中找到，也可以自定义 pattern 排除不想加载的类）

​		前往查看 here: [`{output}/phantom_dependence_classes.txt`](../build/output/phantom_dependence_classes.txt)



### 未建模的方法

​		被分析到且分析器无法从 corax java config plugins 中的方法 summaries 数据中找到对应建模描述的方法。并不是所有此文件中所有的方法都需要方法摘要（Summary），只有 隐式流传递，native method 和 具有附加特殊属性（比如高密级数据这个秘密等级属性是计算机无法感知的，需要人工或者 AI 额外标注）等等几类方法才需要手动到配置项目中添加摘要，一般的方法引擎能够自动完成分析，无需手动额外编写方法摘要。

​		前往查看 here: [`{output}/missing_method_summary.txt`](../build/output/missing_method_summary.txt)


### 详细日志

日志路径： `${sys:user.home}/logs/corax/`   分析命令开始和结束时均会输出此真实路径。

同样的日志等级，日志的内容要多于命令的输出

上次分析的分析日志：`${sys:user.home}/logs/corax/last.log`

最近累计的分析日志：`${sys:user.home}/logs/corax/rolling.log` (大小限制为20MB，无需担心)




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

​A: 在静态分析技术实现上，纯源码分析往往需要先将其转换为 AST（抽象语法树），对于 JVM 上运行的语言（Java、Kotlin、JSP、Scala 和 Groovy 等）都需要分别设计一对一的语言前端来解析，AST 又包含了各种各样复杂的语言特性和各种版本上的语法糖，这将严重增加分析引擎的负担，并且这不是静态分析的侧重点，所以往往 java 静态分析都会采用直接或者间接的方式分析 JVM字节码（即各种形式的编译产物 class） 来获得程序的操作语义信息。
   另一方面，源码分析需要各种语言环境和依赖，比如有 `import package.a.*;` 如果环境没有 `package.a` 这个包，那么所有引用了该包下的声明都将因必要的依赖信息不完整导致解析失败或者丢失分析精度，所以通常来说，分析 class 能比分析源码得到更高精度的报告。
   还有一方面就是使用源码分析，将极大地限制静态分析的使用场景，比如想要分析一个不带源码的二进制包。

**Q: 为什么分析需要源码？**
​

A: 源码不是必须提供的，只是为了更好地展示缺陷报告，以及在使用 `--auto-app-classes` 参数时候可以用来辅助分析器根据源码来匹配对应的 classes 以获知哪些类是项目类哪些是库类，这将帮助分析器更清楚分析的侧重点，简化参数的配置。

**Q: 为什么使用kotlin?**

​
A: kotlin 编写的代码更简单易懂，能极高提升开发者开发效率，易于维护；并且很难出现头疼的 `NullPointerException` 问题。kotlin 还支持协程异步执行，极高地提升了分析器效率；另外 kotlin 支持更多的语法特性，让我们更多的时间花费在算法本身而不是浪费在编程上：)，有木有心动？好吧！当然您仍然可以编写 java 代码并和 kotlin 代码放在一起混合调用，无需另外做任何事。



