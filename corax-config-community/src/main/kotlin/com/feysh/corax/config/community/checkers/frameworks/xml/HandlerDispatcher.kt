package com.feysh.corax.config.community.checkers.frameworks.xml

import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.builder.xml.MyBatisConfigurationXmlHandler
import com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.mybatis.MyBatisMapperXmlHandler
import org.xml.sax.ext.DefaultHandler2


open class HandlerDispatcher : DefaultHandler2() {
    open var handlerName: String = ""
    open val myBatisConfigurationXmlHandler: MyBatisConfigurationXmlHandler = MyBatisConfigurationXmlHandler()
    open val mybatisHandler: MyBatisMapperXmlHandler = MyBatisMapperXmlHandler()

    open var handler: BasedXmlHandler? = null

    override fun startDTD(name: String?, publicId: String?, systemId: String?) {
        if (myBatisConfigurationXmlHandler.detect(name, publicId, systemId)) {
            this.handlerName = myBatisConfigurationXmlHandler.name
            this.handler = myBatisConfigurationXmlHandler
            return
        }
        if (mybatisHandler.detect(name, publicId, systemId)) {
            this.handlerName = mybatisHandler.name
            this.handler = mybatisHandler
            return
        }
    }

}
