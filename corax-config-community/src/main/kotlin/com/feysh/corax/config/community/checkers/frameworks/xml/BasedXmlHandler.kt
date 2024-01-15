package com.feysh.corax.config.community.checkers.frameworks.xml

import org.xml.sax.helpers.DefaultHandler

abstract class BasedXmlHandler : DefaultHandler() {
    abstract val name: String

    abstract fun detect(name: String?, publicId: String?, systemId: String?): Boolean
}
