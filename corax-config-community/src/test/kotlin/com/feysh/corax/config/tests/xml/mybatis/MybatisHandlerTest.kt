package com.feysh.corax.config.tests.xml.mybatis

import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.builder.xml.XMLConfigBuilder
import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.mybatis.MyBatisMapperXmlHandler
import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.mybatis.MybatisEntry
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.net.URL
import kotlin.io.path.toPath

class MybatisHandlerTest {

    private val configuration = XMLConfigBuilder.createConfiguration()

    private fun getMybatisEntry(resource: URL): MybatisEntry? {
        val toURI = resource.toURI().toPath().toAbsolutePath()
        return MyBatisMapperXmlHandler().let {
            it.initSqlFragments(toURI, configuration)
            it.streamToSqls(toURI, configuration)
        }
    }

    @Test
    internal fun support_for_foreach() {
        val resource = this.javaClass.classLoader.getResource("mybatis/OmsOrderOperateHistoryDao.xml")!!
        val sqls = getMybatisEntry(resource)
        Assertions.assertEquals(
            "INSERT INTO oms_order_operate_history (order_id, operate_man, create_time, order_status, note) VALUES (?, ?, ?, ?, ?)",
            sqls?.methodSqlMap?.get("insertList")
        )
    }

    @Test
    internal fun official_author_map_test_cases() {
        val resource = this.javaClass.classLoader.getResource("mybatis/AuthorMapper.xml")!!
        val sqls = getMybatisEntry(resource)

        Assertions.assertEquals(15, sqls?.methodSqlMap?.size)
    }

    @Test
    internal fun official_blog_map_test_cases() {
        val resource = this.javaClass.classLoader.getResource("mybatis/BlogMapper.xml")!!
        val sqls = getMybatisEntry(resource)

        Assertions.assertEquals(6, sqls?.methodSqlMap?.size)
    }

    @Test
    internal fun include_in_mapper() {
        val resource = this.javaClass.classLoader.getResource("mybatis/PmsBrandMapper.xml")!!
        val sqls = getMybatisEntry(resource)

        Assertions.assertEquals(14, sqls?.methodSqlMap?.size)
    }

    @Test
    internal fun should_handle_refid_lost() {
        val resource = this.javaClass.classLoader.getResource("mybatis/OrderMapper.xml")!!
        val sqls = getMybatisEntry(resource)

        Assertions.assertEquals(1, sqls?.methodSqlMap?.size)
    }

    @Test
    internal fun handle_ifnotnull() {
        // apply org.mybatis.generator plugin. from mycollab
        val resource = this.javaClass.classLoader.getResource("mybatis/ProjectRolePermissionMapperExt.xml")!!
        val sqls = getMybatisEntry(resource)

        Assertions.assertEquals(2, sqls?.methodSqlMap?.size)
    }
}