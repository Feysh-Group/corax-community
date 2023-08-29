package testcode.xss;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        Cookie cookie = new Cookie("xss", xss); // 存储 XSS 到 cookie value
        response.addCookie(cookie); // $cwe-79
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
}
