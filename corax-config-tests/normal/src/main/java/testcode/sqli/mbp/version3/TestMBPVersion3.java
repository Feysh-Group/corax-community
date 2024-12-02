package testcode.sqli.mbp.version3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestMBPVersion3 {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    IUserService userService;

    @RequestMapping
    public void test(String column) {
        QueryWrapper<SysUser> sysUserQueryWrapper = new QueryWrapper<>();
        QueryWrapper<SysUser> eq = sysUserQueryWrapper.eq(true, column, "sam");
        List<SysUser> userList = userMapper.selectList(eq);  // $SqlInjection
    }

    @RequestMapping
    public void test2(String sql) {
        QueryWrapper<SysUser> apply = new QueryWrapper<SysUser>().apply(sql);
        userMapper.delete(apply);  // $SqlInjection
    }

    @RequestMapping
    public void test3(String column, String sql) {
        QueryWrapper<SysUser> inSql = new QueryWrapper<SysUser>().inSql(column,"select * from user");
        QueryWrapper<SysUser> inSql2 = new QueryWrapper<SysUser>().inSql("name", sql);
        userService.exists(inSql2);  // $SqlInjection
        userService.getOne(inSql);  // $SqlInjection
    }

}
