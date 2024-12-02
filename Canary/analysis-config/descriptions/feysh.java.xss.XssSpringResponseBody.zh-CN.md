## 跨站脚本XSS攻击-内容嗅探

没有对用户输入进行充分的验证和过滤，导致恶意脚本可以注入到网页中，危害用户数据安全。未经适当的转义处理，用户输入的内容直接展示在页面上，使得攻击者能够执行恶意代码。

## 解释 

在以下情况下会发生跨站点脚本（XSS）漏洞：

1. 数据通过不受信任的来源进入 Web 应用程序。对于反射型 XSS，不受信任的源通常是 Web 请求，而对于持久化（也称为存储型）XSS，它通常是数据库或其他后端数据存储。
2. 数据包含在动态内容中，未经验证即发送给 Web 用户。

发送到 Web 浏览器的恶意内容通常采用 JavaScript 段的形式，但也可能包括 HTML、Flash 或浏览器执行的任何其他类型的代码。基于 XSS 的攻击种类几乎是无限的，但它们通常包括向攻击者传输私人数据（如 cookie 或其他会话信息）、将受害者重定向到攻击者控制的 Web 内容，或以易受攻击的站点在用户的机器上执行其他恶意操作。

要使浏览器将响应呈现为 HTML 或其他可能执行脚本的文档，它必须指定 `text/html` MIME 类型。因此，只有当响应使用此 MIME 类型或任何其他类型时，XSS 才可能，该类型也强制浏览器将响应呈现为 HTML 或其他可能执行脚本（如 SVG 图像 (`image/svg+xml`)、XML 文档 (`image/svg+xml`)等的文档。

大多数现代浏览器在提供具有 MIME 类型（如 `application/octet-stream`）的响应时不会呈现 HTML 或执行脚本。但是某些浏览器（如 Internet Explorer）执行所谓的内容嗅探涉及忽略提供的 MIME 类型，并尝试通过响应的内容推断正确的 MIME 类型。

然而，值得注意的是，MIME 类型只是可能导致 XSS 漏洞的 MIME 类型之一。其他可能执行脚本的文档（如 SVG 图像 (`image/svg+xml`)、XML 文档(`application/xml`) 以及其他文档可能会导致 XSS 漏洞，无论浏览器是否执行内容嗅探。

因此，即使其内容类型标头设置为`application/octet-stream`, `multipart-mixed`等，诸如`<html><body><script>alert(1)</script></body></html>`这样的响应仍可以被呈现为HTML。

**Example 1:**

**不合规范的代码示例**

示例 1：以下 JAX-RS 方法在响应中反映用户数据。

```java
@RestController
public class SomeResource {
    @RequestMapping(value = "/test", produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    public String testResponse(@RequestParam(value="name") String name){
        return name;
    }
}
```

如果攻击者发送的请求参数`name`设置为`<html><body><script>alert(1)</script></body></html>` ，服务器仍将生成以下响应：

```html
HTTP/1.1 200 OK
Content-Length: 51
Content-Type: application/octet-stream
Connection: Closed

<html><body><script>alert(1)</script></body></html>
```



尽管响应明确指出应将其视为 JSON 文档，但旧浏览器仍可能尝试将其呈现为 HTML 文档，使其容易受到跨站点脚本攻击。


**合规范的解决方案**

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

以下代码易受跨站脚本攻击的影响，因为它返回包含用户输入的 HTML 响应。

如果您不打算向客户端发送 HTML 代码，则可以通过指定响应中返回的数据类型来修复漏洞。例如，您可以使用 `GetMapping` 注解的 `produces` 属性。

**不合规范的代码示例**

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

**合规范的解决方案**

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

**Content-Type**

请注意，除了 `text/html` 之外，还有更多内容类型允许在浏览器中执行 JavaScript 代码，因此容易受到跨站脚本攻击的影响。
已知以下内容类型存在受影响的情况:

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



## 修复方案

过滤或者编码特殊字符，需要注意过滤转换后对原有业务的影响

可选择使用的 html escape 方法：

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

