package com.gallery.kakaogallery.domain.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

@Suppress("NonAsciiCharacters")
internal class GalleryDateConvertUtilTest {

    private val size = 10000
    @Test
    fun `convertToMill는 동시성 문제에 안전하다`() {
        val dateTime = "2017-06-21T15:59:30.000+09:00"
        val millList = mutableListOf<Long?>()
        val runnable = Runnable {
            val mill = GalleryDateConvertUtil.convertToMill(dateTime)
            synchronized(millList){
                millList.add(mill)
            }
        }
        val threads = Array(size) { Thread(runnable) }
        threads.forEach {
            it.start()
        }
        threads.forEach {
            it.join()
        }
        assertThat(millList)
            .doesNotContainNull()
            .containsOnly(1498060770000)
            .hasSize(size)
    }

    @Test
    fun `convertDateStrToPrint는 동시성 문제에 안전하다`() {
        val dateTime = "2017-06-21T15:59:30.000+09:00"
        val strList = mutableListOf<String?>()
        val runnable = Runnable {
            val str = GalleryDateConvertUtil.convertToPrint(dateTime)
            synchronized(strList){
                strList.add(str)
            }
        }
        val threads = Array(size) { Thread(runnable) }
        threads.forEach {
            it.start()
        }
        threads.forEach {
            it.join()
        }
        assertThat(strList)
            .doesNotContainNull()
            .containsOnly("2017.06.21 15:59:30")
            .hasSize(size)
    }

    @Test
    fun `convertMillToPrint는 동시성 문제에 안전하다`() {
        val mill: Long = 1498060770000
        val strList = mutableListOf<String?>()
        val runnable = Runnable {
            val str = GalleryDateConvertUtil.convertToPrint(mill)
            synchronized(strList){
                strList.add(str)
            }
        }
        val threads = Array(size) { Thread(runnable) }
        threads.forEach {
            it.start()
        }
        threads.forEach {
            it.join()
        }
        assertThat(strList)
            .doesNotContainNull()
            .containsOnly("2017.06.21 15:59:30")
            .hasSize(size)
    }
}