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

@file:Suppress("unused")

package com.feysh.corax.config.general.checkers

import com.feysh.corax.config.api.ITaintType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


// Hint: 自定义配置插件和商业版插件混合使用时, 可以删改定义, 不可删改声明, 否则会出现商业版插件冲突崩溃。注: 单独使用社区版插件可以任意修改
@Serializable
@SerialName("GeneralTaintTypes")
enum class GeneralTaintTypes(val desc: String? = null) : ITaintType {
    ControlData("use to annotate the data content can be controlled"),

    InternetData("use to annotate data returned by an HTTP request"),
    UserInputData("use to annotate all data that is controllable by the user making a request"),
    FileStreamData("use to annotate data returned by an io stream from file system"),

    CONTAINS_CRLF("The data has CRLF characters"),
    CONTAINS_PATH_TRAVERSAL("The data has .. characters"),
    UNLIMITED_FILE_EXTENSION("with unlimited file extension"),
    CONTAINS_SQL_INJECT("The data has SQL inject characters"),
    CONTAINS_XSS_INJECT("The data has XSS inject characters"),
    CONTAINS_XPATH_INJECT("The data can cause XPATH inject"),
    CONTAINS_COMMAND_INJECT("The data can cause command inject"),
    CONTAINS_LDAP_INJECT("The data can cause ldap inject"),
    CONTAINS_OGNL_INJECT("The data can cause ognl inject"),
    CONTAINS_REDIRECTION_INJECT("The data can cause redirection inject"),

    CONTAINS_SENSITIVE_DATA("The data is sensitive"),

    BASE_ENCODE("base16/32/64 data"),
    ZIP_ENTRY_NAME("Zip Entry Name"),

    UNENCRYPTED_DATA("The data is not encrypted yet"),
    ENCRYPTED_DATA("The data is encrypted yet"),

    EXTERNAL_STORAGE("From a location that is not controlled by the application (for example, an SD card that is universally writable)"),

    URL_ENCODED,
    CR_ENCODED, LF_ENCODED, QUOTE_ENCODED, LT_ENCODED,


    HARDWARE_INFO,
    UNIQUE_IDENTIFIER,
    LOCATION_INFORMATION,
    NETWORK_INFORMATION,
    ACCOUNT_INFORMATION,
    EMAIL_INFORMATION,
    FILE_INFORMATION,
    BLUETOOTH_INFORMATION,
    VOIP_INFORMATION,
    DATABASE_INFORMATION,
    PHONE_INFORMATION,

    ;

    override fun toString(): String = name
}


private val normalSource: MutableSet<GeneralTaintTypes> = mutableSetOf(
    GeneralTaintTypes.ControlData,
    GeneralTaintTypes.CONTAINS_CRLF,
    GeneralTaintTypes.CONTAINS_PATH_TRAVERSAL,
    GeneralTaintTypes.CONTAINS_SQL_INJECT,
    GeneralTaintTypes.CONTAINS_XSS_INJECT,
    GeneralTaintTypes.CONTAINS_XPATH_INJECT,
    GeneralTaintTypes.CONTAINS_COMMAND_INJECT,
    GeneralTaintTypes.CONTAINS_REDIRECTION_INJECT,
    GeneralTaintTypes.CONTAINS_OGNL_INJECT,
    GeneralTaintTypes.UNLIMITED_FILE_EXTENSION,
)

val internetSource: MutableSet<GeneralTaintTypes> =
    (normalSource + GeneralTaintTypes.InternetData).toMutableSet()
val fileIoSource: MutableSet<GeneralTaintTypes> =
    (normalSource + GeneralTaintTypes.FileStreamData).toMutableSet()
val userInputSource: MutableSet<GeneralTaintTypes> =
    (normalSource + GeneralTaintTypes.UserInputData).toMutableSet()

val internetControl: MutableSet<GeneralTaintTypes> = mutableSetOf(GeneralTaintTypes.InternetData, GeneralTaintTypes.ControlData)
val localControl: MutableSet<GeneralTaintTypes> = mutableSetOf(GeneralTaintTypes.UserInputData, GeneralTaintTypes.ControlData)
val fileIoControl: MutableSet<GeneralTaintTypes> = mutableSetOf(GeneralTaintTypes.FileStreamData, GeneralTaintTypes.ControlData)
val control: MutableSet<GeneralTaintTypes> = mutableSetOf(GeneralTaintTypes.ControlData)