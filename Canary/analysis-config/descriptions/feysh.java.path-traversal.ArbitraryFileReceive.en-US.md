## Potential Path Traversal (arbitrary file receive and create)

Creating a file to an untrusted path controlled by an attacker might lead to the creation of dangerous file types or result in arbitrary file overwriting and forgery.

**This rule is a special case of the `feysh.java.path-traversal.PathTraversalOut: Potential Path Traversal (File Creation/Write)` rule, where not only any file can be created but also the file content can be written with controllable data.**

If the analyzed project belongs to the server side, this rule checks for **arbitrary file path upload** vulnerabilities.

**Defective Code:**

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


**Fix Recommendation**

Refer to the rule: `feysh.java.path-traversal.PathTraversalOut: Potential Path Traversal (File Creation/Write)`