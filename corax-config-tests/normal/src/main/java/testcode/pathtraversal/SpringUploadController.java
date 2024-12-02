package testcode.pathtraversal;

import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;

@RestController
public class SpringUploadController {

    @RequestMapping("/uploadVuln1")
    public void uploadVuln1(String path, MultipartFile file) {
        try {
            var uploadedFile = new File(path);
            uploadedFile.createNewFile(); // $PathTraversalOut
            FileCopyUtils.copy(file.getBytes(), uploadedFile); // $ArbitraryFileReceive !$ArbitraryFileLeak

            file.transferTo(uploadedFile); // $ArbitraryFileReceive !$ArbitraryFileLeak
            file.transferTo(uploadedFile.toPath()); // $ArbitraryFileReceive !$ArbitraryFileLeak
        } catch (IOException ignored) {
        }
    }

    @RequestMapping("/uploadSafe1")
    public void uploadSafe1(String path, MultipartFile file) {
        try {
            var uploadedFile = new File("some_dir");
            FileCopyUtils.copy(file.getBytes(), uploadedFile); // !$ArbitraryFileReceive !$ArbitraryFileLeak
            FileCopyUtils.copy(file.getBytes(), uploadedFile); // !$ArbitraryFileReceive !$ArbitraryFileLeak
        } catch (IOException ignored) {
        }
    }

    @RequestMapping("/uploadVuln2")
    public void uploadVuln2(String path, MultipartFile file) {
        try {
            var uploadedFile = new File(file.getOriginalFilename()); // $PathTraversal !$UnrestrictedFileUpload !$PathTraversalOut  !$PathTraversalIn
            uploadedFile.createNewFile();  // $ArbitraryFileReceive $UnrestrictedFileUpload !$PathTraversalOut !$PathTraversalIn
            FileCopyUtils.copy(file.getBytes(), uploadedFile); // $ArbitraryFileReceive $UnrestrictedFileUpload !$PathTraversalOut !$ArbitraryFileLeak
        } catch (IOException ignored) {
        }
    }
}
