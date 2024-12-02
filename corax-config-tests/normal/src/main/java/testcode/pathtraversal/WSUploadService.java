package testcode.pathtraversal;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import org.apache.wink.common.model.multipart.BufferedInMultiPart;
import org.apache.wink.common.model.multipart.InPart;
import testcode.pathtraversal.UploadRecord.UploadFile;

/**
 * UploadService is written to provide upload service for the
 * dojox.form.Uploader widget.
 */
@Path("upload")
public class WSUploadService {
    /**
     * servletConfig injected by the JAX-RS engine
     */
    @Context
    private ServletConfig servletConfig;

    /**
     * upload method receives a JAX-RS Wink BufferedInMultiPart payload. Iterate
     * over the "parts" of this inbound multipart/form-data payload to retrieve
     * each part of the message body. Messages with a Content-Type of
     * multipart/form-data are typically submissions of HTML forms with standard
     * form input fields plus file input fields.
     *
     * @param multiPart
     *            the HTTP message payload, already parsed into "parts"
     * @throws IOException
     * @throws Exception
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public void upload(BufferedInMultiPart multiPart) throws IOException {
        List<InPart> parts = multiPart.getParts();
        for (InPart part : parts) {
            MultivaluedMap<String, String> headers = part.getHeaders();
            if (headers.containsKey("Content-Disposition")) {
                Map<String, String> cdHeaderMap = parseCDHeader(headers.get(
                        "Content-Disposition").get(0));
                String filename = getFilename(cdHeaderMap);
                if (filename != null) {
                    Long id = System.currentTimeMillis();
                    File uploadFile = new File(getUploadPath(), filename + "."
                            + id);
                    saveFile(part.getInputStream(), uploadFile);
                    UploadRecord.add(new UploadFile(id, uploadFile));
                }
            }
        }
    }

    @Path("file/{name}")
    @GET
    public Response download(@PathParam("name") String name) // $ArbitraryFileLeak
            throws FileNotFoundException {
//        UploadFile uploadFile = UploadRecord.findById(id);
        UploadFile uploadFile = new UploadFile(-1L, new File(name));
        if (uploadFile == null || !uploadFile.exists()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return Response
                .ok()
                .type(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .header("Content-Disposition",
                        "attachment;filename=" + uploadFile.getDisplayName())
                .entity(uploadFile.getInputStream()).build();
    }

    @Path("{id}")
    @DELETE
    public Response remove(@PathParam("id") Long id) {
        UploadFile uploadFile = UploadRecord.findById(id);
        if (uploadFile == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        UploadRecord.removeById(id);
        return Response.ok().build();
    }

    @Path("{filename}")
    @DELETE
    public Response remove(@PathParam("filename") String filename) {
        File file = new File(getUploadPath(), filename);
        file.deleteOnExit(); // $PathTraversal
        return Response.ok().build();
    }

    private void saveFile(InputStream is, File file) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            int n = -1;
            byte[] buf = new byte[1024 * 10];
            while ((n = is.read(buf)) > 0) {
                fos.write(buf, 0, n); // $ArbitraryFileReceive
            }
        }
        finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    private File getUploadPath() {
        String uploadPath = servletConfig.getServletContext().getRealPath(
                "/upload");
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        return uploadDir;
    }

    private String getFilename(Map<String, String> cdHeaderMap) {
        String filename = cdHeaderMap.get("filename");
        if (filename != null) { // this part is a file
            if ("".equals(filename)) {
                // probably came from Internet Explorer
                // so let's just use the "name" as the filename
                filename = cdHeaderMap.get("name");
                if (filename.endsWith("[]")) {
                    filename = filename.substring(0, filename.length() - 2);
                }
            }
            return filename;
        }
        return null;
    }

    private Map<String, String> parseCDHeader(String value) {
        Map<String, String> result = new HashMap<String, String>();
        String[] entries = value.split(";");
        for (String entry : entries) {
            entry = entry.trim();
            int eqIdx = entry.indexOf('=');
            if (eqIdx < 0) {
                result.put("Content-Disposition", entry);
            }
            else {
                // TODO: to use the regex way?
                String k = entry.substring(0, eqIdx).trim();
                String v = entry.substring(eqIdx + 1).trim();
                result.put(k, v.substring(1, v.length() - 1));
            }
        }
        return result;
    }

    private static final String UPLOAD_PATH = "resources/images/"; // 上传文件存储目录

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

    @GET
    @Path("/images/{image}")
    @Produces("images/*")
    public Response getImage(@javax.ws.rs.PathParam("image") String image) throws FileNotFoundException {
        File file = new File("resources/images/", image); // 不安全

        if (!file.exists()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok().entity(new FileInputStream(file)).build(); // $ArbitraryFileLeak
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public Response safeUpload(BufferedInMultiPart multiPart) throws IOException {
        List<InPart> parts = multiPart.getParts();
        for (InPart part : parts) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            InputStream uploadedInputStream = part.getInputStream();
            int read;
            byte[] bytes = new byte[1024];

            while ((read = uploadedInputStream.read(bytes)) != -1) {
                os.write(bytes, 0, read);  // !$ArbitraryFileLeak !$ArbitraryFileReceive
                os.write(bytes, 0, read);  // !$ArbitraryFileLeak !$ArbitraryFileReceive
            }
        }

        return Response.ok().build();
    }

}
