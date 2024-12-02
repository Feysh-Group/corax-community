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

import com.feysh.corax.config.api.ISourceFileCheckPoint
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.fs.internal.*
import org.sonarsource.analyzer.commons.xml.XmlFile
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.math.BigInteger
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import kotlin.io.path.absolute
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.reader

val md5: MessageDigest get() = MessageDigest.getInstance("MD5")
fun String.md5() :String = BigInteger(1, md5.digest(toByteArray())).toString(16).padStart(32, '0')

object SNFactory {
    var batchId: AtomicInteger = AtomicInteger()

    @Throws(IOException::class)
    fun createInputFile(p: ISourceFileCheckPoint, language: String? = null, charset: Charset = StandardCharsets.UTF_8, moduleKey: String = "dummy", type: InputFile.Type = InputFile.Type.MAIN): DefaultInputFile? {
        BufferedInputStream(p.path.inputStream()).use { stream ->
            if (stream.isBinaryXml()) {
                return null
            }
        }
        val relativePath = p.relativePath

        val indexedFile = DefaultIndexedFile(
            p.path.absolute(),
            moduleKey,
            relativePath.relativePath,
            relativePath.relativePath,
            type,
            language,
            batchId.incrementAndGet(),
            SensorStrategy(),
            null
        )
        val metadataGenerator = Consumer<DefaultInputFile> {
            it.setMetadata(it.path().inputStream().use { stream ->
                FileMetadata { }.readMetadata(stream, charset, relativePath.relativePath)
            })
        }
        val inputFile = object : DefaultInputFile(indexedFile, metadataGenerator, null, {}) {
            override fun charset(): Charset {
                return charset
            }
        }
        inputFile.setStatus(InputFile.Status.CHANGED)
        inputFile.setCharset(charset)
        inputFile.setPublished(true)
        return inputFile
    }


    @Throws(IOException::class)
    fun readPositionalXML(file: Path, charset: Charset = StandardCharsets.UTF_8): XmlFile? {
        BufferedInputStream(file.inputStream()).use { stream ->
            if (stream.isBinaryXml()) {
                // TODO
                return null
            }
           return XmlFile.create(stream.reader(charset = charset).readText())
        }
    }

    @Throws(IOException::class)
    fun readPositionalXML(xml: String): XmlFile {
        return XmlFile.create(xml)
    }

    @Throws(IOException::class)
    fun readPositionalXML(xml: InputFile): XmlFile {
        return XmlFile.create(xml)
    }

    @Throws(IOException::class)
    fun readPositionalXML(`is`: BufferedInputStream, charset: Charset = StandardCharsets.UTF_8): XmlFile? {
        if (`is`.isBinaryXml()) {
            return null
        }
        return XmlFile.create(`is`.reader(charset = charset).use { it.readText() })
    }
}