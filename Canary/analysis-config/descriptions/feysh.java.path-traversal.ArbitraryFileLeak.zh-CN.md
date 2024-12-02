## 潜在路径遍历（任意泄露文件）

从可被攻击者控制的不可信路径进行 “文件外传”, 可能造成任意文件泄露.

**此规则是 `feysh.java.path-traversal.PathTraversalIn 潜在的路径遍历（任意文件读取）` 规则的一个特例，不仅读取任意文件还会对外部泄露文件内容。**

如果分析项目属于服务端，则此规则检查**任意文件下载泄露**漏洞


**缺陷代码:**

```java
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
  // or
  Files.copy(inputStream, response.getOutputStream());  // $ArbitraryFileLeak
}
```


```java
@GetMapping(value = "/download",produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
public ResponseEntity<byte[]> download(String resourceLocation) throws Exception {
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
```

**修复建议**

参考规则：`feysh.java.path-traversal.PathTraversalIn 潜在的路径遍历（任意文件读取）`