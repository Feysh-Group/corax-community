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

import mu.KotlinLogging
import org.apache.maven.model.InputSource
import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3ReaderEx
import java.nio.file.Path
import kotlin.io.path.inputStream

class MavenParser(val path: Path) {
    private val logger = KotlinLogging.logger {}
    fun parse(): Model? {
        path.inputStream().use {
            try {
                return MavenXpp3ReaderEx().read(it, InputSource())
            } catch (e: Exception) {
                logger.error { "There is an error when parsing the file: $path, e: ${e.message}" }
                logger.debug(e) { "There is an error when parsing the file: $path" }
            }
        }
        return null
    }
}