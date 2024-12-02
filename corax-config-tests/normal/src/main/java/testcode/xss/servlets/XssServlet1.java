package testcode.xss.servlets;

import org.apache.commons.lang.StringEscapeUtils;
import org.owasp.esapi.ESAPI;

import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class XssServlet1 extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String input1 = req.getParameter("input1");

        resp.getWriter().write(input1);  // $cwe-79 !$cwe-22
        resp.getWriter().print("<!--" + req.getQueryString() + "-->");  // $cwe-79 !$cwe-22

        resp.getWriter().write(ESAPI.encoder().encodeForHTML(input1));  // !$cwe-79 !$cwe-22
        resp.getWriter().write(StringEscapeUtils.escapeHtml(input1));  // !$cwe-79 !$cwe-22

        resp.getOutputStream().print(input1);  // $cwe-79 !$cwe-22
        resp.getOutputStream().println(input1);  // $cwe-79 !$cwe-22
        resp.getOutputStream().write(input1.getBytes(), 1, 20);  // $cwe-79 !$cwe-22
        resp.getOutputStream().write(input1.getBytes(StandardCharsets.UTF_8));  // $cwe-79 !$cwe-22

        OutputStreamWriter writer = new OutputStreamWriter(resp.getOutputStream());
        writer.write(input1); // $cwe-79

    }
}
