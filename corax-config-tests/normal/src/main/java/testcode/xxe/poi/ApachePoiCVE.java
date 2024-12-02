package testcode.xxe.poi;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.Version;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.PackageHelper;
import org.apache.poi.xssf.extractor.XSSFExportToXml;
import org.apache.poi.xssf.usermodel.XSSFMap;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.FileInputStream;
import java.io.IOException;

public class ApachePoiCVE {

    /**
     * CVE-2014-3529 漏洞是 OPCPackage.open -> ContentTypeManager() 时候触发的XXE 。
     * org.apache.poi.ss.usermodel.WorkbookFactory#create(java.io.InputStream) 和 org.apache.poi.xssf.usermodel.XSSFWorkbook#XSSFWorkbook(java.io.InputStream) 都会调用  OPCPackage.open  导致XXE。
     * 限制版本区间 < 3.10.1。
     */
    public static void CVE20143529(String xlsx) throws IOException, EncryptedDocumentException, InvalidFormatException {
        FileInputStream inp = new FileInputStream(xlsx);
        new XSSFWorkbook(PackageHelper.open(inp)); // $XxeChecker
        new XSSFWorkbook(OPCPackage.open(inp)); // $XxeChecker
        new XSSFWorkbook(inp); // $XxeChecker
        new XSSFWorkbook(xlsx); // $XxeChecker
        new XSSFWorkbook(OPCPackage.open(xlsx)); // $XxeChecker
        XSSFWorkbook wb = (XSSFWorkbook)WorkbookFactory.create(inp) ; // $XxeChecker
        Sheet sheet = wb.getSheetAt(0);
    }

    /**
     * CVE-2019-12415 漏洞是 exportToXML 时候 validate = true 触发的。
     * 版本区间 < 4.1.1
     */
    public static void CVE201912415(String xlsx) throws IOException, EncryptedDocumentException, InvalidFormatException, ParserConfigurationException, TransformerException, SAXException {
        XSSFWorkbook wb = (XSSFWorkbook)WorkbookFactory.create(new FileInputStream(xlsx)) ;
        Sheet sheet = wb.getSheetAt(0);
        System.out.println(sheet.getLastRowNum());
        for (XSSFMap map : wb.getCustomXMLMappings()) {
            // 使用 XSSFExportToXml 将 xlsx 转成 xml
            XSSFExportToXml exporter = new XSSFExportToXml(map);
            //第一个参数是输出流无所谓，第二个参数要为 true
            exporter.exportToXML(System.out, true);    // $XxeChecker
            exporter.exportToXML(System.out, "utf-8", true);    // $XxeChecker
            exporter.exportToXML(System.out, "utf-8", false);    // !$XxeChecker
        }

        FileInputStream inp = new FileInputStream(xlsx);
        XSSFWorkbook wb2 = new XSSFWorkbook(OPCPackage.open(inp));
        for (XSSFMap map : wb2.getCustomXMLMappings()) {
            // 使用 XSSFExportToXml 将 xlsx 转成 xml
            XSSFExportToXml exporter = new XSSFExportToXml(map);
            // 第一个参数是输出流无所谓，第二个参数要为 true
            exporter.exportToXML(System.out, true);     // $XxeChecker
            exporter.exportToXML(System.out, "utf-8", true);     // $XxeChecker
            // 第一个参数是输出流无所谓，第二个参数要为 true
            exporter.exportToXML(System.out, false);    // !$XxeChecker
            exporter.exportToXML(System.out, "utf-8", false);    // !$XxeChecker
        }
    }

    public static void main(String[] args) {
        String xlsx = args[0];
        try {
            CVE20143529(xlsx);
            CVE201912415(xlsx);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}