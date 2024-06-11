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

import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.fs.internal.DefaultInputFile
import org.sonar.api.batch.fs.internal.FileMetadata
import org.sonar.api.batch.fs.internal.Metadata
import org.sonar.api.batch.fs.internal.TestInputFileBuilder
import org.sonarsource.analyzer.commons.xml.XmlFile
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.io.path.reader

object SonarFactory {

    @Throws(IOException::class)
    fun createInputFile(moduleBaseDir: Path, filename: String, charset: Charset): DefaultInputFile {
        val inputFile: DefaultInputFile = TestInputFileBuilder.create("modulekey", filename)
            .setModuleBaseDir(moduleBaseDir)
            .setType(InputFile.Type.MAIN)
            .setLanguage("xml")
            .setCharset(charset)
            .build()
        val metadata: Metadata = FileMetadata { s -> }.readMetadata(
            FileInputStream(inputFile.file()),
            inputFile.charset(),
            inputFile.absolutePath()
        )
        return inputFile.setMetadata(metadata)
    }


    @Throws(IOException::class)
    fun readPositionalXML(file: Path, charset: Charset = StandardCharsets.UTF_8): XmlFile {
        return XmlFile.create(file.reader(charset = charset).use { it.readText() })
    }

    @Throws(IOException::class)
    fun readPositionalXML(xml: String): XmlFile {
        return XmlFile.create(xml)
    }

    @Throws(IOException::class)
    fun readPositionalXML(`is`: InputStream, charset: Charset = StandardCharsets.UTF_8): XmlFile {
        return XmlFile.create(`is`.reader(charset = charset).use { it.readText() })
    }
}