## 潜在的路径遍历（任意文件访问（读写删））

访问(读写删)一个可被攻击者控制的不可信路径, 可能存在路径穿越漏洞.

**缺陷代码:**

```java
@GET
@Path("/images/{image}")
@Produces("images/*")
public Response getImage(@javax.ws.rs.PathParam("image") String image) {
    File file = new File("resources/images/", image); // 此处会报告 $PathTraversal， 即使后续未对File做任何操作也会报告

    if (!file.exists()) {
        return Response.status(Status.NOT_FOUND).build();
    }

    return Response.ok().entity(new FileInputStream(file)).build();
}
```


**缺陷代码:**

```java
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;

@RestController
public class InsecureFileDownloadController {

  @GetMapping("/download")
  public ResponseEntity<FileSystemResource> downloadFile(@RequestParam String fileName) throws IOException {
    File targetDir = new File("/var/image");

    // 不安全1：直接使用用户输入的文件名，没有进行任何验证
    File targetFile = new File(targetDir, fileName);
    String canonicalPath = targetFile.getCanonicalPath();

    // 不安全2：使用字符串匹配，会不兼容 windows 的存在反斜杠\的路径，导致绕过
    // 不安全3：能够任意访问子级目录文件，如 /var/image/subfolder/subfolder/sensitive.png
    // 不安全4：能够任意访问/var/image前缀的路径，如 /var/image-user/subfolder/sensitive.png
    if (!canonicalPath.startsWith("/var/image")) {
      throw new IllegalArgumentException("Invalid filename");
    }

    FileSystemResource resource = new FileSystemResource(targetFile);
    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + targetFile.getName() + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
  }
}
```



## 防止路径遍历的缓解措施

正如前面例子所见，保护文件上传可能是一项艰巨的任务。问题归结为不验证用户输入。

1. 一个解决方案可能是不信任用户输入，并在服务器端创建一个随机的文件名。

2. 不要自定义脆弱的路径操作和检查方法，容易被绕过。

3. 如果需要根据用户输入文件名来保存，保持安全的最好方法是检查规范路径。例如，在Java中：


```java
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@RestController
public class DownloadService {

  private static final File targetDir = new File("/var/image");
  private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");

  @GetMapping("/download/{filename}")
  public ResponseEntity<FileSystemResource> downloadFile(@PathVariable String filename, HttpServletResponse response) throws IOException {
    // Sanitize the filename to prevent path traversal
    filename = sanitizeFileName(filename);

    // Construct the full path to the file
    Path filePath = Paths.get(targetDir.getPath(), filename);
    File file = filePath.toFile();

    // Check if the file exists
    if (!file.exists()) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
      return null;
    }

    File canonicalParentFile = file.getCanonicalFile().getParentFile();
    // Check whether the parent directory of the file is the target directory: /var/image.
    if (canonicalParentFile == null || !canonicalParentFile.equals(targetDir.getCanonicalFile())) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
      return null;
    }

    // Validate the file extension
    String extension = FilenameUtils.getExtension(filename);
    if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid file extension");
      return null;
    }

    // Set headers for the response
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());

    // Return the file as a resource
    return ResponseEntity.ok()
            .headers(headers)
            .contentLength(file.length())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(new FileSystemResource(file));
  }

  private String sanitizeFileName(String filename) {
    // Get the file name without the extension
    String fileNameWithoutExt = FilenameUtils.getBaseName(filename);
    // Get the file extension
    String fileExt = FilenameUtils.getExtension(filename);

    // Replace any non-alphanumeric characters in the file name and extension with an underscore
    fileNameWithoutExt = fileNameWithoutExt.replaceAll("[^A-Za-z0-9]", "_");
    fileExt = fileExt.replaceAll("[^A-Za-z0-9]", "_");

    // Rebuild the file name
    return fileNameWithoutExt + "." + fileExt;
  }
}
```

规范路径函数将解析为绝对路径，去除`.`和`..`等。通过检查规范路径是否在预期的目录内。

对于路径遍历，在检索时，可以应用上述相同的技术，但作为一种深度防御，你也可以通过在不允许读取和写入任何其他目录的特定非特权用户下运行应用程序来实施缓解措施。

确保在任何情况下都完善检测以捕捉这些路径穿越情况，但要小心不要向用户返回显式信息（比如返回文件不存在，会导致目录爆破），每一个小细节都可能给攻击者提供关于你的系统的信息。



**全面的缓解办法**

- 安全编程：
    - 获取文件名或者后缀的方法请不要自行实现，很可能被绕过。
- 文件类型验证：
    - 通过检查文件扩展名和/或其Magic字节，验证上传的文件是否属于预期类型，如果没有白名单过滤器，切勿直接接受文件名及其扩展名，可以使用固定后缀和随机文件名。
- 文件名消毒：
    - 删除文件名中的任何特殊字符或无效字符，防止目录遍历攻击，如果不需要 Unicode 字符，强烈建议仅接受字母数字字符和一个点作为文件名和扩展名的输入。
- 魔字节防伪：
    - 验证文件的魔法字节，确保文件属于预期类型，未被篡改。
- 防止双重扩展名攻击：
    - 从文件名中删除任何额外的文件扩展名，或验证文件名是否与预期的文件类型相匹配。
- 服务器端验证：
    - 不要仅依赖客户端的文件类型验证，在服务器端执行验证检查，确保上传的文件是安全的，并符合应用程序设定的要求。
- 文件大小验证：
    - 将文件大小限制为最大值以防止拒绝服务攻击。
- 文件存储：
    - 上传的目录不应具有任何“可执行”权限，将上传的文件存储在网络服务器无法访问的独立位置。
- 文件扫描：
    - 使用病毒扫描仪或其他安全软件扫描上传的文件，查找恶意软件或其他恶意内容。
- 日志记录：
    - 跟踪所有文件上传，包括文件名、大小和上传用户，以帮助事件响应和审计。
- 输入验证：
    - 正确验证所有用户输入并对其进行消毒，以防止任何类型的注入攻击。



## 特别注意

特别需要注意的是 `MultipartFile.html#getOriginalFilename()` 这个方法，`filename` 一般会被开发者理解为是返回文件名，是不包含路径的，然而根据[Spring官方文档](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/multipart/MultipartFile.html#getOriginalFilename())和spring代码实现来看，**较多版本spring上的`getOriginalFilename`返回值是可以导致路径穿越或者绕过路径穿越检查的**。

Spring <= 4.1.8 中提供的 `MultipartFile` 对象潜在的威胁，如果不注意就会造成 目录穿越漏洞

**Spring-web** 默认提供了两个 `MultipartFile` 对象，分别是 `StandardMultipartFile`、`CommonsMultipartFile` 。**Spring-test** 中提供了一个 `MockMultipartFile` 对象用于处理文件上传请求。

`MultipartFile` 接口中声明的 `getOriginalFilename` 方法用于获取文件名

```java
String getOriginalFilename();
```

其中 `StandardMultipartFile` 和 `MockMultipartFile` 是没有对文件名进行处理的

应该使用 `FilenameUtils` 安全地获取文件名：`org.apache.commons.io.FilenameUtils.getName(multiPartFile.getOriginalFilename())`

### StandardMultipartFile

*org.springframework.web.multipart.support.StandardMultipartHttpServletRequest.StandardMultipartFile#getOriginalFilename*

```java
public String getOriginalFilename() {
    return this.filename;
}
```

### MockMultipartFile

*org.springframework.mock.web.MockMultipartFile#getOriginalFilename*

```java
public String getOriginalFilename() {
    return this.originalFilename;
}
```

### SpringBoot 威胁

在使用 **SpringBoot** 中当没有自己手动配置的情况下默认使用的是 **`StandardMultipartFile`**. 在这种情况下直接通过 `getOriginalFilename` 方法获取文件名后，不进行处理就使用会造成目录穿越漏洞



### CommonsMultipartFile

而需要手动配置设置的 `CommonsMultipartFile` 中， `getOriginalFilename` 方法的实现对文件名进行了处理，如下代码的版本是 **`4.1.8.RELEASE`**, 但这里的过滤还是存在**绕过**，在 Windows 下可以使用 `../..\\..\\` 绕过造成目录穿越漏洞

```java
public String getOriginalFilename() {
    String filename = this.fileItem.getName();
    if (filename == null) {
        return "";
    } else {
        int pos = filename.lastIndexOf("/");
        if (pos == -1) {
            pos = filename.lastIndexOf("\\");
        }

        return pos != -1 ? filename.substring(pos + 1) : filename;
    }
}
在通过配置且使用CommonsMultipartFile时，对应的修复方法：升级 Spring >= 4.1.9.RELEASE 可以修复该问题。
```



### 注意...

正如之前的例子所示，使用哪种方法来检索参数时要小心，特别是query参数。Spring Boot在拒绝无效路径变量方面做得很好。回顾：

```java
@GetMapping("/f")
public void f(@RequestParam("name") String name) {
  // name会自动解码，所以%2E%2E%2F%2E%2E%2Ftest将变成../../test
}

@GetMapping("/g")
public void g(HttpServletRequest request) {
  var queryString = request.getQueryString(); // 将返回 %2E%2E%2F%2E%2E%2Ftest
}

@GetMapping("/h")
public void h(HttpServletRequest request) {
  var name = request.getParameter("name"); // 将返回 ../../test
```

如果你用`/f?name=%2E%2E%2F%2E%2E%2Ftest`调用`/f`，它将变成`../../test`。如果你用`/g?name=%2E%2E%2F%2E%2E%2Ftest`调用`g`，它将返回`%2E%2E%2F%2E%2E%2Ftest` **没有** 应用解码。使用相同参数的`/h`的行为将与`/f`相同。

如你所见，要小心并熟悉正确的调用方法。在每种情况下，编写单元测试以覆盖编码字符。

### Spring Boot保护

默认情况下，Spring Boot对使用例如路径中的`../`提供了保护。这种保护位于`StrictHttpFirewall`类中。这将保护用户输入是`path`的一部分的端点，比如`/test/1.jpg`，如果你将`1.jpg`替换为`../../secret.txt`，它将阻止请求。对于查询参数，这种保护将不存在。

