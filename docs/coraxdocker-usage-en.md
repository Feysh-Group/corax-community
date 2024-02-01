# CoraxJava + Docker

The scanning in Docker mode is **currently** only suitable for exploration and is not recommended for quick experience and development use.

Make sure you have installed `Docker` before proceeding.

```bash
$ cd corax-community // Must be in the root directory corax-community of this project to build the Docker image
$ docker build --network=host -t corax-community .
```
During the build, it will automatically download the `corax-java-cli-community-x.x.zip` from the GitHub repo release and unzip it to the `/corax-community` directory. After the build is complete, a shortcut will be generated as `/corax-community/corax-cli.jar`.

> Considering that network limitations may be encountered, you can also manually download the `CoraxJava Core Engine` and place it in `/corax-community`, ensuring that the version number matches `Dockerfile CORAX_VERSION`.

Usage:
```bash
$ corax_cmd='java -jar corax-cli.jar --verbosity info --enable-data-flow true --target java --config default-config.yml@./build/analysis-config --result-type sarif --auto-app-classes {Mapped project path inside the container} --output /reports/{Project name}'
$ docker run -it --rm -v {Specify the output path of the scan result on the host machine}:/reports/ -v {Specify the root path of the project to be scanned on the host machine}:{Mapped project path inside the container} corax-community ${corax_cmd}
```
Note:

Since the scanning takes place inside the container, you need to map the code repository into the container; otherwise, the scan will not work.

Because the sarif format requires absolute paths in the description, to experience the jumping functionality of `sarif` related plugins, you need to follow one of the following methods:

- Method 1: On a Linux host machine, you can keep the paths mapped to the container consistent with the real paths on the host machine.

- Method 2: Report path mapping (e.g., for a Windows host or when Method 1 cannot map certain paths)

  You need to add the `--result-type SarifCopySrc` parameter to the analysis command.

  The generated report also needs to use the following command to replace the source code paths in batches:

    - If the host machine has a shell environment (Linux or WSL on Windows)

  ```bash
  >> sudo apt-get install jq
  >> srcroot_uri="file:///C:/Users/xxx/reports/corax-config-tests/sarif-copy/src_root" // Absolute path to src_root on the host machine. Note: it must be an absolute path, and file:/// has three forward slashes 
  >> cd reports/corax-config-tests/sarif-copy // Location of report output on the host machine
  >> (IFS=$'\n'; for sarif in $(find . -maxdepth 1 -name "*.sarif"); do jq ".runs[].originalUriBaseIds.SRCROOT.uri = \"${srcroot_uri}\"" $sarif > $sarif.tmp ; mv $sarif.tmp $sarif ; done)
  ```

    - If the host machine has Python:

  ```bash
  >> python3 -c "import base64;exec(base64.b64decode('aW1wb3J0IG9zLGpzb24sc3lzCgpkaXJlY3RvcnkgPSBzeXMuYXJndlsxXSAgIyBTcGVjaWZ5IHRoZSBkaXJlY3RvcnkKdXJpID0gc3lzLmFyZ3ZbMl0gICMgdXJpIG9mIHNyYyByb290IAoKIyBGaW5kIGZpbGVzIHdpdGggIi5zYXJpZiIgZXh0ZW5zaW9uCnNhcmlmX2ZpbGVzID0gW2ZpbGUgZm9yIGZpbGUgaW4gb3MubGlzdGRpcihkaXJlY3RvcnkpIGlmIGZpbGUuZW5kc3dpdGgoIi5zYXJpZiIpXQoKZm9yIHNhcmlmX2ZpbGUgaW4gc2FyaWZfZmlsZXM6CiAgICBzYXJpZl9wYXRoID0gb3MucGF0aC5qb2luKGRpcmVjdG9yeSwgc2FyaWZfZmlsZSkKCiAgICB3aXRoIG9wZW4oc2FyaWZfcGF0aCwgInIiLCBlbmNvZGluZz0idXRmLTgiKSBhcyBmaWxlOgogICAgICAgIGRhdGEgPSBqc29uLmxvYWQoZmlsZSkKCiAgICAjIE1vZGlmeSB0aGUgdmFsdWVzIG9mIHVyaSBmaWVsZCBpbiB0aGUgSlNPTiBkYXRhCiAgICBmb3IgcnVuIGluIGRhdGEuZ2V0KCJydW5zIiwgW10pOgogICAgICAgIGlmICJvcmlnaW5hbFVyaUJhc2VJZHMiIGluIHJ1bjoKICAgICAgICAgICAgcnVuWyJvcmlnaW5hbFVyaUJhc2VJZHMiXVsiU1JDUk9PVCJdWyJ1cmkiXSA9IHVyaQoKICAgIHdpdGggb3BlbihzYXJpZl9wYXRoLCAidyIsIGVuY29kaW5nPSJ1dGYtOCIpIGFzIGZpbGU6CiAgICAgICAganNvbi5kdW1wKGRhdGEsIGZpbGUsIGluZGVudD00KQ=='))" reports/corax-config-tests/sarif-copy "file:///C:/Users/xxx/reports/corax-config-tests/sarif-copy/src_root"
  ```

  Now you can browse the sarif report normally on the host machine.