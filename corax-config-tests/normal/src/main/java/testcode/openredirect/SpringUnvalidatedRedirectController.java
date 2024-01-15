package testcode.openredirect;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class SpringUnvalidatedRedirectController {

    @RequestMapping("/redirect1")
    public String redirect1(@RequestParam("url") String url) {
        return "redirect:" + url;  // $UnvalidatedRedirect
    }

    @RequestMapping("/redirect2")
    public String redirect2(@RequestParam("url") String url) {
        String view = "redirect:" + url;
        return view;  // $UnvalidatedRedirect
    }

    @RequestMapping("/redirect3")
    public String redirect3(@RequestParam("url") String url) {
        return buildRedirect1(url);  // $UnvalidatedRedirect
    }

    private String buildRedirect1(String u) {
        return "redirect:" + u;
    }

    private View buildRedirect2(String u) {
        return new RedirectView(u);  // $UnvalidatedRedirect
    }

    @RequestMapping("/redirect4")
    public ModelAndView redirect4(@RequestParam("url") String url) {
        return new ModelAndView("redirect:" + url);  // $UnvalidatedRedirect
    }

    @RequestMapping("/redirect5")
    public ModelAndView redirect5(@RequestParam("url") String url) {
        String view = "redirect:" + url;
        return new ModelAndView(view);  // $UnvalidatedRedirect
    }

    @RequestMapping("/redirect6")
    public ModelAndView redirect6(@RequestParam("url") String url) {
        View view = buildRedirect2(url);
        return new ModelAndView(view);  // $UnvalidatedRedirect
    }

    @RequestMapping("/redirectfp")
    public String redirectfp() {
        return "redirect:/";  // !$!UnvalidatedRedirect
    }

}
