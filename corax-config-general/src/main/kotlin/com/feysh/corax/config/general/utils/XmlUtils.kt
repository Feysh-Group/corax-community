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

@file:Suppress("HttpUrlsUsage")

package com.feysh.corax.config.general.utils

import mu.KotlinLogging
import org.apache.ibatis.builder.BuilderException
import org.sonarsource.analyzer.commons.xml.XmlFile
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.*
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParserFactory
import javax.xml.xpath.*

val SAX_FACTORY: SAXParserFactory = SAXParserFactory.newInstance().also {
    it.isValidating = java.lang.Boolean.FALSE
    it.isNamespaceAware = java.lang.Boolean.FALSE
    try {
        it.setFeature("http://xml.org/sax/features/namespaces", java.lang.Boolean.FALSE)
        it.setFeature("http://xml.org/sax/features/validation", java.lang.Boolean.FALSE)
        it.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", java.lang.Boolean.FALSE)
        it.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", java.lang.Boolean.FALSE)
        it.setFeature("http://xml.org/sax/features/external-general-entities", java.lang.Boolean.FALSE)
        it.setFeature("http://xml.org/sax/features/external-parameter-entities", java.lang.Boolean.FALSE)
    } catch (e: SAXException) {
        throw e
    } catch (e: ParserConfigurationException) {
        throw e
    }
}


fun createDocument(inputSource: InputSource, entityResolver: EntityResolver, validation: Boolean = false): Document {
    // important: this must only be called AFTER common constructor
    return try {
        val factory = DocumentBuilderFactory.newInstance()
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
        factory.isValidating = validation
        factory.isNamespaceAware = false
        factory.isIgnoringComments = true
        factory.isIgnoringElementContentWhitespace = false
        factory.isCoalescing = false
        factory.isExpandEntityReferences = true
        val builder = factory.newDocumentBuilder()
        builder.setEntityResolver(entityResolver)
        builder.setErrorHandler(object : ErrorHandler {
            @Throws(SAXException::class)
            override fun error(exception: SAXParseException) {
                throw exception
            }

            @Throws(SAXException::class)
            override fun fatalError(exception: SAXParseException) {
                throw exception
            }

            @Throws(SAXException::class)
            override fun warning(exception: SAXParseException) {
                // NOP
            }
        })
        builder.parse(inputSource)
    } catch (e: Exception) {
        throw BuilderException("Error creating document instance.  Cause: $e", e)
    }
}


object XmlUtils {

    val logger = KotlinLogging.logger {}
    private val xpath: XPath = XPathFactory.newInstance().newXPath()

    fun evaluateAsList(expression: XPathExpression, node: Node): List<Node> {
        return XmlFile.asList(evaluate(expression, node))
    }

    fun evaluate(expression: XPathExpression, node: Node): NodeList? {
        return try {
            expression.evaluate(node, XPathConstants.NODESET) as NodeList
        } catch (e: XPathExpressionException) {
            logger.error(e) { "Unable to evaluate XPath expression: $expression" }
            null
        }
    }

    fun getXPathExpression(expression: String): XPathExpression {
        return xpath.compile(expression)
    }
}