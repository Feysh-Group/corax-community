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
import com.feysh.corax.config.community.*
import com.feysh.corax.config.general.checkers.*
import com.feysh.corax.config.general.model.ConfigCenter
import com.feysh.corax.config.general.model.taint.TaintModelingConfig
import kotlinx.serialization.*


@Suppress("ClassName")
object `taint-checker` : AIAnalysisUnit() {
    @Serializable
    data class CustomSinkDataForCheck(
        val checkTaintTypes: Set<ITaintType>,
        val taintTypeExcludes:  Set<ITaintType> = emptySet(),
        val reportType: CheckType,
        val msgArgs: Map<String, String> = emptyMap(),
        val enable: Boolean = true,
    )

    @Serializable
    class Options : SAOptions {
        val kind2Checker: List<Pair<String, CustomSinkDataForCheck>> = listOf(
            "log4j-injection" to CustomSinkDataForCheck(control, reportType = Log4jChecker.Log4jInjection),

            "path-injection" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_PATH_TRAVERSAL, reportType = PathTraversalChecker.PathTraversalIn),
            "path-injection" to CustomSinkDataForCheck(control + GeneralTaintTypes.InternetData + GeneralTaintTypes.UNLIMITED_FILE_EXTENSION, reportType = UnrestrictedFileUploadChecker.UnrestrictedFileUpload),
            "path-injection" to CustomSinkDataForCheck(control + GeneralTaintTypes.ZIP_ENTRY_NAME, reportType = PathTraversalChecker.ZipSlip),

            "template-injection" to CustomSinkDataForCheck(control, reportType = TemplateIChecker.TemplateInjection),
            "request-forgery" to CustomSinkDataForCheck(control, reportType = SsrfChecker.RequestForgery),
            "ldap-injection" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_LDAP_INJECT, reportType = LdapiChecker.LdapInjection),
            "url-redirection" to CustomSinkDataForCheck(control, reportType = OpenRedirectChecker.UnvalidatedRedirect),
            "response-splitting" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_CRLF, reportType = HttpRespSplitChecker.HttpResponseSplitting),
            "xpath-injection" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_XPATH_INJECT, reportType = XpathiChecker.XpathInjection),
            "command-injection" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_COMMAND_INJECT, reportType = CmdiChecker.CommandInjection),

            "deserialization" to CustomSinkDataForCheck(control, reportType = DeserializationChecker.UnrestrictedObjectDeserialization),
            "xss-injection" to CustomSinkDataForCheck(control+ GeneralTaintTypes.CONTAINS_XSS_INJECT, reportType = XssChecker.XssInjection, msgArgs = mapOf("type" to "XSS Sink")),
            "html-injection" to CustomSinkDataForCheck(control+ GeneralTaintTypes.CONTAINS_XSS_INJECT, reportType = XssChecker.XssInjection, msgArgs = mapOf("type" to "Html Sink")),
            "xss-injection-jsp" to CustomSinkDataForCheck(control+ GeneralTaintTypes.CONTAINS_XSS_INJECT, reportType = XssChecker.XssInjection, msgArgs = mapOf("type" to "JSP Sink")),
            "xss-servlet" to CustomSinkDataForCheck(control+ GeneralTaintTypes.CONTAINS_XSS_INJECT, reportType = XssChecker.XssInjection, msgArgs = mapOf("type" to "Servlet Sink")),

            "trust-boundary-violation" to CustomSinkDataForCheck(control, reportType = TrustBoundaryChecker.TrustBoundaryViolation),

            "groovy-injection" to CustomSinkDataForCheck(control, reportType = CodeInjectionChecker.GroovyShell),
            "spel-injection" to CustomSinkDataForCheck(control, reportType = CodeInjectionChecker.SpringElInjection),
            "script-engine" to CustomSinkDataForCheck(control, reportType = CodeInjectionChecker.ScriptEngineInjection),

            "sql-injection" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_SQL_INJECT, reportType = SqliChecker.SqlInjection, msgArgs = mapOf("type" to "SQL Sink")),
            "sql-injection-aws" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_SQL_INJECT, reportType = SqliChecker.SqlInjection, msgArgs = mapOf("type" to "AmazonAws ")),
            "sql-injection-hibernate" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_SQL_INJECT, reportType = SqliChecker.SqlInjection, msgArgs = mapOf("type" to "Hibernate")),
            "sql-injection-android" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_SQL_INJECT, reportType = SqliChecker.SqlInjection, msgArgs = mapOf("type" to "Android")),
            "sql-injection-jdo" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_SQL_INJECT, reportType = SqliChecker.SqlInjection, msgArgs = mapOf("type" to "JDO")),
            "sql-injection-jpa" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_SQL_INJECT, reportType = SqliChecker.SqlInjection, msgArgs = mapOf("type" to "Hibernate")),
            "sql-injection-scala-anorm" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_SQL_INJECT, reportType = SqliChecker.SqlInjection, msgArgs = mapOf("type" to "Scala Anorm")),
            "sql-injection-scala-slick" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_SQL_INJECT, reportType = SqliChecker.SqlInjection, msgArgs = mapOf("type" to "Scala Slick")),
            "sql-injection-spring" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_SQL_INJECT, reportType = SqliChecker.SqlInjection, msgArgs =  mapOf("type" to "Spring")),
            "sql-injection-turbine" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_SQL_INJECT, reportType = SqliChecker.SqlInjection, msgArgs = mapOf("type" to "Turbine")),
            "sql-injection-vertx" to CustomSinkDataForCheck(control + GeneralTaintTypes.CONTAINS_SQL_INJECT, reportType = SqliChecker.SqlInjection, msgArgs =  mapOf("type" to "Vert.x Sql Client")),
            "xxe" to CustomSinkDataForCheck(internetControl, reportType =  XxeChecker.XxeRemote),
            "xxe" to CustomSinkDataForCheck(localControl, reportType =  XxeChecker.XxeLocal),
            "xxe" to CustomSinkDataForCheck(fileIoControl, reportType =  XxeChecker.XxeLocal),
        )
    }

    private var option: Options = Options()

    context(AIAnalysisApi)
    override suspend fun config() {
        val kindsExists = ConfigCenter.taintRulesManager.sinks.allKinds
        val unusedKinds = kindsExists - option.kind2Checker.mapTo(mutableSetOf()) { it.first }
        if (unusedKinds.isNotEmpty()) {
            logger.warn { "The following types of rules have not be used: $unusedKinds" }
        }
        for ((kind, sink) in option.kind2Checker) {
            if (!sink.enable) {
                continue
            }

            TaintModelingConfig.applyJsonExtSinks(kind, ConfigCenter.taintRulesManager.sinks,
                TaintModelingConfig.SimpleApplySink(sink.checkTaintTypes, sink.taintTypeExcludes, sink.reportType) {
                    args.putAll(sink.msgArgs)
                }
            )

        }
    }
}