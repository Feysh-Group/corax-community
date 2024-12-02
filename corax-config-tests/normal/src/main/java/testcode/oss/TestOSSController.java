package testcode.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.model.*;
import com.aliyuncs.exceptions.ClientException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
public class TestOSSController {

    @RequestMapping("/test")
    public void test(HttpServletRequest request) throws ClientException {
        String key = request.getParameter("key");
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = "https://oss-cn-hangzhou.aliyuncs.com";
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        // 填写Bucket名称，例如examplebucket。
        String bucketName = "examplebucket";
        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
        String objectName = "exampledir/exampleobject.txt" + key;

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);

        try {
            // 填写字符串。
            String content = "Hello OSS，你好世界";
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, new ByteArrayInputStream(content.getBytes()));  // $PathTraversalOut, $UnrestrictedFileUpload
            // 上传字符串。
            PutObjectResult result = ossClient.putObject(putObjectRequest);  // $PathTraversalOut, $UnrestrictedFileUpload
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(content.getBytes()));  // $PathTraversalOut, $UnrestrictedFileUpload

            // 断点继续上传
            UploadFileRequest uploadFileRequest = new UploadFileRequest("examplebucket", key);  // $PathTraversalOut, $UnrestrictedFileUpload
            UploadFileRequest uploadFileRequest2 = new UploadFileRequest("examplebucket", "", key, 1, 1);  // $PathTraversalIn, !$UnrestrictedFileUpload
            UploadFileRequest uploadFileRequest3 = new UploadFileRequest("examplebucket", "", "key", 1, 1, true, objectName);  // $PathTraversalOut, $UnrestrictedFileUpload
            UploadFileRequest uploadFileRequest4 = new UploadFileRequest("examplebucket", "key");
            UploadFileRequest uploadFileRequest5 = new UploadFileRequest("examplebucket", "key");
            uploadFileRequest4.setUploadFile(key);  // $PathTraversalIn, !$UnrestrictedFileUpload
            uploadFileRequest5.setCheckpointFile(objectName);  // $PathTraversalOut, !$UnrestrictedFileUpload
            ossClient.uploadFile(uploadFileRequest);  // $PathTraversal, $UnrestrictedFileUpload
            ossClient.uploadFile(uploadFileRequest2);  // $PathTraversal, !$UnrestrictedFileUpload
            ossClient.uploadFile(uploadFileRequest3);  // $PathTraversal, !$UnrestrictedFileUpload
            ossClient.uploadFile(uploadFileRequest4);  // $PathTraversal, !$UnrestrictedFileUpload
            ossClient.uploadFile(uploadFileRequest5);  // $PathTraversal, !$UnrestrictedFileUpload

            // 分片上传
            // 创建InitiateMultipartUploadRequest对象。
            InitiateMultipartUploadRequest request2 = new InitiateMultipartUploadRequest(bucketName, key);  // $PathTraversalOut, $UnrestrictedFileUpload
            InitiateMultipartUploadResult upresult = ossClient.initiateMultipartUpload(request2);  // $PathTraversalOut, $UnrestrictedFileUpload

            GenericRequest request3 = new GenericRequest();
            request3.setKey(key);  // $PathTraversalOut, $UnrestrictedFileUpload

            UploadPartRequest uploadPartRequest = new UploadPartRequest();
            uploadPartRequest.setKey(key);  // $PathTraversalOut, $UnrestrictedFileUpload
            UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);  // $PathTraversalOut, $UnrestrictedFileUpload

            List<PartETag> partETags = new ArrayList<>();
            List<PartETag> partETags3 = new ArrayList<>();
            List<PartETag> partETags2 = new ArrayList<>();
            partETags.add(new PartETag(1, key));
            PartETag eTag = new PartETag(1, "key", 2, 3L);
            eTag.setETag(key);
            partETags2.add(eTag);
            CompleteMultipartUploadRequest completeMultipartUploadRequest =  // $PathTraversalOut, $UnrestrictedFileUpload
                    new CompleteMultipartUploadRequest(bucketName, key, "uploadId", partETags3);
            CompleteMultipartUploadRequest completeMultipartUploadRequest2 =
                    new CompleteMultipartUploadRequest(bucketName, "key", "uploadId", partETags);
            CompleteMultipartUploadRequest completeMultipartUploadRequest3 =
                    new CompleteMultipartUploadRequest(bucketName, "key", "uploadId", partETags2);
            CompleteMultipartUploadResult completeMultipartUploadResult = ossClient.completeMultipartUpload(completeMultipartUploadRequest);
            ossClient.completeMultipartUpload(completeMultipartUploadRequest);  // $PathTraversalOut, $UnrestrictedFileUpload
            ossClient.completeMultipartUpload(completeMultipartUploadRequest2);  // $PathTraversalOut, $UnrestrictedFileUpload
            ossClient.completeMultipartUpload(completeMultipartUploadRequest3);  // $PathTraversalOut, $UnrestrictedFileUpload


            // 下载文件
            OSSObject ossObject = ossClient.getObject(bucketName, objectName);  // $PathTraversalIn
            ossClient.getObject(new GetObjectRequest(bucketName, objectName), new File("pathName"));  // $PathTraversalIn

            GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, objectName);  // $PathTraversalIn
            getObjectRequest.setRange(0, 999);
            // 范围下载。
            OSSObject ossObject2 = ossClient.getObject(getObjectRequest);  // $PathTraversalIn

            DownloadFileRequest downloadFileRequest = new DownloadFileRequest(bucketName, objectName);  // $PathTraversalIn, !$UnrestrictedFileUpload
            DownloadFileResult downloadRes = ossClient.downloadFile(downloadFileRequest);  // $PathTraversal, !$UnrestrictedFileUpload
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    public void test2(HttpServletRequest request) throws ClientException {
        String endpoint = "https://oss-cn-hangzhou.aliyuncs.com";
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        OSS ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);
        String key = request.getParameter("key");
        String content1 = "Hello OSS，你好世界";
        ObjectMetadata meta = new ObjectMetadata();
        AppendObjectRequest appendObjectRequest = new AppendObjectRequest("bucketName", key, new ByteArrayInputStream(content1.getBytes()), meta);  // $PathTraversalOut, $UnrestrictedFileUpload
        appendObjectRequest.setPosition(0L);
        AppendObjectResult appendObjectResult = ossClient.appendObject(appendObjectRequest);  // $PathTraversalOut, $UnrestrictedFileUpload
    }

    public void manageFile(HttpServletRequest request) throws ClientException {
        String endpoint = "https://oss-cn-hangzhou.aliyuncs.com";
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        String key = request.getParameter("key");
        String objectName = "exampledir/exampleobject.txt" + key;
        String newObjectName = "exampledir/newexampleobject.txt" + key;
        String taint = request.getParameter("count");

        OSS ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);
        ObjectListing objectListing = ossClient.listObjects("bucketName", key);  // $PathTraversalIn, !$UnrestrictedFileUpload
        ObjectListing objectListing2 = ossClient.listObjects(key, "key");  // !$PathTraversalIn, !$UnrestrictedFileUpload


        CreateSelectObjectMetadataRequest createSelectObjectMetadataRequest = new CreateSelectObjectMetadataRequest("bucketName", key);  // $PathTraversalIn, !$UnrestrictedFileUpload

        CreateSelectObjectMetadataRequest createSelectObjectMetadataRequest2 = new CreateSelectObjectMetadataRequest(key, "key")  // !$PathTraversalIn, !$UnrestrictedFileUpload
                .withInputSerialization(
                        new InputSerialization().withCsvInputFormat(
                                // 填写内容中不同记录之间的分隔符，例如\r\n。
                                new CSVFormat().withHeaderInfo(CSVFormat.Header.Use).withRecordDelimiter("\r\n")));
        SelectObjectMetadata selectObjectMetadata = ossClient.createSelectObjectMetadata(createSelectObjectMetadataRequest);  // $PathTraversalIn, !$UnrestrictedFileUpload
        SelectObjectMetadata selectObjectMetadata2 = ossClient.createSelectObjectMetadata(createSelectObjectMetadataRequest2);  // !$PathTraversalIn, !$UnrestrictedFileUpload


        SelectObjectRequest selectObjectRequest =  // $PathTraversalIn, !$UnrestrictedFileUpload
                new SelectObjectRequest("bucketName", key)
                        .withInputSerialization(
                                new InputSerialization().withCsvInputFormat(
                                        new CSVFormat().withHeaderInfo(CSVFormat.Header.Use).withRecordDelimiter("\r\n")))
                        .withOutputSerialization(new OutputSerialization().withCsvOutputFormat(new CSVFormat()));

        SelectObjectRequest selectObjectRequest2 =  // !$PathTraversalIn, !$UnrestrictedFileUpload
                new SelectObjectRequest(key, "key")
                        .withInputSerialization(
                                new InputSerialization().withCsvInputFormat(
                                        new CSVFormat().withHeaderInfo(CSVFormat.Header.Use).withRecordDelimiter("\r\n")))
                        .withOutputSerialization(new OutputSerialization().withCsvOutputFormat(new CSVFormat()));
        // 使用SELECT语句查询第4列，值大于40的所有记录。
        selectObjectRequest.setExpression("select * from ossobject where _4 > " + taint);  // $SqlInjection
        OSSObject ossObject = ossClient.selectObject(selectObjectRequest);  // $PathTraversalIn, !$UnrestrictedFileUpload
        OSSObject ossObject2 = ossClient.selectObject(selectObjectRequest2);  // !$PathTraversalIn, !$UnrestrictedFileUpload


        // 重命名文件
        RenameObjectRequest renameObjectRequest = new RenameObjectRequest("bucketName", "objectName", newObjectName);  // $PathTraversalOut
        RenameObjectRequest renameObjectRequest2 = new RenameObjectRequest("bucketName", objectName, "newObjectName");  // $PathTraversal
        ossClient.renameObject(renameObjectRequest);  // $PathTraversalOut
        ossClient.renameObject(renameObjectRequest2);  // $PathTraversal
        ossClient.renameObject("bucketName", objectName, "newObjectName");  // $PathTraversal
        ossClient.renameObject("bucketName", "objectName", newObjectName);  // $PathTraversalOut

        // 删除文件

        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest("bucketName");
        DeleteObjectsRequest deleteObjectsRequest1 = deleteObjectsRequest.withKeys(Collections.singletonList(key));  // $PathTraversal
        ossClient.deleteObjects(deleteObjectsRequest1);  // $PathTraversal, !$UnrestrictedFileUpload
        ossClient.deleteObject("bucketName", objectName);  // $PathTraversal, !$UnrestrictedFileUpload

        // 拷贝文件
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest("bucketName", objectName, "newObjectName", newObjectName);  // $PathTraversalOut, $UnrestrictedFileUpload
        ossClient.copyObject(copyObjectRequest);  // $PathTraversalOut
        ossClient.copyObject("bucketName", objectName, "newObjectName", newObjectName);  // $PathTraversalOut, $UnrestrictedFileUpload
    }

}
