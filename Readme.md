# CoraxJava - 社区版

## 目录

* [项目介绍](#项目介绍)
* [快速开始](#快速开始)
  * [环境要求](#环境要求)
  * [编译构建](#编译构建)
  * [开始分析](#开始分析)
  * [CoraxJava+Docker](#CoraxJavaDocker)
* [查看报告](#查看报告)
* [测试集表现](#测试集表现)
* [自定义规则检查器](#自定义规则检查器)
* [交流反馈](#交流反馈)

## 项目介绍

CoraxJava(Corax社区版)是一款针对Java项目的静态代码安全分析工具，其核心分析引擎来自于Corax商业版，具备与Corax商业版一致的底层代码分析能力，并在此基础上配套了专用的开源规则检查器与规则。

CoraxJava由两部分组成，分别是`CoraxJava核心分析引擎`和`CoraxJava规则检查器`。其中规则检查器模块支持实现多种规则的检查。目前CoraxJava包含了抽象解释和IFDS（Sparse）等程序分析技术。未来，我们将持续优化引擎分析性能和提高分析精度，并研究引入更强大的程序分析算法，以持续增强核心分析能力，推动代码的安全、质量和性能不断提升。

CoraxJava具有以下特点：

1. 完全开放的规则检查器模块，开源多个规则检查器代码示例。
2. 支持使用Kotlin/Java语言开发自定义规则检查器。
3. 支持通过**配置文件**或**编写代码**的方式修改、生成规则检查器。
4. 分析对象为Java字节码，但需要源代码作为结果显示的参考和依据
5. 分析结果以sarif格式输出。

> 注意：目前CoraxJava核心分析引擎不开源，需要[下载引擎的jar包](https://github.com/Feysh-Group/corax-community/releases)（ corax-cli-x.x.x.jar）配合规则检查器使用。本代码仓库为CoraxJava自定义规则检查器模块，其中包含多个开源规则检查器的代码实现。

**阅读 [Corax社区版功能对比](docs/feature_diff.md) 了解Corax社区版与商业版的差异。**

## 快速开始

本仓库为`CoraxJava规则检查器`模块，并包含测试用例 [corax-config-tests](corax-config-tests)，项目构建完成后，`CoraxJava规则检查器`模块为插件（独立zip包）形式，需配合`CoraxJava核心引擎`模块，可执行Java静态代码检测与分析，测试用例可用来快速测试和验证CoraxJava的检测结果。


### 环境要求

需要安装Java运行环境，并要求JDK版本为17（使用其他版本编译和分析很可能会出现错误）。在 Debian，Ubuntu 等系统上可以使用以下命令安装 JDK 17：
```bash
$ sudo apt-get install openjdk-17-jdk
```
在 Fedora，Centos 等系统上可以使用以下命令安装：
```bash
$ sudo yum -y install java-17-openjdk
```
在 MacOS令安装：
```bash
$ brew install openjdk@17
```
Windows 用户可以下载 [openjdk-17+35_windows-x64_bin.zip](https://download.java.net/openjdk/jdk17/ri/openjdk-17+35_windows-x64_bin.zip) 并解压，在`高级系统设置`中设置好JDK的环境变量。

可以使用以下命令查看jdk版本，并确认为17。
```bash
$ java -version
```

### 编译构建

提示： 此步骤仅供开发者参考，如果只想快速地体验功能，可以在 [release](https://github.com/Feysh-Group/corax-community/releases) 中直接下载最新的已编译好的产物，包含`CoraxJava核心引擎 corax-cli-x.x.jar`和规则配置，然后跳到下个步骤直接开始分析！


在项目根目录`corax-community`下执行gradle构建(建议进入根目录执行./gradlew build构建，避免版本问题)：
```bash
$ cd corax-community
$ ./gradlew build
```
根据提示，需要在 [gradle-local.properties](gradle-local.properties) 文件中指定 `CoraxJava核心引擎` 的文件路径：

```text
coraxEnginePath=<PATH TO corax-cli-x.x.jar> // 使用绝对路径，不要加引号
```

再次执行 build

```bash
$ gradlew build
```

构建成功后，会生成 多个zip后缀的插件 和 配置文件，并按结构存放到 [build/analysis-config](build%2Fanalysis-config) 文件夹中，统称为分析配置目录，结构如下：

```
├── analysis-config                                     // CoraxJava规则检查器 所在位置
│   ├── default-config.yml                                 // 第一次分析后生成，分析工具根据插件中的默认参数自动生成的yaml格式主配置
│   │                                                      // 仅当修改后的主配置文件存在部分配置缺失或者一些配置无法对应到已有插件，
│   ├── default-config.normalize.yml                       // 以及存在风格问题时，引擎将会自动进行修补和规范化主配置并输出到此文件
│   ├── plugins                                            // 插件存放目录
│   │   ├── feysh-config-community-plugin-2.5               // 第一次运行分析后，自动解压，按需删除
│   │   ├── feysh-config-community-plugin-2.5.zip           // 编译产物，corax-config-community module 编译后生成的规则检查器插件
│   │   ├── feysh-config-general-plugin-2.5                 // 第一次运行分析后，自动解压，按需删除
│   │   └── feysh-config-general-plugin-2.5.zip             // 编译产物, corax-config-general module 编译后生成的规则检查器插件
│   └── rules                                              // 规则检查器的一些静态数据，用户可以自定义进行配置
│       ├── **.sources.json                                   // taint sources
│       ├── **.summaries.json                                 // taint summaries,sanitizers
│       ├── **.sinks.json                                     // taint sinks
│       ├── **.access-path.json                               // method signature and sink data
```
> 注意：`feysh-config-community-plugin-2.5.zip` 为 `corax-config-community` 模块编译后生成的规则检查器插件，主要包含了自定义规则检查器的实现，`feysh-config-general-plugin-2.5.zip` 为 `corax-config-general` 模块编译后生成的规则检查器插件，主要包含了一些通用的内建检查器模型，一般不需要修改。

### 开始分析

​		**第一步：首先请准备好您的待分析对象**，一般情况：

- [x] 需要包含java源码的项目且尽量完整，项目源码不可放在压缩归档中
- [x] 需要完整的项目编译产物和尽量完整的三方库jar：（如果没有已经编译打包好的产物，则请手动编译打包：如`mvn package -Dmaven.test.skip.exec=true -DskipTests` 、`gradle build -x test` ， 应避免使用`mvn compile/jar`  or `gradle compile/jar` ，因后者命令往往不会拉取项目依赖的三方库jar且编译产物不完整）
  - [x] 比如此项目源码对应的 包含大量.class的文件夹（`target/classes`、`build/classes`）
  - [x] 项目源码编译后对应的 `.jar`/`.war`/`.zip` 文件，或包含它们的任意文件夹路径
  - [x] 三方库jar的所在文件夹（尽量提供, 没有的话可以使用 `mvn dependency:copy-dependencies -DoutputDirectory=target\libs`命令手动拉取）



​        **第二步**：分析引擎需要载入 `CoraxJava规则检查器插件`（如：`analysis-config/plugins/feysh-config-*-plugin-*.*.*.zip`）及依赖的一些配置文件（如` analysis-config/rules`），所以需要准备好 `analysis-config` (规则配置文件夹) **：**

  - 可以使用从 [release](https://github.com/Feysh-Group/corax-community/releases) 下载并解压zip得到已生成好的 `analysis-config`目录： `{corax-java-cli-community-2.5.zip解压位置}/analysis-config/`
  - 或者使用[编译构建](#编译构建)步骤中生成的[build/analysis-config](build%2Fanalysis-config)目录：`./build/analysis-config/`



​        **第三步：开始分析 ！需要手动配置 `CoraxJava` 以下必要参数：**

- ​        分析器的启动命令 `java -jar corax-cli-x.x.x.jar`    （从 [release](https://github.com/Feysh-Group/corax-community/releases) 下载并解压zip得到 `CoraxJava核心引擎`（`corax-cli-x.x.x.jar`））

- ​        设置输出目录 `--output build/output`

- ​        开启数据流引擎 `--enable-data-flow true`

- ​        设置分析对象的类型 `--target java`

- ​        设置报告输出格式 `--result-type sarif`，可不加，默认为 sarif 格式

- ​        设置分析目标所在路径，此处以本项目所包含的测试用例举例 `--auto-app-classes ./corax-config-tests`，此参数要求该路径或子目录下必须包含项目源码及编译后的字节码产物（class文件或 jar 包都可），

- ​        指定配置的参数格式为 `--config (yaml文件名字.yml)@(规则配置文件夹)`，yml 文件名可以任意命名不必一定存在。`(规则配置文件夹)`就是前面所准备好的 `analysis-config` 的路径。例如
  - `--config default-config.yml@{corax-java-cli-community-2.5.zip解压位置}/analysis-config/`
  - `--config default-config.yml@./build/analysis-config/`
  
  
  
  分析命令模板：

```bash
$ java -jar corax-cli-x.x.x.jar --verbosity info --output build/output --enable-data-flow true --target java --result-type sarif --auto-app-classes {项目根目录（包含源码和编译产物）} --config default-config.yml@{corax-java-cli-community-2.5.zip解压位置}/analysis-config/
```

​        **tips**: 如果项目根目录没有编译产物，可以再增加任意个数的 `--auto-app-classes` 参数指向编译产物所在的位置或文件夹

​		执行此命令时，如果分析引擎无法在指定的 `{corax-java-cli-community-2.5.zip解压位置}/analysis-config/` 目录中找到名为`default-config.yml`的 yml 文件，将自动根据插件中的默认参数生成一个同名的默认yaml主要配置文件到规则配置文件夹：`{corax-java-cli-community-2.5.zip解压位置}/analysis-config/default-config.yml`，如果需要更改配置，请复制整个 `analysis-config` 文件夹到您的工作目录，并适当按照您的需求自定义修改配置，在下次的分析前指定参数 `--config 配置文件名.yml@新的规则配置文件夹` ，使其生效。



​		最终的报告会在`--output` 参数指定的文件夹路径下生成。



​		**阅读 [CoraxJava使用](docs/usage.md) 了解完整的使用详情。**



### CoraxJava+Docker

**如果想体验docker方式的扫描：可阅读 [CoraxJava+Docker扫描教程](docs/coraxdocker-usage.md) 了解完整的使用详情。**

## 查看报告

目前 CoraxJava 支持生成 [SARIF](https://sarifweb.azurewebsites.net/) 格式的结果报告，生成报告的路径由 `--output` 参数指定，在该路径下会生成 `sarif/` 目录，其中每个`java`源码文件会单独生成一个 `sarif` 格式的结果文件。

建议使用 `VSCode` 查看 `SARIF` 格式的报告，可以安装 `Sarif Viewer` 插件方便可视化查看和跳转。

## 测试集表现
**阅读 [Corax社区版功能对比](docs/feature_diff.md#SAST测试集表现) 了解本Java社区版工具在SAST测试集上的表现。**

## 自定义规则检查器

**阅读 [Corax社区版功能对比](docs/feature_diff.md#已开放规则) 了解本Java社区版工具已开放的规则。**

**如需实现自定义的 `CoraxJava规则检查器`，请参考 [CoraxJava规则检查器](docs/checker.md) 了解更多详情。**


## 交流反馈

提交误报漏报：
- github issue    
- 发送邮件至bug@corax.com

交流群：

交流群人数较多，已经无法通过二维码加入，请通过以下方式入群：
- 添加微信号“Corax社区助手”--注明“Corax社区版交流群”--等待被拉入群
<img src="https://github.com/Feysh-Group/corax-community/assets/6300471/4e422679-4889-4209-8cd5-173654007c9f" height="320px">



## 招贤纳士：

如果您对本项目或Java代码静态分析感兴趣，并希望从事这方面的工作或研究（全职/兼职/实习皆可），欢迎发送邮件至 [job@feysh.com](mailto:job@feysh.com)，并注明“CoraxJava岗位”，我们会第一时间联系您。