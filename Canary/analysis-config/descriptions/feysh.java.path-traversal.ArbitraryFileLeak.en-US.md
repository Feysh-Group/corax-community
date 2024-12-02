## Potential Path Traversal (arbitrary file leak)

Transmitting files from an untrusted path that can be controlled by an attacker, potentially introduce an arbitrary file disclosure vulnerability

**This rule is a specific case of the `feysh.java.path-traversal.PathTraversalIn Potential Path Traversal (Arbitrary File Read)` rule, where not only arbitrary files are read but also their content is leaked externally.**

If the analyzed project belongs to the server side, this rule checks for **arbitrary file download leakage** vulnerabilities.

**Defective Code:**

```java
@RequestMapping("/downloadLocal")
public void downloadLocal(String path, HttpServletResponse response) throws IOException {
  // Read into stream
  InputStream inputStream = new FileInputStream(path); // Path where the file is stored
  response.reset();
  response.setContentType("application/octet-stream");
  String filename = new File(path).getName();
  response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
  ServletOutputStream outputStream = response.getOutputStream();
  byte[] b = new byte[1024];
  int len;
  // Reads a certain number of bytes from the input stream and stores them in the buffer byte array, returns -1 when reaching the end
  while ((len = inputStream.read(b)) > 0) {
    outputStream.write(b, 0, len);  // $ArbitraryFileLeak
  }
  inputStream.close();
  // or
  Files.copy(inputStream, response.getOutputStream());  // $ArbitraryFileLeak
}
```


```java
@GetMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
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

**Fix Recommendation**

Refer to the rule: `feysh.java.path-traversal.PathTraversalIn Potential Path Traversal (Arbitrary File Read)`