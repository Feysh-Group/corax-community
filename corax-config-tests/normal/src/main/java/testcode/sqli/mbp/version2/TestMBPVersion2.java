package testcode.sqli.mbp.version2;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestMBPVersion2 {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private IUserService userService;

    @RequestMapping
    public void test(String column) {
        Wrapper<User> wrapper = new EntityWrapper<User>().eq(column, "name");
        userMapper.selectList(wrapper);  // $SqlInjection
    }

    @RequestMapping
    public void test2(String selectSql) {
        User user = new User();
        Wrapper<User> wrapper = new EntityWrapper<>(user, selectSql);
        userMapper.delete(wrapper);  // $SqlInjection
    }

    @RequestMapping
    public void test3(String column) {
        Wrapper<User> wrapper = new EntityWrapper<User>().like("name", "sam").between(column, "123", "456");
        userService.delete(wrapper);  // $SqlInjection
    }

    @RequestMapping
    public void test4(String column) {
        Wrapper<User> wrapper = new EntityWrapper<User>().exists(true, column);
        userService.selectMapsPage(new Page(), wrapper);  // $SqlInjection
    }
}
