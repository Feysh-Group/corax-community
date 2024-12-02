<p align="center">
 <img width="100px" src="https://res.cloudinary.com/dwvfopirq/image/upload/v1706766411/corax.svg" align="center" alt="Corax For Java - Community Edition" />
 <h2 align="center">Corax For Java - Community Edition</h2>
</p>
  <p align="center">
    <a href="/docs/en/feature_diff.md">Feature Diff</a>
    ·
    <a href="https://github.com/Feysh-Group/corax-community/issues/new?assignees=&labels=bug&projects=&template=bug_report.yml">Report Bug</a>
    ·
    <a href="https://github.com/Feysh-Group/corax-community/issues/new?assignees=&labels=enhancement&projects=&template=feature_request.yml">Request Feature</a>
    ·
    <a href="/docs/en/FAQ.md">FAQ</a>
    ·
    <a href="https://github.com/Feysh-Group/corax-community/issues">Ask Question</a>
  </p>
  <p align="center">
    <a href="/Readme-zh.md">简体中文</a>
  </p>
</p>


**Table of contents**

<!-- toc -->

- [Project Introduction](#project-introduction)
- [Quick Start](#quick-start)
  * [Step 1: Prepare your analysis target](#step-1-prepare-your-analysis-target)
  * [Step 2: Analyze Quickly](#step-2-analyze-quickly)
  * [Step 3: View Report](#step-3-view-report)
- [Set up a development environment](#set-up-a-development-environment)
  * [Environment Requirements](#environment-requirements)
  * [Compilation and Build](#compilation-and-build)
  * [Configure parameters](#configure-parameters)
  * [CoraxJava+Docker](#coraxjavadocker)
- [Test Suite Performance](#test-suite-performance)
- [Custom Rule Checker](#custom-rule-checker)
- [License](#license)
- [Communication and Feedback](#communication-and-feedback)
- [Recruitment](#recruitment)

<!-- tocstop -->

## Project Introduction

CoraxJava (Corax Community Edition) is a static code security analysis tool for Java projects. Its core analysis engine is derived from the Corax commercial version, possessing consistent underlying code analysis capabilities with the commercial version. Additionally, it is equipped with dedicated open-source rule checkers and rules.

CoraxJava consists of two parts: `CoraxJava Core Analysis Engine` and `CoraxJava Rule Checker`. The rule checker module supports the implementation of various rule checks. Currently, CoraxJava includes program analysis technologies such as Abstract Interpretation and [IFDS (Sparse analysis implemented by Feysh)](https://github.com/Feysh-Group/FlowDroid). In the future, we will continue to optimize engine analysis performance and improve analytical precision, introducing more powerful program analysis algorithms to continually enhance the core analysis capabilities, pushing for the constant improvement of code safety, quality, and performance.

CoraxJava has the following features:

1. A fully open rule checker module, with several rule checker code examples open-sourced.
2. Support for developing custom rule checkers using Kotlin/Java languages.
3. Support for modifying and generating rule checkers through either **configuration files** or **writing code**.
4. Analysis targets Java bytecode, but requires source code as a reference for results display.
5. Analysis results are outputted in SARIF format.

> Note: Currently, the CoraxJava core analysis engine is not open-sourced. You need to [download the engine's jar file](https://github.com/Feysh-Group/corax-community/releases) (corax-cli-x.x.x.jar) to use it in conjunction with the rule checker. This code repository is for the CoraxJava custom rule checker module, which contains multiple open-source implementations of rule checkers.

**Read the [Corax Community Edition Feature Comparison](docs/en/feature_diff.md) to understand the differences between the Corax Community and Commercial editions.**

## Quick Start

This repository is for the `CoraxJava Rule Checker` module, which also includes test cases [corax-config-tests](corax-config-tests). After the project is built, the `CoraxJava Rule Checker` module will be in the form of a plugin (a separate zip package). It is to be used in combination with the `CoraxJava Core Engine` module to perform Java static code analysis. The test cases can be used for rapid testing and validation of CoraxJava's detection results.



### Step 1: Prepare your analysis target

- [x] It should include a project with Java source code, preferably complete and not compressed.
- [x] You need complete project build artifacts and as many third-party library JARs as possible: (If there are no pre-built artifacts, manually build using commands like `mvn package -Dmaven.test.skip.exec=true -DskipTests` or `gradle build -x test`. Avoid using `mvn compile/jar` or `gradle compile/jar` as these commands often do not pull the project's dependent third-party library JARs and the build artifacts are incomplete.)
  - [x] For example, the project source code corresponds to a folder containing a large number of `.class` files (`target/classes`, `build/classes`).
  - [x] Project build artifacts corresponding to `.jar`/`.war`/`.zip` files, or any folder path containing them.
  - [x] The folder containing the third-party library JARs (provide as many as possible, if not available, use the `mvn dependency:copy-dependencies -DoutputDirectory=target\libs` command to manually pull them).



### Step 2: Analyze Quickly

**One-line command for analysis**

When running for the first time, the script will download JDK and Corax release as needed, unpack them to the corresponding locations, and will not disrupt the original environment~

- For Linux and macOS:
  - Download [coraxjw.sh](coraxjw.sh) (To avoid potential encoding errors, please click the download button instead of copying and pasting.)
  - Copy and run the following command:
    ```bash
    chmod +x ./coraxjw.sh
    ./coraxjw.sh --target java --auto-app-classes "{project root directory (including source code and compiled artifacts)}" --output corax_reports
    ```

- For Windows:
  - Download [coraxjw.ps1](coraxjw.ps1)
  
  - Command Prompt (cmd):
    ```shell
    @"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -InputFormat None -ExecutionPolicy Bypass -File coraxjw.ps1 --target java --auto-app-classes "{project root directory (including source code and compiled artifacts)}" --output corax_reports
    ```
  
  - PowerShell:
    ```PowerShell
    Set-ExecutionPolicy Bypass -Scope Process -Force; ./coraxjw.ps1 --target java --auto-app-classes "{project root directory (including source code and compiled artifacts)}" --output corax_reports
    ```
Note: The output directory specified by the `--output` parameter should not be set within the folder being analyzed; otherwise, the engine will refuse to perform the analysis.

Note: The downloaded script contains a fixed version of the CoraxJava analyzer and rule package. If you need to update the analysis tool later, please find the script you downloaded before, then execute "uninstall" and delete the script. Then, follow the steps above to download the latest version (Master stable branch) of the CoraxJava analyzer.

General parameter explanation: [General Configuration Parameters](#configure-parameters)

Detailed parameter explanation: [Command Line Parameter Details](docs/en/usage.md#command-line-parameters)

To uninstall, simply run `./coraxjw.sh uninstall`

Recommendation: It is advisable to have more than 12GB of remaining memory. Otherwise, when analyzing large projects, there is a higher risk of encountering Out Of Memory (OOM) errors.

### Step 3: View Report

Currently, CoraxJava supports generating results reports in [SARIF](https://sarifweb.azurewebsites.net/) format. The report generation path is specified by the `--output` parameter. Under this path, a `sarif/` directory will be generated, with each `java` source code file having a separate `sarif` format result file.

It is recommended to use `VSCode` to view the `SARIF` format report. You can install the `Sarif Viewer` plugin for convenient visual inspection and navigation.

**Sarif Viewer** Usage:

1. Install [Visual Studio Code](https://code.visualstudio.com/)
2. Install the VSCode: Sarif Viewer extension
   - Open the webpage [sarif-viewer](https://marketplace.visualstudio.com/items?itemName=MS-SarifVSCode.sarif-viewer) and click the install button
   - Alternatively, install via command line `code --install-extension MS-SarifVSCode.sarif-viewer`
3. Viewing SARIF format reports
   1. In VSCode, open any .sarif file in the sarif folder within the corax analysis output directory, and the report preview window should automatically pop up. If the window doesn't appear, you can press `Ctrl+Shift+P`, search for "sarif," and click "Sarif: Show Panel."
   2. In the SARIF Results window of Sarif Viewer, click the folder icon in the top right corner to select and open multiple SARIF files for viewing.

## Set up a development environment

### Environment Requirements

You need to install the Java runtime environment and require JDK version 17 (compiling and analyzing with other versions will likely lead to errors). On Debian, Ubuntu, and similar systems, you can use the following command to install JDK 17:
```bash
$ sudo apt-get install openjdk-17-jdk
```

On Fedora, Centos, and similar systems, you can use the following command to install:
```bash
$ sudo yum -y install java-17-openjdk
```
On MacOS, use the following command to install:
```bash
$ brew install openjdk@17
```
Windows users can download [openjdk-17+35_windows-x64_bin.zip](https://download.java.net/openjdk/jdk17/ri/openjdk-17+35_windows-x64_bin.zip) and unzip it. Set the JDK environment variable in "Advanced system settings".

You can use the following command to check the JDK version and confirm it is 17.
```bash
$ java -version
```

### Compilation and Build

Note: This step is only for developers. If you just want to quickly experience the features, you can directly download the latest pre-compiled artifacts from the [release](https://github.com/Feysh-Group/corax-community/releases) which includes `CoraxJava core engine corax-cli-x.x.jar` and rule configuration, then proceed to the next step to start the analysis!

Execute gradle build in the project root directory `corax-community` (it is recommended to enter the root directory and execute ./gradlew build to avoid version issues):
```bash
$ cd corax-community
$ ./gradlew build
```
As prompted, you need to specify the file path of the `CoraxJava core engine` in the [gradle-local.properties](gradle-local.properties) file:
```text
coraxEnginePath=<PATH TO corax-cli-x.x.jar> // Use absolute path, do not add quotes
```
Execute build again:
```bash
$ gradlew build
```
After a successful build, multiple zip files of plugins and configuration files will be generated and stored in the [build/analysis-config](build%2Fanalysis-config) folder, collectively referred to as the analysis configuration directory. The structure is as follows:
```
├── analysis-config                                     // Location of CoraxJava rule checker
│   ├── default-config.yml                                 // Generated after the first analysis, the analysis tool automatically generates yaml format main configuration based on default parameters in the plugin
│   │                                                      // Only when the modified main configuration file has some missing configurations or some configurations cannot be mapped to existing plugins,
│   ├── default-config.normalize.yml                       // and when there are style issues, the engine will automatically repair and normalize the main configuration and output it to this file
│   ├── plugins                                            // Plugin storage directory
│   │   ├── feysh-config-community-plugin-2.20               // Automatically unzipped after the first analysis run, delete as needed
│   │   ├── feysh-config-community-plugin-2.20.zip           // Compilation artifact, rule checker plugin generated after compiling corax-config-community module
│   │   ├── feysh-config-general-plugin-2.20                 // Automatically unzipped after the first analysis run, delete as needed
│   │   └── feysh-config-general-plugin-2.20.zip             // Compilation artifact, rule checker plugin generated after compiling corax-config-general module
│   └── rules                                              // Some static data of rule checker, users can customize configuration
│       ├── **.sources.json                                   // taint sources
│       ├── **.summaries.json                                 // taint summaries, sanitizers
│       ├── **.sinks.json                                     // taint sinks
│       ├── **.access-path.json                               // method signature and sink data
```
> Note: `feysh-config-community-plugin-2.20.zip` is the rule checker plugin generated after compiling the `corax-config-community` module, mainly containing the implementation of custom rule checkers. `feysh-config-general-plugin-2.20.zip` is the rule checker plugin generated after compiling the `corax-config-general` module, mainly containing some common built-in checker models that generally do not need to be modified.



### Configure parameters

**Step 1: Loading the Analysis Engine**

The analysis engine needs to load the `CoraxJava rule checker plugin` (e.g., `analysis-config/plugins/feysh-config-*-plugin-*.*.*.zip`) and some dependent configuration files (e.g., `analysis-config/rules`). Therefore, you need to prepare the `analysis-config` (rule configuration folder):

- You can download and unzip the pre-generated `analysis-config` directory from the [release](https://github.com/Feysh-Group/corax-community/releases): `{corax-java-cli-community-2.20.zip extraction location}/analysis-config/`
- Or use the [build/analysis-config](build%2Fanalysis-config) directory generated in the [Compilation and Build](#compilation-and-build) step: `./build/analysis-config/`

**Step 2: Start Analysis! Manually configure `CoraxJava` with the following essential parameters:**

- Analyzer startup command `java -jar corax-cli-x.x.x.jar` (Download and unzip `CoraxJava core engine` (`corax-cli-x.x.x.jar`) from the [release](https://github.com/Feysh-Group/corax-community/releases))
- Set output directory `--output build/output`
- Enable data flow engine `--enable-data-flow true`
- Set the type of analysis target `--target java`
- Set the report output format `--result-type sarif`, it can be omitted as it defaults to sarif format
- Set the path of the analysis target. For example, for this project's test cases: `--auto-app-classes ./corax-config-tests`, this parameter requires that the path or subdirectory must contain project source code and compiled bytecode artifacts (class files or jar files).

- Specify the configuration parameters in the format `--config (yaml file name.yml)@(rule configuration folder)`, where the yaml file name can be arbitrary. The `(rule configuration folder)` is the path to the previously prepared `analysis-config`. For example:
- `--config default-config.yml@{corax-java-cli-community-2.20.zip extraction location}/analysis-config/`
- `--config default-config.yml@./build/analysis-config/`

Analysis command template:

```bash
$ java -jar corax-cli-x.x.x.jar --verbosity info --output build/output --enable-data-flow true --target java --result-type sarif --auto-app-classes {project root directory (containing source code and build artifacts)} --config default-config.yml@{corax-java-cli-community-2.20.zip extraction location}/analysis-config/
```

**Tips**: If there are no build artifacts in the project root directory, you can add any number of `--auto-app-classes` parameters pointing to the location or folder of the build artifacts.

When executing this command, if the analysis engine cannot find a yaml file named `default-config.yml` in the specified `{corax-java-cli-community-2.20.zip extraction location}/analysis-config/` directory, it will automatically generate a default yaml main configuration file with the same name based on default parameters in the plugin in the rules configuration folder: `{corax-java-cli-community-2.20.zip extraction location}/analysis-config/default-config.yml`. If you need to change the configuration, copy the entire `analysis-config` folder to your working directory and customize the configuration according to your requirements. Specify the parameters `--config configuration file name.yml@new rule configuration folder` before the next analysis to make it effective.

The final report will be generated in the folder path specified by `--output`.

**Read [CoraxJava Usage](docs/en/usage.md) for complete usage details.**

### CoraxJava+Docker

**If you want to experience scanning with Docker, read [CoraxJava+Docker Scanning Tutorial](docs/en/coraxdocker-usage.md) for complete usage details.**

## Test Suite Performance

**Read [Corax Community Edition Feature Comparison](docs/en/feature_diff.md#sast-test-set-performance) to understand the performance of this Java community edition tool on SAST test suites.**

## Custom Rule Checker

**Read [Corax Community Edition Feature Comparison](docs/en/feature_diff.md#open-rules) to understand the open rules of this Java community edition tool.**

**If you need to implement a custom `CoraxJava rule checker`, refer to [CoraxJava Rule Checker](docs/en/checker.md) for more details.**

## License

corax-community is licensed under the LGPL-2.1 license, see LICENSE file. This basically means that you are free to use the tool (even in commercial, closed-source projects). However, if you extend or modify the tool, you must make your changes available under the LGPL as well. This ensures that we can continue to improve the tool as a community effort.

## Communication and Feedback

To report false positives or false negatives:
- GitHub issue
- Send an email to bug@feysh.com

Community group:

As the community group has a large number of members and cannot be joined via QR code, please join using the following method:
- Add the WeChat ID "Corax社区助手" and specify "Corax Community Edition Community Group" to be added to the group
  <img src="https://github.com/Feysh-Group/corax-community/assets/6300471/4e422679-4889-4209-8cd5-173654007c9f" height="320px">

## Recruitment

If you are interested in this project or Java static code analysis and want to work or research in this area (full-time/part-time/internship), please send an email to job@feysh.com, specifying "CoraxJava Position", and we will contact you as soon as possible.
