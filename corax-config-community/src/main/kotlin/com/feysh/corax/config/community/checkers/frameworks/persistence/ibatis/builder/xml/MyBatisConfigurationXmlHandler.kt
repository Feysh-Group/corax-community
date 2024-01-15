package com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.builder.xml

import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.type.TypeAliasRegistry
import com.feysh.corax.config.community.checkers.frameworks.xml.BasedXmlHandler
import mu.KotlinLogging
import soot.Scene
import java.nio.file.Path
import kotlin.io.path.inputStream

data class MybatisConfiguration(
    val resource: Path,
    val typeAliasRegistry: TypeAliasRegistry
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
                MybatisConfiguration(resource, configBuilder.typeAliasRegistry)
            } catch (e: Exception) {
                logger.warn(e) { e.message }
                null
            }
        }
    }

}
