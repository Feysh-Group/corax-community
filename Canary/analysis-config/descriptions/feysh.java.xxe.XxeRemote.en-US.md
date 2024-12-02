## Remote XXE Injection

Parsing untrusted XML files with a weakly configured XML parser may lead to an XML External Entity (XXE) attack. This type of attack uses external entity references to access arbitrary files on a system, carry out denial of service, or server side request forgery. Even when the result of parsing is not returned to the user, out-of-band data retrieval techniques may allow attackers to steal sensitive data. Denial of services can also be carried out in this situation.

## Example

**Noncompliant code example**

The following example calls `parse` on a `DocumentBuilder` that is not safely configured on untrusted data, and is therefore inherently unsafe.

```java
public void parse(Socket sock) throws Exception {
  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  DocumentBuilder builder = factory.newDocumentBuilder();
  builder.parse(sock.getInputStream()); //unsafe
}
```

**Compliant code example**

In this example, the `DocumentBuilder` is created with DTD disabled, securing it against XXE attack.

```java
public void disableDTDParse(Socket sock) throws Exception {
  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
  DocumentBuilder builder = factory.newDocumentBuilder();
  builder.parse(sock.getInputStream()); //safe
}
```

**Vulnerable Code:**

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

## Recommendation

The best way to prevent XXE attacks is to **disable the parsing of any Document Type Declarations (DTDs)** in untrusted data. If this is not possible you should disable the parsing of external general entities and external parameter entities. This improves security but the code will still be at risk of denial of service and server side request forgery attacks. Protection against denial of service attacks may also be implemented by setting entity expansion limits, which is done by default in recent JDK and JRE implementations. Because there are many different ways to disable external entity retrieval with varying support between different providers, in this query we choose to specifically check for the [OWASP recommended way](https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#java) to disable external entity retrieval for a particular parser. There may be other ways of making a particular parser safe which deviate from these guidelines, in which case this query will continue to flag the parser as potentially dangerous.

There are many XML parsers for Java, and most of them are vulnerable to XXE because their default settings enable parsing of external entities. This rule checker currently identifies vulnerable XML parsing from the following parsers: 

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
  - solution of CVE-2019-12415: update version of `org.apache.poi:poi-ooxml >= 4.1.1`
- `org.apache.poi.xssf.usermodel.XSSFWorkbook`
  - solution of CVE-2014-3529: update version of `org.apache.poi:poi-ooxml >= 3.10.1`




## References

- OWASP vulnerability description: [XML External Entity (XXE) Processing](https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Processing).
- OWASP guidance on parsing xml files: [XXE Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#java).
- Paper by Timothy Morgen: [XML Schema, DTD, and Entity Attacks](https://research.nccgroup.com/2014/05/19/xml-schema-dtd-and-entity-attacks-a-compendium-of-known-techniques/)
- Out-of-band data retrieval: Timur Yunusov & Alexey Osipov, Black hat EU 2013: [XML Out-Of-Band Data Retrieval](https://www.slideshare.net/qqlan/bh-ready-v4).
- Denial of service attack (Billion laughs): [Billion Laughs.](https://en.wikipedia.org/wiki/Billion_laughs)
- The Java Tutorials: [Processing Limit Definitions.](https://docs.oracle.com/javase/tutorial/jaxp/limits/limits.html)
- Common Weakness Enumeration: [CWE-611](https://cwe.mitre.org/data/definitions/611.html).
- Common Weakness Enumeration: [CWE-776](https://cwe.mitre.org/data/definitions/776.html).
- Common Weakness Enumeration: [CWE-827](https://cwe.mitre.org/data/definitions/827.html).

