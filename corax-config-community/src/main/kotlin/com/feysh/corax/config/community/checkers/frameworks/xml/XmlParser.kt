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
import com.feysh.corax.config.general.utils.createDocumentBuilder
import mu.KotlinLogging
import org.apache.ibatis.session.Configuration
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.parsing.FailFastProblemReporter
import org.springframework.beans.factory.parsing.Problem
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry
import org.springframework.beans.factory.xml.*
import org.springframework.core.io.DescriptiveResource
import org.springframework.core.io.Resource
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.EntityResolver
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.ext.DefaultHandler2
import soot.Scene
import java.io.InputStream
import java.nio.file.Path
import javax.xml.parsers.ParserConfigurationException
import kotlin.io.path.inputStream
import kotlin.reflect.KClass

data class XmlParser(
    val handlerName: String,
    val contentHandler: BasedXmlHandler,
    val filePath: Path
) {

    fun <T: BasedXmlHandler> checkIs(type: KClass<T>): Boolean {
        return getHandler(type) != null
    }

    fun <T: BasedXmlHandler> getHandler(type: KClass<T>): T? {
        if (type.isInstance(contentHandler)) {
            @Suppress("UNCHECKED_CAST")
            return contentHandler as T
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

        private fun XmlParser.processMyBatisSqlFragments(configuration: Configuration): Boolean? {
            return getHandler(MyBatisMapperXmlHandler::class)?.initSqlFragments(filePath, configuration)
        }

        private fun XmlParser.processMyBatisMapper(configuration: Configuration): MybatisEntry? {
            return getHandler(MyBatisMapperXmlHandler::class)?.compute(filePath, configuration)
        }

        private fun XmlParser.processMyBatisConfiguration(scene: Scene): MybatisConfiguration? {
            return getHandler(MyBatisConfigurationXmlHandler::class)?.compute(scene, filePath)
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
            parse(dispatcher, filePath)
            val contentHandler = dispatcher.handler ?: return null
            return XmlParser(dispatcher.handlerName, contentHandler, filePath)
        }

        @Throws(ParserConfigurationException::class, SAXException::class)
        fun <T: DefaultHandler2> parse(dispatcher: T, filePath: Path): T {
            filePath.inputStream().use { inputSource ->
                val parser = SAX_FACTORY.newSAXParser()

                // 1. first detect xml handler
                parser.setProperty("http://xml.org/sax/properties/lexical-handler", dispatcher)

                try {
                    parser.parse(inputSource, dispatcher)
                } catch (e: Exception) {
                    logger.warn { "Parse $filePath with a exception: ${e.message}" }
                    return dispatcher
                }

                return dispatcher
            }
        }

        fun getXmlInfo(filePath: Path): XmlInfo {
            return parse(XmlInfo.Visitor(), filePath).value
        }

        fun isSpringBeansXmlFile(filePath: Path): Boolean {
            return filePath.inputStream().use { byteStream ->
                try {
                    val doc = createDocumentBuilder(entityResolver = null, validation = false).parse(InputSource(byteStream))
                    val beanDefinitionRegistry = SimpleBeanDefinitionRegistry()
                    var isBeansXmlFile = false
                    val beanDefinitionReader = object : XmlBeanDefinitionReader(beanDefinitionRegistry) {
                        override fun detectValidationMode(resource: Resource): Int {
                            return VALIDATION_NONE
                        }

                        override fun getEntityResolver(): EntityResolver? {
                            return null
                        }

                        override fun createBeanDefinitionDocumentReader(): BeanDefinitionDocumentReader {
                            return object : DefaultBeanDefinitionDocumentReader() {

                                override fun importBeanDefinitionResource(ele: Element) {
                                    // import is not required, and an error will be reported when the import is executed
                                    isBeansXmlFile = true
                                }

                                override fun processAliasRegistration(ele: Element) {
                                    isBeansXmlFile = true
                                }

                                override fun processBeanDefinition(ele: Element, delegate: BeanDefinitionParserDelegate?) {
                                    isBeansXmlFile = true
                                }

                                override fun doRegisterBeanDefinitions(ele: Element) {
                                    fun nodeNameEquals(node: Node, desiredName: String): Boolean {
                                        return desiredName == node.nodeName || desiredName == node.localName
                                    }
                                    if (nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
                                        isBeansXmlFile = true
                                    }
                                    super.doRegisterBeanDefinitions(ele)
                                }

                                override fun createDelegate(
                                    readerContext: XmlReaderContext?,
                                    root: Element?,
                                    parentDelegate: BeanDefinitionParserDelegate?
                                ): BeanDefinitionParserDelegate {
                                    val delegate = object: BeanDefinitionParserDelegate(readerContext) {
                                        override fun parseCustomElement(ele: Element?): BeanDefinition? {
                                            return null
                                        }
                                    }
                                    delegate.initDefaults(root, parentDelegate)
                                    return delegate
                                }
                            }
                        }

                    }
                    beanDefinitionReader.setProblemReporter(object : FailFastProblemReporter() {
                        override fun error(problem: Problem?) = warning(problem)
                        override fun fatal(problem: Problem?) = warning(problem)
                    })

                    beanDefinitionReader.registerBeanDefinitions(doc, DescriptiveResource("resource loaded through SAX InputSource"))
                    return@use isBeansXmlFile
                } catch (ignore: Exception) { }
                return@use false
            }
        }
    }
}
