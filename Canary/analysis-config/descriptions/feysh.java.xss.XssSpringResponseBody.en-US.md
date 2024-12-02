# Cross-Site Scripting: Content Sniffing

Lack of proper validation and filtering of user input can lead to the injection of malicious scripts into web pages, compromising user data security. Without appropriate escaping treatment, user-provided content is directly displayed on the page, allowing attackers to execute malicious code.

### Explanation

Cross-site scripting (XSS) vulnerabilities occur when:

1. Data enters a web application through an untrusted source. In the case of reflected XSS, the untrusted source is typically a web request, while in the case of persisted (also known as stored) XSS it is typically a database or other back-end data store.

2. The data is included in dynamic content that is sent to a web user without validation.


The malicious content sent to the web browser often takes the form of a JavaScript segment, but may also include HTML, Flash or any other type of code that the browser executes. The variety of attacks based on XSS is almost limitless, but they commonly include transmitting private data such as cookies or other session information to the attacker, redirecting the victim to web content controlled by the attacker, or performing other malicious operations on the user's machine under the guise of the vulnerable site.

For the browser to render the response as HTML, or other document that may execute scripts, it has to specify a `text/html` MIME type. Therefore, XSS is only possible if the response uses this MIME type or any other that also forces the browser to render the response as HTML or other document that may execute scripts such as SVG images (`image/svg+xml`), XML documents (`application/xml`), etc.

Most modern browsers do not render HTML or execute scripts when provided a response with MIME types such as `application/octet-stream`. However, some browsers such as Internet Explorer perform what is known as `Content Sniffing`. Content Sniffing involves ignoring the provided MIME type and attempting to infer the correct MIME type by the contents of the response.
It is worth noting however, a MIME type of `text/html` is only one such MIME type that may lead to XSS vulnerabilities. Other documents that may execute scripts such as SVG images (`image/svg+xml`), XML documents (`application/xml`), as well as others may lead to XSS vulnerabilities regardless of whether the browser performs Content Sniffing.

Therefore, a response such as `<html><body><script>alert(1)</script></body></html>`, could be rendered as HTML even if its `content-type` header is set to `application/octet-stream`, `multipart-mixed`, and so on.


**Example 1:**

**Noncompliant code example**

The following JAX-RS method reflects user data in an `application/octet-stream` response.


```java
@RestController
public class SomeResource {
    @RequestMapping(value = "/test", produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    public String response5(@RequestParam(value="name") String name){
        return name;
    }
}
```

If an attacker sends a request with the `name` parameter set to `<html><body><script>alert(1)</script></body></html>`, the server will produce the following response:



```html
HTTP/1.1 200 OK
Content-Length: 51
Content-Type: application/octet-stream
Connection: Closed

<html><body><script>alert(1)</script></body></html>
```

Even though, the response clearly states that it should be treated as a JSON document, an old browser may still try to render it as an HTML document, making it vulnerable to a Cross-Site Scripting attack.



**Compliant solution**

```java
import org.springframework.web.util.HtmlUtils;

@RestController
public class SomeResource {
    @RequestMapping(value = "/test", produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    public String testResponse(@RequestParam(value="name") String name){
        return HtmlUtils.htmlEscape(name);
    }
}
```


**Example 2:**

The following code is vulnerable to cross-site scripting because it returns an HTML response that contains user input.

If you do not intend to send HTML code to clients, the vulnerability can be fixed by specifying the type of data returned in the response. For example, you can use the `produces` property of the `GetMapping` annotation.

**Noncompliant code example**

```java
@RestController
public class ApiController
{
    @GetMapping(value = "/endpoint")
    public String endpoint(@RequestParam("input") String input)
    {
        return input;
    }
}
```

**Compliant solution**

```java
@RestController
public class ApiController
{
    @GetMapping(value = "/endpoint", produces = "text/plain")
    public String endpoint(@RequestParam("input") String input)
    {
        return input;
    }
}
```

### Content-types

Be aware that there are more content-types than `text/html` that allow to execute JavaScript code in a browser and thus are prone to cross-site scripting vulnerabilities.
The following content-types are known to be affected:

- `application/mathml+xml`
- `application/rdf+xml`
- `application/vnd.wap.xhtml+xml`
- `application/xhtml+xml`
- `application/xml`
- `image/svg+xml`
- `multipart/x-mixed-replace`
- `text/html`
- `text/rdf`
- `text/xml`
- `text/xsl`

        


### Solution

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