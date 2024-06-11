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

package com.feysh.corax.config.general.utils

import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList



open class XmlVisitor {
    open fun visitAttrNameAndValue(node: Node, name: String, value: String) {}

    private fun visitAttributes(node: Node) {
        if (!node.hasAttributes()) {
            return
        }
        val attributes: NamedNodeMap = node.attributes
        for (i in 0 until attributes.length) {
            val attribute: Node = attributes.item(i)
            visitAttrNameAndValue(attribute, attribute.nodeName, attribute.textContent)
        }
    }

    fun visitElements(element: Node) {
        visitNode(element)
        visitAttributes(element)

        val children: NodeList = element.childNodes
        for (i in 0 until children.length) {
            visitElements(children.item(i))
        }
    }

    private fun visitNode(node: Node) {
        val childNodes: NodeList = node.childNodes
        if (childNodes.length == 0) {
            return
        }
        if (childNodes.length != 1) {
            return
        }
        val childNode: Node = childNodes.item(0)
        val nodeName = node.nodeName ?: return
        when (childNode.nodeType) {
            Node.TEXT_NODE -> {
                visitAttrNameAndValue(node, nodeName, childNode.textContent)
            }

            Node.COMMENT_NODE -> {
                visitAttrNameAndValue(node, nodeName, childNode.textContent)
            }
        }

    }
}
