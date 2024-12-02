## Cross-site scripting (XSS) injection

Insufficient validation and filtering of user inputs allow malicious scripts to be injected into web pages, compromising user data security. Without proper escaping, user-input content is directly displayed on the page, such as URL parameters, enabling attackers to execute malicious code.


### In Servlet

The following code is vulnerable to cross-site scripting because it returns an HTML response that contains user input.

Third-party data, such as user input, is not to be trusted. If embedded in HTML code, it should be HTML-encoded to prevent the injection of additional code. This can be done with the OWASP Java Encoder or similar libraries.

**Noncompliant code example**

```java
public void endpoint(HttpServletRequest request, HttpServletResponse response) throws IOException
{
    String data        = request.getParameter("input");
    PrintWriter writer = response.getWriter();

    writer.print(data);
}
```


**Compliant solution**

```java
import org.owasp.encoder.Encode;

public void endpoint(HttpServletRequest request, HttpServletResponse response) throws IOException
{
    String data        = request.getParameter("input");
    PrintWriter writer = response.getWriter();

    writer.print(Encode.forHtml(data));
}
```

### Solution - Escape

Filter or encode special characters, and be aware of the impact on the original business after conversion.

Optional html escape methods to use:

```java
String param = "中文 english <script> <img> / </script>";

org.springframework.web.util.HtmlUtils.htmlEscape(param);        // 中文 english &lt;script&gt; &lt;img&gt; / &lt;/script&gt;
org.owasp.encoder.Encode.forHtml(param);                         // 中文 english &lt;script&gt; &lt;img&gt; / &lt;/script&gt;

org.apache.commons.lang3.StringEscapeUtils.escapeHtml3(param);   // 中文 english &lt;script&gt; &lt;img&gt; / &lt;/script&gt;
org.apache.commons.lang3.StringEscapeUtils.escapeHtml4(param);   // 中文 english &lt;script&gt; &lt;img&gt; / &lt;/script&gt;

org.apache.commons.text.StringEscapeUtils.escapeHtml3(param);    // 中文 english &lt;script&gt; &lt;img&gt; / &lt;/script&gt;
org.apache.commons.text.StringEscapeUtils.escapeHtml4(param);    // 中文 english &lt;script&gt; &lt;img&gt; / &lt;/script&gt;

org.springframework.web.util.HtmlUtils.htmlEscapeDecimal(param); // 中文 english &#60;script&#62; &#60;img&#62; / &#60;/script&#62;
org.apache.commons.lang.StringEscapeUtils.escapeHtml(param);     // &#20013;&#25991; english &lt;script&gt; &lt;img&gt; / &lt;/script&gt;
```


### Solution - Content Security Policy (CSP) Header

Notice: We have not been able to check this solution at this time, and we will report it again after using this CSP solution

With a defense-in-depth security approach, the CSP response header can be added to instruct client browsers to block loading data that does not meet the application’s security requirements. If configured correctly, this can prevent any attempt to exploit XSS in the application.
