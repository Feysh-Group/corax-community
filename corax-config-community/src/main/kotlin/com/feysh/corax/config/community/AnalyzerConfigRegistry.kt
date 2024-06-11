/*
 *  CoraxJava - a Java Static Analysis Framework
 *  Copyright (C) 2024.  Feysh-Tech Group
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

@file:Suppress("MemberVisibilityCanBePrivate", "ObjectPropertyName", "ClassName")

package com.feysh.corax.config.community

import com.feysh.corax.config.api.*
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

            it += com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.mybatis.`mybatis-sql-injection-checker`
            it += com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.IbatisAnnotationSQLSinks
            it += com.feysh.corax.config.community.checkers.frameworks.spring.ResponseBodyCheck
            it += com.feysh.corax.config.community.checkers.cipher.`insecure-cipher`
            it += com.feysh.corax.config.community.checkers.cipher.`insecure-cipher-api-call`
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
            it += com.feysh.corax.config.community.checkers.`external-xxe-attacks`
            it += com.feysh.corax.config.community.checkers.`weak-hash`
            it += com.feysh.corax.config.community.checkers.`open-redirect`
            it += com.feysh.corax.config.community.checkers.`permissive-cors`
            it += com.feysh.corax.config.community.checkers.`permissive-cors`.`any-url-request`
            it += com.feysh.corax.config.community.checkers.deserialize.`deserialize-insecure-call`
        }

        override val sootConfig: ISootInitializeHandler = DefaultSootConfiguration
        override val name: String = "feysh.community.java"
        override fun toString(): String = name
    }

}
