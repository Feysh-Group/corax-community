<p align="center">
 <img width="100px" src="https://res.cloudinary.com/dwvfopirq/image/upload/v1706766411/corax.svg" align="center" alt="Corax For Java - Community Edition" />
 <h2 align="center">Corax For Java - 社区版</h2>
</p>
  <p align="center">
    <a href="/docs/zh/feature_diff.md">社区版功能对比</a>
    ·
    <a href="https://github.com/Feysh-Group/corax-community/issues/new?assignees=&labels=bug&projects=&template=bug_report.yml">报告BUG</a>
    ·
    <a href="https://github.com/Feysh-Group/corax-community/issues/new?assignees=&labels=enhancement&projects=&template=feature_request.yml">功能需求</a>
    ·
    <a href="/docs/zh/FAQ.md">常见问题</a>
    ·
    <a href="https://github.com/Feysh-Group/corax-community/issues">提问</a>
  </p>
  <p align="center">
    <a href="/Readme.md">English</a>
  </p>
</p>


## 目录

<!-- toc -->

- [项目介绍](#%E9%A1%B9%E7%9B%AE%E4%BB%8B%E7%BB%8D)
- [快速开始](#%E5%BF%AB%E9%80%9F%E5%BC%80%E5%A7%8B)
  * [1. 准备待分析项目](#1-%E5%87%86%E5%A4%87%E5%BE%85%E5%88%86%E6%9E%90%E9%A1%B9%E7%9B%AE)
  * [2. 快速开始分析](#2-%E5%BF%AB%E9%80%9F%E5%BC%80%E5%A7%8B%E5%88%86%E6%9E%90)
  * [3. 查看报告](#3-%E6%9F%A5%E7%9C%8B%E6%8A%A5%E5%91%8A)
- [搭建开发环境](#%E6%90%AD%E5%BB%BA%E5%BC%80%E5%8F%91%E7%8E%AF%E5%A2%83)
  * [环境要求](#%E7%8E%AF%E5%A2%83%E8%A6%81%E6%B1%82)
  * [编译构建](#%E7%BC%96%E8%AF%91%E6%9E%84%E5%BB%BA)
  * [参数配置](#%E5%8F%82%E6%95%B0%E9%85%8D%E7%BD%AE)
  * [CoraxJava+Docker](#coraxjavadocker)
- [测试集表现](#%E6%B5%8B%E8%AF%95%E9%9B%86%E8%A1%A8%E7%8E%B0)
- [自定义规则检查器](#%E8%87%AA%E5%AE%9A%E4%B9%89%E8%A7%84%E5%88%99%E6%A3%80%E6%9F%A5%E5%99%A8)
- [交流反馈](#%E4%BA%A4%E6%B5%81%E5%8F%8D%E9%A6%88)
- [招贤纳士](#%E6%8B%9B%E8%B4%A4%E7%BA%B3%E5%A3%AB)

<!-- tocstop -->

## 项目介绍

CoraxJava(Corax社区版)是一款针对Java项目的静态代码安全分析工具，其核心分析引擎来自于Corax商业版，具备与Corax商业版一致的底层代码分析能力，并在此基础上配套了专用的开源规则检查器与规则。

CoraxJava由两部分组成，分别是`CoraxJava核心分析引擎`和`CoraxJava规则检查器`。其中规则检查器模块支持实现多种规则的检查。目前CoraxJava包含了抽象解释和 [IFDS (Sparse analysis implemented by Feysh)](https://github.com/Feysh-Group/FlowDroid) 等程序分析技术。未来，我们将持续优化引擎分析性能和提高分析精度，并研究引入更强大的程序分析算法，以持续增强核心分析能力，推动代码的安全、质量和性能不断提升。

CoraxJava具有以下特点：

1. 完全开放的规则检查器模块，开源多个规则检查器代码示例。
2. 支持使用Kotlin/Java语言开发自定义规则检查器。
3. 支持通过**配置文件**或**编写代码**的方式修改、生成规则检查器。
4. 分析对象为Java字节码，但需要源代码作为结果显示的参考和依据
5. 分析结果以sarif格式输出。

> 注意：目前CoraxJava核心分析引擎不开源，需要[下载引擎的jar包](https://github.com/Feysh-Group/corax-community/releases)（ corax-cli-x.x.x.jar）配合规则检查器使用。本代码仓库为CoraxJava自定义规则检查器模块，其中包含多个开源规则检查器的代码实现。

**阅读 [Corax社区版功能对比](docs/zh/feature_diff.md) 了解Corax社区版与商业版的差异。**

## 快速开始

本仓库为`CoraxJava规则检查器`模块，并包含测试用例 [corax-config-tests](corax-config-tests)，项目构建完成后，`CoraxJava规则检查器`模块为插件（独立zip包）形式，需配合`CoraxJava核心引擎`模块，可执行Java静态代码检测与分析，测试用例可用来快速测试和验证CoraxJava的检测结果。



### 1. 准备待分析项目

**首先请准备好您的待分析对象**，一般情况：

- [x] 需要包含java源码的项目且尽量完整，项目源码不可放在压缩归档中
- [x] 需要完整的项目编译产物和尽量完整的三方库jar：（如果没有已经编译打包好的产物，则请手动编译打包：如`mvn package -Dmaven.test.skip.exec=true -DskipTests` 、`gradle build -x test` ， 应避免使用`mvn compile/jar`  or `gradle compile/jar` ，因后者命令往往不会拉取项目依赖的三方库jar且编译产物不完整）
  - [x] 比如此项目源码对应的 包含大量.class的文件夹（`target/classes`、`build/classes`）
  - [x] 项目源码编译后对应的 `.jar`/`.war`/`.zip` 文件，或包含它们的任意文件夹路径
  - [x] 三方库jar的所在文件夹（尽量提供, 没有的话可以使用 `mvn dependency:copy-dependencies -DoutputDirectory=target\libs`命令手动拉取）



### 2. 快速开始分析

**一行命令即可分析**

首次运行，脚本会按需联网下载jdk和corax release并解压到相应位置，并且不会破坏原有环境~

- linux、darwin
  - 下载 [coraxjw.sh](coraxjw.sh)
  - 复制并运行以下命令：
    ```bash
    chmod +x ./coraxjw.sh
    ./coraxjw.sh --target java --auto-app-classes "{项目根目录 (包含源码和编译产物) }" --output corax_reports
    ```
- windows
  - 下载 [coraxjw.ps1](coraxjw.ps1)
  - cmd:
    ```shell
    @"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -InputFormat None -ExecutionPolicy Bypass -File coraxjw.ps1 --target java --auto-app-classes "{项目根目录 (包含源码和编译产物) }" --output corax_reports
    ```

  - Powershell:
    ```PowerShell
    Set-ExecutionPolicy Bypass -Scope Process -Force; ./coraxjw.ps1 --target java --auto-app-classes "{项目根目录 (包含源码和编译产物) }" --output corax_reports
    ```

注：下载的脚本中包含的固定版本的CoraxJava分析器和规则包，后续需要更新分析工具，请找到您之前已经下载的脚本，然后执行uninstall并删除脚本，再次按照上面步骤下载即获得最新版（Master稳定分支）的 CoraxJava分析器

一般参数说明： [一般配置参数](#参数配置)

详细参数说明：[命令行参数详解](docs/zh/usage.md#命令行参数)

如需卸载，直接运行 `./coraxjw.sh uninstall` 即可

建议剩余内存大于12g, 否则分析大型项目时容易出现OOM（Out Of Memory）错误

### 3. 查看报告

目前 CoraxJava 支持生成 [SARIF](https://sarifweb.azurewebsites.net/) 格式的结果报告，生成报告的路径由 `--output` 参数指定，在该路径下会生成 `sarif/` 目录，其中每个`java`源码文件会单独生成一个 `sarif` 格式的结果文件。

建议使用 `VSCode` 查看 `SARIF` 格式的报告，可以安装 **Sarif Viewer** 插件方便可视化查看和跳转。

**Sarif Viewer** 使用方法：

1. 安装 [VisualStudio Code](https://code.visualstudio.com/)
2. 安装 VSCode: Sarif Viewer 插件
   - 打开网页 [sarif-viewer](https://marketplace.visualstudio.com/items?itemName=MS-SarifVSCode.sarif-viewer)  并点击 install 按钮
   - 或者命令行安装 `code --install-extension MS-SarifVSCode.sarif-viewer`
3. 查看 sarif 格式报告
   1. 在 VSCode 中打开 corax 分析输出目录中的 sarif 文件夹中的任意 .sarif 文件后，则应会自动弹出报告预览窗口，如果没有对应窗口，可以`ctrl+shit+p`  搜索 "sarif" 点击"Sarif: Show Panel" 即可。
   2. 在 Sarif Viewer 的 SARIF Results 窗口的右上角处点击文件夹图标，可选中并打开多个 sarif 文件进行查看。



## 搭建开发环境


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
│   │   ├── feysh-config-community-plugin-2.6               // 第一次运行分析后，自动解压，按需删除
│   │   ├── feysh-config-community-plugin-2.6.zip           // 编译产物，corax-config-community module 编译后生成的规则检查器插件
│   │   ├── feysh-config-general-plugin-2.6                 // 第一次运行分析后，自动解压，按需删除
│   │   └── feysh-config-general-plugin-2.6.zip             // 编译产物, corax-config-general module 编译后生成的规则检查器插件
│   └── rules                                              // 规则检查器的一些静态数据，用户可以自定义进行配置
│       ├── **.sources.json                                   // taint sources
│       ├── **.summaries.json                                 // taint summaries,sanitizers
│       ├── **.sinks.json                                     // taint sinks
│       ├── **.access-path.json                               // method signature and sink data
```
> 注意：`feysh-config-community-plugin-2.6.zip` 为 `corax-config-community` 模块编译后生成的规则检查器插件，主要包含了自定义规则检查器的实现，`feysh-config-general-plugin-2.6.zip` 为 `corax-config-general` 模块编译后生成的规则检查器插件，主要包含了一些通用的内建检查器模型，一般不需要修改。

### 参数配置

​        **第一步**：分析引擎需要载入 `CoraxJava规则检查器插件`（如：`analysis-config/plugins/feysh-config-*-plugin-*.*.*.zip`）及依赖的一些配置文件（如` analysis-config/rules`），所以需要准备好 `analysis-config` (规则配置文件夹) **：**

  - 可以使用从 [release](https://github.com/Feysh-Group/corax-community/releases) 下载并解压zip得到已生成好的 `analysis-config`目录： `{corax-java-cli-community-2.6.zip解压位置}/analysis-config/`
  - 或者使用[编译构建](#编译构建)步骤中生成的[build/analysis-config](build%2Fanalysis-config)目录：`./build/analysis-config/`



​        **第二步：开始分析 ！需要手动配置 `CoraxJava` 以下必要参数：**

- ​        分析器的启动命令 `java -jar corax-cli-x.x.x.jar`    （从 [release](https://github.com/Feysh-Group/corax-community/releases) 下载并解压zip得到 `CoraxJava核心引擎`（`corax-cli-x.x.x.jar`））

- ​        设置输出目录 `--output build/output`

- ​        开启数据流引擎 `--enable-data-flow true`

- ​        设置分析对象的类型 `--target java`

- ​        设置报告输出格式 `--result-type sarif`，可不加，默认为 sarif 格式

- ​        设置分析目标所在路径，此处以本项目所包含的测试用例举例 `--auto-app-classes ./corax-config-tests`，此参数要求该路径或子目录下必须包含项目源码及编译后的字节码产物（class文件或 jar 包都可），

- ​        指定配置的参数格式为 `--config (yaml文件名字.yml)@(规则配置文件夹)`，yml 文件名可以任意命名不必一定存在。`(规则配置文件夹)`就是前面所准备好的 `analysis-config` 的路径。例如
  - `--config default-config.yml@{corax-java-cli-community-2.6.zip解压位置}/analysis-config/`
  - `--config default-config.yml@./build/analysis-config/`
  
  
  
  分析命令模板：

```bash
$ java -jar corax-cli-x.x.x.jar --verbosity info --output build/output --enable-data-flow true --target java --result-type sarif --auto-app-classes {项目根目录（包含源码和编译产物）} --config default-config.yml@{corax-java-cli-community-2.6.zip解压位置}/analysis-config/
```

​        **tips**: 如果项目根目录没有编译产物，可以再增加任意个数的 `--auto-app-classes` 参数指向编译产物所在的位置或文件夹

​		执行此命令时，如果分析引擎无法在指定的 `{corax-java-cli-community-2.6.zip解压位置}/analysis-config/` 目录中找到名为`default-config.yml`的 yml 文件，将自动根据插件中的默认参数生成一个同名的默认yaml主要配置文件到规则配置文件夹：`{corax-java-cli-community-2.6.zip解压位置}/analysis-config/default-config.yml`，如果需要更改配置，请复制整个 `analysis-config` 文件夹到您的工作目录，并适当按照您的需求自定义修改配置，在下次的分析前指定参数 `--config 配置文件名.yml@新的规则配置文件夹` ，使其生效。



​		最终的报告会在`--output` 参数指定的文件夹路径下生成。



​		**阅读 [CoraxJava使用](docs/zh/usage.md) 了解完整的使用详情。**



### CoraxJava+Docker

**如果想体验docker方式的扫描：可阅读 [CoraxJava+Docker扫描教程](docs/zh/coraxdocker-usage.md) 了解完整的使用详情。**

## 测试集表现
**阅读 [Corax社区版功能对比](docs/zh/feature_diff.md#SAST测试集表现) 了解本Java社区版工具在SAST测试集上的表现。**

## 自定义规则检查器

**阅读 [Corax社区版功能对比](docs/zh/feature_diff.md#已开放规则) 了解本Java社区版工具已开放的规则。**

**如需实现自定义的 `CoraxJava规则检查器`，请参考 [CoraxJava规则检查器](docs/zh/checker.md) 了解更多详情。**


## 交流反馈

提交误报漏报：
- github issue    
- 发送邮件至bug@feysh.com

交流群：

交流群人数较多，已经无法通过二维码加入，请通过以下方式入群：
- 添加微信号“Corax社区助手”--注明“Corax社区版交流群”--等待被拉入群
<img src="https://github.com/Feysh-Group/corax-community/assets/6300471/4e422679-4889-4209-8cd5-173654007c9f" height="320px">



## 招贤纳士

如果您对本项目或Java代码静态分析感兴趣，并希望从事这方面的工作或研究（全职/兼职/实习皆可），欢迎发送邮件至 [job@feysh.com](mailto:job@feysh.com)，并注明“CoraxJava岗位”，我们会第一时间联系您。