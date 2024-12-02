## 潜在路径遍历（任意接受并创建文件）

“创建文件” 到一个可被攻击者控制的不可信路径, 可能会创建危险类型文件或造成任意文件覆盖和伪造.

**此规则是 `feysh.java.path-traversal.PathTraversalOut 潜在的路径遍历（文件创建/写入）` 规则的一个特例，不仅创建任意文件还会写入可控文件内容。**

如果分析项目属于服务端，则此规则检查**任意文件路径上传**漏洞

**缺陷代码:**

```java
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
            out.write(bytes, 0, read); // $ArbitraryFileReceive
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
```


**修复建议**

参考规则：`feysh.java.path-traversal.PathTraversalOut 潜在的路径遍历（文件创建/写入）`