# Unrestricted File Upload

Detected an unrestricted file upload, which allows users to upload files to the web server.

The consequences of unrestricted file upload can vary, including complete system takeover, an overloaded file system or database, forwarding attacks to back-end systems, and simple defacement. It depends on what the application does with the uploaded file and especially where it is stored. Here is the list of attacks that the attacker might do:

**Impact**

- Execution of Arbitrary code
- Server Compromise
- Client Side Attacks
- Cross Site Scripting

**详细**

- Compromise the web server by uploading and executing a web-shell which can run commands, browse system files, browse local resources, attack other servers, and exploit the local vulnerabilities, and so forth.
- Put a phishing page into the website.
- Put a jsp、php、html file into the website.
- Put a permanent XSS into the website.
- Bypass cross-origin resource sharing (CORS) policy and exfiltrate potentially sensitive data.
- Upload a file using malicious path or name which overwrites critical file or personal data that other users access. For example: the attacker might replace the `.htaccess` file to allow him/her to execute specific scripts.

**Solution**

Here is the translation of the provided content into English, keeping the structure intact as per your instructions:

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



## Example

**Vulnerable Code:**

Let us consider an example case and understand the CWE 434 with context of Vulnerable code and Mitigated code.


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

This code is vulnerable because it does not validate the file type or content before allowing it to be uploaded. An attacker could upload a malicious file that can execute arbitrary code on the server, potentially leading to data breaches, server compromise, or other malicious activities.


**Solution:**


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
        return fileNameWithoutExt  + "." + fileExt;
    }
}
```

**Relate**

- [OWASP Unrestricted File Upload](https://owasp.org/www-community/vulnerabilities/Unrestricted_File_Upload)

- [CWE434](https://cwe.mitre.org/data/definitions/434.html)

- [IDS56-J](https://wiki.sei.cmu.edu/confluence/display/java/IDS56-J.+Prevent+arbitrary+file+upload)
