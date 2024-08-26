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

import org.xml.sax.InputSource
import org.xml.sax.ext.DefaultHandler2

data class XmlInfo(
    val dtdName: String? = null,
    val entityName: String? = null,
    val publicId: String? = null,
    val systemId: String? = null,
    val baseURI: String? = null,
) {

    class Visitor : DefaultHandler2() {
        var value: XmlInfo = XmlInfo()
        override fun startDTD(name: String?, publicId: String?, systemId: String?) {
            value = value.copy(dtdName = name ?: value.dtdName, publicId = publicId ?: value.publicId, systemId = systemId ?: value.systemId)
        }

        override fun startEntity(name: String?) {
            value = value.copy(entityName = name ?: value.entityName)
        }

        override fun resolveEntity(name: String?, publicId: String?, baseURI: String?, systemId: String?): InputSource {
            value = value.copy(entityName = name ?: value.entityName, publicId = publicId ?: value.publicId, systemId = systemId ?: value.systemId, baseURI = baseURI ?: value.baseURI)
            return super.resolveEntity(name, publicId, baseURI, systemId)
        }
    }
}