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

package com.feysh.corax.config.community.checkers.deserialize

import com.feysh.corax.config.api.SAOptions
import com.feysh.corax.config.api.baseimpl.RawSignatureMatch
import com.feysh.corax.config.api.PreAnalysisApi
import com.feysh.corax.config.api.PreAnalysisUnit
import com.feysh.corax.config.community.DeserializationChecker
import kotlinx.serialization.Serializable

@Suppress("ClassName")
object `deserialize-insecure-call` : PreAnalysisUnit() {

    @Serializable
    class Options : SAOptions {
        val objectInputStreamReadMethodNames =
            listOf("readObject", "readUnshared", "readArray", "readClassAndObject", "readObjectOrNull")
        val objectInputStream = listOf(
            "java.io.ObjectInputStream",
            "java.io.ObjectInput",
            "java.beans.XMLDecoder",
            "com.esotericsoftware.kryo.Kryo",
            "com.esotericsoftware.kryo5.Kryo",
            // TODO
        )

        val calleeWhitelist = listOf(
            "org.bouncycastle.asn1.ASN1InputStream",
            "org.apache.commons.io.serialization.ValidatingObjectInputStream"
        )
        val containerWhitelist = listOf("java.lang.reflect.InvocationHandler", "java.io.Serializable")
    }

    private var option: Options = Options()

//    private val ANNOTATION_TYPES: List<String> = listOf("Lcom/fasterxml/jackson/annotation/JsonTypeInfo;")
//
//    private val VULNERABLE_USE_NAMES: List<String> = listOf("CLASS", "MINIMAL_CLASS")
//
//    private val OBJECT_MAPPER_CLASSES: List<String> = listOf(
//        "com.fasterxml.jackson.databind.ObjectMapper", "org.codehaus.jackson.map.ObjectMapper"
//    )
//    TODO: JacksonUnsafeDeserialization checker is weak

    context (PreAnalysisApi)
    override suspend fun config() {
        atAnyInvoke {
            if (callee.name !in option.objectInputStreamReadMethodNames) {
                return@atAnyInvoke
            }

            if (callee.declaringClass.name.endsWith("InputStream").not() &&
                option.objectInputStream.none { callee.declaringClass.isInstanceOf(it) == true }
            ) {
                return@atAnyInvoke
            }

            if (option.calleeWhitelist.any { callee.declaringClass.isInstanceOf(it) == true } ||
                option.containerWhitelist.any { container.declaringClass.isInstanceOf(it) == true }
            ) {
                return@atAnyInvoke
            }

            report(DeserializationChecker.ObjectDeserialization)
        }

        atInvoke(RawSignatureMatch("com.fasterxml.jackson.databind.ObjectMapper", "enableDefaultTyping", null, null)) {
            report(DeserializationChecker.JacksonUnsafeDeserialization)
        }
    }
}