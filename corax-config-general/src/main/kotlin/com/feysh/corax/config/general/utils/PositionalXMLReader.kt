package com.feysh.corax.config.general.utils

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.Attributes
import org.xml.sax.Locator
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.IOException
import java.io.InputStream
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParser

object PositionalXMLReader {
    const val LINE_NUMBER_KEY_NAME = "lineNumber"
    const val COLUMN_NUMBER_KEY_NAME = "columnNumber"
    @Throws(IOException::class, SAXException::class)
    fun readXML(`is`: InputStream?): Document {
        val doc: Document
        val parser: SAXParser
        try {
            val factory = SAX_FACTORY
            parser = factory.newSAXParser()
            val dbf = DocumentBuilderFactory.newInstance()
            val docBuilder = dbf.newDocumentBuilder()
            doc = docBuilder.newDocument()
        } catch (e: ParserConfigurationException) {
            throw RuntimeException(
                "Can't create SAX parser / DOM builder.", e
            )
        }
        val elementStack = Stack<Element>()
        val textBuffer = StringBuilder()
        val handler: DefaultHandler = object : DefaultHandler() {
            private var locator: Locator? = null
            override fun setDocumentLocator(locator: Locator) {
                this.locator = locator // Save the locator, so that it can be
                // used later for line tracking when
                // traversing nodes.
            }

            override fun startElement(
                uri: String, localName: String,
                qName: String, attributes: Attributes
            ) {
                addTextIfNeeded()
                val el = doc.createElement(qName)
                for (i in 0 until attributes.length) {
                    el.setAttribute(
                        attributes.getQName(i),
                        attributes.getValue(i)
                    )
                }
                locator?.let {
                    el.setUserData(LINE_NUMBER_KEY_NAME, it.lineNumber, null)
                    el.setUserData(COLUMN_NUMBER_KEY_NAME, it.columnNumber, null)
                }
                elementStack.push(el)
            }

            override fun endElement(
                uri: String, localName: String,
                qName: String
            ) {
                addTextIfNeeded()
                val closedEl = elementStack.pop()
                if (elementStack.isEmpty()) { // Is this the root element?
                    doc.appendChild(closedEl)
                } else {
                    val parentEl = elementStack.peek()
                    parentEl.appendChild(closedEl)
                }
            }

            override fun characters(
                ch: CharArray, start: Int,
                length: Int
            ) {
                textBuffer.append(ch, start, length)
            }

            // Outputs text accumulated under the current node
            private fun addTextIfNeeded() {
                if (textBuffer.isNotEmpty()) {
                    val el = elementStack.peek()
                    val textNode: Node = doc.createTextNode(
                        textBuffer
                            .toString()
                    )
                    el.appendChild(textNode)
                    textBuffer.delete(0, textBuffer.length)
                }
            }
        }
        parser.parse(`is`, handler)
        return doc
    }
}

val Node.lineNumber: Int get() = this.getUserData(PositionalXMLReader.LINE_NUMBER_KEY_NAME) as? Int ?: -1
val Node.columnNumber: Int get() = this.getUserData(PositionalXMLReader.COLUMN_NUMBER_KEY_NAME) as? Int ?: -1
