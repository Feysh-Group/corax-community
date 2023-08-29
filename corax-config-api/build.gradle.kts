val apiInnerJar: FileTree by rootProject.extra

dependencies {
    api(files(apiInnerJar))
}
