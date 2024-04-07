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

package com.feysh.corax.config.community.checkers.frameworks.xml

import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.builder.xml.MyBatisConfigurationXmlHandler
import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.builder.xml.MybatisConfiguration
import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.mybatis.MyBatisMapperXmlHandler
import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.mybatis.MybatisEntry
import com.feysh.corax.config.general.utils.SAX_FACTORY
import mu.KotlinLogging
import org.apache.ibatis.session.Configuration
import org.xml.sax.SAXException
import soot.Scene
import java.nio.file.Path
import javax.xml.parsers.ParserConfigurationException
import kotlin.Exception
import kotlin.String
import kotlin.Throws
import kotlin.io.path.inputStream

data class XmlParser(
    val handlerName: String,
    val contentHandler: BasedXmlHandler,
    val filePath: Path
) {
    fun processMyBatisSqlFragments(configuration: Configuration): Boolean {
        when (contentHandler) {
            is MyBatisMapperXmlHandler -> {
                contentHandler.initSqlFragments(filePath, configuration)
                return true
            }
        }
        return false
    }

    fun processMyBatisMapper(configuration: Configuration): MybatisEntry? {
        when (contentHandler) {
            is MyBatisMapperXmlHandler -> {
                return contentHandler.compute(filePath, configuration)
            }
        }
        return null
    }

    fun processMyBatisConfiguration(scene: Scene): MybatisConfiguration? {
        when (contentHandler) {
            is MyBatisConfigurationXmlHandler -> {
                return contentHandler.compute(scene, filePath)
            }
        }
        return null
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        @Throws(ParserConfigurationException::class, SAXException::class)
        fun parseMybatisMapper(filePath: Path, configuration: Configuration): MybatisEntry? {
            return parseMybatisMapper(HandlerDispatcher(), filePath, configuration)
        }

        @Throws(ParserConfigurationException::class, SAXException::class)
        fun parseMyBatisSqlFragments(path: Path, configuration: Configuration): Boolean {
            return parseMyBatisSqlFragments(HandlerDispatcher(), path, configuration)
        }

        @Throws(ParserConfigurationException::class, SAXException::class)
        fun parseMybatisMapper(dispatcher: HandlerDispatcher, path: Path, configuration: Configuration): MybatisEntry? {
            return fromFile(dispatcher, path)?.processMyBatisMapper(configuration)
        }

        @Throws(ParserConfigurationException::class, SAXException::class)
        fun parseMyBatisSqlFragments(dispatcher: HandlerDispatcher, path: Path, configuration: Configuration): Boolean {
            return fromFile(dispatcher, path)?.processMyBatisSqlFragments(configuration) ?: false
        }

        @Throws(ParserConfigurationException::class, SAXException::class)
        fun parseMybatisConfiguration(scene: Scene, filePath: Path): MybatisConfiguration? {
            return fromFile(HandlerDispatcher(), filePath)?.processMyBatisConfiguration(scene)
        }

        @Throws(ParserConfigurationException::class, SAXException::class)
        fun fromFile(dispatcher: HandlerDispatcher, filePath: Path): XmlParser? {
            filePath.inputStream().use { inputSource ->
                val parser = SAX_FACTORY.newSAXParser()

                // 1. first detect xml handler
                parser.setProperty("http://xml.org/sax/properties/lexical-handler", dispatcher)

                try {
                    parser.parse(inputSource, dispatcher)
                } catch (e: Exception) {
                    logger.warn { "Parse $filePath with a exception: ${e.message}" }
                    return null
                }

                val contentHandler = dispatcher.handler ?: return null
                return XmlParser(dispatcher.handlerName, contentHandler, filePath)
            }
        }
    }
}
