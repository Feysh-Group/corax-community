package testcode.sqli.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import testcode.sqli.dao.User;
import testcode.sqli.domain.entity.SysDept;

import java.util.List;

@Mapper
public interface UserMapper {

    /**
     * If using simple sql, we can use annotation. Such as @Select @Update.
     * If using ${username}, application will send an error.
     */
    @Select("select * from users where username = #{username}")
    User findByUserName(@Param("username") String username);

    @Select("select * from users where username = '${username}'")
    List<User> findByUserNameVuln01(@Param("username") String username);

    List<User> findByUserNameVuln02(String username);
    List<User> findByUserNameVuln03(@Param("order") String order);

    User findById(Integer id);

    User OrderByUsername();


    /**
     * 修改所在部门的父级部门状态
     *
     * @param dept 部门
     */
    public void updateDeptStatus(SysDept dept); // cwe-89

    void readOnly();
    void oneUpdate();
    void oneDelete();
    void oneInsert();
    void nonAtomic();
}
