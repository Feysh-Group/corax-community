package com.feysh.corax.config.community.checkers.frameworks.xml

import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.builder.xml.MyBatisConfigurationXmlHandler
import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.builder.xml.MybatisConfiguration
import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.mybatis.MyBatisMapperXmlHandler
import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.mybatis.MybatisEntry
import com.feysh.corax.config.general.utils.SAX_FACTORY
import mu.KotlinLogging
import org.xml.sax.SAXException
import soot.Scene
import java.nio.file.Path
import javax.xml.parsers.ParserConfigurationException
import kotlin.Exception
import kotlin.String
import kotlin.Throws
import kotlin.io.path.inputStream

data class XmlParser(
    val handlerName: String,
    val contentHandler: BasedXmlHandler,
    val filePath: Path
) {
    fun processMyBatisMapper(): MybatisEntry? {
        when (contentHandler) {
            is MyBatisMapperXmlHandler -> {
                return contentHandler.compute(filePath)
            }
        }
        return null
    }

    fun processMyBatisConfiguration(scene: Scene): MybatisConfiguration? {
        when (contentHandler) {
            is MyBatisConfigurationXmlHandler -> {
                return contentHandler.compute(scene, filePath)
            }
        }
        return null
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        @Throws(ParserConfigurationException::class, SAXException::class)
        fun parseMybatisMapper(filePath: Path): MybatisEntry? {
            return fromFile(HandlerDispatcher(), filePath)?.processMyBatisMapper()
        }

        @Throws(ParserConfigurationException::class, SAXException::class)
        fun parseMybatisMapper(dispatcher: HandlerDispatcher, filePath: Path): MybatisEntry? {
            return fromFile(dispatcher, filePath)?.processMyBatisMapper()
        }

        @Throws(ParserConfigurationException::class, SAXException::class)
        fun parseMybatisConfiguration(scene: Scene, filePath: Path): MybatisConfiguration? {
            return fromFile(HandlerDispatcher(), filePath)?.processMyBatisConfiguration(scene)
        }

        @Throws(ParserConfigurationException::class, SAXException::class)
        fun fromFile(dispatcher: HandlerDispatcher, filePath: Path): XmlParser? {
            filePath.inputStream().use { inputSource ->
                val parser = SAX_FACTORY.newSAXParser()

                // 1. first detect xml handler
                parser.setProperty("http://xml.org/sax/properties/lexical-handler", dispatcher)

                try {
                    parser.parse(inputSource, dispatcher)
                } catch (e: Exception) {
                    logger.warn { "Parse $filePath with a exception: ${e.message}" }
                    return null
                }

                val contentHandler = dispatcher.handler ?: return null
                return XmlParser(dispatcher.handlerName, contentHandler, filePath)
            }
        }
    }
}
