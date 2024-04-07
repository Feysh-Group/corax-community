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

import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.type.TypeAliasRegistry
import com.feysh.corax.config.community.checkers.frameworks.xml.BasedXmlHandler
import mu.KotlinLogging
import org.apache.ibatis.session.Configuration
import soot.Scene
import java.nio.file.Path
import kotlin.io.path.inputStream

data class MybatisConfiguration(
    val resource: Path,
    val typeAliasRegistry: TypeAliasRegistry,
    val configuration: Configuration
)

open class MyBatisConfigurationXmlHandler : BasedXmlHandler() {

    companion object {
        const val NAME = "MyBatisConfigurationXmlHandler"
        private val logger = KotlinLogging.logger {}
    }

    override val name: String = NAME

    override fun detect(name: String?, publicId: String?, systemId: String?): Boolean {
        if (systemId != null) {
            if (name == "configuration" && (systemId.endsWith("mybatis-3-config.dtd"))) {
                return true
            }
        }

        return false
    }

    fun compute(scene: Scene, filePath: Path): MybatisConfiguration? {
        logger.info("process mybatis configuration file: $filePath")
        return streamToConfiguration(scene, filePath)
    }

    private fun streamToConfiguration(scene: Scene, resource: Path): MybatisConfiguration? {
        return resource.inputStream().use { inputSource ->
            try {
                val configBuilder = XMLConfigBuilder(scene, inputSource)
                configBuilder.parse()
                MybatisConfiguration(resource, configBuilder.typeAliasRegistry, configBuilder.configuration)
            } catch (e: Exception) {
                logger.warn(e) { e.message }
                null
            }
        }
    }

}
