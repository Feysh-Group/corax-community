## 本地XXE注入

解析未经信任的XML文件时，如果使用的XML解析器配置不当，可能会导致XML外部实体（XXE）攻击。此类攻击利用外部实体引用来访问系统上的任意文件、执行拒绝服务攻击或服务器端请求伪造。即使解析结果不返回给用户，攻击者也可能通过脱链数据检索技术窃取敏感信息，并且在这种情况下也能够进行拒绝服务攻击。

## 示例

以下示例在不受信任的数据上调用`parse`方法操作一个未安全配置的`DocumentBuilder`，因此本质上是不安全的。

**不合规代码：**

```java
public void parse(Socket sock) throws Exception {
  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  DocumentBuilder builder = factory.newDocumentBuilder();
  builder.parse(sock.getInputStream()); // 不安全
}
```

**合规代码：**

在这个示例中，`DocumentBuilder`在创建时禁用了DTD功能，从而增强了针对XXE攻击的安全性。

```java
public void disableDTDParse(Socket sock) throws Exception {
  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
  DocumentBuilder builder = factory.newDocumentBuilder();
  builder.parse(sock.getInputStream()); // 安全
}
```

**缺陷代码：**

```java
public static void vuln() throws SAXException, IOException {
    final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    final Schema schema = schemaFactory.newSchema();
    final Validator validator = schema.newValidator();

    final String documentSource = "<?xml version=\"1.0\"?><!DOCTYPE document [ <!ENTITY entity SYSTEM \"file:///etc/passwd\"> ]><document>&entity;</document>";
    final ByteArrayInputStream inputStream = new ByteArrayInputStream(documentSource.getBytes(StandardCharsets.UTF_8));
    final StreamSource source = new StreamSource(inputStream);

    validator.validate(source);
}
```



## 建议

防止XXE攻击的最佳方法是**禁用对不信任数据中任何文档类型声明（DTDs）的解析**。如果不具备这种可能性，您应该禁用对外部一般实体和外部参数实体的解析。这样可以提高安全性，但代码仍然可能存在拒绝服务和服务器端请求伪造攻击的风险。针对拒绝服务攻击的防护可以通过设置实体扩展限制来实现，这在近期的JDK和JRE实现中已作为默认设置。由于不同提供商之间存在多种禁用外部实体检索的方法，且支持程度各异，在此查询中我们选择专门检查特定解析器按照[OWASP推荐的方式](https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#java)禁用外部实体检索的方法。可能存在其他使特定解析器安全的方法与这些指导原则有所偏离，在这种情况下，此查询将继续标记该解析器为潜在危险。

Java中有许多XML解析器，其中大多数由于默认设置启用了对外部实体的解析，因此存在XXE漏洞。当前，该规则可以识别以下解析器中易受XXE攻击的XML解析行为：

- `javax.xml.parsers.DocumentBuilder`
- `javax.xml.stream.XMLStreamReader`
- `org.jdom.input.SAXBuilder`/`org.jdom2.input.SAXBuilder`
- `javax.xml.parsers.SAXParser`
- `org.dom4j.io.SAXReader`
- `org.xml.sax.XMLReader`
- `javax.xml.transform.sax.SAXSource`
- `javax.xml.transform.TransformerFactory`,
- `javax.xml.transform.sax.SAXTransformerFactory`
- `javax.xml.validation.SchemaFactory`
- `javax.xml.bind.Unmarshaller`
- `javax.xml.xpath.XPathExpression`
- `org.apache.poi.xssf.extractor.XSSFExportToXml`
    - CVE-2019-12415 修复方法: 升级`org.apache.poi:poi-ooxml >= 4.1.1`
- `org.apache.poi.xssf.usermodel.XSSFWorkbook`
    - CVE-2014-3529 修复方法: 升级`org.apache.poi:poi-ooxml >= 3.10.1`





## 参考资料

- OWASP漏洞描述：[XML External Entity (XXE) Processing](https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Processing).
- OWASP关于解析XML文件的指南：[XXE Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#java).
- Paper by Timothy Morgen：[XML Schema, DTD, and Entity Attacks](https://research.nccgroup.com/2014/05/19/xml-schema-dtd-and-entity-attacks-a-compendium-of-known-techniques/)
- Out-of-band data retrieval：Timur Yunusov & Alexey Osipov，Black Hat EU 2013：[XML Out-Of-Band Data Retrieval](https://www.slideshare.net/qqlan/bh-ready-v4).
- 拒绝服务攻击（Billion laughs）：[Billion Laughs](https://en.wikipedia.org/wiki/Billion_laughs).
- Java教程：[处理限制定义](https://docs.oracle.com/javase/tutorial/jaxp/limits/limits.html)
- CWE：[CWE-611](https://cwe.mitre.org/data/definitions/611.html) ，[CWE-776](https://cwe.mitre.org/data/definitions/776.html)，[CWE-827](https://cwe.mitre.org/data/definitions/827.html)

