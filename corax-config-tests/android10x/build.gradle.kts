

dependencies {
    // android
    compileOnly(files("dist/platforms/android-33/android.jar"))
    compileOnly("com.google.android:support-v4:r6"){ isTransitive = false }
}
