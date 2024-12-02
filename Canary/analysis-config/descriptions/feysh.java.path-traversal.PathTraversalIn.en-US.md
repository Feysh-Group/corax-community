## Potential Path Traversal (arbitrary file read) 

Reading from an untrusted path that can be controlled by an attacker, potentially introduce a path traversal vulnerability, could allow access to files from arbitrary file system locations.

**Vulnerable Code:**

```java
@GET
@Path("/images/{image}")
@Produces("images/*")
public Response getImage(@javax.ws.rs.PathParam("image") String image) {
    File file = new File("resources/images/", image); // Weak point

    if (!file.exists()) {
        return Response.status(Status.NOT_FOUND).build();
    }

    return Response.ok().entity(new FileInputStream(file)).build();
}
```


**Vulnerable Code:**


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


## Path traversal mitigation

As seen in the previous example, securing file uploads can be a daunting task. The issue boils down to a lack of validation of user input.

1. One solution might be to not trust user input and create a random filename on the server side.

2. Avoid customizing fragile path manipulation and checking methods that can easily be bypassed.

3. If saving according to user-inputted filenames is necessary, the best way to stay secure is by validating sanitized paths. For instance, in Java:

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

The canonical path function will resolve to an absolute path, removing `.` and `..` etc. By checking whether the canonical the path is inside the expected directory.

For path traversals, while retrieving, one can apply the same technique described above, but as a defense in depth you can also implement mitigation by running the application under a specific not privileged user who is not allowed to read and write in any other directory.

Make sure that you build detection for catching these cases in any case, but be careful with returning explicit information to the user. Every tiny detail might give the attacker knowledge about your system. (For instance, returning a response indicating that a file does not exist can lead to directory brute-forcing.)

**Solution**

- Safe Programming:
    - Do not implement your own methods for obtaining file names or extensions, as they are likely to be bypassed.
- File Type Validation:
    - Validate the type of uploaded files by checking their file extension and/or Magic bytes. Never directly accept file names and their extensions without a whitelist filter; consider using fixed extensions and random file names.
- Filename Sanitization:
    - Remove any special characters or invalid characters from file names to prevent directory traversal attacks. If Unicode characters are not required, it is strongly recommended to accept only alphanumeric characters and one dot as input for filenames and extensions.
- Magic Byte Authentication:
    - Verify the magic bytes of the file to ensure it is of the expected type and has not been tampered with.
- Prevention of Double Extension Attacks:
    - Remove any additional file extensions from the filename or verify that the filename matches the expected file type.
- Server-side Validation:
    - Do not rely solely on client-side file type validation; perform validation checks on the server-side to ensure uploaded files are safe and meet the requirements set by the application.
- File Size Validation:
    - Limit file size to a maximum value to prevent denial of service attacks.
- File Storage:
    - The directory where files are uploaded should not have any "executable" permissions. Store uploaded files in a separate location that is inaccessible to the web server.
- File Scanning:
    - Scan uploaded files using an antivirus scanner or other security software to detect malware or other malicious content.
- Logging:
    - Track all file uploads, including filenames, sizes, and uploading users, to aid in incident response and auditing.
- Input Validation:
    - Properly validate all user inputs and sanitize them to prevent any type of injection attacks.



## Special Attention

It is particularly important to note the `MultipartFile.html#getOriginalFilename()` method. The `filename` is generally understood by developers to return the file name, which does not contain a path. However, according to the [Spring official documentation](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/multipart/MultipartFile.html#getOriginalFilename()) and the Spring code implementation, the return value of `getOriginalFilename` in many versions of Spring can lead to path traversal or bypass path traversal checks.

The potential threat provided by the `MultipartFile` object in Spring <= 4.1.8, if not paid attention to, can lead to a directory traversal vulnerability.

**Spring-web** provides two default `MultipartFile` objects, namely `StandardMultipartFile` and `CommonsMultipartFile`. **Spring-test** provides a `MockMultipartFile` object for handling file upload requests.

The `getOriginalFilename` method declared in the `MultipartFile` interface is used to obtain the file name.

```java
String getOriginalFilename();
```

Both `StandardMultipartFile` and `MockMultipartFile` do not process the file name.

One should use `FilenameUtils` to safely obtain the filename: `org.apache.commons.io.FilenameUtils.getName(multiPartFile.getOriginalFilename())`

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

### SpringBoot Threat

When using **SpringBoot**, the default `MultipartFile` used without manual configuration is **`StandardMultipartFile`**. In this case, using the `getOriginalFilename` method directly to obtain the file name without processing can lead to a directory traversal vulnerability.

### CommonsMultipartFile

In the `CommonsMultipartFile` that requires manual configuration, the implementation of the `getOriginalFilename` method processes the file name, as shown in the following code version **`4.1.8.RELEASE`**. However, the filtering here still has **bypassing** issues, and on Windows, it is possible to bypass and cause a directory traversal vulnerability using `../..\\..\\`.

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
To fix the issue when using `CommonsMultipartFile`, the corresponding solution is to upgrade to Spring >= 4.1.9.RELEASE.
```

Please note that I have translated the provided Chinese Markdown content into English, following the rules you've outlined. If there are any specific sections or additional content you would like me to translate, please let me know.


#### Be aware…

As shown in the previous examples, be careful which method you use to retrieve parameters, especially query parameters. Spring Boot does a decent job denying invalid path variables. To recap:

```java
@Getter("/f")
public void f(@RequestParam("name") String name) {
  //name is automatically decoded so %2E%2E%2F%2E%2E%2Ftest will become ../../test
}

@Getter("/g")
public void g(HttpServletRequest request) {
  var queryString = request.getQueryString(); // will return %2E%2E%2F%2E%2E%2Ftest
}

@Getter("/h")
public void h(HttpServletRequest request) {
  var name = request.getParam("name"); //will return ../../test
}
```

If you invoke `/f` with `/f?name=%2E%2E%2F%2E%2E%2Ftest` it will become `../../test`. If you invoke `g` with `/g?name=%2E%2E%2F%2E%2E%2Ftest` it will return `%2E%2E%2F%2E%2E%2Ftest` **NO** decoding will be applied. The behavior of `/h` with the same parameter will be the same as `/f`

As you can see, be careful and familiarize yourself with the correct methods to call. In every case, write a unit test in such cases, which covers encoded characters.

#### Spring Boot protection

By default, Spring Boot has protection for using, for example, `../` in a path. The projection resides in the `StrictHttpFirewall` class. This will protect endpoint where the user input is part of the `path` like `/test/1.jpg` if you replace `1.jpg` with `../../secret.txt`, it will block the request. With query parameters, that protection will not be there.
