@file:Suppress("HttpUrlsUsage")

package com.feysh.corax.config.general.utils

import org.apache.ibatis.builder.BuilderException
import org.w3c.dom.Document
import org.xml.sax.*
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParserFactory

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
