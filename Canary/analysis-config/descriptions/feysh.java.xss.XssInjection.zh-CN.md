## 跨站脚本 (XSS) 攻击

没有对用户输入进行充分的验证和过滤，导致恶意脚本可以注入到网页中，危害用户数据安全。未经适当的转义处理，用户输入的内容（如 URL 参数）直接展示在页面上，使得攻击者能够执行恶意代码。


### Servlet 场景

以下代码易受跨站脚本攻击的影响，因为它返回包含用户输入的 HTML 响应。

第三方数据，如用户输入，是不可信的。如果嵌入到 HTML 代码中，应进行 HTML 编码以防止注入额外的代码。可以使用 OWASP Java Encoder 或类似的库来实现。

**非符合规范的代码示例**

```java
public void endpoint(HttpServletRequest request, HttpServletResponse response) throws IOException
{
    String data        = request.getParameter("input");
    PrintWriter writer = response.getWriter();

    writer.print(data);
}
```


**符合规范的解决方案**

```java
import org.owasp.encoder.Encode;

public void endpoint(HttpServletRequest request, HttpServletResponse response) throws IOException
{
    String data        = request.getParameter("input");
    PrintWriter writer = response.getWriter();

    writer.print(Encode.forHtml(data));
}
```


## 修复方案-编码

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


### 修复方案 - Content Security Policy (CSP) Header

通过深度防御安全方法，可以添加 CSP 响应标头来指示客户端浏览器阻止加载不满足应用程序安全要求的数据。如果配置正确，这可以防止任何在应用程序中利用 XSS 的尝试。