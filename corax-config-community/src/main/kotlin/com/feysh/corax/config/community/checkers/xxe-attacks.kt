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

package com.feysh.corax.config.community.checkers

import com.feysh.corax.config.api.*
import com.feysh.corax.config.api.baseimpl.QualifiedRefType
import com.feysh.corax.config.api.baseimpl.RawSignatureMatch
import com.feysh.corax.config.api.baseimpl.matchParameters
import com.feysh.corax.config.api.baseimpl.method
import com.feysh.corax.config.community.XxeChecker
import com.feysh.corax.config.general.checkers.analysis.LibVersionProvider
import com.feysh.corax.config.general.checkers.internetControl
import com.feysh.corax.config.general.checkers.localControl
import com.feysh.corax.config.general.utils.isStringType
abstract class MethodAccess


// TODO no tests and no safe XxeSanitizer

sealed class XmlClass(private val clazz: List<IClassMatch>) {
    constructor(m: IClassMatch) : this(listOf(m))

    fun method(vararg methodNames: String): List<RawSignatureMatch> = clazz.map { it.method(*methodNames) }
}


sealed class XmlParserCall : MethodAccess() {
    open val ext: String? = null
    abstract val method: List<IMethodMatch>
    abstract val sink: ISootMethodDecl.CheckBuilder<Any>.() -> ILocalT<Any>?
    open val remoteCheckType: CheckType = XxeChecker.XxeRemote
    open val localCheckType: CheckType = XxeChecker.XxeLocal
}

object DocumentBuilder : XmlClass(QualifiedRefType("javax.xml.parsers", "DocumentBuilder")) {
    object Parse : XmlParserCall() {
        override val method: List<IMethodMatch> = method("parse").map { it.matchParameters("*", "**") }
        override val sink: ISootMethodDecl.CheckBuilder<Any>.() -> ILocalT<Any> = { p0 }
    }
}

object DocumentHelper : XmlClass(QualifiedRefType("org.dom4j", "DocumentHelper")) {
    object Call : XmlParserCall() {
        override val method: List<IMethodMatch> = method("parseText").map { it.matchParameters("*", "**") }
        override val sink: ISootMethodDecl.CheckBuilder<Any>.() -> ILocalT<Any> = { p0 }
    }
}


object XSSFWorkbook : XmlClass(QualifiedRefType("org.apache.poi.xssf.usermodel", "XSSFWorkbook")) {
    object Call : XmlParserCall() {
        override val ext: String get() = "@active:condition:version:risk-org.apache.poi-ooxml"
        override val method: List<IMethodMatch> = method("<init>").map { it.matchParameters("*", "**") }
        override val sink: ISootMethodDecl.CheckBuilder<Any>.() -> ILocalT<Any> = { p0 }
    }
}

object StreamingReader : XmlClass(QualifiedRefType("com.monitorjbl.xlsx", "StreamingReader\$Builder")) {
    object Call : XmlParserCall() {
        override val method: List<IMethodMatch> = method("open").map { it.matchParameters("*", "**") }
        override val sink: ISootMethodDecl.CheckBuilder<Any>.() -> ILocalT<Any> = { p0 }
    }
}


object XmlInputFactory : XmlClass(QualifiedRefType("javax.xml.stream", "XMLInputFactory")) {
    object StreamReader : XmlParserCall() {
        override val method = method("createXMLStreamReader")
        override val sink: ISootMethodDecl.CheckBuilder<Any>.() -> ILocalT<Any> = {
            if (p0.type.isStringType) p1 else p0
        }
    }

    object EventReader : XmlParserCall() {
        override val method = method("createXMLEventReader")
        override val sink: ISootMethodDecl.CheckBuilder<Any>.() -> ILocalT<Any> = {
            if (p0.type.isStringType) p1 else p0
        }
    }
}


object SaxBuilder : XmlClass(
    listOf(
        QualifiedRefType(`package` = listOf("org.jdom2.input", "org.jdom.input"), "SAXBuilder"),
    )
) {
    object Parse : XmlParserCall() {
        override val method = method("build").map { it.matchParameters("*", "**") }
        override val sink: ISootMethodDecl.CheckBuilder<Any>.() -> ILocalT<Any> = { p0 }
    }
}

object SaxParser : XmlClass(QualifiedRefType("javax.xml.parsers", "SAXParser")) {
    object Parse : XmlParserCall() {
        override val method = method("parse").map { it.matchParameters("*", "**") }
        override val sink: ISootMethodDecl.CheckBuilder<Any>.() -> ILocalT<Any> = { p0 }
    }
}

object Digester : XmlClass(QualifiedRefType("org.apache.commons.digester3", "Digester")) {
    object Parse : XmlParserCall() {
        override val method = method("parse").map { it.matchParameters("*", "**") }
        override val sink: ISootMethodDecl.CheckBuilder<Any>.() -> ILocalT<Any> = { p0 }
    }
}


object SaxReader : XmlClass(QualifiedRefType("org.dom4j.io", "SAXReader")) {
    object Read : XmlParserCall() {
        override val method = method("read").map { it.matchParameters("*", "**") }
        override val sink: ISootMethodDecl.CheckBuilder<Any>.() -> ILocalT<Any> = { p0 }
    }
}

object XmlReader : XmlClass(QualifiedRefType("org.xml.sax", "XMLReader")) {
    object Parse : XmlParserCall() {
        override val method = method("parse").map { it.matchParameters("*", "**") }
        override val sink: ISootMethodDecl.CheckBuilder<Any>.() -> ILocalT<Any> = { p0 }
    }
}

object Transformer : XmlClass(QualifiedRefType("javax.xml.transform", "Transformer")) {
    object Transform : XmlParserCall() {
        override val method = method("transform").map { it.matchParameters("*", "**") }
        override val sink: ISootMethodDecl.CheckBuilder<Any>.() -> ILocalT<Any> = { p0 }
    }
}

object TransformerFactory : XmlClass(
    listOf(
        QualifiedRefType("javax.xml.transform", "TransformerFactory"),
        QualifiedRefType("javax.xml.transform.sax", "SAXTransformerFactory")
    )
) {
    object Source : XmlParserCall() {
        override val method = method("newTransformer").map { it.matchParameters("*", "**") }
        override val sink: ISootMethodDecl.CheckBuilder<Any>.() -> ILocalT<Any> = { p0 }
    }
}

object SaxTransformerFactory : XmlClass(QualifiedRefType("javax.xml.transform.sax", "SAXTransformerFactory")) {
    object NewXmlFilter : XmlParserCall() {
        override val method = method("newXMLFilter").map { it.matchParameters("*", "**") }
        override val sink: ISootMethodDecl.CheckBuilder<Any>.() -> ILocalT<Any> = { p0 }
    }
}

object SchemaFactory : XmlClass(QualifiedRefType("javax.xml.validation", "SchemaFactory")) {
    object NewSchema : XmlParserCall() {
        override val method = method("newSchema").map { it.matchParameters("*", "**") }
        override val sink: ISootMethodDecl.CheckBuilder<Any>.() -> ILocalT<Any> = { p0 }
    }
}

object XmlUnmarshaller : XmlClass(QualifiedRefType("javax.xml.bind", "Unmarshaller")) {
    object Unmarshal : XmlParserCall() {
        override val method = method("unmarshal").map { it.matchParameters("*", "**") }
        override val sink: ISootMethodDecl.CheckBuilder<Any>.() -> ILocalT<Any> = { p0 }
    }
}

object XPathExpression : XmlClass(QualifiedRefType("javax.xml.xpath", "XPathExpression")) {
    object Evaluate : XmlParserCall() {
        override val method = method("evaluate").map { it.matchParameters("*", "**") }
        override val sink: ISootMethodDecl.CheckBuilder<Any>.() -> ILocalT<Any> = { p0 }
    }
}

object SimpleXmlPersister : XmlClass(QualifiedRefType("org.simpleframework.xml.core", "Persister")) {
    object Call : XmlParserCall() {
        override val method = method("validate", "read").map { it.matchParameters("*", "*", "**") }
        override val sink: ISootMethodDecl.CheckBuilder<Any>.() -> ILocalT<Any> = { p1 }
    }
}

object SimpleXmlProvider : XmlClass(
    listOf(
        QualifiedRefType("org.simpleframework.xml.stream", "DocumentProvider"),
        QualifiedRefType("org.simpleframework.xml.stream", "StreamProvider")
    )
) {
    object Call : XmlParserCall() {
        override val method = method("provide").map { it.matchParameters("*", "**") }
        override val sink: ISootMethodDecl.CheckBuilder<Any>.() -> ILocalT<Any> = { p0 }
    }
}

object SimpleXmlNodeBuilder : XmlClass(
    listOf(
        QualifiedRefType("org.simpleframework.xml.stream", "NodeBuilder"),
    )
) {
    object Call : XmlParserCall() {
        override val method = method("read").map { it.matchParameters("*", "**") }
        override val sink: ISootMethodDecl.CheckBuilder<Any>.() -> ILocalT<Any> = { p0 }
    }
}

object SimpleXmlFormatter : XmlClass(
    listOf(
        QualifiedRefType("org.simpleframework.xml.stream", "Formatter"),
    )
) {
    object Call : XmlParserCall() {
        override val method = method("format").map { it.matchParameters("*", "**") }
        override val sink: ISootMethodDecl.CheckBuilder<Any>.() -> ILocalT<Any> = { p0 }
    }
}


@Suppress("ClassName", "unused", "HttpUrlsUsage")
object `xxe-attacks` : AIAnalysisUnit() {


    private const val FEATURE_DISALLOW_DTD = "http://apache.org/xml/features/disallow-doctype-decl"
    private const val FEATURE_SECURE_PROCESSING = "http://javax.xml.XMLConstants/feature/secure-processing"

    //These two need to be set together to work
    private const val FEATURE_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities"
    private const val FEATURE_EXTERNAL_ENTITIES = "http://xml.org/sax/features/external-parameter-entities"


    context (AIAnalysisApi)
    private fun XmlParserCall.apply() {
        val ext = ext
        if (ext != null && !LibVersionProvider.isEnable(ext)) {
            return
        }
        for (mc in method) {
            val sootDecls = method(mc).sootDecl
            for (methodDecl in sootDecls) {
                methodDecl.modelNoArg {
                    val access = sink() ?: return@modelNoArg
                    check(access.taint.containsAll(taintOf(internetControl)), remoteCheckType) {
                        args["type"] = this@apply.javaClass.simpleName
                    }
                    check(access.taint.containsAll(taintOf(localControl)), localCheckType) {
                        args["type"] = this@apply.javaClass.simpleName
                    }
                }
            }
        }
    }

    context (AIAnalysisApi)
    override suspend fun config() {
        DocumentBuilder.Parse.apply()
        XmlInputFactory.StreamReader.apply()
        XmlInputFactory.EventReader.apply()
        SaxBuilder.Parse.apply()
        SaxParser.Parse.apply()
        Digester.Parse.apply()
        SaxReader.Read.apply()
        XmlReader.Parse.apply()
        Transformer.Transform.apply()
        TransformerFactory.Source.apply()
        SaxTransformerFactory.NewXmlFilter.apply()
        SchemaFactory.NewSchema.apply()
        XmlUnmarshaller.Unmarshal.apply()
        XPathExpression.Evaluate.apply()
        SimpleXmlPersister.Call.apply()
        SimpleXmlProvider.Call.apply()
        SimpleXmlNodeBuilder.Call.apply()
        SimpleXmlFormatter.Call.apply()
        DocumentHelper.Call.apply()
        XSSFWorkbook.Call.apply()
        StreamingReader.Call.apply()
    }
}