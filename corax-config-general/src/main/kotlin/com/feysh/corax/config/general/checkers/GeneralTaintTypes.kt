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


val injectionTypes: MutableSet<GeneralTaintTypes> = mutableSetOf(
    GeneralTaintTypes.CONTAINS_CRLF,
    GeneralTaintTypes.CONTAINS_PATH_TRAVERSAL,
    GeneralTaintTypes.CONTAINS_SQL_INJECT,
    GeneralTaintTypes.CONTAINS_XSS_INJECT,
    GeneralTaintTypes.CONTAINS_XPATH_INJECT,
    GeneralTaintTypes.CONTAINS_COMMAND_INJECT,
    GeneralTaintTypes.CONTAINS_REDIRECTION_INJECT,
    GeneralTaintTypes.CONTAINS_OGNL_INJECT
)

val internetSource: MutableSet<GeneralTaintTypes> =
    (injectionTypes + GeneralTaintTypes.InternetData + GeneralTaintTypes.ControlData).toMutableSet()
val fileIoSource: MutableSet<GeneralTaintTypes> =
    (injectionTypes + GeneralTaintTypes.FileStreamData + GeneralTaintTypes.ControlData).toMutableSet()
val userInputSource: MutableSet<GeneralTaintTypes> =
    (injectionTypes + GeneralTaintTypes.UserInputData + GeneralTaintTypes.ControlData).toMutableSet()

val internetControl: MutableSet<GeneralTaintTypes> = mutableSetOf(GeneralTaintTypes.InternetData, GeneralTaintTypes.ControlData)
val localControl: MutableSet<GeneralTaintTypes> = mutableSetOf(GeneralTaintTypes.UserInputData, GeneralTaintTypes.ControlData)
val fileIoControl: MutableSet<GeneralTaintTypes> = mutableSetOf(GeneralTaintTypes.FileStreamData, GeneralTaintTypes.ControlData)
val control: MutableSet<GeneralTaintTypes> = mutableSetOf(GeneralTaintTypes.ControlData)