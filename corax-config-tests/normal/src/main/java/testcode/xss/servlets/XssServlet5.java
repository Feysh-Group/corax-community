package testcode.xss.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class XssServlet5 extends HttpServlet {


    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String input1 = req.getParameter("input1");

        testWrite(resp.getWriter(), input1, req);
        testFormatUnsafe(resp.getWriter(), input1, req);
        testFormatSafe(resp.getWriter(), input1, req);
        testPrintUnsafe(resp.getWriter(), input1, req);
        testPrintSafe(resp.getWriter(), input1, req);
        testAppend(resp.getWriter(), input1, req);
        testPrintfUnsafe(resp.getWriter(), input1, req);
        testPrintfSafe(resp.getWriter(), input1, req);
    }

    public void testWrite(PrintWriter pw, String input1, HttpServletRequest req) {
        pw.write(input1);  // $cwe-79
        pw.write(input1, 0, 10);  // $cwe-79
        pw.write(input1.toCharArray());  // $cwe-79
        pw.write(input1.toCharArray(), 0, 10);  // $cwe-79
    }

    public void testFormatUnsafe(PrintWriter pw, String input1, HttpServletRequest req) {
        pw.format(req.getLocale(), "%s", input1);  // $cwe-79
        pw.format("%s", input1);  // $cwe-79
        pw.format("%s %s", "SAFE", input1);  // $cwe-79
        pw.format("%s %s %s", "SAFE", "SAFE", input1);  // $cwe-79
        pw.format("%s %s %s %s", "SAFE", "SAFE", input1, "SAFE");  // $cwe-79
        pw.format(input1, "<== the actual format string can be alter");  // $cwe-79
    }

    public void testFormatSafe(PrintWriter pw, String input1, HttpServletRequest req) {
        pw.format(req.getLocale(), "Data : %s", "Constant data");  // !$cwe-79
        pw.format("%s", "SAFE");  // !$cwe-79
        pw.format("%s %s", "SAFE", "SAFE");  // !$cwe-79
        pw.format("%s %s %s", "SAFE", "SAFE", "SAFE");  // !$cwe-79
    }

    public void testPrintUnsafe(PrintWriter pw, String input1, HttpServletRequest req) {
        pw.print(input1.toCharArray());  // $cwe-79
        pw.print(input1);  // $cwe-79
        pw.print((Object) input1);  // $cwe-79
        for (char c : input1.toCharArray()) {
            pw.print(c);  // $cwe-79
        }

        pw.println(input1.toCharArray());  // $cwe-79
        pw.println(input1);  // $cwe-79
        pw.println((Object) input1);  // $cwe-79
        for (char c : input1.toCharArray()) {
            pw.println(c);  // $cwe-79
        }
    }

    public void testPrintSafe(PrintWriter pw, String input1, HttpServletRequest req) {
        pw.print("".equals(input1)); //Boolean is consider unexploitable (safe for the other primitive type)  // !$cwe-79
        pw.print(Double.parseDouble(input1));  // !$cwe-79
        pw.print(Integer.parseInt(input1));  // !$cwe-79
        pw.print(Float.parseFloat(input1));  // !$cwe-79
        pw.print(Long.parseLong(input1));  // !$cwe-79

        pw.print("SAFE".toCharArray());  // !$cwe-79
        pw.print("SAFE AGAIN");  // !$cwe-79
        pw.print((Object) "SAFE SAFE SAFE");  // !$cwe-79

        pw.println("".equals(input1)); //Boolean is consider unexploitable (safe for the other primitive type)  // !$cwe-79
        pw.println(Double.parseDouble(input1));  // !$cwe-79
        pw.println(Integer.parseInt(input1));  // !$cwe-79
        pw.println(Float.parseFloat(input1));  // !$cwe-79
        pw.println(Long.parseLong(input1));  // !$cwe-79

        pw.println("SAFE".toCharArray());  // !$cwe-79
        pw.println("SAFE AGAIN");  // !$cwe-79
        pw.println((Object) "SAFE SAFE SAFE");  // !$cwe-79
    }

    public void testPrintfUnsafe(PrintWriter pw, String input1, HttpServletRequest req) {
        pw.printf(req.getLocale(), "%s", input1);  // $cwe-79
        pw.printf(req.getLocale(), input1, "<== the actual format string can be alter");  // $cwe-79
        pw.printf(req.getLocale(), input1, input1);  // $cwe-79
        pw.printf("%s", input1);  // $cwe-79
        pw.printf(input1, "<== the actual format string can be alter");  // $cwe-79
        pw.printf(input1, input1);  // $cwe-79
    }

    public void testPrintfSafe(PrintWriter pw, String input1, HttpServletRequest req) {
        pw.printf(req.getLocale(), "%s", "SAFE");  // !$cwe-79
        pw.printf("%s", "SAFE");  // !$cwe-79
    }


    public void testAppend(PrintWriter pw, String input1, HttpServletRequest req) {
        pw.append(input1);  // $cwe-79
        pw.append(input1, 0, 10);  // $cwe-79
        for (char c : input1.toCharArray()) {
            pw.append(c);  // $cwe-79
        }
    }


}
