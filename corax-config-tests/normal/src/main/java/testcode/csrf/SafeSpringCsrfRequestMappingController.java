package testcode.csrf;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/test")
public class SafeSpringCsrfRequestMappingController {

    /**
     * Mapping to the HTTP request method `GET` is safe as long as no state-changing operations are performed within this method.
     */
    @GetMapping("/get-mapping")
    public void getMapping() {
    }

    @PostMapping("/post-mapping")
    public void postMapping() {
    }

    @PutMapping("/put-mapping")
    public void putMapping() {
    }

    @DeleteMapping("/delete-mapping")
    public void deleteMapping() {
    }

    @PatchMapping("/patch-mapping")
    public void patchMapping() {
    }

    /**
     * Mapping to the HTTP request method `GET` is safe as long as no state-changing operations are performed within this method.
     */
    @RequestMapping(value = "/request-mapping-get", method = RequestMethod.GET)
    public void requestMappingGet() {  // !$SpringCsrfUnrestrictedRequestMapping
    }

    /**
     * Mapping to the HTTP request method `HEAD` is safe as long as no state-changing operations are performed within this method.
     */
    @RequestMapping(value = "/request-mapping-head", method = RequestMethod.HEAD)
    public void requestMappingHead() {  // !$SpringCsrfUnrestrictedRequestMapping
    }

    /**
     * Mapping to the HTTP request method `TRACE` is safe as long as no state-changing operations are performed within this method.
     */
    @RequestMapping(value = "/request-mapping-trace", method = RequestMethod.TRACE)
    public void requestMappingTrace() {  // !$SpringCsrfUnrestrictedRequestMapping
    }

    /**
     * Mapping to the HTTP request method `OPTIONS` is safe as long as no state-changing operations are performed within this method.
     */
    @RequestMapping(value = "/request-mapping-options", method = RequestMethod.OPTIONS)
    public void requestMappingOptions() {  // !$SpringCsrfUnrestrictedRequestMapping
    }

    @RequestMapping(value = "/request-mapping-post", method = RequestMethod.POST)
    public void requestMappingPost() {  // !$SpringCsrfUnrestrictedRequestMapping
    }

    @RequestMapping(value = "/request-mapping-put", method = RequestMethod.PUT)
    public void requestMappingPut() {  // !$SpringCsrfUnrestrictedRequestMapping
    }

    @RequestMapping(value = "/request-mapping-delete", method = RequestMethod.DELETE)
    public void requestMappingDelete() {  // !$SpringCsrfUnrestrictedRequestMapping
    }

    @RequestMapping(value = "/request-mapping-patch", method = RequestMethod.PATCH)
    public void requestMappingPatch() {  // !$SpringCsrfUnrestrictedRequestMapping
    }

    /**
     * Mapping to several HTTP request methods is fine as long as all the HTTP request methods used are unprotected.
     */
    @RequestMapping(value = "/request-mapping-several-unprotected-methods",
            method = {RequestMethod.GET, RequestMethod.HEAD, RequestMethod.TRACE, RequestMethod.OPTIONS})
    public void requestMappingSeveralUnprotectedMethods() {  // !$SpringCsrfUnrestrictedRequestMapping
    }

    /**
     * Mapping to several HTTP request methods is fine as long as all the HTTP request methods used are protected.
     */
    @RequestMapping(value = "/request-mapping-several-protected-methods",
            method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public void requestMappingSeveralProtectedMethods() {  // !$SpringCsrfUnrestrictedRequestMapping
    }

}
