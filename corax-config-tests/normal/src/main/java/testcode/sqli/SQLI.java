package testcode.sqli;


import org.springframework.web.bind.annotation.*;
import testcode.sqli.dao.User;
import testcode.sqli.dao.UserNoSetter;
import testcode.sqli.domain.entity.SysDept;
import testcode.sqli.dto.UserProfile;
import testcode.sqli.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.sql.*;
import java.util.List;


@SuppressWarnings("Duplicates")
@RestController
@RequestMapping("/sqli")
public class SQLI {

    private static Logger logger = LoggerFactory.getLogger(SQLI.class);
    private static String driver = "com.mysql.jdbc.Driver";

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String user;

    @Value("${spring.datasource.password}")
    private String password;

    @Autowired
    private UserMapper userMapper;


    /**
     * Vuln Code.
     * http://localhost:8080/sqli/jdbc/vuln?username=NAME
     *
     * @param username username
     */
    @RequestMapping("/jdbc/vuln")
    public String jdbc_sqli_vul(@RequestParam("username") String username) {

        StringBuilder result = new StringBuilder();

        try {
            Class.forName(driver);
            Connection con = DriverManager.getConnection(url, user, password);

            if (!con.isClosed())
                System.out.println("Connect to database successfully.");

            // sqli vuln code
            Statement statement = con.createStatement();
            String sql = "select * from users where username = '" + username + "'";
            logger.info(sql);
            ResultSet rs = statement.executeQuery(sql);  // $SqliChecker

            while (rs.next()) {
                String res_name = rs.getString("username");
                String res_pwd = rs.getString("password");
                String info = String.format("%s: %s\n", res_name, res_pwd);
                result.append(info);
                logger.info(info);
            }
            rs.close();
            con.close();


        } catch (ClassNotFoundException e) {
            logger.error("Sorry,can`t find the Driver!");
        } catch (SQLException e) {
            logger.error(e.toString());
        }
        return result.toString();
    }


    /**
     * Security Code.
     * http://localhost:8080/sqli/jdbc/sec?username=NAME
     *
     * @param username username
     */
    @RequestMapping("/jdbc/sec")
    public String jdbc_sqli_sec(@RequestParam("username") String username) {

        StringBuilder result = new StringBuilder();
        try {
            Class.forName(driver);
            Connection con = DriverManager.getConnection(url, user, password);

            if (!con.isClosed())
                System.out.println("Connecting to Database successfully.");

            // fix code
            String sql = "select * from users where username = ?";
            PreparedStatement st = con.prepareStatement(sql); // !$SqliChecker
            st.setString(1, username);

            logger.info(st.toString());  // sql after prepare statement
            ResultSet rs = st.executeQuery();                 // !$SqliChecker

            while (rs.next()) {
                String res_name = rs.getString("username");
                String res_pwd = rs.getString("password");
                String info = String.format("%s: %s\n", res_name, res_pwd);
                result.append(info);
                logger.info(info);
            }

            rs.close();
            con.close();

        } catch (ClassNotFoundException e) {
            logger.error("Sorry, can`t find the Driver!");
            e.printStackTrace();
        } catch (SQLException e) {
            logger.error(e.toString());
        }
        return result.toString();
    }


    /**
     * http://localhost:8080/sqli/jdbc/ps/vuln?username=NAME' or 'a'='a
     *
     * Incorrect use of prepareStatement. prepareStatement must use ? as a placeholder.
     */
    @RequestMapping("/jdbc/ps/vuln")
    public String jdbc_ps_vuln(@RequestParam("username") String username) {

        StringBuilder result = new StringBuilder();
        try {
            Class.forName(driver);
            Connection con = DriverManager.getConnection(url, user, password);

            if (!con.isClosed())
                System.out.println("Connecting to Database successfully.");

            String sql = "select * from users where username = '" + username + "'";
            PreparedStatement st = con.prepareStatement(sql);  // $SqliChecker

            logger.info(st.toString());
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                String res_name = rs.getString("username");
                String res_pwd = rs.getString("password");
                String info = String.format("%s: %s\n", res_name, res_pwd);
                result.append(info);
                logger.info(info);
            }

            rs.close();
            con.close();

        } catch (ClassNotFoundException e) {
            logger.error("Sorry, can`t find the Driver!");
            e.printStackTrace();
        } catch (SQLException e) {
            logger.error(e.toString());
        }
        return result.toString();
    }


    /**
     * vuln code
     * http://localhost:8080/sqli/mybatis/vuln01?username=NAME' or '1'='1
     *
     * @param username username
     */
    @GetMapping("/mybatis/vuln01")
    public List<User> mybatisVuln01(@RequestParam("username") String username) {
        return userMapper.findByUserNameVuln01(username); // $SqliChecker
    }

    /**
     * vul code
     * http://localhost:8080/sqli/mybatis/vuln02?username=NAME' or '1'='1' %23
     *
     * @param username username
     */
    @GetMapping("/mybatis/vuln02")
    public List<User> mybatisVuln02(@RequestParam("username") String username) {
        return userMapper.findByUserNameVuln02(username); // $SqliChecker
    }

    // http://localhost:8080/sqli/mybatis/orderby/vuln03?sort=1 desc%23
    @GetMapping("/mybatis/orderby/vuln03")
    public List<User> mybatisVuln03(@RequestParam("sort") String sort) {
        return userMapper.findByUserNameVuln03(sort); // $SqliChecker
    }
    Connection connection;

    @GetMapping("/mybatis/orderby/vuln04")
    public void mybatisVuln04(User user) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // 1. 加载驱动程序
            Statement statement = connection.createStatement();  // 3. 创建Statement对象
            String sql = "select * from users where username = '" + user.getUsername() + "'";
            statement.executeQuery(sql);
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/mybatis/orderby/vuln05")
    public void mybatisVuln05(UserNoSetter userNoSetter) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // 1. 加载驱动程序
            Statement statement = connection.createStatement();  // 3. 创建Statement对象
            String sql = "select * from users where username = '" + userNoSetter.username + "'";
            statement.executeQuery(sql);
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/mybatis/vuln06")
    public void mybatisVuln06(SysDept sysDept) {
        userMapper.updateDeptStatus(sysDept);// $cwe-89
    }

    /**
     * security code
     * http://localhost:8080/sqli/mybatis/sec01?username=NAME
     *
     * @param username username
     */
    @GetMapping("/mybatis/sec01")
    public User mybatisSec01(@RequestParam("username") String username) {
        return userMapper.findByUserName(username); // !$SqliChecker
    }

    /**
     * http://localhost:8080/sqli/mybatis/sec02?id=1
     *
     * @param id id
     */
    @GetMapping("/mybatis/sec02")
    public User mybatisSec02(@RequestParam("id") Integer id) {
        return userMapper.findById(id); // !$SqliChecker
    }


    /**
     * http://localhost:8080/sqli/mybatis/sec03
     */
    @GetMapping("/mybatis/sec03")
    public User mybatisSec03() {
        return userMapper.OrderByUsername(); // !$SqliChecker
    }


    @PutMapping(path = "/IDOR/profile/{userId}", consumes = "application/json")
    @ResponseBody
    public List<User> completed(@PathVariable("userId") String userId, @RequestBody UserProfile userSubmittedProfile) {
        return userMapper.findByUserNameVuln03(userSubmittedProfile.getName()); // $SqliChecker
    }
}
