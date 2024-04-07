
fun properties(key: String) = project.findProperty(key)?.toString()

kotlin.run {
    val coraxEnginePath = properties("coraxEnginePath")?.let{ File(it) }
    if (coraxEnginePath == null || !coraxEnginePath.exists()) {
        val properties = File("${project.rootDir}${File.separator}gradle-local.properties")
        if (!properties.exists()) {
            properties.writeText("coraxEnginePath=your-corax-jar-path-or-directory\n")
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


    val libCoraxConfigApiJar = zipTree(engineJar).matching { include("/BOOT-INF/lib/corax-config-api*.jar") }.also {
        check(!it.isEmpty) {
            "The file \"$engineJar!/BOOT-INF/lib/corax-config-api*.jar\" does not exist. Please provide the corresponding version of the Coraxjava engine."
        }
    }.singleFile
    println("corax-config-api-*.jar: $libCoraxConfigApiJar")
    val coraxConfigApiJar by extra{ libCoraxConfigApiJar }
}