package testcode.cmdi;


import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandInjectionSuspicious {
    HttpServletRequest req;
    public void test(){
        String p = req.getParameter("cmdi");
        insecureConstructorArray(p);
        insecureConstructorList(p);
        insecureCommandMethodArray(p);
        insecureCommandMethodList(p);
    }

    public void insecureConstructorArray(String input) {
        new ProcessBuilder("ls",input);                         // $CommandInjection
        String[] cmd = new String[] {"ls",input};
        new ProcessBuilder(cmd);                                          // $CommandInjection

    }

    public void insecureConstructorList(String input) {
        List<String> cmd1 = new ArrayList<String>();
        cmd1.add(input);
        new ProcessBuilder(cmd1);                                         // $CommandInjection

        List<String> cmd2 = Arrays.asList("ls", input);
        new ProcessBuilder(cmd2);                                         // $CommandInjection
    }

    public void insecureCommandMethodArray(String input) {
        new ProcessBuilder().command("ls", input);                        // $CommandInjection
        String[] cmd = new String[] {"ls",input};
        new ProcessBuilder().command(cmd);                                // $CommandInjection
    }

    public void insecureCommandMethodList(String input) {
        List<String> cmd1 = new ArrayList<String>();
        cmd1.add(input);
        new ProcessBuilder().command(cmd1);                               // $CommandInjection

        List<String> cmd2 = Arrays.asList("ls",input);
        new ProcessBuilder().command(cmd2);                               // $CommandInjection
    }
}
