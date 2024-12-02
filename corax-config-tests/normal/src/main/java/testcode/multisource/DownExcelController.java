package testcode.multisource;

import cn.hutool.core.io.FileUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/adminUser")
public class DownExcelController {

    @PostMapping("/downExcel")
    public void downExcel(@RequestParam("token") String token, HttpServletRequest request) { // $PathTraversal
        String path = getTmpDirPath1(request) + "/" + token;
        if (FileUtil.exist(path)) {
            File file = new File(path); // $PathTraversal
        }
    }

    public static String getTmpDirPath1(HttpServletRequest request) {
        return getTmpDirPath2(request) + "sss";
    }

    public static String getTmpDirPath2(HttpServletRequest request) {
        return request.getParameter("x") + "ttt"; // $PathTraversal
    }
}