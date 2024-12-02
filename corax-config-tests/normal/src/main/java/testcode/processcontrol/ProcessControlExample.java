package testcode.processcontrol;

import javax.servlet.http.HttpServletRequest;

public class ProcessControlExample {
    public void loadLibBad(HttpServletRequest req) {
        String path = req.getParameter("libPath");
        System.load(path); // $ProcessControl
        Runtime.getRuntime().loadLibrary(path); // $ProcessControl
    }

    public void loadLibGood(HttpServletRequest req) {
        System.load("D:\\Users\\library.dll"); // !$ProcessControl
    }
}
