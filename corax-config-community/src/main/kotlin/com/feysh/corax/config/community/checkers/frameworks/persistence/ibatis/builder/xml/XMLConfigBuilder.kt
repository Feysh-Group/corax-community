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

package com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.builder.xml

import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.io.Resources
import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.type.TypeAliasRegistry
import org.apache.ibatis.builder.BuilderException
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver
import org.apache.ibatis.mapping.ResultSetType
import org.apache.ibatis.parsing.XNode
import org.apache.ibatis.parsing.XPathParser
import org.apache.ibatis.session.Configuration
import soot.RefType
import soot.Scene
import java.io.InputStream
import java.io.Reader
import java.util.*

/**
 * @author NotifyBiBi
 * @Link org.apache.ibatis.builder.xml.XMLConfigBuilder
 */
class XMLConfigBuilder private constructor(
    private val scene: Scene,
    private val parser: XPathParser,
) {
    val typeAliasRegistry = TypeAliasRegistry()
    private var parsed: Boolean = false
    val configuration: Configuration = Configuration()

    constructor(scene: Scene, reader: Reader, props: Properties? = null) : this(
        scene, XPathParser(reader, true, props, XMLMapperEntityResolver())
    )

    constructor(scene: Scene, inputStream: InputStream, props: Properties? = null) : this(
        scene, XPathParser(inputStream, true, props, XMLMapperEntityResolver())
    )


    fun parse() {
        if (parsed) {
            throw BuilderException("Each XMLConfigBuilder can only be used once.")
        }
        parsed = true
        parseConfiguration(parser.evalNode("/configuration"))
    }

    private fun parseConfiguration(root: XNode) {
        try {
            // issue #117 read properties first
            typeAliasesElement(root.evalNode("typeAliases"))
        } catch (e: Exception) {
            throw BuilderException("Error parsing SQL Mapper Configuration. Cause: $e", e)
        }
    }

    private fun typeAliasesElement(parent: XNode?) {
        if (parent != null) {
            for (child in parent.getChildren()) {
                if ("package" == child.name) {
                    val typeAliasPackage = child.getStringAttribute("name")
                    typeAliasRegistry.registerAliases(typeAliasPackage, scene.objectType.sootClass)
                } else {
                    val alias = child.getStringAttribute("alias")
                    val type = child.getStringAttribute("type")
                    try {
                        val clazz: RefType = Resources.classForName(scene, type) ?: continue
                        if (alias == null) {
                            typeAliasRegistry.registerAlias(clazz)
                        } else {
                            typeAliasRegistry.registerAlias(alias, clazz)
                        }
                    } catch (e: ClassNotFoundException) {
                        throw BuilderException("Error registering typeAlias for '$alias'. Cause: $e", e)
                    }
                }
            }
        }
    }

    companion object {
        fun createConfiguration(): Configuration {
            val configuration = Configuration()
            configuration.defaultResultSetType = ResultSetType.SCROLL_INSENSITIVE
            configuration.isShrinkWhitespacesInSql = true
            return configuration
        }
    }
}
