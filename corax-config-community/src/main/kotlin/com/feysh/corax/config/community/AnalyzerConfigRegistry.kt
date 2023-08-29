@file:Suppress("MemberVisibilityCanBePrivate", "ObjectPropertyName")

package com.feysh.corax.config.community

import com.feysh.corax.config.api.AnalyzerConfigEntry
import com.feysh.corax.config.api.IConfigPluginExtension
import com.feysh.corax.config.api.ISootInitializeHandler
import com.feysh.corax.config.builtin.soot.DefaultSootConfiguration
import com.feysh.corax.config.general.model.`outstanding-summaries`
import com.feysh.corax.config.general.model.taint.TaintModelingConfig
import com.feysh.corax.config.general.model.javaee.JavaeeAnnotationSource
import com.feysh.corax.config.general.model.`main-method-source`
import com.feysh.corax.config.general.model.`secret-data-annotation`
import com.feysh.corax.config.community.checkers.frameworks.persistence.hibernate.jpa.JpaAnnotationSqlSinks
import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.IbatisAnnotationSQLSinks
import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.mybatis.MybatisMapperXmlSQLSinkConsumer
import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.mybatis.MybatisMapperXmlSQLSinkProvider
import com.feysh.corax.config.community.checkers.frameworks.spring.ResponseBodyCheck
import com.feysh.corax.config.community.checkers.`httponly-cookie`
import com.feysh.corax.config.community.checkers.`insecure-cookie`
import com.feysh.corax.config.community.checkers.`taint-checker`
import com.feysh.corax.config.community.checkers.`weak-ssl`
import mu.KotlinLogging
import org.pf4j.Extension
import org.pf4j.Plugin
import org.pf4j.PluginWrapper

class AnalyzerConfigRegistry(wrapper: PluginWrapper) : Plugin(wrapper) {
    override fun start() {
        logger.info("Plugin: Welcome")
    }

    override fun stop() {
        logger.info("Plugin: Stop")
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Extension
    class CommunityJavaDefault : IConfigPluginExtension {
        companion object{
            val configCallBack: AnalyzerConfigEntry = {
                preAnalysisImpl?.apply {
                    this += listOf(
                        MybatisMapperXmlSQLSinkProvider,
                        `weak-ssl`.`default-http-client`,
                    )
                }

                aiCheckerImpl?.apply {
                    this += listOf(
                        TaintModelingConfig,
                        JavaeeAnnotationSource,
                        `main-method-source`,
                        `secret-data-annotation`,
                        `outstanding-summaries`,
                        `taint-checker`,
                        JpaAnnotationSqlSinks,
                        MybatisMapperXmlSQLSinkConsumer,
                        IbatisAnnotationSQLSinks,
                        ResponseBodyCheck,
                        `insecure-cookie`,
                        `httponly-cookie`,
                        `weak-ssl`.SSLContext,
                    )
                }
            }
        }

        override val entry: AnalyzerConfigEntry = configCallBack
        override val sootConfig: ISootInitializeHandler = DefaultSootConfiguration
        override val name: String = "community.java"
        override fun toString(): String = name
    }

}
