# CoraxJava + Docker

docker 方式的扫描**目前**仅适用于尝新，不建议用于快速体验和开发使用

请先确保已安装 `Docker`

```bash
$ cd corax-community // 必须 在本项目根目录 corax-community 下构建 Docker 镜像
$ docker build --network=host -t corax-community .
```
构建期间会自动下载 github repo release 中的 `corax-java-cli-community-x.x.zip` 然后解压到 `/corax-community` 目录下，构建完成后生成快捷方式为 `/corax-community/corax-cli.jar`

>考虑到可能会遇到网络环境受限，也可以手动下载 `CoraxJava核心引擎`，放在 `/corax-community` 即可，注意版本号与 `Dockerfile CORAX_VERSION` 保持一致。

使用方式:
```bash
$ corax_cmd='java -jar corax-cli.jar --verbosity info --enable-data-flow true --target java --config default-config.yml@./build/analysis-config --result-type sarif --auto-app-classes {映射到容器内的项目路径} --output /reports/{项目名}'
$ docker run -it --rm -v {指定宿主机上扫描结果的输出路径}:/reports/ -v {指定在宿主机上需要扫描的项目根路径}:{映射到容器内的项目路径} corax-community ${corax_cmd}
```
注意:

由于扫描行为发生在容器内，所以需要将代码仓库映射到容器内，否则无法扫描。

由于sarif格式中的位置描述需要使用绝对路径，体验 `sarif` 相关插件的跳转功能需要采用如下方式：

- 方式1： Linux宿主机上：可以将映射到容器内的路径和宿主机真实路径保持一致。

- 方式2： 报告路径映射：（比如宿主机为windows或者方式1无法映射某些路径时）

  需要在分析命令上增加参数`--result-type SarifCopySrc`

  生成的报告还需要使用下面命令批量替换掉源码路径

    - 如果宿主机有shell env （linux or win+wsl）

  ```bash
  >> sudo apt-get install jq
  >> srcroot_uri="file:///C:/Users/xxx/reports/corax-config-tests/sarif-copy/src_root" // 宿主机上的src_root路径 注意是绝对路径且 file:/// 有三个正斜杠 
  >> cd reports/corax-config-tests/sarif-copy // 宿主机上报告输出的位置
  >> (IFS=$'\n'; for sarif in $(find . -maxdepth 1 -name "*.sarif"); do jq ".runs[].originalUriBaseIds.SRCROOT.uri = \"${srcroot_uri}\"" $sarif > $sarif.tmp ; mv $sarif.tmp $sarif ; done)
  ```

    - 如果宿主机有python:

  ```bash
  >> python3 -c "import base64;exec(base64.b64decode('aW1wb3J0IG9zLGpzb24sc3lzCgpkaXJlY3RvcnkgPSBzeXMuYXJndlsxXSAgIyBTcGVjaWZ5IHRoZSBkaXJlY3RvcnkKdXJpID0gc3lzLmFyZ3ZbMl0gICMgdXJpIG9mIHNyYyByb290IAoKIyBGaW5kIGZpbGVzIHdpdGggIi5zYXJpZiIgZXh0ZW5zaW9uCnNhcmlmX2ZpbGVzID0gW2ZpbGUgZm9yIGZpbGUgaW4gb3MubGlzdGRpcihkaXJlY3RvcnkpIGlmIGZpbGUuZW5kc3dpdGgoIi5zYXJpZiIpXQoKZm9yIHNhcmlmX2ZpbGUgaW4gc2FyaWZfZmlsZXM6CiAgICBzYXJpZl9wYXRoID0gb3MucGF0aC5qb2luKGRpcmVjdG9yeSwgc2FyaWZfZmlsZSkKCiAgICB3aXRoIG9wZW4oc2FyaWZfcGF0aCwgInIiLCBlbmNvZGluZz0idXRmLTgiKSBhcyBmaWxlOgogICAgICAgIGRhdGEgPSBqc29uLmxvYWQoZmlsZSkKCiAgICAjIE1vZGlmeSB0aGUgdmFsdWVzIG9mIHVyaSBmaWVsZCBpbiB0aGUgSlNPTiBkYXRhCiAgICBmb3IgcnVuIGluIGRhdGEuZ2V0KCJydW5zIiwgW10pOgogICAgICAgIGlmICJvcmlnaW5hbFVyaUJhc2VJZHMiIGluIHJ1bjoKICAgICAgICAgICAgcnVuWyJvcmlnaW5hbFVyaUJhc2VJZHMiXVsiU1JDUk9PVCJdWyJ1cmkiXSA9IHVyaQoKICAgIHdpdGggb3BlbihzYXJpZl9wYXRoLCAidyIsIGVuY29kaW5nPSJ1dGYtOCIpIGFzIGZpbGU6CiAgICAgICAganNvbi5kdW1wKGRhdGEsIGZpbGUsIGluZGVudD00KQ=='))" reports/corax-config-tests/sarif-copy "file:///C:/Users/xxx/reports/corax-config-tests/sarif-copy/src_root"
  ```

  然后就可以在宿主机上正常浏览sarif报告了