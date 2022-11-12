// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classPath(Dependencies.Libraries.GradlePlugIn)
        classPath(Dependencies.Libraries.Hilt)
        classPath(Dependencies.Libraries.Firebase)
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}


task("clean", Delete::class) {
    delete = setOf(rootProject.buildDir)
}