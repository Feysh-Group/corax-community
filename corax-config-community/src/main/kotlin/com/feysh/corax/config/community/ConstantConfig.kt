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

package com.feysh.corax.config.community

import com.feysh.corax.config.api.rules.ProcessRule
import com.feysh.corax.config.api.rules.ProcessRulesType

object ConstantConfig {

    val javaExtensions = listOf("java", "kt", "kts", "scala", "groovy", "jsp")

    object NormalProcessRules {
        val NormalIgnoreFile = ProcessRule.cvt<ProcessRule.FileMatch>(
            "(-)path=/out/",
            "(-)path=/output/",
            "(-)path=/tmp/",
            "(-)path=/temp/",
            "(-)path=/log/",
            "(-)path=/logs/",
            "(-)path=/build/",
            "(-)path=/target/",
            "(+)path=/src/",
            "(+)path=/java/",
            "(+)path=/[^/]+_jsp.java$",
            "(-)path=/R\\.java$",
            "(-)path=/\\.git/",
            "(-)path=/\\.idea/",
            "(-)path=/\\.gradle/",
            "(-)path=/\\.mvn",
            "(-)path=/\\.run/",
            "(-)path=/protobuf/",
        )
        val AndroidRJavaSignature = ProcessRule.cvt<ProcessRule.ClassMemberMatch>(
            "(-)class:name=\\.R(\\$.*)?$"
        )
    }
}