package com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.builder.xml

import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.io.Resources
import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.type.TypeAliasRegistry
import org.apache.ibatis.builder.BuilderException
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver
import org.apache.ibatis.parsing.XNode
import org.apache.ibatis.parsing.XPathParser
import soot.RefType
import soot.Scene
import java.io.InputStream
import java.io.Reader
import java.util.*

/**
 * @author NotifyBiBi
 */
class XMLConfigBuilder private constructor(
    private val scene: Scene,
    private val parser: XPathParser,
) {
    val typeAliasRegistry = TypeAliasRegistry()
    private var parsed: Boolean = false

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

}
