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

package com.feysh.corax.config.community.checkers

import com.feysh.corax.config.api.*
import com.feysh.corax.config.api.baseimpl.*
import com.feysh.corax.config.community.XxeChecker
import com.feysh.corax.config.general.checkers.analysis.LibVersionProvider
import com.feysh.corax.config.general.checkers.internetControl
import com.feysh.corax.config.general.checkers.localControl

@Suppress("ClassName", "unused", "HttpUrlsUsage")
object `external-xxe-attacks` : AIAnalysisUnit() {


    private const val FEATURE_DISALLOW_DTD = "http://apache.org/xml/features/disallow-doctype-decl"
    private const val FEATURE_SECURE_PROCESSING = "http://javax.xml.XMLConstants/feature/secure-processing"

    //These two need to be set together to work
    private const val FEATURE_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities"
    private const val FEATURE_EXTERNAL_ENTITIES = "http://xml.org/sax/features/external-parameter-entities"


    context (AIAnalysisApi)
    override suspend fun config() {
        if (LibVersionProvider.isEnable("{\"@active:condition:version\":\"risk-org.apache.poi-ooxml-CVE-2019-12415\"}")) { // "poi-ooxml: 4.1.1"
            listOf(
                matchSimpleSig("org.apache.poi.xssf.extractor.XSSFExportToXml: * exportToXML(OutputStream os, String encoding, boolean validate)") to 2,
                matchSimpleSig("org.apache.poi.xssf.extractor.XSSFExportToXml: * exportToXML(OutputStream os, boolean validate)") to 1,
            ).forEach { (sig, validateParamIndex) ->
                method(sig).modelNoArg {
                    val validate = parameter(validateParamIndex) ?: return@modelNoArg
                    check(`this`.taint.containsAll(taintOf(internetControl)) and validate.getBoolean(), XxeChecker.XxeRemote) {
                        args["type"] = "XSSFExportToXml(CVE-2019-12415)"
                    }
                    check(`this`.taint.containsAll(taintOf(localControl)) and validate.getBoolean(), XxeChecker.XxeLocal) {
                        args["type"] = "XSSFExportToXml(CVE-2019-12415)"
                    }
                }
            }
        }
    }
}