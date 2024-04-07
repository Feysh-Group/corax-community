package testcode.insecurecookie;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class InsecureCookieSamples {

    void unsafeCookie() {
        boolean unsafe = true;
        Cookie newCookie = new Cookie("test1","1234");
        newCookie.setSecure(false);          // $InsecureCookie
        newCookie.setSecure(unsafe);         // $InsecureCookie
        newCookie.setSecure(true);           // !$InsecureCookie
    }

    void multipleCookies(HttpServletResponse resp) {
        Cookie safeSecureCookie = new Cookie("cookie 3", "foo");
        safeSecureCookie.setSecure(true);
        resp.addCookie(safeSecureCookie);    // !$InsecureCookie

        Cookie unsafeSecureCookie = new Cookie("cookie 4", "bar");
        unsafeSecureCookie.setSecure(false);
        resp.addCookie(unsafeSecureCookie);  // $InsecureCookie

        Cookie unsafeCookie = new Cookie("cookie 3", "foo");
        resp.addCookie(unsafeCookie);        // $InsecureCookie

        Cookie mixedCookiesSafe = new Cookie("cookie 4", "bar");
        Cookie mixedCookies = new Cookie("cookie 5", "bar");
        mixedCookiesSafe.setSecure(true);    // !$InsecureCookie
        resp.addCookie(mixedCookies);        // $InsecureCookie

    }
}
