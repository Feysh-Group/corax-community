package com.feysh.corax.config.community.checkers.properties

import com.feysh.corax.config.api.CheckType
import com.feysh.corax.config.api.PreAnalysisApi
import com.feysh.corax.config.api.PreAnalysisUnit
import com.feysh.corax.config.api.SAOptions
import com.feysh.corax.config.api.report.Region
import com.feysh.corax.config.community.CookiePersistentChecker
import com.feysh.corax.config.community.HttponlyCookieChecker
import com.feysh.corax.config.community.InfoLeakChecker
import com.feysh.corax.config.community.InsecureCookieChecker
import com.feysh.corax.config.community.OverlyBroadCookieAttributeChecker
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import java.util.*
import java.util.regex.Pattern
import kotlin.io.path.inputStream

fun filenamePredicate(fileNameMatches: Set<String>): Lazy<(String) -> Boolean> = lazy {
    val compiled = fileNameMatches.map {
        when {
            it.startsWith("regex:") -> it.substringAfter("regex:").toRegex()
            else -> it
        }
    }
    return@lazy { filename: String ->
        compiled.any {
            when (it) {
                is Regex -> it.matches(filename)
                is String -> filename == it
                else -> error("unsupported pattern: $it")
            }
        }
    }
}

@Suppress("ClassName")
object `properties-check` : PreAnalysisUnit() {

    private val logger = KotlinLogging.logger {}

    @Serializable
    data class PropertiesCheckConfig(
        val keyRegex: String,
        val valueRegex: String,
        val checkType: CheckType,
        var fileNames: Set<String>
    ) {
        val filenamePredicate: (String) -> Boolean by filenamePredicate(fileNames)
    }

    @Serializable
    class Options : SAOptions {
        val propertiesCheckConfigs: List<PropertiesCheckConfig> = listOf(
            PropertiesCheckConfig(
                keyRegex = "(?i)log4j\\.rootLogger|rootLogger\\.level",
                valueRegex = ".*(?i)(debug|trace|all).*",
                checkType = InfoLeakChecker.DebugLogFileLeak,
                fileNames = setOf("regex:.*log4j.*\\.properties", "regex:.*log4j2.*\\.properties")
            ),
            PropertiesCheckConfig(
                keyRegex = ".*(?i)session\\.cookie\\.secure",
                valueRegex = "(?i)false|off",
                checkType = InsecureCookieChecker.PropertiesInsecureCookie,
                fileNames = setOf("application.properties")
            ),
            PropertiesCheckConfig(
                keyRegex = ".*(?i)session(\\.cookie)?\\.persistent",
                valueRegex = "(?i)true|on",
                checkType = CookiePersistentChecker.PropertiesCookiePersistent,
                fileNames = setOf("application.properties")
            ),
            PropertiesCheckConfig(
                keyRegex = ".*(?i)session\\.cookie\\.domain" ,
                valueRegex = "^\\..*",
                checkType = OverlyBroadCookieAttributeChecker.PropertiesOverlyBroadDomain,
                fileNames = setOf("application.properties")
            ),
            PropertiesCheckConfig(
                keyRegex = ".*(?i)session\\.cookie\\.path",
                valueRegex = "^/$",
                checkType = OverlyBroadCookieAttributeChecker.PropertiesOverlyBroadPath,
                fileNames = setOf("application.properties")
            ),
            PropertiesCheckConfig(
                keyRegex = ".*(?i)session\\.cookie\\.http-only",
                valueRegex = "(?i)false|off",
                checkType = HttponlyCookieChecker.PropertiesHttpOnlyCookie,
                fileNames = setOf("application.properties")
            )
        )

        fun checkExtension() {
            propertiesCheckConfigs.forEach {
                it.fileNames.forEach { fileName ->
                    if (!fileName.endsWith(".properties")) {
                        logger.error { "file should end with '.properties', but '$fileName' does not." }
                    }
                }
            }
        }
    }

    private var option: Options = Options()

    context(PreAnalysisApi)
    override suspend fun config() {
        option.checkExtension()
        option.propertiesCheckConfigs.forEach { propertiesCheckConfig ->
            val keyPattern = Pattern.compile(propertiesCheckConfig.keyRegex)
            val valuePattern = Pattern.compile(propertiesCheckConfig.valueRegex)
            atAnySourceFile(extension = "properties", config = { skipFilesInArchive = true }) {
                if (!propertiesCheckConfig.filenamePredicate(filename)) {
                    return@atAnySourceFile
                }
                val properties = Properties()
                path.inputStream().use { properties.load(it) }
                properties.forEach propertiesForEach@{ (key, value) ->
                    if (key !is String || value !is String) return@propertiesForEach
                    if (keyPattern.matcher(key).matches() && valuePattern.matcher(value).matches()) {
                        report(propertiesCheckConfig.checkType, Region(1, 1, 1, 1)) {
                            args["node"] = key
                        }
                    }
                }
            }
        }
    }
}

