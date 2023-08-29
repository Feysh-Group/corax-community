package com.feysh.corax.config.community.soot

import com.feysh.corax.config.api.ISootInitializeHandler
import soot.options.Options
import java.util.*

@Suppress("unused", "DuplicatedCode")
object CustomSootConfiguration: ISootInitializeHandler {
    override fun configure(options: Options) {
        // explicitly include packages for shorter runtime:
        val excludeList: MutableList<String> = LinkedList()
        excludeList.add("java.*")
        excludeList.add("javax.*")
        excludeList.add("jdk.*")

        excludeList.add("com.apple.*")
        excludeList.add("apple.awt.*")
        excludeList.add("org.w3c.*")
        excludeList.add("org.xml.*")
        excludeList.add("com.ibm.*")
        excludeList.add("com.sun.*")
        excludeList.add("sun.*")

        // exclude classes of android.* will cause layout class cannot be
        // loaded for layout file based callback analysis.

        // 2020-07-26 (SA): added back the exclusion, because removing it breaks
        // calls to Android SDK stubs. We need a proper test case for the layout
        // file issue and then see how to deal with it.
        excludeList.add("android.*")
        excludeList.add("androidx.*")

        // logger
        excludeList.add("org.slf4j.*")
        excludeList.add("org.apache.log4j.*")
        excludeList.add("org.apache.logging.*")
        excludeList.add("java.util.logging.*")
        excludeList.add("ch.qos.logback.*")
        excludeList.add("com.mysql.*")

//        excludeList.add("org.apache.*")
        excludeList.add("org.eclipse.*")
        excludeList.add("soot.*")
        options.set_exclude(excludeList)
        options.set_no_bodies_for_excluded(true)
    }
}