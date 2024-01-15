@file:Suppress("MemberVisibilityCanBePrivate", "ObjectPropertyName", "ClassName")

package com.feysh.corax.config.community

import com.feysh.corax.config.api.*
import com.feysh.corax.config.api.IConfigPluginExtension
import com.feysh.corax.config.api.ISootInitializeHandler
import com.feysh.corax.config.builtin.soot.DefaultSootConfiguration

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

        override val units: LinkedHashSet<CheckerUnit> = linkedSetOf<CheckerUnit>().also {
            it += com.feysh.corax.config.general.checkers.analysis.LibVersionProvider
            it += com.feysh.corax.config.general.model.taint.TaintModelingConfig
            it += com.feysh.corax.config.general.model.javaee.JavaeeAnnotationSource
            it += com.feysh.corax.config.general.model.`outstanding-summaries`
            it += com.feysh.corax.config.general.model.`main-method-source`
            it += com.feysh.corax.config.general.model.`secret-data-annotation`

            it += com.feysh.corax.config.community.checkers.frameworks.persistence.hibernate.jpa.JpaAnnotationSqlSinks
            it += com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.mybatis.`mybatis-sql-injection-checker`
            it += com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.IbatisAnnotationSQLSinks
            it += com.feysh.corax.config.community.checkers.frameworks.spring.ResponseBodyCheck
            it += com.feysh.corax.config.community.checkers.cipher.`insecure-cipher`
            it += com.feysh.corax.config.community.checkers.cipher.`insecure-cipher-api-call`
            it += com.feysh.corax.config.community.checkers.frameworks.persistence.hibernate.jpa.JpaAnnotationSqlSinks
            it += com.feysh.corax.config.community.checkers.frameworks.spring.ResponseBodyCheck
            it += com.feysh.corax.config.community.checkers.hardcode.`hardcode-crypto-key`
            it += com.feysh.corax.config.community.checkers.hardcode.`hardcode-credential`
            it += com.feysh.corax.config.community.checkers.jwt.MissingJWTSignatureCheck
            it += com.feysh.corax.config.community.checkers.`weak-ssl`.`default-http-client`
            it += com.feysh.corax.config.community.checkers.`predict-random`
            it += com.feysh.corax.config.community.checkers.csrf
            it += com.feysh.corax.config.community.checkers.`taint-checker`
            it += com.feysh.corax.config.community.checkers.`insecure-cookie`
            it += com.feysh.corax.config.community.checkers.`httponly-cookie`
            it += com.feysh.corax.config.community.checkers.`weak-ssl`.SSLContext
            it += com.feysh.corax.config.community.checkers.`xxe-attacks`
            it += com.feysh.corax.config.community.checkers.`weak-hash`
            it += com.feysh.corax.config.community.checkers.`open-redirect`
            it += com.feysh.corax.config.community.checkers.`permissive-cors`
            it += com.feysh.corax.config.community.checkers.`permissive-cors`.`any-url-request`
        }

        override val sootConfig: ISootInitializeHandler = DefaultSootConfiguration
        override val name: String = "feysh.community.java"
        override fun toString(): String = name
    }

}
