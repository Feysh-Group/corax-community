## Potential Path Traversal (file create/write)

Creating a file in an untrusted path that can be controlled by an attacker, may result in the creation of dangerous file types or lead to arbitrary file overwrite or forgery.


**Vulnerable Code:**


```java
@Path("/upload")
public class UnsafeFileUploadService {

  private static final String UPLOAD_PATH = "resources/images/"; // Upload the file storage directory

  @POST
  @Path("/image")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response uploadFile(
          @FormDataParam("file") InputStream uploadedInputStream,
          @FormDataParam("file") FormDataContentDisposition fileDetail) {

    String uploadedFileLocation = UPLOAD_PATH + fileDetail.getFileName();

    try (OutputStream out = new FileOutputStream(new File(uploadedFileLocation))) {
      int read;
      byte[] bytes = new byte[1024];

      while ((read = uploadedInputStream.read(bytes)) != -1) {
        out.write(bytes, 0, read);
      }

      String output = "File uploaded to : " + uploadedFileLocation;
      return Response.status(Response.Status.CREATED)
              .entity(output)
              .build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
              .entity("Error occurred while uploading the file.")
              .build();
    }
  }
}
```

**Vulnerable Code:**

```java
import org.apache.commons.io.FilenameUtils;

MultipartFile multiPartFile = ...;
File targetDir = new File("/var/image");

String originalFileName = multiPartFile.getOriginalFilename();
if (originalFileName == null || StringUtils.isBlank(originalFileName)) {
    return;
}
File targetFile = new File("/var/image", originalFileName);
String canonicalPath = targetFile.getCanonicalPath();

// Insecure 1: Using string matching can fail to handle backslashes (\) in Windows paths, allowing bypasses.
// Insecure 2: Allows arbitrary access to subdirectory files, such as /var/image/subfolder/subfolder/sensitive.png.
// Insecure 3: Enables access to any path prefixed with /var/image, including /var/image-user/subfolder/sensitive.png.
if (!canonicalPath.startsWith("/var/image")) {
    throw new IllegalArgumentException("Invalid filename");
}

store(multiPartFile, targetFile);
```



## Path traversal mitigation

As seen in the previous example, securing file uploads can be a daunting task. The issue boils down to a lack of validation of user input.

1. One solution might be to not trust user input and create a random filename on the server side.

2. Avoid customizing fragile path manipulation and checking methods that can easily be bypassed.

3. If saving according to user-inputted filenames is necessary, the best way to stay secure is by validating sanitized paths. For instance, in Java:

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
        // result of getOriginalName can be "../../etc/passwd"
        String originalFileName = multiPartFile.getOriginalFilename();
        if (originalFileName == null || StringUtils.isBlank(originalFileName)) {
            return;
        }
        if (multiPartFile.isEmpty()) {
            throw new StorageException("Failed to store empty file.");
        }
        // Do not implement custom, fragile methods for obtaining file names; it is very likely that they can be maliciously bypassed. Even standard libraries of frameworks like Spring have historically had bypass risks leading to path traversal vulnerabilities.
        String filename = FilenameUtils.getName(originalFileName);

        // Validate the file type
        String contentType = multiPartFile.getContentType(); // The contentType can be forged.
        // Use a whitelist to strictly limit file types in order to prevent the upload of dangerous files to the server.
        if (!ALLOWED_TYPES.contains(contentType)) {
            // This is a security check
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid file type");
            return;
        }
        // Do not implement custom, fragile methods for obtaining file extensions or file types, as they may be vulnerable to malicious bypasses.
        String extension = FilenameUtils.getExtension(filename); // Necessary checks
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
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
        // Check whether the parent directory of the file is the target directory: /var/image.
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
