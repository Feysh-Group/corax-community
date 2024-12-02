package testcode.xss;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Controller
@RequestMapping("/xss")
public class XSS {

    /**
     * Vul Code.
     * StoredXSS Step1
     * http://localhost:8080/xss/stored/store?xss=<script>alert(1)</script>
     *
     * @param xss unescape string
     */
    @RequestMapping("/stored/store")
    @ResponseBody
    public String store(String xss, HttpServletResponse response) {
        // 存储 XSS 到 cookie value
        Cookie cookie = new Cookie("xss", xss);  // $cwe-79
        cookie.setValue(xss);  // $cwe-79
        response.addCookie(cookie); // !$cwe-79
        return "Set param into cookie";
    }

    @RequestMapping("/stored/show")
    @ResponseBody
    public String show(@CookieValue("xss") String xss) {
        return xss; // $cwe-79
    }

    @RequestMapping("/stored/users")
    @ResponseBody
    public String getUsers(HttpServletRequest request) {
        String users = request.getRemoteUser();
        return "users: " + users; // $cwe-79
    }

    @GetMapping(value = "/static/**")
    // @ApiOperation(value = "预览图片&下载文件")
    public void view1(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String users = request.getRemoteUser();
        OutputStream outputStream = response.getOutputStream();
        URL url = new URL("http://example.com/");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5 * 1000);
        InputStream inputStream = conn.getInputStream();// 通过输入流获取图片数据
        byte[] buf = new byte[1024];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            outputStream.write(buf, 0, len); // !$XssInjection
        }
    }

    @GetMapping(value = "/static/**")
    // @ApiOperation(value = "预览图片&下载文件")
    public void view2(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String users = request.getRemoteUser();
        OutputStream outputStream = response.getOutputStream();
        URL url = new URL(request.getParameter("url"));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5 * 1000);
        InputStream inputStream = conn.getInputStream();// 通过输入流获取图片数据
        byte[] buf = new byte[1024];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            outputStream.write(buf, 0, len); // $XssInjection
        }
    }
}
