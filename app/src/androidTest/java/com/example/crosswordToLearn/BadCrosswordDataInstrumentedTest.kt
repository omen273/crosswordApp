package com.example.crosswordToLearn

import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class BadCrosswordDataInstrumentedTest : BadCrosswordDataTest() {

    override fun spoil() {
        File(getContext().filesDir, "${crossword.title}${GameActivity.DATA_SUFFIX}").apply {
            FileOutputStream(this).use {
                getTestContext().resources.assets.open("tooLongWordsData.json")
            }
        }
    }

    @Test
    fun testData() {
        test()
    }
}