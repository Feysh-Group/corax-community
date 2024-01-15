package testcode.cmdi;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.codecs.WindowsCodec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
public abstract class CommandInjection {
    public static HttpServletRequest req; //Could be override at any time. (tainted)
    @SuppressWarnings("ToArrayCallWithZeroLengthArrayArgument")
    public static void main(String[] args) throws IOException {
        String input = args.length > 0 ? args[0] : ";cat /etc/passwd";
        List<String> cmd = Arrays.asList("ls", "-l", input);
        //Runtime exec()
        Runtime r = Runtime.getRuntime();
        r.exec("ls -l " + input);                                   // $CommandInjection
        r.exec("ls -l " + input, null);                       // $CommandInjection
        r.exec("ls -l " + input, null, null);             // $CommandInjection
        r.exec(cmd.toArray(new String[cmd.size()]));                         // $CommandInjection
        r.exec(cmd.toArray(new String[cmd.size()]), null);             // $CommandInjection
        r.exec(cmd.toArray(new String[cmd.size()]), null, null);   // $CommandInjection
        //ProcessBuilder
        new ProcessBuilder()
                .command("ls", "-l", input)                                  // $CommandInjection
                .start();
        new ProcessBuilder()
                .command(cmd)                                                // $CommandInjection
                .start();
    }

    public void bad() throws IOException {
        String tainted = req.getParameter("cmdi");
        StringBuilder builder = new StringBuilder("<" + tainted + ">");
        builder.insert(3, tainted).append("");
        builder.reverse();
        StringBuilder builder2 = new StringBuilder("xxx");
        builder2.append("").append(builder);
        String safe = "yyy";
        String unsafe = safe.replace("y", builder2.toString());
        Runtime.getRuntime().exec(unsafe.toLowerCase().substring(1).intern());  // $CommandInjection
    }

    public void good() throws IOException {
        String hardcoded = "constant";
        boolean b = "xxx".equals(hardcoded);
        StringBuilder builder = new StringBuilder("<" + hardcoded + ">");
        builder.insert(3, hardcoded).append("");
        builder.reverse();
        StringBuilder builder2 = b ? new StringBuilder("xxx") : new StringBuilder(8);
        builder2.append("").append(builder);
        String safe = "yyy";
        String unsafe = safe.replace("y", builder2.toString());
        Runtime.getRuntime().exec(unsafe.toLowerCase().substring(1).intern());  // !$CommandInjection
    }

    public void badWithException() throws Exception {
        String data = "";
        String fileName = req.getParameter("cmd");
        File file = new File(fileName);
        FileInputStream streamFileInput;
        InputStreamReader readerInputStream;
        BufferedReader readerBuffered;
        try {
            streamFileInput = new FileInputStream(file);
            readerInputStream = new InputStreamReader(streamFileInput, "UTF-8");
            readerBuffered = new BufferedReader(readerInputStream);
            data = readerBuffered.readLine();
        } catch (IOException ex) {
        }
        Runtime.getRuntime().exec(data);                                                   // $CommandInjection
    }

    public void badInterMethod() throws Exception {
        Runtime.getRuntime().exec(taintSource(""));                                 // $CommandInjection
    }

    public void badWithTaintSink() throws Exception {
        taintSink("safe", req.getHeader("x"));
    }

    private void taintSink(String param1, String param2) throws Exception {
        Runtime.getRuntime().exec(param1 + " safe " + param2);                   // $CommandInjection
    }

    public void badWithDoubleTaintSink() throws Exception {
        taintSinkTransfer(req.getParameter("y"));
    }

    public void taintSinkTransfer(String str) throws Exception {
        taintSink2(str.toLowerCase());
    }

    public void taintSink2(String param) throws Exception {
        Runtime.getRuntime().exec(param);                                                  // $CommandInjection
    }

    public void badCombo() throws Exception {
        String str = taintSource("").toUpperCase();
        str = str.concat("aaa" + "bbb");
        comboSink(new StringBuilder(str).substring(1));
    }

    public static void comboSink(String str) throws Exception {
        Runtime.getRuntime().exec(str);                                                    // $CommandInjection
    }

    public void badTransfer() throws IOException {
        String tainted = req.getParameter("zzz");
        Runtime.getRuntime().exec(combine("safe", tainted));                            // $CommandInjection
    }

    public void goodTransfer() throws IOException {
        String safe = "zzz";
        Runtime.getRuntime().exec(combine("safe", safe));                                // !$CommandInjection
    }

    public void stringArrays(String param) throws Exception {
        Runtime.getRuntime().exec(transferThroughArray(taintSource("")));            // $CommandInjection
        Runtime.getRuntime().exec(transferThroughArray("const" + param));               // !$CommandInjection
        Runtime.getRuntime().exec(transferThroughArray("const"));                       // !$CommandInjection
    }

    public void safeCall() throws IOException {
        sinkWithSafeInput("safe");
    }
    public void sinkWithSafeInput(String str) throws IOException {
        Runtime.getRuntime().exec(str);                                                     // !$CommandInjection
    }


    public void safeCommandMethodArray() {
        new ProcessBuilder().command("ls", "-la");
        String[] cmd = new String[] {"ls","-la"};
        new ProcessBuilder().command(cmd);                                                 // !$CommandInjection
    }

    public void safeCommandMethodList() {
        List<String> cmd1 = new ArrayList<String>();
        cmd1.add("ls");
        cmd1.add("-la");
        new ProcessBuilder().command(cmd1);                                                 // !$CommandInjection

        List<String> cmd2 = Arrays.asList("ls","-la");
        new ProcessBuilder().command(cmd2);                                                 // !$CommandInjection
    }

    public void safeCommandEcoded(String input) {
        String cmd = "ls "+ ESAPI.encoder().encodeForOS(new WindowsCodec() , input);
        new ProcessBuilder().command(cmd.split(" "));                                 // !$CommandInjection
    }

    private String transferThroughArray(String in) {
        String[] strings = new String[3];
        strings[0] = "safe1";
        strings[1] = in;
        strings[2] = "safe2";
        // whole array is tainted, index is not important
        String str = "safe3" + strings[1].trim();
        return str.split("a")[0];
    }
    
    private String transferThroughListIterator(String str) {
        List<String> list = new LinkedList<String>();
        ListIterator<String> listIterator = list.listIterator();
        listIterator.add(str);
        return listIterator.next();
    }
    
    private String transferListIteratorIndirect(String str) {
        List<String> list = new LinkedList<String>();
        // not able to transfer this, set as UNKNOWN even if str is SAFE
        ListIterator<String> listIterator = list.listIterator();
        listIterator.add(str);
        return list.get(0);
    }

    public String taintSource(String param) throws IOException {
        String fileName = req.getParameter("cmd");
        File file = new File(fileName);
        FileInputStream streamFileInput;
        InputStreamReader readerInputStream;
        BufferedReader readerBuffered;
        streamFileInput = new FileInputStream(file);
        readerInputStream = new InputStreamReader(streamFileInput, "UTF-8");
        readerBuffered = new BufferedReader(readerInputStream);
        return param + readerBuffered.readLine();
    }

    public String combine(String x, String y) {
        StringBuilder sb = new StringBuilder("safe");
        sb.append((Object) x);
        HashSet<String> set = new HashSet<String>();
        set.add("ooo");
        set.add(sb.append("x").append("y").toString().toLowerCase());
        for (String str : set) {
            if (str.equals(y.toLowerCase())) {
                return str + String.join("-", set) + String.join("a", "b", "c");
            }
        }
        return new StringBuilder(String.format("%s", y)).toString().trim() + "a".concat("aaa");
    }

}
