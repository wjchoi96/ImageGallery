package com.gallery.kakaogallery.data

import java.io.File

object UnitTestUtil {

    fun readResource(fileName: String): String {
        return File("src/test/java/com/gallery/kakaogallery/data/resources/$fileName").readText()
    }
}