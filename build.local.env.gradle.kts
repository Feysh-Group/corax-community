import java.io.File

fun properties(key: String) = project.findProperty(key)?.toString()

kotlin.run {

    val coraxEnginePath = properties("coraxEnginePath")?.let{ File(it) }
    if (coraxEnginePath == null || !coraxEnginePath.exists()) {
        val properties = File("${project.rootDir}${File.separator}gradle-local.properties")
        if (!properties.exists()) {
            properties.writeText("coraxEnginePath=your-corax-jar-path-or-directory\n" +
                    "coraxConfigDependencePaths=")
        }
        error("coraxEnginePath: $coraxEnginePath does not exist. Please modify $properties and " +
                "set property: \"coraxEngine\" to the path of corax-cli_x.x.x.jar." )
    }
    val engineJar by extra {
        if (coraxEnginePath.isDirectory){
            val foundJar = coraxEnginePath.listFiles()?.filter { it.extension == "jar" }
            println("foundJar: \n[${foundJar?.joinToString("\n"){ "\t$it "}}]")
            foundJar?.sortedBy { it.name }?.lastOrNull() ?: error("directory: $coraxEnginePath doesn't contains any corax_cli_x.x.x.jar")
        } else {
            coraxEnginePath
        }
    }
    println("used engine jar: $engineJar")

    val apiInnerJar by extra {
        zipTree(engineJar).matching { include("lib/api/*.jar") }
    }
    println("api jar: ${apiInnerJar.joinToString(", ")}")
    val coraxConfigDependencePaths by extra {
        properties("coraxConfigDependencePaths")
    }
}