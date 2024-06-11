package com.feysh.corax.config.tests.xml.mybatis

import org.apache.ibatis.io.Resources
import org.apache.ibatis.session.SqlSession
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import org.junit.Ignore
import org.junit.jupiter.api.Test
import java.io.IOException
import java.io.Reader
import java.util.*
import kotlin.collections.HashMap


class User {
    var id: Int? = null
    var name: String? = null
    var pwd: String? = null

    constructor()
    constructor(id: Int?, name: String?, pwd: String?) {
        this.id = id
        this.name = name
        this.pwd = pwd
    }

    override fun toString(): String {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", pwd='" + pwd + '\'' +
                '}'
    }
}

interface UserMapper {
    fun addUser(map: Map<String, Any>): Int
}



class MybatisInterfaceTest {
    @Throws(IOException::class)
    private fun getSessionFactory(): SqlSessionFactory {
        val reader: Reader = Resources.getResourceAsReader("mybatis/mybatis-config.xml")
        val builder = SqlSessionFactoryBuilder()
        val p = Properties().apply {
            this["driver"] = "com.mysql.jdbc.Driver"
            this["url"] = "jdbc:mysql://localhost:3306/banchoobot?useUnicode=true&characterEncoding=UTF-8"
            this["username"] = "root"
            this["password"] = "root"
        }
        return builder.build(reader, p)
    }

    @Ignore
    fun test() {
        val sqlSession: SqlSession = getSessionFactory().openSession()
        val mapper: UserMapper = sqlSession.getMapper(UserMapper::class.java)
        val map: HashMap<String, Any> = HashMap()
        map["userId"] = 5
        map["passWord"] = "12345"
        mapper.addUser(map)
        sqlSession.commit()
        sqlSession.close()
    }

}
