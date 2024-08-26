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
import net.sf.saxon.s9api.Processor
import net.sf.saxon.s9api.SaxonApiException
import net.sf.saxon.s9api.XdmNode
import org.apache.ibatis.builder.BuilderException
import org.sonarsource.analyzer.commons.xml.XmlFile
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.*
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.sax.SAXSource
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


@Throws(ParserConfigurationException::class, SAXException::class)
fun createDocumentBuilder(entityResolver: EntityResolver?, validation: Boolean = false, isNamespaceAware: Boolean = true, isIgnoringComments: Boolean = false): DocumentBuilder {
    // important: this must only be called AFTER common constructor
    return try {
        val factory = org.apache.xerces.jaxp.DocumentBuilderFactoryImpl.newInstance()
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        factory.setFeature("http://apache.org/xml/features/dom/create-entity-ref-nodes", false)
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
        factory.isValidating = validation
        factory.isNamespaceAware = isNamespaceAware
        factory.isXIncludeAware = false
        factory.isIgnoringComments = isIgnoringComments
        factory.isIgnoringElementContentWhitespace = false
        factory.isCoalescing = false
        factory.isExpandEntityReferences = true
        val builder = factory.newDocumentBuilder()
        builder.setEntityResolver(entityResolver)
        // Implementations of DocumentBuilder usually provide Error Handlers, which may add some extra logic, such as logging.
        // This line disable these custom handlers during parsing, as we don't need it
        builder.setErrorHandler(null)
        builder
    } catch (e: Exception) {
        throw BuilderException("Error creating document instance.  Cause: $e", e)
    }
}


@Throws(SaxonApiException::class)
fun createSaxonDocument(processor: Processor, inputSource: InputSource): XdmNode {
    val builder = processor.newDocumentBuilder()
    builder.isLineNumbering = true
    builder.isDTDValidation = false

    val reader = SAX_FACTORY.newSAXParser().xmlReader
    val saxSource = SAXSource(inputSource)
    saxSource.xmlReader = reader
    return builder.build(saxSource)
}

object XmlUtils {

    val logger = KotlinLogging.logger {}
    private val xpath: XPath = XPathFactory.newInstance().newXPath()

    fun evaluateAsList(expression: XPathExpression, node: Node, path: String): List<Node> {
        return XmlFile.asList(evaluate(expression, node, path) ?: return emptyList())
    }

    fun evaluate(expression: XPathExpression, node: Node, path: String): NodeList? {
        return try {
            expression.evaluate(node, XPathConstants.NODESET) as? NodeList
        } catch (e: Exception) {
            logger.debug(e) { "Unable to evaluate XPath expression: $expression. xml: $path" }
            logger.warn { "Unable to evaluate XPath expression: $expression. xml: $path" }
            null
        }
    }

    fun getXPathExpression(expression: String): XPathExpression {
        return xpath.compile(expression)
    }
}