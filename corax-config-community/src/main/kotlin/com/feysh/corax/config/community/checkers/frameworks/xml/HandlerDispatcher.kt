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
