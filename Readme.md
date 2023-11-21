# CoraxJava - 社区版

## 目录

* [项目介绍](#项目介绍)
* [快速开始](#快速开始)
  * [环境要求](#环境要求)
  * [编译构建](#编译构建)
  * [开始分析](#开始分析)
  * [支持Docker](#支持Docker)
* [查看报告](#查看报告)
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
├── analysis-config                                        // 分析配置目录
│   ├── default-config.yml                                 // 第一次分析后生成，分析工具根据插件中的默认参数自动生成的yaml格式主配置
│   │                                                      // 仅当修改后的主配置文件存在部分配置缺失或者一些配置无法对应到已有插件，
│   ├── default-config.normalize.yml                       // 以及存在风格问题时，引擎将会自动进行修补和规范化主配置并输出到此文件
│   ├── plugins                                            // 插件存放目录
│   │   ├── feysh-config-community-plugin-2.0-SNAPSHOT     		// 第一次运行分析后，自动解压，按需删除
│   │   ├── feysh-config-community-plugin-2.0-SNAPSHOT.zip 		// 编译产物，corax-config-community module 编译后生成的规则检查器插件
│   │   ├── feysh-config-general-plugin-2.0-SNAPSHOT     		// 第一次运行分析后，自动解压，按需删除
│   │   └── feysh-config-general-plugin-2.0-SNAPSHOT.zip   		// 编译产物, corax-config-general module 编译后生成的规则检查器插件
│   └── rules                                              // checker 的配置文件
│       ├── **.summaries.json
│       ├── **.sinks.json
│       ├── **.sources.json
│       ├── **.access-path.json
```
> 注意：`feysh-config-community-plugin-2.0-SNAPSHOT.zip` 为 `corax-config-community` 模块编译后生成的规则检查器插件，主要包含了自定义规则检查器的实现，`feysh-config-general-plugin-2.0-SNAPSHOT.zip` 为 `corax-config-general` 模块编译后生成的规则检查器插件，主要包含了一些通用的内建检查器模型，一般不需要修改。

### 开始分析

开始 `CoraxJava` 的分析，需要使用 `Java` 执行 `CoraxJava核心引擎`（`corax-cli-x.x.x.jar`），引擎会以插件方式载入 `CoraxJava规则检查器`（`feysh-config-general-plugin-2.0-SNAPSHOT.zip`及`feysh-config-community-plugin-2.0-SNAPSHOT.zip`） 及其配置文件（`default-config.yml`），最后再正确指定需要分析的Java项目的字节码及源代码的路径即可。

手动配置 `CoraxJava` 以下必要参数，开始进行分析

- ​		分析器的启动命令 `java -jar corax-cli-x.x.x.jar` 
- ​		设置输出目录 `--output build/output`
- ​		开启数据流引擎 `--enable-data-flow true`
- ​		设置分析对象的类型 `--target java`
- ​		设置报告输出格式 `--result-type sarif`，可不加，默认为 sarif 格式
- ​		设置分析目标所在路径，此处以本项目所包含的测试用例举例 `--auto-app-classes ./corax-config-tests`，此参数要求该路径或子目录下必须包含项目源码及编译后的字节码产物（class文件或 jar 包都可），
- ​		指定配置的参数格式为 `--config (yaml文件名字.yml)@(配置文件夹路径)`，yml 文件名可以任意命名不必一定存在，配置的路径为上一  步骤 [编译构建](#编译构建) 中生成的配置文件夹 [build/analysis-config](build%2Fanalysis-config)

```bash
$ java -jar corax-cli-x.x.x.jar --verbosity info --output build/output --enable-data-flow true --target java --result-type sarif --auto-app-classes ./corax-config-tests --config default-config.yml@./build/analysis-config
```

执行此命令时，如果分析器无法在指定的 `./build/analysis-config` 目录中被找到该名为`default-config.yml`的 yml 文件，将自动根据插件中的默认参数生成一个同名的默认配置：[build/analysis-config/default-config.yml](build%2Fanalysis-config%2Fdefault-config.yml)，如果需要更改配置，请复制整个 [build/analysis-config](build%2Fanalysis-config) 文件夹到您的工作目录，并适当按照您的需求自定义修改配置，在下次的分析前指定参数 `--config 配置文件名.yml@配置文件夹` ，使其生效。

最终的结果会在`--output` 参数指定的路径下生成。

**阅读 [CoraxJava使用](docs/usage.md) 了解完整的使用详情。**

### 支持Docker

请先确保已安装 `Docker`，在项目根目录 `corax-community` 下构建Docker镜像：
```bash
$ docker build -t --network=host corax-community .
```
构建期间会下载 `CoraxJava核心引擎`，并解压到 `/corax-community` 目录下，构建完成后生成快捷方式为 `/corax-community/corax-cli.jar`

>考虑到可能会遇到网络环境受限，也可以手动下载 `CoraxJava核心引擎`，放在 `/corax-community` 即可，注意版本号与 `Dockerfile CORAX_VERSION` 保持一致。

使用方式: 
```bash
$ corax_cmd='java -jar corax-cli.jar --verbosity info --output build/output --enable-data-flow true --target java --result-type sarif --auto-app-classes ./corax-config-tests --config default-config.yml@./build/analysis-config'
$ docker run -it --rm -v {指定扫描结果输出路径}:/corax-community/build/output -v {指定需要扫描的代码仓库路径}:{映射到容器内的路径} corax-community ${corax_cmd}
```
注意:
1. 由于扫描行为发生在容器内，所以需要将代码仓库映射到容器内，否则无法扫描，建议映射到容器内的路径和宿主机真实路径保持一致，否则可能无法体验 `sarif` 相关插件的跳转功能。
2. 由于 Windows 与 Linux 文件系统差异，Windows 用户无法完整体验 `sarif` 相关插件的跳转功能，建议使用 Linux 系统或者手动构建。

## 查看报告

目前 CoraxJava 支持生成 [SARIF](https://sarifweb.azurewebsites.net/) 格式的结果报告，生成报告的路径由 `--output` 参数指定，在该路径下会生成 `sarif/` 目录，其中每个`java`源码文件会单独生成一个 `sarif` 格式的结果文件。

建议使用 `VSCode` 查看 `SARIF` 格式的报告，可以安装 `Sarif Viewer` 插件方便可视化查看和跳转。

## 自定义规则检查器

目前本项目中包含以下已实现的规则检查器，我们会在后续的更新中持续添加其他的检测规则。
| 名称                 | 说明                                     |
| -------------------- | ---------------------------------------- |
| SQL注入检测          | 支持检测常见的SQL注入，并支持mybatis框架 |
| XSS漏洞检测          | 支持检测Spring接口的responsebody注入场景     |
| 命令注入检测         | 支持检测常见的命令注入                   |
| cookie未设置httpOnly | 支持检测Cookie未设置httpOnly标志位       |
| cookie未设置secure   | 支持检测Cookie未设置secure标志位         |
| 不安全的TLS版本      | 支持检测设置TLS版本低于1.2的情况         |


**如需实现自定义的 `CoraxJava规则检查器`，请参考 [CoraxJava规则检查器](docs/checker.md) 了解更多详情。**


## 交流反馈

提交误报漏报：
- github issue    
- 发送邮件至bug@corax.com

交流群：

交流群人数较多，已经无法通过二维码加入，请通过以下方式入群：
- 添加微信号“Corax社区助手”--注明“Corax社区版交流群”--等待被拉入群
<img src="https://github.com/Feysh-Group/corax-community/assets/6300471/4e422679-4889-4209-8cd5-173654007c9f" height="320px">

