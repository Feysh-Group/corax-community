# Unrestricted File Upload

允许用户将不受限制的文件上传到 Web 服务器。

不受限制的文件上传的后果可能会有所不同，包括系统完全接管、文件系统或数据库过载、将攻击转发到后端系统以及简单的破坏。这取决于应用程序对上传文件的处理方式，尤其是文件的存储位置。以下是攻击者可能进行的攻击列表

**影响**

- Execution of Arbitrary code 执行任意代码
- Server Compromise 服务器受损
- Client Side Attacks 客户端攻击
- Cross Site Scripting 跨站脚本

**详细**

- 通过上传并执行Web-Shell来危害Web服务器，该Web-Shell可以运行命令、浏览系统文件、浏览本地资源、攻击其他服务器、利用本地漏洞等。
- 将网络钓鱼页面放入网站中。
- 将 jsp、php、html 等文件放入网站中。
- 将永久性 XSS 放入网站中。
- 绕过跨源资源共享 (CORS) 策略并泄露潜在的敏感数据。
- 使用恶意路径或名称上传文件，这会覆盖其他用户访问的关键文件或个人数据。例如：攻击者可能会替换该文件以允许他/她执行特定的脚本。

**缓解办法**

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



## 示例代码

考虑一个示例案例，并通过易受攻击的代码和缓解的代码的上下文来理解此类型漏洞。

**易受攻击的代码**。

```java
import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet("/upload")
@MultipartConfig
public class FileUploadServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Get the file from the request
        Part filePart = request.getPart("file");
        // Get the file name
        String fileName = filePart.getSubmittedFileName();
        // Get the file input stream
        InputStream fileContent = filePart.getInputStream();
        // Write the file to the server
        String filePath = "C:\\uploads\\" + fileName;
        File file = new File(filePath);
        OutputStream out = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = fileContent.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        out.close();
    }
}
```

此代码很容易受到攻击，因为它在允许上传之前不会验证文件类型或内容。攻击者可以上传可以在服务器上执行任意代码的恶意文件，从而可能导致数据泄露、服务器泄露或其他恶意活动。并且该代码还存在路径穿越漏洞



**缓解示例**


```java
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;


public class StoreService {
    File targetDir = new File("/var/image");
    // Allowed file types
    private static final List<String> ALLOWED_TYPES = Arrays.asList("image/jpeg", "image/png");
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");
    private static final List<String> ALLOWED_FILE_MAGIC = Arrays.asList("JPEG", "PNG "); // 必须长度为4
    private static final long FILE_SIZE_LIMIT = 1000000;

    public void storeImage(MultipartFile multiPartFile, HttpServletResponse response) throws IOException, StorageException {
        // getOriginalName 可以返回 "../../etc/passwd"
        String originalFileName = multiPartFile.getOriginalFilename();
        if (originalFileName == null || StringUtils.isBlank(originalFileName)) {
            return;
        }
        if (multiPartFile.isEmpty()) {
            throw new StorageException("Failed to store empty file.");
        }
        // 千万不要自定义编写脆弱的获取文件名的方法，非常可能被恶意绕过，即使是Spring框架的标准库，历史上都存在绕过风险导致路径穿越。
        String filename = FilenameUtils.getName(originalFileName);

        // Validate the file type
        String contentType = multiPartFile.getContentType(); // contentType 可以被伪造
        // 使用白名单严格限制文件类型，以防止上传危险类型的文件到服务器
        if (!ALLOWED_TYPES.contains(contentType)) {
            // This is a security check
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid file type");
            return;
        }
        // 千万不要自实现编写脆弱的获取文件后缀或者文件类型的方法，可能被恶意绕过
        String extension = FilenameUtils.getExtension(filename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) { // 必要的检查
            // This is a security check
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid file extensions");
            return;
        }

        // limit file size
        if (multiPartFile.getSize() > FILE_SIZE_LIMIT) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "File size exceeded");
            return;
        }

        // Sanitize the file name to prevent double extension attacks
        filename = sanitizeFileName(filename);


        File destinationFile = new File(targetDir.getPath(), filename);
        File canonicalParentFile = destinationFile.getCanonicalFile().getParentFile();
        // 判断文件的父目录是否为 targetDir: /var/image 目录
        if (canonicalParentFile == null || !canonicalParentFile.equals(targetDir.getCanonicalFile())) {
            // This is a security check
            throw new StorageException("Cannot store file outside current directory.");
        }

        try (InputStream inputStream = multiPartFile.getInputStream()) {
            // Check for magic bytes forgery
            byte[] magicBytes = new byte[4];
            inputStream.read(magicBytes);
            inputStream.reset();
            String magicBytesString = new String(magicBytes);
            if (!ALLOWED_FILE_MAGIC.contains(magicBytesString)) {
              response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid file type - magic bytes forgery detected");
              return;
            }
            Files.copy(inputStream, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
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
        return fileNameWithoutExt  + "." + fileExt;
    }
}
```

**相关**

- [OWASP Unrestricted File Upload](https://owasp.org/www-community/vulnerabilities/Unrestricted_File_Upload)

- [CWE434](https://cwe.mitre.org/data/definitions/434.html)

- [IDS56-J](https://wiki.sei.cmu.edu/confluence/display/java/IDS56-J.+Prevent+arbitrary+file+upload)

