val coraxConfigApiJar: File by rootProject.extra

dependencies {
    api(files(coraxConfigApiJar))
}
