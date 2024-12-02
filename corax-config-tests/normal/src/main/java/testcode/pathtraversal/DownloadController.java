package testcode.pathtraversal;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import testcode.vo.Result;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.alibaba.fastjson.JSON;

@RestController
public class DownloadController {

    /**
     * @param path     想要下载的文件的路径
     * @param response
     * @功能描述 下载文件:  将文件以流的形式一次性读取到内存，通过响应输出流输出到前端
     */
    @RequestMapping("/download")
    public void download(String path, HttpServletResponse response) {
        try {
            // path是指想要下载的文件的路径
            File file = new File(path);
            // 获取文件名
            String filename = file.getName();
            // 获取文件后缀名
            String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
//            log.info("文件后缀名：" + ext);

            // 将文件写入输入流
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStream fis = new BufferedInputStream(fileInputStream);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();

            // 清空response
            response.reset();
            // 设置response的Header
            response.setCharacterEncoding("UTF-8");
            //Content-Disposition的作用：告知浏览器以何种方式显示响应返回的文件，用浏览器打开还是以附件的形式下载到本地保存
            //attachment表示以附件方式下载   inline表示在线打开   "Content-Disposition: inline; filename=文件名.mp3"
            // filename表示文件的默认名称，因为网络传输只支持URL编码的相关支付，因此需要将文件名URL编码后进行传输,前端收到后需要反编码才能获取到真正的名称
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
            // 告知浏览器文件的大小
            response.addHeader("Content-Length", "" + file.length());
            OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            outputStream.write(buffer);  // $ArbitraryFileLeak
            outputStream.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @param path     指想要下载的文件的路径
     * @param response
     * @功能描述 下载文件:将输入流中的数据循环写入到响应输出流中，而不是一次性读取到内存
     */
    @RequestMapping("/downloadLocal")
    public void downloadLocal(String path, HttpServletResponse response) throws IOException {
        // 读到流中
        InputStream inputStream = new FileInputStream(path);// 文件的存放路径
        response.reset();
        response.setContentType("application/octet-stream");
        String filename = new File(path).getName();
        response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
        ServletOutputStream outputStream = response.getOutputStream();
        byte[] b = new byte[1024];
        int len;
        //从输入流中读取一定数量的字节，并将其存储在缓冲区字节数组中，读到末尾返回-1
        while ((len = inputStream.read(b)) > 0) {
            outputStream.write(b, 0, len);  // $ArbitraryFileLeak
        }
        inputStream.close();
    }
    /**
     * @param path       下载后的文件路径和名称
     * @param netAddress 文件所在网络地址
     * @功能描述 网络文件下载到服务器本地
     */
    @RequestMapping("/uploadFromNet")
    public void uploadFromNet(String netAddress, String path) throws IOException {
        URL url = new URL(netAddress);
        URLConnection conn = url.openConnection();
        InputStream inputStream = conn.getInputStream();
        FileOutputStream fileOutputStream = new FileOutputStream(path); // $PathTraversalOut

        int bytesum = 0;
        int byteread;
        byte[] buffer = new byte[1024];
        while ((byteread = inputStream.read(buffer)) != -1) {
            bytesum += byteread;
            System.out.println(bytesum);
            fileOutputStream.write(buffer, 0, byteread);  // $ArbitraryFileReceive
        }
        fileOutputStream.close();
    }

    @GetMapping(value = "/template/parameters/excel/download2",produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadExcelTemplate2(String resourceLocation) throws Exception {
//        String resourceLocation = "classpath:templates/1.jpg";
        File downloadFile = ResourceUtils.getFile(resourceLocation);
        byte[] output = new byte[0];
        try {
            output = FileUtils.readFileToByteArray(downloadFile);
        } catch (IOException e) {
            System.out.println();
        }
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM_VALUE));
        responseHeaders.setContentLength(output.length);
        responseHeaders.set("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(downloadFile.getName(), "UTF-8")+"\"");
        return new ResponseEntity<>(output, responseHeaders, HttpStatus.OK);  // $ArbitraryFileLeak
    }

    @GetMapping("/downloadPdf.pdf")
    // 1.
    public ResponseEntity<Resource> downloadPdf(String path) {
        FileSystemResource resource = new FileSystemResource(path);
        // 2.
        MediaType mediaType = MediaTypeFactory
                .getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        // 3
        ContentDisposition disposition = ContentDisposition
                // 3.2
                .inline() // or .attachment()
                // 3.1
                .filename(resource.getFilename())
                .build();
        headers.setContentDisposition(disposition);
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);  // $ArbitraryFileLeak
    }

    @RequestMapping(value = "/files/{file_name}", method = RequestMethod.GET)
    public void getFile(@PathVariable("file_name") String fileName, HttpServletResponse response) {
        try {
            // get your file as InputStream
            InputStream is = new FileInputStream(fileName);
            // copy it to response's OutputStream
            org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());  // $ArbitraryFileLeak
            response.flushBuffer();
        } catch (IOException ex) {
            throw new RuntimeException("IOError writing file to output stream");
        }
    }

    @RequestMapping(value = "/files/{file_name}", method = RequestMethod.GET)
    @ResponseBody
    public Resource getFile(@PathVariable("file_name") String fleName) {
        return new FileSystemResource(fleName);  // $ArbitraryFileLeak
    }

    @RequestMapping(value = "/files", method = RequestMethod.GET)
    @ResponseBody
    public Resource getFile3() {
        String filename = "a.txt";
        return new FileSystemResource(filename);  // !$ArbitraryFileLeak
    }

    @RequestMapping(value = "/files/{file_name3}", method = RequestMethod.GET)
    @ResponseBody
    public Resource getFile2(@PathVariable("file_name3") String fleName) throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream(fleName);
        return new InputStreamResource(inputStream);  // $ArbitraryFileLeak
    }

    @RequestMapping(value = "/files/{fileName}", method = RequestMethod.GET)
    public HttpEntity<byte[]> createPdf(@PathVariable("fileName") String fileName) throws IOException {

        byte[] documentBody = FileUtils.readFileToByteArray(new File(fileName));

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_PDF);
        header.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=" + fileName.replace(" ", "_"));
        header.setContentLength(documentBody.length);

        return new HttpEntity<byte[]>(documentBody, header);  // $ArbitraryFileLeak
    }

    @RequestMapping(value = "/stuff/{fullPath}", method = RequestMethod.GET)
    public ResponseEntity<FileSystemResource> downloadStuff(@PathVariable String fullPath)
            throws IOException {
//        String fullPath = stuffService.figureOutFileNameFor(stuffId);
        File file = new File(fullPath);
        long fileLength = file.length(); // this is ok, but see note below

        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(MediaType.ALL);
        respHeaders.setContentLength(fileLength);
        respHeaders.setContentDispositionFormData("attachment", "fileNameIwant.pdf");

        FileSystemResource fileSystemResource = new FileSystemResource(file);
        return new ResponseEntity<FileSystemResource>(fileSystemResource, respHeaders, HttpStatus.OK);  // $ArbitraryFileLeak
    }
    @RequestMapping(value = "/stuff/{fullPath}", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> downloadStuff2(@PathVariable String fullPath)
            throws IOException {
        FileInputStream inputStream = new FileInputStream(fullPath);
        InputStreamResource isr = new InputStreamResource(inputStream);
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(MediaType.ALL);
        return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);  // $ArbitraryFileLeak
    }


    // 官方case Download a single file
    @GetMapping
    public void download (HttpServletRequest request, HttpServletResponse response, String path) throws IOException {

        // The file to be downloaded.
        Path file = Paths.get(path);

        // Get the media type of the file
        String contentType = Files.probeContentType(file);
        if (contentType == null) {
            // Use the default media type
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        response.setContentType(contentType);
        // File Size
        response.setContentLengthLong(Files.size(file));
        /**
         * Building the Content-Disposition header with the ContentDisposition utility class can avoid the problem of garbled downloaded file names.
         */
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                .filename(file.getFileName().toString(), StandardCharsets.UTF_8)
                .build()
                .toString());
        // Response data to the client
        Files.copy(file, response.getOutputStream());  // $ArbitraryFileLeak
    }

    // 官方case Compressing files with Gzip
    @GetMapping("/gzip")
    public void gzipDownload (HttpServletRequest request, HttpServletResponse response, String path) throws IOException {

        Path file = Paths.get(path);

        String contentType = Files.probeContentType(file);
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        // Tell the client what encoding is used by the body and the client will automatically decode it.
        response.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
        response.setContentType(contentType);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                .filename(file.getFileName().toString(), StandardCharsets.UTF_8)
                .build()
                .toString());

        // Compress the body with GZIPOutputStream
        try(GZIPOutputStream gzipOutputStream = new GZIPOutputStream(response.getOutputStream())){
            Files.copy(file, gzipOutputStream);  // $ArbitraryFileLeak
        }
    }

    // 官方case Download multiple files
    @GetMapping("/zip")
    public void zipDownload (HttpServletRequest request, HttpServletResponse response, String path, String path2) throws IOException {

        // List of files to be downloaded
        List<Path> files = Arrays.asList(Paths.get(path),
                Paths.get(path2));


        response.setContentType("application/zip"); // zip archive format
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                .filename("download.zip", StandardCharsets.UTF_8)
                .build()
                .toString());


        // Archiving multiple files and responding to the client
        try(ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())){
            for (Path file : files) {
                try (InputStream inputStream = Files.newInputStream(file)) {
                    zipOutputStream.putNextEntry(new ZipEntry(file.getFileName().toString()));
                    StreamUtils.copy(inputStream, zipOutputStream);  // $ArbitraryFileLeak
                    zipOutputStream.flush();
                }
            }
        }
    }

    /**
     * 通用json访问接口
     * 格式： http://localhost:8080/jeecg-boot/api/json/{filename}
     * @param filename
     * @return
     */
    @RequestMapping(value = "/json/{filename}", method = RequestMethod.GET)
    public String getJsonData(@PathVariable("filename") String filename) {
        String jsonpath = "classpath:org/jeecg/modules/demo/mock/json/"+filename+".json";
        return readJson(jsonpath);  // $ArbitraryFileLeak
    }

    private String readJson(String jsonSrc) {
        String json = "";
        try {
            //File jsonFile = ResourceUtils.getFile(jsonSrc);
            //json = FileUtils.re.readFileToString(jsonFile);
            //换个写法，解决springboot读取jar包中文件的问题
            InputStream stream = getClass().getClassLoader().getResourceAsStream(jsonSrc.replace("classpath:", ""));
            json = IOUtils.toString(stream,"UTF-8");
        } catch (IOException e) {
        }
        return json;
    }

    @GetMapping(value = "/asynTreeList")
    public Result asynTreeList(String id) {
        String json = readJson("JSON_PATH" + "/asyn_tree_list_" + id + ".json");
        return Result.OK(JSON.parseArray(json));  // $ArbitraryFileLeak
    }
}
